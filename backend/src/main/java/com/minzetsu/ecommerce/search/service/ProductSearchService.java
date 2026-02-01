package com.minzetsu.ecommerce.search.service;

import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.repository.ProductRepository;
import com.minzetsu.ecommerce.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProductSearchService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductRepository productRepository;

    public ProductSearchService(
            ElasticsearchOperations elasticsearchOperations,
            ProductRepository productRepository
    ) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.productRepository = productRepository;
    }

    public Page<Product> searchByName(String keyword, Pageable pageable) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m.query(keyword).fields("name", "description")))
                .withPageable(pageable)
                .build();
        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        List<Long> ids = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .filter(Objects::nonNull)
                .map(ProductDocument::getId)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }
        List<Product> products = productRepository.findAllByIdInWithCategory(ids);
        return new PageImpl<>(products, pageable, hits.getTotalHits());
    }
}
