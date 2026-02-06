package com.minzetsu.ecommerce.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClickstreamEventRepository extends MongoRepository<ClickstreamEventDocument, String> {
}
