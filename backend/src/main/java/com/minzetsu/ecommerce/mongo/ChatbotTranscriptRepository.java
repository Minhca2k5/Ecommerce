package com.minzetsu.ecommerce.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatbotTranscriptRepository extends MongoRepository<ChatbotTranscriptDocument, String> {
}
