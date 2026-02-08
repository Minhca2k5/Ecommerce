package com.minzetsu.ecommerce.common.utils;

import com.minzetsu.ecommerce.common.base.SortableFilter;
import com.minzetsu.ecommerce.common.exception.InvalidObjectException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageableUtilsTest {

    @Test
    void applySorting_shouldApplyDescendingSortWhenFilterProvidesSortFields() {
        Pageable pageable = PageRequest.of(1, 20);
        SortableFilter filter = new TestFilter("createdAt", "desc");

        Pageable result = PageableUtils.applySorting(pageable, filter);

        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(20);
        Sort.Order order = result.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void applySorting_shouldReturnOriginalPageableWhenSortMissing() {
        Pageable pageable = PageRequest.of(0, 10);
        SortableFilter filter = new TestFilter(null, "asc");

        Pageable result = PageableUtils.applySorting(pageable, filter);

        assertThat(result).isSameAs(pageable);
    }

    @Test
    void applySorting_shouldFallbackToAscendingWhenDirectionIsUnknown() {
        Pageable pageable = PageRequest.of(0, 10);
        SortableFilter filter = new TestFilter("id", "sideways");

        Pageable result = PageableUtils.applySorting(pageable, filter);

        Sort.Order order = result.getSort().getOrderFor("id");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void applySorting_shouldThrowWhenPageableOrFilterIsNull() {
        SortableFilter filter = new TestFilter("id", "asc");
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> PageableUtils.applySorting(null, filter))
                .isInstanceOf(InvalidObjectException.class)
                .hasMessageContaining("Pageable cannot be null");

        assertThatThrownBy(() -> PageableUtils.applySorting(pageable, null))
                .isInstanceOf(InvalidObjectException.class)
                .hasMessageContaining("UserFilter cannot be null");
    }

    private static class TestFilter implements SortableFilter {
        private final String sortBy;
        private final String sortDirection;

        private TestFilter(String sortBy, String sortDirection) {
            this.sortBy = sortBy;
            this.sortDirection = sortDirection;
        }

        @Override
        public String getSortBy() {
            return sortBy;
        }

        @Override
        public String getSortDirection() {
            return sortDirection;
        }
    }
}
