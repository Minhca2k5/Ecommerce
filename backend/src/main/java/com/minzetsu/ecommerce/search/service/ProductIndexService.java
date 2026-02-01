package com.minzetsu.ecommerce.search.service;

import lombok.RequiredArgsConstructor;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.repository.ProductRepository;
import com.minzetsu.ecommerce.search.document.ProductDocument;
import com.minzetsu.ecommerce.search.repository.ProductSearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductIndexService {
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    

    public void indexProduct(Long productId) {
        productRepository.findById(productId)
                .ifPresent(product -> productSearchRepository.save(toDocument(product)));
    }

    public void deleteProduct(Long productId) {
        productSearchRepository.deleteById(productId);
    }

    public void reindexAll() {
        List<Product> products = productRepository.findAll();
        productSearchRepository.saveAll(products.stream().map(this::toDocument).toList());
    }

    private ProductDocument toDocument(Product product) {
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        return new ProductDocument(
                product.getId(),
                product.getName(),
                product.getDescription(),
                categoryName,
                product.getStatus() != null ? product.getStatus().name() : null,
                product.getPrice() != null ? product.getPrice().doubleValue() : null
        );
    }
}
