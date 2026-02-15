package com.minzetsu.ecommerce.mongo;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClickstreamEventRepository extends MongoRepository<ClickstreamEventDocument, String> {
    List<ClickstreamEventDocument> findByEventTimeGreaterThanEqualAndEventTimeLessThan(LocalDateTime from, LocalDateTime to);
}
