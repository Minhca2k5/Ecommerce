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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.minzetsu.ecommerce.common.audit.repository.AuditLogRepository;
import com.minzetsu.ecommerce.common.audit.service.AuditLogQueryServiceImpl;
import com.minzetsu.ecommerce.common.audit.entity.AuditLog;


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
        log.setId(1L);
        log.setUserId(10L);
        log.setAction("LOGIN");
        log.setEntityType("USER");
        log.setEntityId(20L);
        log.setSuccess(true);
        log.setErrorMessage(null);
        log.setIpAddress("127.0.0.1");
        log.setUserAgent("JUnit");
        log.setCreatedAt(LocalDateTime.of(2026, 2, 8, 21, 30));
        log.setUpdatedAt(LocalDateTime.of(2026, 2, 8, 21, 31));

        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log)));

        Page<AuditLogResponse> result = service.search(new AuditLogFilter(), null);

        assertThat(result.getContent()).hasSize(1);
        AuditLogResponse response = result.getContent().get(0);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(10L);
        assertThat(response.getAction()).isEqualTo("LOGIN");
        assertThat(response.getEntityType()).isEqualTo("USER");
        assertThat(response.getEntityId()).isEqualTo(20L);
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(response.getUserAgent()).isEqualTo("JUnit");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditLogRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(20);
        assertThat(used.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(used.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void search_shouldApplyDefaultSortWhenPageableIsUnsorted() {
        Pageable unsorted = PageRequest.of(2, 7);
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty(unsorted));

        service.search(new AuditLogFilter(), unsorted);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditLogRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(2);
        assertThat(used.getPageSize()).isEqualTo(7);
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

