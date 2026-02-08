package com.minzetsu.ecommerce.common.idempotency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private IdempotencyKeyRepository repository;

    private IdempotencyService service;

    @BeforeEach
    void setUp() {
        service = new IdempotencyService(repository);
    }

    @Test
    void execute_shouldCreateDirectlyWhenKeyIsBlank() {
        AtomicInteger creates = new AtomicInteger(0);

        String result = service.execute(
                " ",
                "ORDER_CREATE",
                1L,
                "ORDER",
                id -> "loaded-" + id,
                () -> {
                    creates.incrementAndGet();
                    return "created";
                },
                value -> 10L
        );

        assertThat(result).isEqualTo("created");
        assertThat(creates.get()).isEqualTo(1);
        verify(repository, never()).findByKeyValueAndScopeAndUserId(any(), any(), any());
        verify(repository, never()).save(any(IdempotencyKey.class));
    }

    @Test
    void execute_shouldCreateDirectlyWhenKeyIsNull() {
        AtomicInteger creates = new AtomicInteger(0);

        String result = service.execute(
                null,
                "ORDER_CREATE",
                1L,
                "ORDER",
                id -> "loaded-" + id,
                () -> {
                    creates.incrementAndGet();
                    return "created";
                },
                value -> 11L
        );

        assertThat(result).isEqualTo("created");
        assertThat(creates.get()).isEqualTo(1);
        verify(repository, never()).findByKeyValueAndScopeAndUserId(any(), any(), any());
        verify(repository, never()).save(any(IdempotencyKey.class));
    }

    @Test
    void execute_shouldLoadExistingResourceWhenKeyAlreadyStored() {
        IdempotencyKey existing = new IdempotencyKey();
        existing.setResourceId(123L);
        when(repository.findByKeyValueAndScopeAndUserId("k1", "ORDER_CREATE", 2L))
                .thenReturn(Optional.of(existing));

        AtomicInteger creates = new AtomicInteger(0);

        String result = service.execute(
                "k1",
                "ORDER_CREATE",
                2L,
                "ORDER",
                id -> "loaded-" + id,
                () -> {
                    creates.incrementAndGet();
                    return "created";
                },
                value -> 20L
        );

        assertThat(result).isEqualTo("loaded-123");
        assertThat(creates.get()).isEqualTo(0);
        verify(repository, never()).save(any(IdempotencyKey.class));
    }

    @Test
    void execute_shouldCreateAndPersistKeyWhenNotExisting() {
        when(repository.findByKeyValueAndScopeAndUserId("k2", "ORDER_CREATE", 3L))
                .thenReturn(Optional.empty());

        String result = service.execute(
                "k2",
                "ORDER_CREATE",
                3L,
                "ORDER",
                id -> "loaded-" + id,
                () -> "created-new",
                value -> 456L
        );

        assertThat(result).isEqualTo("created-new");

        ArgumentCaptor<IdempotencyKey> captor = ArgumentCaptor.forClass(IdempotencyKey.class);
        verify(repository).save(captor.capture());
        IdempotencyKey saved = captor.getValue();
        assertThat(saved.getKeyValue()).isEqualTo("k2");
        assertThat(saved.getScope()).isEqualTo("ORDER_CREATE");
        assertThat(saved.getUserId()).isEqualTo(3L);
        assertThat(saved.getResourceType()).isEqualTo("ORDER");
        assertThat(saved.getResourceId()).isEqualTo(456L);
    }
}
