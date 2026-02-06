package com.minzetsu.ecommerce.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditEventRepository extends MongoRepository<AuditEventDocument, String> {
}
