package com.minzetsu.ecommerce.mongo.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import com.minzetsu.ecommerce.mongo.document.ClickstreamEventDocument;


public interface ClickstreamEventRepository extends MongoRepository<ClickstreamEventDocument, String> {
    @Query("{ 'eventTime': { $gte: ?0, $lt: ?1 } }")
    List<ClickstreamEventDocument> findByEventTimeInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = "{ 'eventType': ?0, 'eventTime': { $gte: ?1, $lt: ?2 } }", count = true)
    long countByEventTypeAndEventTimeRange(
            String eventType,
            LocalDateTime from,
            LocalDateTime to
    );
}




