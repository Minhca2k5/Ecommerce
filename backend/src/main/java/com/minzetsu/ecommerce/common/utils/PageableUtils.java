package com.minzetsu.ecommerce.common.utils;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import com.minzetsu.ecommerce.common.exception.InvalidObjectException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.function.Function;

public final class PageableUtils {
    private PageableUtils() {}

    public static Pageable applySorting(Pageable pageable, SortableFilter filter) {
        if (pageable == null) {
            throw new InvalidObjectException("Pageable cannot be null");
        }
        if (filter == null) {
            throw new InvalidObjectException("UserFilter cannot be null");
        }
        if (filter.getSortBy() != null && filter.getSortDirection() != null) {
            Sort.Direction direction = filter.getSortDirection().equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            String sortBy = normalizeSortField(filter.getSortBy());
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(direction, sortBy));
        }
        return pageable;
    }

    private static String normalizeSortField(String sortBy) {
        if (sortBy == null) {
            return null;
        }
        return "salePrice".equalsIgnoreCase(sortBy) ? "price" : sortBy;
    }

    public static <E, R, F extends SortableFilter> Page<R> search(
            F filter,
            Pageable pageable,
            JpaSpecificationExecutor<E> repository,
            Specification<E> specification,
            Function<E, R> mapper
    ) {
        Pageable sortedPageable = applySorting(pageable, filter);
        Page<E> page = repository.findAll(specification, sortedPageable);
        return page.map(mapper);
    }
}



