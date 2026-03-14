package com.minzetsu.ecommerce.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.minzetsu.ecommerce.mongo.document.ChatbotTranscriptDocument;


public interface ChatbotTranscriptRepository extends MongoRepository<ChatbotTranscriptDocument, String> {
}




