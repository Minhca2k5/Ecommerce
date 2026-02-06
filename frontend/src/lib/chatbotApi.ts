import { apiJson, apiMultipart } from "@/lib/http";
import { getApiBaseUrl } from "@/lib/env";
import { clearStoredTokens, getStoredTokens, setStoredTokens } from "@/lib/authStorage";

export type ChatResponse = {
  reply: string;
};

export type ChatMessage = {
  role: "user" | "assistant";
  content: string;
  userId?: number;
  senderName?: string;
  createdAt?: string;
};

export type ChatConversation = {
  id: number;
  title: string;
  updatedAt?: string;
};

export type ChatProject = {
  id: number;
  name: string;
  updatedAt?: string;
};

export type ChatGroup = {
  id: number;
  name: string;
  ownerUserId: number;
  role: "OWNER" | "MEMBER" | string;
  updatedAt?: string;
};

export type ChatGroupInvite = {
  id: number;
  groupId: number;
  inviterUserId: number;
  inviteeUserId: number;
  status: "PENDING" | "ACCEPTED" | "DECLINED" | string;
  updatedAt?: string;
};

export type ChatGroupMember = {
  userId: number;
  username: string;
  fullName?: string;
  role: "OWNER" | "MEMBER" | string;
};

export async function sendChatMessageToConversation(message: string, conversationId: number, groupId?: number) {
  return apiJson<ChatResponse>("/api/users/me/chatbot", {
    method: "POST",
    auth: true,
    body: { message, conversationId, ...(groupId != null ? { groupId } : {}) },
  });
}

function createRequestId() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `req_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}

async function refreshTokensIfNeeded() {
  const tokens = getStoredTokens();
  if (!tokens?.refreshToken) {
    clearStoredTokens();
    return null;
  }
  const response = await fetch(`${getApiBaseUrl()}/api/auth/refresh-token`, {
    method: "POST",
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken: tokens.refreshToken }),
  });
  if (!response.ok) {
    clearStoredTokens();
    return null;
  }
  const payload = (await response.json()) as { accessToken?: string; refreshToken?: string; tokenType?: string };
  if (!payload.accessToken || !payload.refreshToken) {
    clearStoredTokens();
    return null;
  }
  setStoredTokens({
    accessToken: payload.accessToken,
    refreshToken: payload.refreshToken,
    tokenType: payload.tokenType || tokens.tokenType || "Bearer",
  });
  return getStoredTokens();
}

export async function streamChatMessageToConversation(
  message: string,
  conversationId: number,
  groupId: number | undefined,
  onChunk: (chunk: string) => void,
  onDone?: () => void,
) {
  const baseUrl = `${getApiBaseUrl()}/api/users/me/chatbot/stream`;
  const buildStreamUrl = (tokens: ReturnType<typeof getStoredTokens>) => {
    if (!tokens?.accessToken) return baseUrl;
    const qs = new URLSearchParams({ access_token: tokens.accessToken }).toString();
    return `${baseUrl}?${qs}`;
  };
  const makeHeaders = (tokens: ReturnType<typeof getStoredTokens>) => {
    const headers: Record<string, string> = {
      Accept: "text/event-stream",
      "Content-Type": "application/json",
      "X-Request-Id": createRequestId(),
    };
    if (tokens?.accessToken) {
      headers.Authorization = `${tokens.tokenType || "Bearer"} ${tokens.accessToken}`;
    }
    return headers;
  };

  let tokens = getStoredTokens();
  let url = buildStreamUrl(tokens);
  let response = await fetch(url, {
    method: "POST",
    headers: makeHeaders(tokens),
    body: JSON.stringify({ message, conversationId, ...(groupId != null ? { groupId } : {}) }),
  });

  if (response.status === 401 || response.status === 403) {
    tokens = await refreshTokensIfNeeded();
    url = buildStreamUrl(tokens);
    response = await fetch(url, {
      method: "POST",
      headers: makeHeaders(tokens),
      body: JSON.stringify({ message, conversationId, ...(groupId != null ? { groupId } : {}) }),
    });
  }
  if (!response.ok || !response.body) {
    throw new Error(`Stream request failed: ${response.status}`);
  }
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    let splitIndex = buffer.indexOf("\n\n");
    while (splitIndex !== -1) {
      const raw = buffer.slice(0, splitIndex);
      buffer = buffer.slice(splitIndex + 2);
      const lines = raw.split("\n");
      let event = "message";
      let data = "";
      for (const line of lines) {
        if (line.startsWith("event:")) {
          event = line.slice(6).trim();
        } else if (line.startsWith("data:")) {
          data += line.slice(5).trim();
        }
      }
      if (event === "chunk" && data) {
        onChunk(data);
      }
      if (event === "done") {
        onDone?.();
      }
      splitIndex = buffer.indexOf("\n\n");
    }
  }
  onDone?.();
}

export async function listChatConversations() {
  return apiJson<ChatConversation[]>("/api/users/me/chatbot/conversations", { method: "GET", auth: true });
}

export async function listPersonalConversations() {
  return apiJson<ChatConversation[]>("/api/users/me/chatbot/conversations/personal", { method: "GET", auth: true });
}

export async function createChatConversation(title?: string, projectId?: number, groupId?: number) {
  const qs = new URLSearchParams();
  if (title) qs.set("title", title);
  if (projectId != null) qs.set("projectId", String(projectId));
  if (groupId != null) qs.set("groupId", String(groupId));
  const suffix = qs.toString() ? `?${qs.toString()}` : "";
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


export async function renameChatConversation(conversationId: number, title: string) {
  return apiJson<ChatConversation>(`/api/users/me/chatbot/conversations/${conversationId}`, {
    method: "PUT",
    auth: true,
    body: { title },
  });
}

export async function deleteConversation(conversationId: number) {
  return apiJson<void>(`/api/users/me/chatbot/conversations/${conversationId}`, {
    method: "DELETE",
    auth: true,
  });
}

export async function listChatProjects() {
  return apiJson<ChatProject[]>("/api/users/me/chatbot/projects", { method: "GET", auth: true });
}

export async function createChatProject(name: string) {
  return apiJson<ChatProject>("/api/users/me/chatbot/projects", { method: "POST", auth: true, body: { name } });
}

export async function renameChatProject(projectId: number, name: string) {
  return apiJson<ChatProject>(`/api/users/me/chatbot/projects/${projectId}`, { method: "PUT", auth: true, body: { name } });
}

export async function deleteChatProject(projectId: number) {
  return apiJson<void>(`/api/users/me/chatbot/projects/${projectId}`, { method: "DELETE", auth: true });
}

export async function listConversationsByProject(projectId: number) {
  return apiJson<ChatConversation[]>(`/api/users/me/chatbot/projects/${projectId}/conversations`, { method: "GET", auth: true });
}


export async function uploadChatFile(file: File, question?: string) {
  const form = new FormData();
  form.append("file", file);
  if (question) form.append("question", question);
  return apiMultipart<{ reply: string }>("/api/users/me/chatbot/media/file", form, { auth: true });
}

export async function translateVoiceText(text: string, target: "en" | "vi" = "en") {
  const q = new URLSearchParams({ text, target }).toString();
  return apiJson<{ text: string }>(`/api/users/me/chatbot/translate?${q}`, { method: "POST", auth: true });
}


export async function listChatGroups() {
  return apiJson<ChatGroup[]>("/api/users/me/chatbot/groups", { method: "GET", auth: true });
}

export async function createChatGroup(name: string) {
  return apiJson<ChatGroup>("/api/users/me/chatbot/groups", { method: "POST", auth: true, body: { name } });
}

export async function deleteChatGroup(groupId: number) {
  return apiJson<void>(`/api/users/me/chatbot/groups/${groupId}`, { method: "DELETE", auth: true });
}

export async function addChatGroupMember(groupId: number, payload: { userId?: number; email?: string }) {
  return apiJson<void>(`/api/users/me/chatbot/groups/${groupId}/members`, { method: "POST", auth: true, body: payload });
}

export async function removeChatGroupMember(groupId: number, userId: number) {
  return apiJson<void>(`/api/users/me/chatbot/groups/${groupId}/members/${userId}`, { method: "DELETE", auth: true });
}

export async function listGroupConversations(groupId: number) {
  return apiJson<ChatConversation[]>(`/api/users/me/chatbot/groups/${groupId}/conversations`, { method: "GET", auth: true });
}

export async function listGroupMembers(groupId: number) {
  return apiJson<ChatGroupMember[]>(`/api/users/me/chatbot/groups/${groupId}/members`, { method: "GET", auth: true });
}

export async function listGroupInvites() {
  return apiJson<ChatGroupInvite[]>("/api/users/me/chatbot/groups/invites", { method: "GET", auth: true });
}

export async function getInviteBadgeCount() {
  return apiJson<{ count: number }>("/api/users/me/chatbot/groups/invites/badge-count", { method: "GET", auth: true });
}

export async function acceptGroupInvite(inviteId: number) {
  return apiJson<void>(`/api/users/me/chatbot/groups/invites/${inviteId}/accept`, { method: "POST", auth: true });
}

export async function declineGroupInvite(inviteId: number) {
  return apiJson<void>(`/api/users/me/chatbot/groups/invites/${inviteId}/decline`, { method: "POST", auth: true });
}

export async function editChatMessage(messageId: number, content: string) {
  return apiJson<{ role: "user" | "assistant"; content: string; createdAt?: string }>(`/api/users/me/chatbot/messages/${messageId}`, {
    method: "PUT",
    auth: true,
    body: { content },
  });
}
