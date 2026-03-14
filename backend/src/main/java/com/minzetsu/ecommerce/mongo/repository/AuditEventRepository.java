package com.minzetsu.ecommerce.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.minzetsu.ecommerce.mongo.document.AuditEventDocument;


public interface AuditEventRepository extends MongoRepository<AuditEventDocument, String> {
}




