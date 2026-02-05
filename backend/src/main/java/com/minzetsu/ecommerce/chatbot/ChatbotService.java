package com.minzetsu.ecommerce.chatbot;

import lombok.RequiredArgsConstructor;
import com.minzetsu.ecommerce.chatbot.dto.ChatRequest;
import com.minzetsu.ecommerce.chatbot.dto.ChatResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatMessageResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatConversationResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatGroupResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatGroupInviteResponse;
import com.minzetsu.ecommerce.notification.dto.request.NotificationCreateRequest;
import com.minzetsu.ecommerce.notification.service.NotificationService;
import com.minzetsu.ecommerce.realtime.ChatbotRealtimeService;
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
import com.minzetsu.ecommerce.user.repository.UserRepository;
import com.minzetsu.ecommerce.user.entity.User;
import com.minzetsu.ecommerce.auth.service.EmailService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.function.Supplier;
import java.nio.charset.StandardCharsets;
import com.minzetsu.ecommerce.common.exception.AppException;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private static final int MAX_MESSAGE_LENGTH = 2000;
    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);
    private final ChatbotProperties properties;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ChatProjectRepository chatProjectRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final ChatGroupMemberRepository chatGroupMemberRepository;
    private final ChatGroupInviteRepository chatGroupInviteRepository;
    private final NotificationService notificationService;
    private final ChatbotRealtimeService chatbotRealtimeService;
    private final UserRepository userRepository;
    private final EmailService emailService;
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
        Long conversationId = resolveConversationId(userId, request.getConversationId(), request.getProjectId(), request.getGroupId());
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
        requireConversationAccess(userId, conversationId);
        int size = Math.max(1, Math.min(50, limit));
        var page = chatMessageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId,
                org.springframework.data.domain.PageRequest.of(0, size)
        );
        if (page.isEmpty()) {
            return List.of();
        }
        return page.getContent().stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .map(m -> new ChatMessageResponse(
                        m.getRole(),
                        m.getContent(),
                        m.getUserId(),
                        userRepository.findById(m.getUserId())
                                .map(u -> (u.getFullName() != null && !u.getFullName().isBlank()) ? u.getFullName() : u.getUsername())
                                .orElse("Unknown"),
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public void clearHistory(Long userId, Long conversationId) {
        chatMessageRepository.deleteByUserIdAndConversationId(userId, conversationId);
    }

    public void deleteConversation(Long userId, Long conversationId) {
        chatConversationRepository.findByUserIdAndId(userId, conversationId).ifPresent(c -> {
            chatMessageRepository.deleteByConversationId(conversationId);
            chatConversationRepository.delete(c);
        });
    }

    public ChatConversationResponse renameConversation(Long userId, Long conversationId, String title) {
        ChatConversation conversation = chatConversationRepository.findByUserIdAndId(userId, conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setTitle(sanitizeTitle(title));
        ChatConversation saved = chatConversationRepository.save(conversation);
        return new ChatConversationResponse(saved.getId(), saved.getTitle(), saved.getUpdatedAt());
    }


    public List<ChatConversationResponse> listPersonalConversations(Long userId) {
        return chatConversationRepository.findByUserIdAndProjectIdIsNullAndGroupIdIsNullOrderByUpdatedAtDesc(userId).stream()
                .map(c -> new ChatConversationResponse(c.getId(), c.getTitle(), c.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public List<ChatConversationResponse> listConversations(Long userId) {
        java.util.Set<Long> groupIds = chatGroupMemberRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream().map(ChatGroupMember::getGroupId).collect(java.util.stream.Collectors.toSet());
        java.util.List<ChatConversation> all = new java.util.ArrayList<>(chatConversationRepository.findByUserIdOrderByUpdatedAtDesc(userId));
        for (Long gid : groupIds) { all.addAll(chatConversationRepository.findByGroupIdOrderByUpdatedAtDesc(gid)); }
        return all.stream()
                .collect(java.util.stream.Collectors.toMap(ChatConversation::getId, c -> c, (a,b) -> a))
                .values().stream()
                .sorted((a,b) -> java.util.Comparator.nullsLast(java.time.LocalDateTime::compareTo).reversed().compare(a.getUpdatedAt(), b.getUpdatedAt()))
                .map(c -> new ChatConversationResponse(c.getId(), c.getTitle(), c.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public List<com.minzetsu.ecommerce.chatbot.dto.ChatProjectResponse> listProjects(Long userId) {
        return chatProjectRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(p -> new com.minzetsu.ecommerce.chatbot.dto.ChatProjectResponse(p.getId(), p.getName(), p.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public com.minzetsu.ecommerce.chatbot.dto.ChatProjectResponse createProject(Long userId, String name) {
        ChatProject p = new ChatProject();
        p.setUserId(userId);
        p.setName(sanitizeProjectName(name));
        ChatProject saved = chatProjectRepository.save(p);
        return new com.minzetsu.ecommerce.chatbot.dto.ChatProjectResponse(saved.getId(), saved.getName(), saved.getUpdatedAt());
    }

    public com.minzetsu.ecommerce.chatbot.dto.ChatProjectResponse renameProject(Long userId, Long projectId, String name) {
        ChatProject p = chatProjectRepository.findByUserIdAndId(userId, projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        p.setName(sanitizeProjectName(name));
        ChatProject saved = chatProjectRepository.save(p);
        return new com.minzetsu.ecommerce.chatbot.dto.ChatProjectResponse(saved.getId(), saved.getName(), saved.getUpdatedAt());
    }

    public void deleteProject(Long userId, Long projectId) {
        chatProjectRepository.findByUserIdAndId(userId, projectId).ifPresent(p -> {
            chatConversationRepository.findByUserIdAndProjectIdOrderByUpdatedAtDesc(userId, projectId).forEach(c -> {
                chatMessageRepository.deleteByUserIdAndConversationId(userId, c.getId());
                chatConversationRepository.delete(c);
            });
            chatProjectRepository.delete(p);
        });
    }

    public List<ChatConversationResponse> listConversationsByProject(Long userId, Long projectId) {
        return chatConversationRepository.findByUserIdAndProjectIdOrderByUpdatedAtDesc(userId, projectId).stream()
                .map(c -> new ChatConversationResponse(c.getId(), c.getTitle(), c.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public ChatConversationResponse createConversation(Long userId, String title, Long projectId, Long groupId) {
        String safeTitle = (title == null || title.isBlank()) ? "New chat" : title.trim();
        if (projectId != null) {
            chatProjectRepository.findByUserIdAndId(userId, projectId)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
        }
        if (groupId != null) {
            requireGroupMember(userId, groupId);
        }
        ChatConversation conversation = new ChatConversation();
        conversation.setUserId(userId);
        conversation.setTitle(safeTitle);
        conversation.setProjectId(projectId);
        conversation.setGroupId(groupId);
        ChatConversation saved = chatConversationRepository.save(conversation);
        return new ChatConversationResponse(saved.getId(), saved.getTitle(), saved.getUpdatedAt());
    }

    public Long resolveConversationId(Long userId, Long conversationId, Long projectId, Long groupId) {
        if (conversationId != null) {
            ChatConversation c = chatConversationRepository.findById(conversationId).orElse(null);
            if (c != null) {
                assertCanAccessConversation(userId, c);
                return c.getId();
            }
            return getOrCreateDefaultConversation(userId, projectId, groupId).getId();
        }
        return getOrCreateDefaultConversation(userId, projectId, groupId).getId();
    }

    private ChatConversation getOrCreateDefaultConversation(Long userId, Long projectId, Long groupId) {
        if (groupId != null) {
            requireGroupMember(userId, groupId);
            java.util.List<ChatConversation> groupConversations = chatConversationRepository.findByGroupIdOrderByUpdatedAtDesc(groupId);
            for (ChatConversation c : groupConversations) {
                if ("General".equals(c.getTitle())) return c;
            }
        }
        if (projectId == null && groupId == null) {
            return chatConversationRepository.findByUserIdAndProjectIdIsNullAndGroupIdIsNullOrderByUpdatedAtDesc(userId).stream()
                    .filter(c -> "General".equals(c.getTitle()))
                    .findFirst()
                    .orElseGet(() -> {
                        ChatConversation conversation = new ChatConversation();
                        conversation.setUserId(userId);
                        conversation.setTitle("General");
                        return chatConversationRepository.save(conversation);
                    });
        }
        return chatConversationRepository.findByUserIdAndTitle(userId, "General")
                .orElseGet(() -> {
                    ChatConversation conversation = new ChatConversation();
                    conversation.setUserId(userId);
                    conversation.setTitle("General");
                    conversation.setProjectId(projectId);
        conversation.setGroupId(groupId);
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



    public List<ChatGroupResponse> listGroups(Long userId) {
        return chatGroupMemberRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(m -> chatGroupRepository.findById(m.getGroupId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(g -> new ChatGroupResponse(g.getId(), g.getName(), g.getOwnerUserId(), roleOf(userId, g.getId()), g.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public ChatGroupResponse createGroup(Long userId, String name) {
        ChatGroup g = new ChatGroup();
        g.setOwnerUserId(userId);
        g.setName(sanitizeProjectName(name));
        ChatGroup saved = chatGroupRepository.save(g);

        ChatGroupMember owner = new ChatGroupMember();
        owner.setGroupId(saved.getId());
        owner.setUserId(userId);
        owner.setRole("OWNER");
        chatGroupMemberRepository.save(owner);
        return new ChatGroupResponse(saved.getId(), saved.getName(), saved.getOwnerUserId(), "OWNER", saved.getUpdatedAt());
    }

    public void addMember(Long ownerUserId, Long groupId, Long userId) {
        requireGroupOwner(ownerUserId, groupId);
        if (chatGroupMemberRepository.findByGroupIdAndUserId(groupId, userId).isPresent()) return;
        if (chatGroupInviteRepository.findByGroupIdAndInviteeUserIdAndStatus(groupId, userId, "PENDING").isPresent()) return;
        ChatGroupInvite invite = new ChatGroupInvite();
        invite.setGroupId(groupId);
        invite.setInviterUserId(ownerUserId);
        invite.setInviteeUserId(userId);
        invite.setStatus("PENDING");
        ChatGroupInvite saved = chatGroupInviteRepository.save(invite);

        ChatGroup g = chatGroupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        String inviterDisplay = userRepository.findById(ownerUserId)
                .map(u -> (u.getFullName() != null && !u.getFullName().isBlank()) ? u.getFullName() : u.getUsername())
                .orElse("Group owner");
        NotificationCreateRequest req = NotificationCreateRequest.builder()
                .userId(userId)
                .title("Group Invitation")
                .message(inviterDisplay + " invited you to join group: " + g.getName())
                .type("SYSTEM")
                .referenceId(saved.getId().intValue())
                .referenceType("chat_group_invite")
                .build();
        notificationService.createNotificationResponse(req, userId);
        userRepository.findById(userId).map(User::getEmail).ifPresent(email -> sendGroupInviteEmailSafely(email, g.getName(), groupId, userId));
    }


    public java.util.List<ChatGroupInviteResponse> listPendingInvites(Long userId) {
        return chatGroupInviteRepository.findByInviteeUserIdAndStatusOrderByUpdatedAtDesc(userId, "PENDING").stream()
                .map(i -> new ChatGroupInviteResponse(i.getId(), i.getGroupId(), i.getInviterUserId(), i.getInviteeUserId(), i.getStatus(), i.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public long pendingInviteBadgeCount(Long userId) {
        long received = chatGroupInviteRepository.countByInviteeUserIdAndStatus(userId, "PENDING");
        long sent = chatGroupInviteRepository.countByInviterUserIdAndStatus(userId, "PENDING");
        return received + sent;
    }

    public void acceptInvite(Long userId, Long inviteId) {
        ChatGroupInvite invite = chatGroupInviteRepository.findByIdAndInviteeUserId(inviteId, userId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        if (!"PENDING".equalsIgnoreCase(invite.getStatus())) {
            throw new RuntimeException("Invite already processed");
        }
        if (chatGroupMemberRepository.findByGroupIdAndUserId(invite.getGroupId(), userId).isEmpty()) {
            ChatGroupMember m = new ChatGroupMember();
            m.setGroupId(invite.getGroupId());
            m.setUserId(userId);
            m.setRole("MEMBER");
            chatGroupMemberRepository.save(m);
        }
        invite.setStatus("ACCEPTED");
        chatGroupInviteRepository.save(invite);

        ChatGroup g = chatGroupRepository.findById(invite.getGroupId()).orElseThrow(() -> new RuntimeException("Group not found"));
        String memberName = userRepository.findById(userId)
                .map(u -> (u.getFullName() != null && !u.getFullName().isBlank()) ? u.getFullName() : u.getUsername())
                .orElse("A member");
        NotificationCreateRequest req = NotificationCreateRequest.builder()
                .userId(g.getOwnerUserId())
                .title("Invitation Accepted")
                .message(memberName + " accepted invitation to group: " + g.getName())
                .type("SYSTEM")
                .referenceId(invite.getId().intValue())
                .referenceType("chat_group_invite")
                .build();
        notificationService.createNotificationResponse(req, g.getOwnerUserId());
        String memberEmail = userRepository.findById(userId).map(User::getEmail).orElse("member");
        userRepository.findById(g.getOwnerUserId()).map(User::getEmail).ifPresent(email -> sendInviteAcceptedEmailSafely(email, g.getName(), memberEmail, invite.getId()));
    }


    public void addMemberByEmail(Long ownerUserId, Long groupId, String email) {
        if (email == null || email.isBlank()) throw new AppException("Email is required", HttpStatus.BAD_REQUEST);
        Long userId = userRepository.findByEmail(email.trim().toLowerCase())
                .map(u -> u.getId())
                .orElseThrow(() -> new AppException("No account found with this email", HttpStatus.NOT_FOUND));
        addMember(ownerUserId, groupId, userId);
    }

    @Transactional
    public void removeMember(Long ownerUserId, Long groupId, Long userId) {
        requireGroupOwner(ownerUserId, groupId);
        ChatGroup g = chatGroupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (g.getOwnerUserId().equals(userId)) throw new RuntimeException("Cannot remove owner");
        chatGroupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Transactional
    public void deleteGroup(Long ownerUserId, Long groupId) {
        requireGroupOwner(ownerUserId, groupId);
        chatConversationRepository.findByGroupIdOrderByUpdatedAtDesc(groupId).forEach(c -> chatMessageRepository.deleteByConversationId(c.getId()));
        chatConversationRepository.findByGroupIdOrderByUpdatedAtDesc(groupId).forEach(chatConversationRepository::delete);
        chatGroupMemberRepository.deleteByGroupId(groupId);
        chatGroupRepository.deleteById(groupId);
    }

    public List<ChatConversationResponse> listGroupConversations(Long userId, Long groupId) {
        requireGroupMember(userId, groupId);
        return chatConversationRepository.findByGroupIdOrderByUpdatedAtDesc(groupId).stream()
                .map(c -> new ChatConversationResponse(c.getId(), c.getTitle(), c.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    public ChatMessageResponse editMessage(Long userId, Long messageId, String content) {
        ChatMessage msg = chatMessageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        ChatConversation c = chatConversationRepository.findById(msg.getConversationId()).orElseThrow(() -> new RuntimeException("Conversation not found"));
        assertCanAccessConversation(userId, c);
        if (!msg.getUserId().equals(userId) && !isGroupOwner(userId, c.getGroupId())) {
            throw new RuntimeException("No permission to edit this message");
        }
        msg.setContent(sanitize(content));
        ChatMessage saved = chatMessageRepository.save(msg);
        return new ChatMessageResponse(
                saved.getRole(),
                saved.getContent(),
                saved.getUserId(),
                userRepository.findById(saved.getUserId())
                        .map(u -> (u.getFullName() != null && !u.getFullName().isBlank()) ? u.getFullName() : u.getUsername())
                        .orElse("Unknown"),
                saved.getCreatedAt()
        );
    }

    private void requireConversationAccess(Long userId, Long conversationId) {
        ChatConversation c = chatConversationRepository.findById(conversationId).orElseThrow(() -> new RuntimeException("Conversation not found"));
        assertCanAccessConversation(userId, c);
    }

    public void requireConversationAccessForRealtime(Long userId, Long conversationId) {
        requireConversationAccess(userId, conversationId);
    }

    private void assertCanAccessConversation(Long userId, ChatConversation c) {
        if (c.getGroupId() != null) {
            requireGroupMember(userId, c.getGroupId());
            return;
        }
        if (!c.getUserId().equals(userId)) throw new RuntimeException("Forbidden");
    }

    private void requireGroupMember(Long userId, Long groupId) {
        chatGroupMemberRepository.findByGroupIdAndUserId(groupId, userId).orElseThrow(() -> new RuntimeException("Not a group member"));
    }

    private void requireGroupOwner(Long userId, Long groupId) {
        if (!isGroupOwner(userId, groupId)) throw new RuntimeException("Only owner can do this action");
    }

    private boolean isGroupOwner(Long userId, Long groupId) {
        if (groupId == null) return false;
        return chatGroupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(m -> "OWNER".equalsIgnoreCase(m.getRole()))
                .orElse(false);
    }

    private String roleOf(Long userId, Long groupId) {
        return chatGroupMemberRepository.findByGroupIdAndUserId(groupId, userId).map(ChatGroupMember::getRole).orElse("MEMBER");
    }

    private String sanitizeTitle(String input) {
        String t = sanitize(input);
        if (t.isBlank()) return "New chat";
        return t.length() > 80 ? t.substring(0, 80) : t;
    }

    private String sanitizeProjectName(String input) {
        String t = sanitize(input);
        if (t.isBlank()) return "New project";
        return t.length() > 60 ? t.substring(0, 60) : t;
    }

    private String deriveTitleFromFirstMessage(String message) {
        String t = sanitize(message).replaceAll("\\s+", " ").trim();
        if (t.isBlank()) return "New chat";
        return t.length() > 60 ? t.substring(0, 60) : t;
    }

    private boolean isGenericTitle(String title) {
        if (title == null) return true;
        String t = title.trim().toLowerCase();
        return t.isBlank() || t.equals("general") || t.equals("new chat") || t.matches("chat\\s*\\d+");
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

    public String translateText(String text, String targetLang) {
        String target = (targetLang == null || targetLang.isBlank()) ? properties.getDefaultVoiceLang() : targetLang.trim().toLowerCase();
        String prompt = "Translate to " + ("vi".equals(target) ? "Vietnamese" : "English") + ":\n" + sanitize(text);
        String result = callLlm(prompt, "");
        return result == null || result.isBlank() ? sanitize(text) : result.trim();
    }

    public String transcribeAudio(MultipartFile file) {
        // Local-only baseline: browser Web Speech handles STT on client; backend keeps upload flow consistent.
        return "[Local STT] Audio received: " + file.getOriginalFilename() + ". Use browser mic recognition to transcribe in real time.";
    }

    public String readFileAndAnswer(MultipartFile file, String question) {
        try {
            String name = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename().toLowerCase();
            String text;
            if (name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".log") || name.endsWith(".csv") || name.endsWith(".json") || name.endsWith(".xml") || name.endsWith(".yml") || name.endsWith(".yaml")) {
                text = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else {
                return "Current local mode supports text-like files (.txt, .md, .json, .xml, .yml, .csv, .log).";
            }
            if (text.length() > 12000) text = text.substring(0, 12000);
            String q = (question == null || question.isBlank()) ? "Summarize this file." : question;
            String prompt = q + "\n\nFile content:\n" + text;
            String out = callLlm(prompt, "");
            return out == null || out.isBlank() ? "Cannot process file right now." : out;
        } catch (Exception ex) {
            return "Cannot process file right now.";
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

    public java.util.List<com.minzetsu.ecommerce.chatbot.dto.ChatGroupMemberResponse> listGroupMembers(Long userId, Long groupId) {
        requireGroupMember(userId, groupId);
        return chatGroupMemberRepository.findByGroupId(groupId).stream()
                .map(m -> userRepository.findById(m.getUserId())
                        .map(u -> new com.minzetsu.ecommerce.chatbot.dto.ChatGroupMemberResponse(
                                u.getId(),
                                u.getUsername(),
                                u.getFullName(),
                                m.getRole()
                        ))
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public void declineInvite(Long userId, Long inviteId) {
        ChatGroupInvite invite = chatGroupInviteRepository.findByIdAndInviteeUserId(inviteId, userId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        if (!"PENDING".equalsIgnoreCase(invite.getStatus())) {
            throw new RuntimeException("Invite already processed");
        }
        invite.setStatus("DECLINED");
        chatGroupInviteRepository.save(invite);
    }

    private void sendGroupInviteEmailSafely(String inviteeEmail, String groupName, Long groupId, Long inviteeUserId) {
        try {
            emailService.sendGroupInviteMail(inviteeEmail, groupName);
        } catch (Exception ex) {
            log.warn(
                    "Group invite email failed (groupId={}, inviteeUserId={}, email={}): {}",
                    groupId,
                    inviteeUserId,
                    inviteeEmail,
                    ex.getMessage()
            );
        }
    }

    private void sendInviteAcceptedEmailSafely(String ownerEmail, String groupName, String memberEmail, Long inviteId) {
        try {
            emailService.sendInviteAcceptedMail(ownerEmail, groupName, memberEmail);
        } catch (Exception ex) {
            log.warn(
                    "Invite accepted email failed (inviteId={}, ownerEmail={}): {}",
                    inviteId,
                    ownerEmail,
                    ex.getMessage()
            );
        }
    }

    private void saveMessage(Long userId, Long conversationId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setUserId(userId);
        msg.setConversationId(conversationId);
        msg.setRole(role);
        msg.setContent(content);
        ChatMessage saved = chatMessageRepository.save(msg);

        if (conversationId != null) {
            chatConversationRepository.findById(conversationId).ifPresent(c -> {
                if ("user".equals(role) && isGenericTitle(c.getTitle())) {
                    c.setTitle(deriveTitleFromFirstMessage(content));
                }
                c.setUpdatedAt(LocalDateTime.now());
                chatConversationRepository.save(c);

                String senderName = "";
                if ("user".equals(saved.getRole())) {
                    senderName = userRepository.findById(saved.getUserId())
                            .map(u -> (u.getFullName() != null && !u.getFullName().isBlank()) ? u.getFullName() : u.getUsername())
                            .orElse("Unknown");
                }
                Map<String, Object> payload = Map.of(
                        "conversationId", conversationId,
                        "role", saved.getRole(),
                        "content", saved.getContent(),
                        "userId", saved.getUserId(),
                        "senderName", senderName,
                        "createdAt", (saved.getCreatedAt() == null ? LocalDateTime.now() : saved.getCreatedAt()).toString()
                );
                chatbotRealtimeService.publishToConversation(conversationId, "chat-message", payload);
            });
        }
    }
}
