package com.minzetsu.ecommerce.common.idempotency;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class IdempotencyService {
    private final IdempotencyKeyRepository repository;

    public IdempotencyService(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public <T> T execute(
            String key,
            String scope,
            Long userId,
            String resourceType,
            Function<Long, T> existingLoader,
            Supplier<T> createSupplier,
            Function<T, Long> idExtractor
    ) {
        if (key == null || key.isBlank()) {
            return createSupplier.get();
        }
        Optional<IdempotencyKey> existing = repository.findByKeyValueAndScopeAndUserId(key, scope, userId);
        if (existing.isPresent()) {
            return existingLoader.apply(existing.get().getResourceId());
        }
        T created = createSupplier.get();
        Long resourceId = idExtractor.apply(created);
        IdempotencyKey record = new IdempotencyKey();
        record.setKeyValue(key);
        record.setScope(scope);
        record.setUserId(userId);
        record.setResourceType(resourceType);
        record.setResourceId(resourceId);
        repository.save(record);
        return created;
    }
}
