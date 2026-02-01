import { apiJson } from "@/lib/http";

export type ChatResponse = {
  reply: string;
};

export type ChatMessage = {
  role: "user" | "assistant";
  content: string;
  createdAt?: string;
};

export type ChatConversation = {
  id: number;
  title: string;
  updatedAt?: string;
};

export async function sendChatMessageToConversation(message: string, conversationId: number) {
  return apiJson<ChatResponse>("/api/users/me/chatbot", {
    method: "POST",
    auth: true,
    body: { message, conversationId },
  });
}

export async function listChatConversations() {
  return apiJson<ChatConversation[]>("/api/users/me/chatbot/conversations", {
    method: "GET",
    auth: true,
  });
}

export async function createChatConversation(title?: string) {
  const suffix = title ? `?title=${encodeURIComponent(title)}` : "";
  return apiJson<ChatConversation>(`/api/users/me/chatbot/conversations${suffix}`, {
    method: "POST",
    auth: true,
  });
}

export async function getChatHistory(conversationId: number, limit = 20) {
  return apiJson<ChatMessage[]>(
    `/api/users/me/chatbot/conversations/${conversationId}/messages?limit=${limit}`,
    {
      method: "GET",
      auth: true,
    },
  );
}

export async function clearChatHistory(conversationId: number) {
  return apiJson<void>(`/api/users/me/chatbot/conversations/${conversationId}`, {
    method: "DELETE",
    auth: true,
  });
}
