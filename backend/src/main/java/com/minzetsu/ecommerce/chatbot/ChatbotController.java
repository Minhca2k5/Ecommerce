package com.minzetsu.ecommerce.chatbot;

import lombok.RequiredArgsConstructor;
import com.minzetsu.ecommerce.chatbot.dto.ChatRequest;
import com.minzetsu.ecommerce.chatbot.dto.ChatResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatMessageResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatConversationResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatConversationUpdateRequest;
import com.minzetsu.ecommerce.chatbot.dto.ChatProjectCreateRequest;
import com.minzetsu.ecommerce.chatbot.dto.ChatProjectResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatMessageUpdateRequest;
import com.minzetsu.ecommerce.chatbot.dto.ChatGroupInviteResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatGroupResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatGroupMemberRequest;
import com.minzetsu.ecommerce.chatbot.dto.ChatGroupCreateRequest;
import com.minzetsu.ecommerce.chatbot.dto.ChatGroupMemberResponse;
import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me/chatbot")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatbotService.chat(getCurrentUserId(), request);
    }

    @GetMapping("/conversations")
    public java.util.List<ChatConversationResponse> conversations() {
        return chatbotService.listConversations(getCurrentUserId());
    }

    @GetMapping("/conversations/personal")
    public java.util.List<ChatConversationResponse> personalConversations() {
        return chatbotService.listPersonalConversations(getCurrentUserId());
    }

    @PostMapping("/conversations")
    public ChatConversationResponse createConversation(@RequestParam(required = false) String title,
                                                       @RequestParam(required = false) Long projectId,
                                                       @RequestParam(required = false) Long groupId) {
        return chatbotService.createConversation(getCurrentUserId(), title, projectId, groupId);
    }

    @PutMapping("/conversations/{conversationId}")
    public ChatConversationResponse renameConversation(@PathVariable Long conversationId,
                                                       @Valid @RequestBody ChatConversationUpdateRequest request) {
        return chatbotService.renameConversation(getCurrentUserId(), conversationId, request.getTitle());
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public java.util.List<ChatMessageResponse> history(@PathVariable Long conversationId,
                                                       @RequestParam(defaultValue = "20") int limit) {
        return chatbotService.listHistory(getCurrentUserId(), conversationId, limit);
    }

    @DeleteMapping("/conversations/{conversationId}")
    public void deleteConversation(@PathVariable Long conversationId) {
        chatbotService.deleteConversation(getCurrentUserId(), conversationId);
    }

    @GetMapping("/projects")
    public java.util.List<ChatProjectResponse> projects() {
        return chatbotService.listProjects(getCurrentUserId());
    }

    @PostMapping("/projects")
    public ChatProjectResponse createProject(@Valid @RequestBody ChatProjectCreateRequest request) {
        return chatbotService.createProject(getCurrentUserId(), request.getName());
    }

    @PutMapping("/projects/{projectId}")
    public ChatProjectResponse renameProject(@PathVariable Long projectId,
                                             @Valid @RequestBody ChatProjectCreateRequest request) {
        return chatbotService.renameProject(getCurrentUserId(), projectId, request.getName());
    }

    @DeleteMapping("/projects/{projectId}")
    public void deleteProject(@PathVariable Long projectId) {
        chatbotService.deleteProject(getCurrentUserId(), projectId);
    }

    @GetMapping("/projects/{projectId}/conversations")
    public java.util.List<ChatConversationResponse> conversationsByProject(@PathVariable Long projectId) {
        return chatbotService.listConversationsByProject(getCurrentUserId(), projectId);
    }



    @GetMapping("/groups")
    public java.util.List<ChatGroupResponse> groups() {
        return chatbotService.listGroups(getCurrentUserId());
    }

    @PostMapping("/groups")
    public ChatGroupResponse createGroup(@Valid @RequestBody ChatGroupCreateRequest request) {
        return chatbotService.createGroup(getCurrentUserId(), request.getName());
    }

    @DeleteMapping("/groups/{groupId}")
    public void deleteGroup(@PathVariable Long groupId) {
        chatbotService.deleteGroup(getCurrentUserId(), groupId);
    }

    @PostMapping("/groups/{groupId}/members")
    public void addMember(@PathVariable Long groupId, @Valid @RequestBody ChatGroupMemberRequest request) {
        if (request.getUserId() != null) {
            chatbotService.addMember(getCurrentUserId(), groupId, request.getUserId());
            return;
        }
        chatbotService.addMemberByEmail(getCurrentUserId(), groupId, request.getEmail());
    }

    @DeleteMapping("/groups/{groupId}/members/{userId}")
    public void removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        chatbotService.removeMember(getCurrentUserId(), groupId, userId);
    }


    @GetMapping("/groups/invites")
    public java.util.List<ChatGroupInviteResponse> pendingInvites() {
        return chatbotService.listPendingInvites(getCurrentUserId());
    }

    @GetMapping("/groups/invites/badge-count")
    public java.util.Map<String, Long> pendingInviteBadgeCount() {
        return java.util.Map.of("count", chatbotService.pendingInviteBadgeCount(getCurrentUserId()));
    }

    @PostMapping("/groups/invites/{inviteId}/accept")
    public void acceptInvite(@PathVariable Long inviteId) {
        chatbotService.acceptInvite(getCurrentUserId(), inviteId);
    }

    @PostMapping("/groups/invites/{inviteId}/decline")
    public void declineInvite(@PathVariable Long inviteId) {
        chatbotService.declineInvite(getCurrentUserId(), inviteId);
    }

    @GetMapping("/groups/{groupId}/conversations")
    public java.util.List<ChatConversationResponse> groupConversations(@PathVariable Long groupId) {
        return chatbotService.listGroupConversations(getCurrentUserId(), groupId);
    }

    @GetMapping("/groups/{groupId}/members")
    public java.util.List<ChatGroupMemberResponse> groupMembers(@PathVariable Long groupId) {
        return chatbotService.listGroupMembers(getCurrentUserId(), groupId);
    }

    @PutMapping("/messages/{messageId}")
    public ChatMessageResponse editMessage(@PathVariable Long messageId, @Valid @RequestBody ChatMessageUpdateRequest request) {
        return chatbotService.editMessage(getCurrentUserId(), messageId, request.getContent());
    }

    @PostMapping("/translate")
    public java.util.Map<String, String> translate(@RequestParam String text, @RequestParam(defaultValue = "en") String target) {
        return java.util.Map.of("text", chatbotService.translateText(text, target));
    }

    @PostMapping("/voice/transcribe")
    public java.util.Map<String, String> transcribe(@RequestParam("audio") MultipartFile audio) {
        return java.util.Map.of("text", chatbotService.transcribeAudio(audio));
    }
    @PostMapping("/media/file")
    public java.util.Map<String, String> readFile(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(required = false) String question) {
        return java.util.Map.of("reply", chatbotService.readFileAndAnswer(file, question));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
