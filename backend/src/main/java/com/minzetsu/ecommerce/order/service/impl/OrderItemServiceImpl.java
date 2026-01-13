package com.minzetsu.ecommerce.order.service.impl;

import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.order.dto.filter.OrderItemFilter;
import com.minzetsu.ecommerce.order.dto.response.OrderItemResponse;
import com.minzetsu.ecommerce.order.entity.OrderItem;
import com.minzetsu.ecommerce.order.mapper.OrderItemMapper;
import com.minzetsu.ecommerce.order.repository.OrderItemRepository;
import com.minzetsu.ecommerce.order.repository.OrderItemSpecification;
import com.minzetsu.ecommerce.order.service.OrderItemService;
import com.minzetsu.ecommerce.order.service.OrderService;
import com.minzetsu.ecommerce.product.entity.ProductImage;
import com.minzetsu.ecommerce.product.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderService orderService;
    private final ProductImageRepository productImageRepository;

    private Map<Long, String> getPrimaryImageUrlMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return productImageRepository.findPrimaryByProductIds(productIds).stream()
                .filter(image -> image.getProduct() != null && image.getProduct().getId() != null)
                .collect(Collectors.toMap(
                        image -> image.getProduct().getId(),
                        ProductImage::getUrl,
                        (existing, ignored) -> existing
                ));
    }

    private OrderItemResponse toResponseWithUrl(OrderItem orderItem) {
        OrderItemResponse response = orderItemMapper.toResponse(orderItem);
        Long productId = response.getProductId();
        Optional<ProductImage> mainImage = productImageRepository.findByIsPrimaryTrueAndProductId(productId);
        mainImage.ifPresent(image -> response.setUrl(image.getUrl()));
        return response;
    }

    private List<OrderItemResponse> toResponseListWithUrl(List<OrderItem> orderItems) {
        List<OrderItemResponse> responses = orderItemMapper.toResponseList(orderItems);
        List<Long> productIds = responses.stream()
                .map(OrderItemResponse::getProductId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, String> urlMap = getPrimaryImageUrlMap(productIds);
        responses.forEach(response -> response.setUrl(urlMap.get(response.getProductId())));
        return responses;
    }

    private Page<OrderItemResponse> toResponsePageWithUrl(Page<OrderItem> orderItems) {
        List<OrderItemResponse> responses = toResponseListWithUrl(orderItems.getContent());
        return new PageImpl<>(responses, orderItems.getPageable(), orderItems.getTotalElements());
    }

    private OrderItem getExistingOrderItem(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order item not found with id: " + id));
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return orderItemRepository.existsByOrderId(orderId);
    }

    @Override
    public boolean existsById(Long id) {
        return orderItemRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        if (!existsByOrderId(orderId)) {
            throw new NotFoundException("Order items not found for order id: " + orderId);
        }
        return orderItemRepository.findByOrderIdOrderByUpdatedAtDesc(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItem getOrderItemByIdAndUserId(Long orderItemId, Long userId) {
        OrderItem orderItem = getExistingOrderItem(orderItemId);
        if (!orderItem.getOrder().getUser().getId().equals(userId)) {
            throw new UnAuthorizedException("User not authorized to access this order item");
        }
        return orderItem;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItem getOrderItemById(Long orderItemId) {
        return getExistingOrderItem(orderItemId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderItemResponse> searchOrderItemResponses(OrderItemFilter filter, Pageable pageable) {
        Pageable sortedPageable = PageableUtils.applySorting(pageable, filter);
        Page<OrderItem> page = orderItemRepository.findAll(OrderItemSpecification.filter(filter), sortedPageable);
        return toResponsePageWithUrl(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemResponse> getOrderItemResponsesByOrderIdAndUserId(Long orderId, Long userId) {
        if (userId != null) {
            orderService.getOrderByIdAndUserId(orderId, userId); // xác thực quyền truy cập
        }
        List<OrderItem> items = getOrderItemsByOrderId(orderId);
        return toResponseListWithUrl(items);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderItemResponse getOrderItemResponseByIdAndUserId(Long orderItemId, Long userId) {
        OrderItem orderItem = (userId != null)
                ? getOrderItemByIdAndUserId(orderItemId, userId)
                : getOrderItemById(orderItemId);
        return toResponseWithUrl(orderItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderItemResponse> getOrderItemResponsesByOrderIdAndUserId(Long orderId, Long userId, Pageable pageable) {
        orderService.getOrderByIdAndUserId(orderId, userId);
        Page<OrderItem> items = orderItemRepository.findByOrderId(orderId, pageable);
        return toResponsePageWithUrl(items);
    }
}
