package com.minzetsu.ecommerce.chatbot;

import com.minzetsu.ecommerce.chatbot.dto.ChatRequest;
import com.minzetsu.ecommerce.chatbot.dto.ChatResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatMessageResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatConversationResponse;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.product.repository.ProductRepository;
import com.minzetsu.ecommerce.product.repository.ProductSpecification;
import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.repository.OrderRepository;
import com.minzetsu.ecommerce.payment.entity.Payment;
import com.minzetsu.ecommerce.payment.repository.PaymentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatbotService {
    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private final ChatbotProperties properties;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProjectKnowledgeService projectKnowledgeService;
    private final RestTemplate restTemplate;
    private final Map<Long, Integer> productPageByConversation = new ConcurrentHashMap<>();
    private final Map<Long, CachedContext> contextByConversation = new ConcurrentHashMap<>();

    public ChatbotService(
            ChatbotProperties properties,
            ChatMessageRepository chatMessageRepository,
            ChatConversationRepository chatConversationRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            ProjectKnowledgeService projectKnowledgeService,
            RestTemplate restTemplate
    ) {
        this.properties = properties;
        this.chatMessageRepository = chatMessageRepository;
        this.chatConversationRepository = chatConversationRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.projectKnowledgeService = projectKnowledgeService;
        this.restTemplate = restTemplate;
    }

    public ChatResponse chat(Long userId, ChatRequest request) {
        String message = sanitize(request.getMessage());
        Long conversationId = resolveConversationId(userId, request.getConversationId());
        saveMessage(userId, conversationId, "user", message);

        String reply = null;
        String productSuggestion = handleProductPagingIfRequested(message, conversationId);
        if (productSuggestion != null) {
            reply = productSuggestion;
        } else {
            String context = buildContextBlock(userId, message, conversationId);
            if (properties.isEnabled() && properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank()) {
                log.info("Chatbot: LLM enabled, calling LLM");
                reply = callLlm(message, context);
            }
            if (reply == null || reply.isBlank()) {
                if (!context.isBlank()) {
                    log.info("Chatbot: context-only reply used");
                    reply = context;
                } else {
                    log.info("Chatbot: fallback reply used");
                    reply = fallbackReply(message);
                }
            }
        }

        saveMessage(userId, conversationId, "assistant", reply);
        return new ChatResponse(reply);
    }

    public List<ChatMessageResponse> listHistory(Long userId, Long conversationId, int limit) {
        int size = Math.max(1, Math.min(50, limit));
        var page = chatMessageRepository.findByUserIdAndConversationIdOrderByCreatedAtDesc(
                userId,
                conversationId,
                org.springframework.data.domain.PageRequest.of(0, size)
        );
        if (page.isEmpty()) {
            return List.of();
        }
        return page.getContent().stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .map(m -> new ChatMessageResponse(m.getRole(), m.getContent(), m.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public void clearHistory(Long userId, Long conversationId) {
        chatMessageRepository.deleteByUserIdAndConversationId(userId, conversationId);
    }

    public List<ChatConversationResponse> listConversations(Long userId) {
        return chatConversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(c -> new ChatConversationResponse(c.getId(), c.getTitle(), c.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public ChatConversationResponse createConversation(Long userId, String title) {
        String safeTitle = (title == null || title.isBlank()) ? "New chat" : title.trim();
        ChatConversation conversation = new ChatConversation();
        conversation.setUserId(userId);
        conversation.setTitle(safeTitle);
        ChatConversation saved = chatConversationRepository.save(conversation);
        return new ChatConversationResponse(saved.getId(), saved.getTitle(), saved.getUpdatedAt());
    }

    public Long resolveConversationId(Long userId, Long conversationId) {
        if (conversationId != null) {
            return chatConversationRepository.findByUserIdAndId(userId, conversationId)
                    .map(ChatConversation::getId)
                    .orElseGet(() -> getOrCreateDefaultConversation(userId).getId());
        }
        return getOrCreateDefaultConversation(userId).getId();
    }

    private ChatConversation getOrCreateDefaultConversation(Long userId) {
        return chatConversationRepository.findByUserIdAndTitle(userId, "General")
                .orElseGet(() -> {
                    ChatConversation conversation = new ChatConversation();
                    conversation.setUserId(userId);
                    conversation.setTitle("General");
                    return chatConversationRepository.save(conversation);
                });
    }

    private String sanitize(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.length() > MAX_MESSAGE_LENGTH) {
            trimmed = trimmed.substring(0, MAX_MESSAGE_LENGTH);
        }
        return trimmed;
    }

    private String fallbackReply(String message) {
        return "I can help with products, orders, payments, and project questions. Please describe what you need.";
    }

    private String buildContextBlock(Long userId, String message, Long conversationId) {
        String lower = message.toLowerCase();
        StringBuilder sb = new StringBuilder();
        String projectContext = "";
        if (isLikelyProjectContextQuery(message)) {
            projectContext = getCachedProjectContext(conversationId, message);
            if (!projectContext.isBlank()) {
                sb.append(projectContext).append("\n");
            }
        }
        if (containsAny(lower, "product", "products", "san pham", "hang", "item", "catalog", "category", "categories")) {
            String productContext = buildProductContext();
            if (!productContext.isBlank()) {
                sb.append("Product context:\n").append(productContext).append("\n");
            }
        }
        if (containsAny(lower, "order", "orders", "don", "don hang", "status", "shipping", "giao")) {
            String orderContext = buildOrderContext(userId);
            if (!orderContext.isBlank()) {
                sb.append("Order context:\n").append(orderContext).append("\n");
            }
        }
        if (containsAny(lower, "payment", "pay", "momo", "thanh toan", "refund", "hoan")) {
            String paymentContext = buildPaymentContext(userId);
            if (!paymentContext.isBlank()) {
                sb.append("Payment context:\n").append(paymentContext).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLikelyProjectContextQuery(String message) {
        String lower = message.toLowerCase();
        return containsAny(
                lower,
                "code",
                "class",
                "service",
                "controller",
                "repository",
                "endpoint",
                "api",
                "exception",
                "error",
                "bug",
                "stacktrace",
                "config",
                "yaml",
                "properties",
                "database",
                "entity",
                "schema",
                "sql",
                ".java",
                ".ts",
                ".tsx",
                ".yml",
                ".yaml"
        );
    }

    private String getCachedProjectContext(Long conversationId, String message) {
        String key = normalizeKey(message);
        if (conversationId != null) {
            CachedContext cached = contextByConversation.get(conversationId);
            if (cached != null && cached.key.equals(key) && !cached.context.isBlank()) {
                return cached.context;
            }
        }
        String context = projectKnowledgeService.buildContext(message);
        if (conversationId != null) {
            contextByConversation.put(conversationId, new CachedContext(key, context));
        }
        return context;
    }

    private String normalizeKey(String message) {
        String lower = message == null ? "" : message.toLowerCase();
        String[] parts = lower.split("[^a-z0-9_\\-\\.]+");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String part : parts) {
            if (part.length() < 3) {
                continue;
            }
            sb.append(part).append("|");
            count++;
            if (count >= 12) {
                break;
            }
        }
        return sb.toString();
    }

    private String handleProductPagingIfRequested(String message, Long conversationId) {
        String lower = message.toLowerCase();
        if (!containsAny(lower, "product", "products", "san pham", "hang", "item", "popular", "common")) {
            return null;
        }
        if (!containsAny(lower, "more", "next", "nua", "them", "tieptuc", "tiep tuc", "tiep", "list", "show")) {
            return null;
        }
        return buildProductSuggestion(lower, conversationId);
    }

    private String buildProductSuggestion(String lower, Long conversationId) {
        try {
            int pageIndex = 0;
            if (conversationId != null) {
                int current = productPageByConversation.getOrDefault(conversationId, 0);
                if (containsAny(lower, "more", "next", "nua", "them", "tieptuc", "tiep tuc", "tiep")) {
                    pageIndex = current + 1;
                } else if (containsAny(lower, "first", "start", "bat dau", "dau tien")) {
                    pageIndex = 0;
                } else {
                    pageIndex = current;
                }
            }
            ProductFilter filter = new ProductFilter();
            filter.setStatus(ProductStatus.ACTIVE.name());
            var pageable = PageRequest.of(pageIndex, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
            var page = productRepository.findAll(ProductSpecification.filter(filter), pageable);
            if (page.isEmpty()) {
                if (pageIndex > 0) {
                    return "No more products right now.";
                }
                return "There are no active products available right now.";
            }
            if (conversationId != null) {
                productPageByConversation.put(conversationId, pageIndex);
            }
            StringBuilder sb = new StringBuilder("Here are some products you can check:\n");
            int idx = 1;
            for (Product p : page.getContent()) {
                sb.append(idx++)
                  .append(". ")
                  .append(p.getName())
                  .append(" - ")
                  .append(p.getPrice())
                  .append(" ")
                  .append(p.getCurrency())
                  .append("\n");
            }
            return sb.toString().trim();
        } catch (Exception ex) {
            return null;
        }
    }

    private String buildProductContext() {
        try {
            ProductFilter filter = new ProductFilter();
            filter.setStatus(ProductStatus.ACTIVE.name());
            var pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
            var page = productRepository.findAll(ProductSpecification.filter(filter), pageable);
            if (page.isEmpty()) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            int idx = 1;
            for (var p : page.getContent()) {
                sb.append(idx++)
                  .append(". ")
                  .append(p.getName())
                  .append(" - ")
                  .append(p.getPrice())
                  .append(" ")
                  .append(p.getCurrency())
                  .append("\n");
            }
            return sb.toString().trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private String buildOrderContext(Long userId) {
        try {
            List<Order> orders = orderRepository.findByUserIdOrderByUpdatedAtDesc(userId);
            if (orders.isEmpty()) {
                return "No recent orders found.";
            }
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Order order : orders) {
                sb.append("Order #")
                  .append(order.getId())
                  .append(" - ")
                  .append(order.getStatus())
                  .append(" - ")
                  .append(order.getTotalAmount())
                  .append(" ")
                  .append(order.getCurrency())
                  .append("\n");
                count++;
                if (count >= 3) {
                    break;
                }
            }
            return sb.toString().trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private String buildPaymentContext(Long userId) {
        try {
            List<Order> orders = orderRepository.findByUserIdOrderByUpdatedAtDesc(userId);
            if (orders.isEmpty()) {
                return "No payments found.";
            }
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (Order order : orders) {
                List<Payment> payments = paymentRepository.findByOrderIdOrderByUpdatedAtDesc(order.getId());
                if (payments.isEmpty()) {
                    continue;
                }
                Payment payment = payments.get(0);
                sb.append("Payment for order #")
                  .append(order.getId())
                  .append(": ")
                  .append(payment.getStatus())
                  .append(" - ")
                  .append(payment.getAmount())
                  .append(" ")
                  .append(order.getCurrency())
                  .append("\n");
                count++;
                if (count >= 3) {
                    break;
                }
            }
            return sb.toString().trim();
        } catch (Exception ex) {
            return "";
        }
    }

    private String callLlm(String message, String context) {
        try {
            String baseUrl = properties.getBaseUrl();
            String apiKey = properties.getApiKey();
            String model = properties.getModel() == null ? "gpt-3.5-turbo" : properties.getModel();

            List<Map<String, Object>> messages = new java.util.ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "You are a concise and helpful assistant for an ecommerce project. " +
                            "Respond in Vietnamese unless the user writes in English. " +
                            "Use provided context if relevant and cite file paths when context comes from project files. " +
                            "If a question is about orders or payments, ask for order id when missing. " +
                            "If context is missing, answer generally and say what info is needed."));
            if (context != null && !context.isBlank()) {
                messages.add(Map.of("role", "system", "content", context));
            }
            messages.add(Map.of("role", "user", "content", message));

            Map<String, Object> payload = Map.of(
                    "model", model,
                    "messages", messages,
                    "temperature", 0.2,
                    "max_tokens", 120
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isBlank()) {
                headers.setBearerAuth(apiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            Map<?, ?> response = null;
            try {
                log.info("Chatbot: calling OpenAI-compatible endpoint");
                response = restTemplate.postForObject(baseUrl + "/v1/chat/completions", entity, Map.class);
            } catch (Exception ex) {
                log.warn("Chatbot: OpenAI-compatible call failed: {}", ex.getMessage());
                response = null;
            }

            String parsed = parseLlmReply(response);
            if (parsed != null) {
                return parsed;
            }

            // Fallback to Ollama native API if OpenAI-compatible endpoint isn't available.
            try {
                log.info("Chatbot: calling Ollama /api/chat endpoint");
                Map<String, Object> ollamaPayload = Map.of(
                        "model", model,
                        "messages", messages,
                        "stream", false,
                        "options", Map.of("num_predict", 120)
                );
                HttpEntity<Map<String, Object>> ollamaEntity = new HttpEntity<>(ollamaPayload, headers);
                Map<?, ?> ollamaResponse = restTemplate.postForObject(baseUrl + "/api/chat", ollamaEntity, Map.class);
                String ollamaParsed = parseLlmReply(ollamaResponse);
                if (ollamaParsed != null) {
                    return ollamaParsed;
                }
            } catch (Exception ex) {
                log.warn("Chatbot: Ollama /api/chat call failed: {}", ex.getMessage());
            }

            log.info("Chatbot: LLM response not usable, fallback reply used");
            return null;
        } catch (Exception ex) {
            log.warn("Chatbot: callLlm failed: {}", ex.getMessage());
            return null;
        }
    }

    private static class CachedContext {
        private final String key;
        private final String context;

        private CachedContext(String key, String context) {
            this.key = key;
            this.context = context == null ? "" : context;
        }
    }

    private String parseLlmReply(Map<?, ?> response) {
        if (response == null) {
            return null;
        }
        try {
            Object directMessage = response.get("message");
            if (directMessage instanceof Map<?, ?> msgMap) {
                Object content = msgMap.get("content");
                if (content != null) {
                    return content.toString();
                }
            }
            Object directResponse = response.get("response");
            if (directResponse != null) {
                return directResponse.toString();
            }
            List<?> choices = (List<?>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            Map<?, ?> choice = (Map<?, ?>) choices.get(0);
            Map<?, ?> msg = (Map<?, ?>) choice.get("message");
            Object content = msg == null ? null : msg.get("content");
            return content == null ? null : content.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    private void saveMessage(Long userId, Long conversationId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setUserId(userId);
        msg.setConversationId(conversationId);
        msg.setRole(role);
        msg.setContent(content);
        chatMessageRepository.save(msg);

        if (conversationId != null) {
            chatConversationRepository.findById(conversationId).ifPresent(c -> {
                c.setUpdatedAt(LocalDateTime.now());
                chatConversationRepository.save(c);
            });
        }
    }
}
