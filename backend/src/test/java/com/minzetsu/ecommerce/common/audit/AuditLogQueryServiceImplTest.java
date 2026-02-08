package com.minzetsu.ecommerce.common.audit;

import com.minzetsu.ecommerce.common.audit.dto.filter.AuditLogFilter;
import com.minzetsu.ecommerce.common.audit.dto.response.AuditLogResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogQueryServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuditLogQueryServiceImpl(auditLogRepository);
    }

    @Test
    void search_shouldUseDefaultPageableWhenInputIsNull() {
        AuditLog log = new AuditLog();
        log.setAction("LOGIN");
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));

        Page<AuditLogResponse> result = service.search(new AuditLogFilter(), null);

        assertThat(result.getContent()).hasSize(1);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditLogRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(20);
        assertThat(used.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(used.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void search_shouldPreserveSortedPageable() {
        Pageable sorted = PageRequest.of(1, 5, Sort.by(Sort.Direction.ASC, "action"));
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty(sorted));

        service.search(new AuditLogFilter(), sorted);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditLogRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue()).isEqualTo(sorted);
    }
}
