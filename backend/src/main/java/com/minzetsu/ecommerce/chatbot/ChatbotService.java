package com.minzetsu.ecommerce.chatbot;

import lombok.RequiredArgsConstructor;
import com.minzetsu.ecommerce.chatbot.dto.ChatRequest;
import com.minzetsu.ecommerce.chatbot.dto.ChatResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatMessageResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatConversationResponse;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.entity.ProductStatus;
import com.minzetsu.ecommerce.product.entity.Category;
import com.minzetsu.ecommerce.product.repository.CategoryRepository;
import com.minzetsu.ecommerce.product.repository.ProductRepository;
import com.minzetsu.ecommerce.product.repository.ProductSpecification;
import com.minzetsu.ecommerce.product.dto.filter.ProductFilter;
import com.minzetsu.ecommerce.product.dto.response.ProductResponse;
import com.minzetsu.ecommerce.product.service.ProductService;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.repository.OrderRepository;
import com.minzetsu.ecommerce.payment.entity.Payment;
import com.minzetsu.ecommerce.payment.repository.PaymentRepository;
import com.minzetsu.ecommerce.review.repository.ReviewRepository;
import com.minzetsu.ecommerce.product.repository.projection.ProductRatingView;
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
import java.text.Normalizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private final ChatbotProperties properties;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final ProjectKnowledgeService projectKnowledgeService;
    private final RestTemplate restTemplate;
    private final ChatbotQueryService chatbotQueryService;
    private static final String CACHE_PRODUCTS = "chatbot:products";
    private static final String CACHE_CATEGORIES = "chatbot:categories";
    private final Map<Long, Integer> productPageByConversation = new ConcurrentHashMap<>();
    private final Map<Long, CachedContext> contextByConversation = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry> sharedCache = new ConcurrentHashMap<>();

    

    public ChatResponse chat(Long userId, ChatRequest request) {
        String message = sanitize(request.getMessage());
        Long conversationId = resolveConversationId(userId, request.getConversationId());
        saveMessage(userId, conversationId, "user", message);

        String reply = null;
        String productSuggestion = handleProductPagingIfRequested(message, conversationId);
        if (productSuggestion != null) {
            reply = productSuggestion;
        } else {
            String fastReply = handleFastProjectQuery(message);
            if (fastReply != null && !fastReply.isBlank()) {
                reply = fastReply;
            }
        }
        if (reply == null || reply.isBlank()) {
            boolean projectScope = isProjectScopeQuestion(message);
            String context = projectScope ? buildContextBlock(userId, message, conversationId) : "";
            if (properties.isQueryPlannerEnabled()) {
                String dbAnswer = chatbotQueryService.answerWithDb(message);
                if (dbAnswer != null && !dbAnswer.isBlank()) {
                    reply = dbAnswer;
                }
            }
            if ((reply == null || reply.isBlank())
                    && properties.isEnabled()
                    && properties.getBaseUrl() != null
                    && !properties.getBaseUrl().isBlank()) {
                log.info("Chatbot: LLM enabled, calling LLM");
                reply = callLlm(message, context);
            }
            if (reply == null || reply.isBlank()) {
                reply = fallbackReply(message);
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
        String lower = normalizeQuery(message);
        StringBuilder sb = new StringBuilder();
        String projectContext = "";
        if (isLikelyProjectContextQuery(message)) {
            projectContext = getCachedProjectContext(conversationId, message);
            if (!projectContext.isBlank()) {
                sb.append(projectContext).append("\n");
            }
        }
        boolean categoryQuery = isCategoryQuery(lower);
        boolean ratingQuery = containsAny(lower, "rating", "danh gia", "rated", "cao nhat", "cao nhat", "top rated");
        int requestedLimit = extractRequestedLimit(lower, 5);
        if (categoryQuery) {
            String categoryContext = buildCategoryContext();
            if (!categoryContext.isBlank()) {
                sb.append("Category context:\n").append(categoryContext).append("\n");
            }
        } else if (containsAny(lower, "product", "products", "san pham", "hang", "item", "catalog")) {
            String productContext;
            if (ratingQuery) {
                productContext = buildTopRatedProductsList(requestedLimit);
            } else {
                productContext = buildProductList(requestedLimit);
            }
            if (!productContext.isBlank()) {
                sb.append("Product context:\n").append(productContext).append("\n");
            }
            if (isTopProductQuery(lower)) {
                // no-op
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

    private String normalizeQuery(String text) {
        if (text == null) {
            return "";
        }
        String lower = text.toLowerCase();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private boolean isLikelyProjectContextQuery(String message) {
        String lower = normalizeQuery(message);
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

    private boolean isProjectScopeQuestion(String message) {
        String lower = normalizeQuery(message);
        return containsAny(
                lower,
                "cart", "gio hang", "basket",
                "wishlist", "yeu thich",
                "voucher", "coupon", "ma giam gia",
                "order", "don hang", "checkout",
                "payment", "thanh toan", "momo", "vnpay",
                "product", "products", "san pham",
                "category", "categories", "danh muc",
                "inventory", "ton kho", "stock",
                "search", "elasticsearch",
                "notification", "thong bao",
                "chatbot", "assistant",
                "rabbitmq", "redis"
        );
    }

    private boolean isCategoryQuery(String lower) {
        return containsAny(lower, "category", "categories", "danh muc", "the loai", "nhom hang");
    }

    private boolean isTopProductQuery(String lower) {
        return containsAny(
                lower,
                "ban chay",
                "best seller",
                "bestseller",
                "top",
                "rating",
                "danh gia",
                "high rating",
                "rated"
        );
    }

    private String handleFastProjectQuery(String message) {
        String lower = normalizeQuery(message);
        if (!containsAny(lower, "product", "products", "san pham", "hang")) {
            return null;
        }
        int limit = extractRequestedLimit(lower, 5);
        limit = Math.max(1, Math.min(20, limit));
        if (containsAny(lower, "ban chay", "best selling", "best seller", "bestseller")) {
            return formatProductResponses(productService.getBestSellingProductResponses(30, limit), limit, lower);
        }
        if (containsAny(lower, "rating", "rated", "danh gia", "cao nhat", "top rated")) {
            return formatProductResponses(productService.getTopRatingProductResponses(3650, limit), limit, lower);
        }
        if (containsAny(lower, "favorite", "favourite", "yeu thich")) {
            return formatProductResponses(productService.getMostFavoriteProductResponses(3650, limit), limit, lower);
        }
        if (containsAny(lower, "view", "xem", "most viewed", "xem nhieu")) {
            return formatProductResponses(productService.getMostViewedProductResponses(3650, limit), limit, lower);
        }
        return null;
    }

    private String formatProductResponses(List<ProductResponse> products, int limit, String lower) {
        if (products == null || products.isEmpty()) {
            return lower.contains("best") ? "No products found." : "Khong tim thay san pham phu hop.";
        }
        StringBuilder sb = new StringBuilder();
        int idx = 1;
        for (ProductResponse p : products) {
            sb.append(idx++)
              .append(". ")
              .append(p.getName())
              .append(" - ")
              .append(p.getPrice())
              .append(" ")
              .append(p.getCurrency())
              .append("\n");
            if (idx > limit) {
                break;
            }
        }
        return sb.toString().trim();
    }

    private int extractRequestedLimit(String text, int fallback) {
        if (text == null) {
            return fallback;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d{1,3})").matcher(text);
        if (matcher.find()) {
            try {
                int value = Integer.parseInt(matcher.group(1));
                if (value > 0) {
                    return Math.min(value, 20);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
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
        String lower = normalizeQuery(message);
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

    private String getCachedValue(String key, Supplier<String> loader) {
        long ttlMs = properties.getCacheTtlMs();
        if (ttlMs <= 0) {
            return loader.get();
        }
        long now = System.currentTimeMillis();
        CacheEntry existing = sharedCache.get(key);
        if (existing != null && existing.expiresAtMs >= now) {
            return existing.value;
        }
        String value = loader.get();
        sharedCache.put(key, new CacheEntry(value, now + ttlMs));
        return value;
    }

    public void invalidateProductCache() {
        sharedCache.keySet().removeIf(key -> key.startsWith(CACHE_PRODUCTS));
    }

    public void invalidateCategoryCache() {
        sharedCache.remove(CACHE_CATEGORIES);
    }

    public void invalidateVoucherCache() {
        sharedCache.keySet().removeIf(key -> key.startsWith("chatbot:vouchers"));
    }

    public void invalidateAllCaches() {
        sharedCache.clear();
        contextByConversation.clear();
        productPageByConversation.clear();
    }

    private String handleProductPagingIfRequested(String message, Long conversationId) {
        String lower = normalizeQuery(message);
        if (!containsAny(lower, "product", "products", "san pham", "hang", "item", "popular", "common", "ban chay", "top")) {
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

    private String buildProductList(int limit) {
        int size = Math.max(1, Math.min(20, limit));
        String cacheKey = CACHE_PRODUCTS + ":" + size;
        return getCachedValue(cacheKey, () -> {
            try {
                ProductFilter filter = new ProductFilter();
                filter.setStatus(ProductStatus.ACTIVE.name());
                var pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"));
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
        });
    }

    private String buildTopRatedProductsList(int limit) {
        int size = Math.max(1, Math.min(20, limit));
        String cacheKey = CACHE_PRODUCTS + ":top-rated:" + size;
        return getCachedValue(cacheKey, () -> {
            try {
                List<ProductRatingView> ratingViews = reviewRepository
                        .getProductRatingViewsByAverageRatingLastDaysAndLimit(3650, size);
                if (ratingViews == null || ratingViews.isEmpty()) {
                    return "";
                }
                List<Long> ids = ratingViews.stream()
                        .map(ProductRatingView::getProductId)
                        .filter(id -> id != null)
                        .toList();
                if (ids.isEmpty()) {
                    return "";
                }
                Map<Long, Product> productMap = productRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));
                StringBuilder sb = new StringBuilder();
                int idx = 1;
                for (ProductRatingView view : ratingViews) {
                    Product product = productMap.get(view.getProductId());
                    if (product == null) {
                        continue;
                    }
                    Double avg = view.getAverageRating();
                    sb.append(idx++)
                      .append(". ")
                      .append(product.getName())
                      .append(" - ")
                      .append(product.getPrice())
                      .append(" ")
                      .append(product.getCurrency());
                    if (avg != null) {
                        sb.append(" (rating ").append(String.format(java.util.Locale.US, "%.1f", avg)).append(")");
                    }
                    sb.append("\n");
                    if (idx > size) {
                        break;
                    }
                }
                return sb.toString().trim();
            } catch (Exception ex) {
                return "";
            }
        });
    }

    private String buildCategoryContext() {
        return getCachedValue(CACHE_CATEGORIES, () -> {
            try {
                var pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdAt"));
                var page = categoryRepository.findAll(pageable);
                if (page.isEmpty()) {
                    return "";
                }
                StringBuilder sb = new StringBuilder();
                int idx = 1;
                for (Category c : page.getContent()) {
                    sb.append(idx++)
                      .append(". ")
                      .append(c.getName())
                      .append(" (")
                      .append(c.getSlug())
                      .append(")")
                      .append("\n");
                }
                return sb.toString().trim();
            } catch (Exception ex) {
                return "";
            }
        });
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
            int numPredict = Math.max(16, properties.getNumPredict());

            List<Map<String, Object>> messages = new java.util.ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "You are a concise and helpful assistant for an ecommerce project. " +
                            "Respond in Vietnamese unless the user writes in English. " +
                            "Use provided context as the source of truth for project questions; do NOT invent data, " +
                            "file paths, or API endpoints. If the context does not include the answer, say so and ask " +
                            "for the missing info. For non-project questions, you may answer normally."));
            if (context != null && !context.isBlank()) {
                messages.add(Map.of("role", "system", "content", context));
            }
            messages.add(Map.of("role", "user", "content", message));

            Map<String, Object> payload = Map.of(
                    "model", model,
                    "messages", messages,
                    "temperature", properties.getTemperature(),
                    "stream", false,
                    "options", Map.of("num_predict", numPredict)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isBlank()) {
                headers.setBearerAuth(apiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            try {
                log.info("Chatbot: calling Ollama /api/chat endpoint");
                HttpEntity<Map<String, Object>> ollamaEntity = new HttpEntity<>(payload, headers);
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

    private static class CacheEntry {
        private final String value;
        private final long expiresAtMs;

        private CacheEntry(String value, long expiresAtMs) {
            this.value = value == null ? "" : value;
            this.expiresAtMs = expiresAtMs;
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
