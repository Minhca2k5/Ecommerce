package com.minzetsu.ecommerce.cart.mapper;

import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.cart.entity.Cart;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
public interface CartMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "guestId", source = "guestId")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "itemCount", ignore = true)
    @Mapping(target = "totalQuantity", ignore = true)
    @Mapping(target = "itemsSubtotal", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "shippingFee", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "currency", ignore = true)
    CartResponse toResponse(Cart cart);

    default CartResponse toFullResponse(
            Cart cart,
            List<CartItemResponse> items,
            BigDecimal discount,
            BigDecimal shippingFee,
            String currency
    ) {
        CartResponse res = toResponse(cart);

        int itemCount = items != null ? items.size() : 0;
        int totalQty = 0;
        BigDecimal subtotal = BigDecimal.ZERO;

        if (items != null) {
            for (CartItemResponse item : items) {
                int q = item.getQuantity() != null ? item.getQuantity() : 0;
                totalQty += q;

                BigDecimal lt = item.getLineTotal();
                if (lt == null) {
                    BigDecimal up = item.getUnitPriceSnapshot() != null ? item.getUnitPriceSnapshot() : BigDecimal.ZERO;
                    lt = up.multiply(BigDecimal.valueOf(q));
                    item.setLineTotal(lt);
                }
                subtotal = subtotal.add(lt);
            }
        }

        BigDecimal safeDiscount = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal safeShipping = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(safeDiscount).add(safeShipping);

        res.setItems(items);
        res.setItemCount(itemCount);
        res.setTotalQuantity(totalQty);
        res.setItemsSubtotal(subtotal);
        res.setDiscount(safeDiscount);
        res.setShippingFee(safeShipping);
        res.setTotalAmount(total);
        res.setCurrency(currency != null ? currency : "VND");

        return res;
    }

    List<CartResponse> toResponseList(List<Cart> carts);

}
