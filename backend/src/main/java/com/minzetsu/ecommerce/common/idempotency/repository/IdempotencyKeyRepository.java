package com.minzetsu.ecommerce.common.idempotency.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import com.minzetsu.ecommerce.common.idempotency.entity.IdempotencyKey;


public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByKeyValueAndScopeAndUserId(String keyValue, String scope, Long userId);
}




