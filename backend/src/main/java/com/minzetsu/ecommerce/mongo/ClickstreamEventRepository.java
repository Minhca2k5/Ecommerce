package com.minzetsu.ecommerce.mongo;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClickstreamEventRepository extends MongoRepository<ClickstreamEventDocument, String> {
    @Query("{ 'eventTime': { $gte: ?0, $lt: ?1 } }")
    List<ClickstreamEventDocument> findByEventTimeInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
