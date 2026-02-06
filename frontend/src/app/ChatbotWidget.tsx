import { useEffect, useMemo, useRef, useState } from "react";
import { createPortal } from "react-dom";
import {
  acceptGroupInvite,
  addChatGroupMember,
  createChatConversation,
  createChatGroup,
  createChatProject,
  deleteChatProject,
  deleteChatGroup,
  deleteConversation,
  declineGroupInvite,
  getChatHistory,
  getInviteBadgeCount,
  listChatConversations,
  listConversationsByProject,
  listGroupConversations,
  listPersonalConversations,
  listChatGroups,
  listChatProjects,
  listGroupInvites,
  listGroupMembers,
  removeChatGroupMember,
  renameChatConversation,
  sendChatMessageToConversation,
  uploadChatFile,
  type ChatConversation,
  type ChatGroup,
  type ChatGroupInvite,
  type ChatGroupMember,
  type ChatProject,
} from "@/lib/chatbotApi";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { createAuthedEventSource } from "@/lib/sse";

type ChatItem = { role: "user" | "assistant"; content: string; userId?: number; senderName?: string; createdAt?: string };
type Scope = "personal" | "project" | "group";
type ChatConversationView = ChatConversation & { scopeLabel?: "PERSONAL" | "PROJECT" | "GROUP" };

export default function ChatbotWidget() {
  const auth = useAuth();
  const toast = useToast();
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState("");
  const [items, setItems] = useState<ChatItem[]>([]);
  const [isSending, setIsSending] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [conversations, setConversations] = useState<ChatConversationView[]>([]);
  const [activeConversationId, setActiveConversationId] = useState<number | null>(null);
  const [scope, setScope] = useState<Scope>("personal");
  const [projects, setProjects] = useState<ChatProject[]>([]);
  const [groups, setGroups] = useState<ChatGroup[]>([]);
  const [invites, setInvites] = useState<ChatGroupInvite[]>([]);
  const [groupMembers, setGroupMembers] = useState<ChatGroupMember[]>([]);
  const [inviteBadgeCount, setInviteBadgeCount] = useState(0);
  const [activeProjectId, setActiveProjectId] = useState<number | null>(null);
  const [activeGroupId, setActiveGroupId] = useState<number | null>(null);
  const [lastByScope, setLastByScope] = useState<Record<string, number | null>>({});
  const sendingRef = useRef(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [pendingFile, setPendingFile] = useState<File | null>(null);
  const selfSenderName = auth.user?.fullName?.trim() ? auth.user.fullName : auth.user?.username ?? "You";
  const useGroupSseRender = scope === "group";

  const [stickerPos, setStickerPos] = useState({ x: 0, y: 0 });
  const dragRef = useRef({ dragging: false, startX: 0, startY: 0, originX: 0, originY: 0 });

  const activeGroup = useMemo(() => groups.find((g) => g.id === activeGroupId) ?? null, [groups, activeGroupId]);

  async function refreshMeta() {
    try {
      const [p, g, i, badge] = await Promise.all([listChatProjects(), listChatGroups(), listGroupInvites(), getInviteBadgeCount()]);
      setProjects(p);
      setGroups(g);
      setInvites(i.filter((x) => x.status === "PENDING"));
      setInviteBadgeCount(badge.count ?? 0);
      if (activeGroupId) {
        const members = await listGroupMembers(activeGroupId);
        setGroupMembers(members);
      } else {
        setGroupMembers([]);
      }
    } catch {
      // ignore
    }
  }

  async function loadConversations() {
    try {
      let list: ChatConversation[] = [];
      if (scope === "personal") {
        list = await listPersonalConversations();
      } else if (scope === "project" && activeProjectId) {
        list = await listConversationsByProject(activeProjectId);
      } else if (scope === "group" && activeGroupId) {
        list = await listGroupConversations(activeGroupId);
      } else {
        list = await listChatConversations();
      }
      setConversations(list.map((c) => ({ ...c, scopeLabel: scope === "project" ? "PROJECT" : scope === "group" ? "GROUP" : "PERSONAL" } as any)));
      if (list.length > 0) {
        const key = `${scope}:${activeProjectId ?? "-"}:${activeGroupId ?? "-"}`;
        const remembered = lastByScope[key];
        const chosen = remembered && list.some((x) => x.id === remembered) ? remembered : list[0].id;
        setActiveConversationId(chosen);
      } else {
        setActiveConversationId(null);
        setItems([]);
      }
    } catch {
      // ignore
    }
  }

  async function loadHistory(conversationId: number) {
    setIsLoading(true);
    try {
      const history = await getChatHistory(conversationId, 30);
      setItems(
        history?.map((h) => ({
          role: h.role,
          content: h.content,
          userId: h.userId,
          senderName: h.senderName,
          createdAt: h.createdAt,
        })) ?? [],
      );
    } catch {
      setItems([]);
    } finally {
      setIsLoading(false);
    }
  }

  async function onSend() {
    if (sendingRef.current || !activeConversationId) return;
    const text = message.trim();
    if (!text && !pendingFile) return;
    sendingRef.current = true;
    setIsSending(true);
    try {
      if (pendingFile) {
        setItems((prev) => [
          ...prev,
          {
            role: "user",
            content: `[File] ${pendingFile.name}${text ? `\n${text}` : ""}`,
            userId: auth.user?.id,
            senderName: scope === "group" ? selfSenderName : undefined,
            createdAt: new Date().toISOString(),
          },
        ]);
        const res = await uploadChatFile(pendingFile, text || "Summarize and answer key points");
        setItems((prev) => [...prev, { role: "assistant", content: res.reply }]);
        setPendingFile(null);
      }

      if (text && !pendingFile) {
        if (!useGroupSseRender) {
          setItems((prev) => [
            ...prev,
            {
              role: "user",
              content: text,
              userId: auth.user?.id,
              senderName: undefined,
              createdAt: new Date().toISOString(),
            },
          ]);
        }
        const res = await sendChatMessageToConversation(text, activeConversationId, scope === "group" ? activeGroupId ?? undefined : undefined);
        if (!useGroupSseRender) {
          setItems((prev) => [...prev, { role: "assistant", content: res.reply }]);
        }
        setConversations((prev) => prev.map((c) => (c.id === activeConversationId ? { ...c, updatedAt: new Date().toISOString() } : c)));
      }

      setMessage("");
    } catch (e) {
      toast.push({ variant: "error", title: "Chatbot error", message: getErrorMessage(e, "Please try again.") });
    } finally {
      setIsSending(false);
      sendingRef.current = false;
    }
  }

  async function startNewChat() {
    try {
      const created = await createChatConversation("New chat", scope === "project" ? activeProjectId ?? undefined : undefined, scope === "group" ? activeGroupId ?? undefined : undefined);
      setConversations((prev) => [created, ...prev]);
      setActiveConversationId(created.id);
      setItems([]);
    } catch (e) {
      toast.push({ variant: "error", title: "Create chat failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  async function onRenameActiveChat() {
    if (!activeConversationId) return;
    const current = conversations.find((c) => c.id === activeConversationId);
    const next = window.prompt("Rename chat", current?.title ?? "");
    if (!next || !next.trim()) return;
    try {
      const updated = await renameChatConversation(activeConversationId, next.trim());
      setConversations((prev) => prev.map((c) => (c.id === activeConversationId ? { ...c, title: updated.title } : c)));
    } catch (e) {
      toast.push({ variant: "error", title: "Rename failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  async function onDeleteActiveChat() {
    if (!activeConversationId || !window.confirm("Delete this chat?")) return;
    try {
      await deleteConversation(activeConversationId);
      const remaining = conversations.filter((c) => c.id !== activeConversationId);
      setConversations(remaining);
      setActiveConversationId(remaining[0]?.id ?? null);
      if (!remaining.length) setItems([]);
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  async function onCreateProject() {
    const name = window.prompt("Project name");
    if (!name?.trim()) return;
    try {
      const created = await createChatProject(name.trim());
      setProjects((prev) => [created, ...prev]);
      setScope("project");
      setActiveProjectId(created.id);
    } catch (e) {
      toast.push({ variant: "error", title: "Create project failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  async function onDeleteProject(projectId: number) {
    if (!window.confirm("Delete this project and its chats?")) return;
    try {
      await deleteChatProject(projectId);
      setProjects((prev) => prev.filter((p) => p.id !== projectId));
      if (activeProjectId === projectId) {
        setActiveProjectId(null);
        setScope("personal");
      }
    } catch (e) {
      toast.push({ variant: "error", title: "Delete project failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  async function onCreateGroup() {
    const name = window.prompt("Group name");
    if (!name?.trim()) return;
    try {
      const created = await createChatGroup(name.trim());
      setGroups((prev) => [created, ...prev]);
      setScope("group");
      setActiveGroupId(created.id);
      const ownerName = auth.user?.fullName?.trim() ? auth.user.fullName : auth.user?.username ?? "Owner";
      const ownerId = typeof auth.user?.id === "number" ? auth.user.id : 0;
      setGroupMembers([{ userId: ownerId, username: auth.user?.username ?? "owner", fullName: ownerName, role: "OWNER" }]);
    } catch (e) {
      toast.push({ variant: "error", title: "Create group failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  async function onInviteMember() {
    if (!activeGroupId) return;
    const email = window.prompt("Invite member email");
    if (!email?.trim()) return;
    try {
      await addChatGroupMember(activeGroupId, { email: email.trim() });
      toast.push({ variant: "success", title: "Invitation sent", message: `${email} invited.` });
      await refreshMeta();
    } catch (e) {
      toast.push({ variant: "error", title: "Invite failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  async function onRemoveMember() {
    if (!activeGroupId) return;
    const targetName = window.prompt("Nhập username hoặc full name thành viên cần xóa");
    if (!targetName?.trim()) return;
    const normalized = targetName.trim().toLowerCase();
    const target = groupMembers.find(
      (m) =>
        m.role !== "OWNER" &&
        (m.username?.toLowerCase() === normalized || m.fullName?.trim().toLowerCase() === normalized),
    );
    if (!target) {
      toast.push({ variant: "error", title: "Remove failed", message: "Không tìm thấy thành viên theo tên." });
      return;
    }
    try {
      await removeChatGroupMember(activeGroupId, target.userId);
      toast.push({ variant: "success", title: "Member removed", message: `${target.fullName || target.username} removed.` });
      await refreshMeta();
    } catch (e) {
      toast.push({ variant: "error", title: "Remove failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  async function onDeleteGroup() {
    if (!activeGroupId || !window.confirm("Delete this group?")) return;
    try {
      await deleteChatGroup(activeGroupId);
      setGroups((prev) => prev.filter((g) => g.id !== activeGroupId));
      setActiveGroupId(null);
      setScope("personal");
    } catch (e) {
      toast.push({ variant: "error", title: "Delete group failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  async function onAcceptInvite(inviteId: number) {
    try {
      await acceptGroupInvite(inviteId);
      toast.push({ variant: "success", title: "Invite accepted", message: "Bạn đã chấp nhận lời mời vào nhóm." });
      await refreshMeta();
      await loadConversations();
    } catch (e) {
      toast.push({ variant: "error", title: "Accept failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  async function onDeclineInvite(inviteId: number) {
    try {
      await declineGroupInvite(inviteId);
      toast.push({ variant: "success", title: "Invite declined", message: "Invitation declined." });
      await refreshMeta();
    } catch (e) {
      toast.push({ variant: "error", title: "Decline failed", message: getErrorMessage(e, "Please try again.") });
    }
  }

  function onVoiceInput() {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      toast.push({ variant: "default", title: "Voice unsupported", message: "Browser does not support speech recognition." });
      return;
    }
    const rec = new SpeechRecognition();
    rec.lang = "en-US";
    rec.interimResults = false;
    rec.maxAlternatives = 1;
    rec.onresult = async (event: any) => {
      const transcript = event?.results?.[0]?.[0]?.transcript ?? "";
      if (!transcript) return;
      if (sendingRef.current || !activeConversationId) {
        setMessage(transcript);
        return;
      }
      sendingRef.current = true;
      setIsSending(true);
      if (!useGroupSseRender) {
        setItems((prev) => [
          ...prev,
          {
            role: "user",
            content: transcript,
            userId: auth.user?.id,
            senderName: undefined,
            createdAt: new Date().toISOString(),
          },
        ]);
      }
      try {
        const res = await sendChatMessageToConversation(
          transcript,
          activeConversationId,
          scope === "group" ? activeGroupId ?? undefined : undefined,
        );
        if (!useGroupSseRender) {
          setItems((prev) => [...prev, { role: "assistant", content: res.reply }]);
        }
        setConversations((prev) =>
          prev.map((c) => (c.id === activeConversationId ? { ...c, updatedAt: new Date().toISOString() } : c)),
        );
        setMessage("");
      } catch (e) {
        toast.push({ variant: "error", title: "Chatbot error", message: getErrorMessage(e, "Please try again.") });
        setMessage(transcript);
      } finally {
        setIsSending(false);
        sendingRef.current = false;
      }
    };
    rec.start();
  }

  useEffect(() => {
    if (!open) return;
    void refreshMeta();
  }, [open]);

  useEffect(() => {
    if (!open) return;
    void refreshMeta();
  }, [open, activeGroupId]);

  useEffect(() => {
    if (!open) return;
    void loadConversations();
  }, [open, scope, activeProjectId, activeGroupId]);

  useEffect(() => {
    if (!open || !activeConversationId) return;
    void loadHistory(activeConversationId);
  }, [open, activeConversationId]);

  useEffect(() => {
    if (!open || scope !== "group" || !activeConversationId) return;
    const es = createAuthedEventSource(`/api/users/me/realtime/chatbot/conversations/${activeConversationId}`);
    const onChatMessage = (evt: MessageEvent) => {
      try {
        const data = JSON.parse(evt.data ?? "{}") as {
          conversationId?: number;
          role?: "user" | "assistant";
          content?: string;
          userId?: number;
          senderName?: string;
          createdAt?: string;
        };
        if (!data || Number(data.conversationId) !== Number(activeConversationId) || !data.role || !data.content) {
          return;
        }
        const nextItem: ChatItem = {
          role: data.role,
          content: data.content,
          userId: data.userId,
          senderName: data.senderName,
          createdAt: data.createdAt,
        };
        setItems((prev) => {
          const duplicated = prev.some(
            (x) =>
              x.role === nextItem.role &&
              x.content === nextItem.content &&
              Number(x.userId ?? 0) === Number(data.userId ?? 0) &&
              String(x.createdAt ?? "") === String(data.createdAt ?? ""),
          );
          if (duplicated) return prev;
          return [...prev, nextItem];
        });
      } catch {
        // ignore malformed event payload
      }
    };
    es.addEventListener("chat-message", onChatMessage as EventListener);
    es.onerror = () => {
      // browser auto-reconnects EventSource
    };
    return () => {
      es.close();
    };
  }, [open, scope, activeConversationId]);

  useEffect(() => {
    if (!activeConversationId) return;
    const key = `${scope}:${activeProjectId ?? "-"}:${activeGroupId ?? "-"}`;
    setLastByScope((prev) => ({ ...prev, [key]: activeConversationId }));
  }, [activeConversationId, scope, activeProjectId, activeGroupId]);

  useEffect(() => {
    const margin = 20;
    const width = 76;
    const height = 76;
    setStickerPos({ x: Math.max(margin, window.innerWidth - width - margin), y: Math.max(margin, window.innerHeight - height - margin) });
  }, []);

  function clampSticker(x: number, y: number) {
    const margin = 12;
    const width = 76;
    const height = 76;
    const maxX = Math.max(margin, window.innerWidth - width - margin);
    const maxY = Math.max(margin, window.innerHeight - height - margin);
    return { x: Math.min(Math.max(margin, x), maxX), y: Math.min(Math.max(margin, y), maxY) };
  }

  function onDragStart(clientX: number, clientY: number) {
    dragRef.current.dragging = true;
    dragRef.current.startX = clientX;
    dragRef.current.startY = clientY;
    dragRef.current.originX = stickerPos.x;
    dragRef.current.originY = stickerPos.y;
  }

  function onDragMove(clientX: number, clientY: number) {
    if (!dragRef.current.dragging) return;
    const dx = clientX - dragRef.current.startX;
    const dy = clientY - dragRef.current.startY;
    setStickerPos(clampSticker(dragRef.current.originX + dx, dragRef.current.originY + dy));
  }

  function onDragEnd() {
    dragRef.current.dragging = false;
  }

  return (
    <>
      <div
        role="button"
        tabIndex={0}
        aria-label="Open chatbot"
        title="Chatbot"
        onClick={() => {
          if (!auth.isAuthenticated) {
            toast.push({ variant: "default", title: "Login required", message: "Please login to use the chatbot." });
            return;
          }
          setOpen((v) => !v);
        }}
        onMouseDown={(e) => { e.preventDefault(); onDragStart(e.clientX, e.clientY); }}
        onMouseMove={(e) => onDragMove(e.clientX, e.clientY)}
        onMouseUp={onDragEnd}
        onMouseLeave={onDragEnd}
        onTouchStart={(e) => { const t = e.touches[0]; if (t) onDragStart(t.clientX, t.clientY); }}
        onTouchMove={(e) => { const t = e.touches[0]; if (t) onDragMove(t.clientX, t.clientY); }}
        onTouchEnd={onDragEnd}
        className="fixed z-40 flex h-[72px] w-[72px] select-none items-center justify-center transition hover:-translate-y-1"
        style={{ left: stickerPos.x, top: stickerPos.y, touchAction: "none" }}
      >
        <div className="relative">
          <svg viewBox="0 0 64 64" className="h-14 w-14 drop-shadow-[0_10px_16px_rgba(0,0,0,0.35)]" aria-hidden="true">
            <path d="M10 22l10-8 8 10" fill="#F6A85F" />
            <path d="M54 22l-10-8-8 10" fill="#F6A85F" />
            <circle cx="32" cy="34" r="18" fill="#F8C27B" />
            <circle cx="24" cy="32" r="3" fill="#2B2B2B" />
            <circle cx="40" cy="32" r="3" fill="#2B2B2B" />
            <circle cx="32" cy="38" r="2.6" fill="#D16E6E" />
            <path d="M26 41c2 3 5 4 6 4s4-1 6-4" stroke="#2B2B2B" strokeWidth="2" fill="none" strokeLinecap="round" />
            <path d="M18 38h8M38 38h8" stroke="#2B2B2B" strokeWidth="2" strokeLinecap="round" />
          </svg>
        </div>
      </div>

      {open ? createPortal(
        <div className="fixed bottom-24 right-6 z-50 w-[360px] sm:w-[760px]">
          <div className="rounded-2xl border bg-background/95 shadow-xl backdrop-blur">
            <div className="flex items-center justify-between border-b px-4 py-3">
              <div className="text-sm font-semibold">Chatbot</div>
              <div className="flex items-center gap-2">
                <button type="button" onClick={startNewChat} className="rounded-lg px-2 py-1 text-xs hover:bg-muted">New chat</button>
                <button type="button" onClick={onRenameActiveChat} className="rounded-lg px-2 py-1 text-xs hover:bg-muted">Rename</button>
                <button type="button" onClick={onDeleteActiveChat} className="rounded-lg px-2 py-1 text-xs text-red-500 hover:bg-red-50">Delete</button>
                <button type="button" onClick={() => setOpen(false)} className="rounded-lg px-2 py-1 text-xs hover:bg-muted">x</button>
              </div>
            </div>

            <div className="flex min-h-[420px]">
              <div className="w-[260px] border-r px-3 py-3 text-xs space-y-3 hidden sm:block">
                <div className="space-y-2">
                  <div className="font-semibold text-muted-foreground">Scope</div>
                  <div className="flex gap-2">
                    <button className={`rounded px-2 py-1 border ${scope === "personal" ? "bg-primary/10 border-primary" : ""}`} onClick={() => setScope("personal")}>Personal</button>
                    <button className={`rounded px-2 py-1 border ${scope === "project" ? "bg-primary/10 border-primary" : ""}`} onClick={() => setScope("project")}>Project</button>
                    <button className={`rounded px-2 py-1 border ${scope === "group" ? "bg-primary/10 border-primary" : ""}`} onClick={() => setScope("group")}>Group</button>
                  </div>
                </div>

                <div className="space-y-2">
                  <div className="font-semibold text-muted-foreground">Projects</div>
                  <button className="rounded border px-2 py-1 w-full text-left" onClick={onCreateProject}>+ New project</button>
                  <div className="max-h-24 overflow-auto space-y-1">
                    {projects.map((p) => (
                      <div key={p.id} className={`flex items-center gap-1 rounded border px-2 py-1 ${activeProjectId===p.id ? "bg-primary/10 border-primary" : ""}`}>
                        <button onClick={() => { setActiveProjectId(p.id); setScope("project"); }} className="min-w-0 flex-1 text-left truncate">{p.name}</button>
                        <button onClick={() => void onDeleteProject(p.id)} className="rounded px-1 text-red-500 hover:bg-red-50">x</button>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="space-y-2">
                  <div className="font-semibold text-muted-foreground">Groups</div>
                  <div className="flex gap-1">
                    <button className="rounded border px-2 py-1" onClick={onCreateGroup}>+ Group</button>
                    {activeGroup?.role === "OWNER" ? <button className="rounded border px-2 py-1" onClick={onInviteMember}>Invite</button> : null}
                    {activeGroup?.role === "OWNER" ? <button className="rounded border px-2 py-1" onClick={onRemoveMember}>Remove</button> : null}
                    {activeGroup?.role === "OWNER" ? <button className="rounded border px-2 py-1 text-red-500" onClick={onDeleteGroup}>Delete</button> : null}
                  </div>
                  <div className="max-h-24 overflow-auto space-y-1">
                    {groups.map((g) => <button key={g.id} onClick={() => { setActiveGroupId(g.id); setScope("group"); }} className={`w-full rounded border px-2 py-1 text-left ${activeGroupId===g.id ? "bg-primary/10 border-primary" : ""}`}>{g.name} ({g.role})</button>)}
                  </div>
                  {activeGroup ? (
                    <div className="rounded border px-2 py-2">
                      <div className="mb-1 text-[11px] text-muted-foreground">Members ({groupMembers.length})</div>
                      <div className="max-h-24 space-y-1 overflow-auto">
                        {groupMembers.map((m) => (
                          <div key={m.userId} className="text-[11px]">
                            {(m.fullName && m.fullName.trim()) ? m.fullName : m.username} ({m.role})
                          </div>
                        ))}
                      </div>
                    </div>
                  ) : null}
                </div>

                <div className="space-y-2">
                  <div className="font-semibold text-muted-foreground">Invites ({inviteBadgeCount})</div>
                  <div className="max-h-24 overflow-auto space-y-1">
                    {invites.map((i) => (
                      <div key={i.id} className="rounded border px-2 py-1">
                        <div>Group #{i.groupId}</div>
                        <div className="mt-1 flex gap-1">
                          <button className="rounded border px-2 py-1" onClick={() => onAcceptInvite(i.id)}>Accept</button>
                          <button className="rounded border px-2 py-1 text-red-500" onClick={() => onDeclineInvite(i.id)}>Refuse</button>
                        </div>
                      </div>
                    ))}
                    {!invites.length ? <div className="text-muted-foreground">No pending invites</div> : null}
                  </div>
                </div>
              </div>

              <div className="flex flex-1 flex-col">
                <div className="border-b px-3 py-2 text-xs text-muted-foreground">{scope.toUpperCase()} {scope === "project" && activeProjectId ? `#${activeProjectId}` : ""}{scope === "group" && activeGroup ? ` - ${activeGroup.name}` : ""}</div>
                <div className="border-b px-3 py-2 overflow-x-auto flex gap-2">
                  {conversations.map((c) => (
                    <button key={c.id} type="button" className={`rounded-full border px-3 py-1 text-xs ${activeConversationId===c.id ? "bg-primary/10 border-primary" : ""}`} onClick={() => { setActiveConversationId(c.id); setLastByScope((prev) => ({ ...prev, [`${scope}:${activeProjectId ?? "-"}:${activeGroupId ?? "-"}`]: c.id })); }}>{c.title} <span className="opacity-70">[{c.scopeLabel ?? (scope === "project" ? "PROJECT" : scope === "group" ? "GROUP" : "PERSONAL")}]</span></button>
                  ))}
                </div>
                <div className="max-h-80 flex-1 overflow-auto px-4 py-3">
                  {isLoading ? <div className="text-sm text-muted-foreground">Loading...</div> : items.length ? (
                    <div className="space-y-2">
                      {items.map((item, idx) => (
                        <div key={`${item.role}-${idx}`} className={`rounded-2xl px-3 py-2 text-sm whitespace-pre-wrap ${item.role === "user" ? "ml-auto max-w-[80%] bg-primary text-primary-foreground" : "mr-auto max-w-[80%] bg-muted"}`}>
                          {scope === "group" && item.role === "user" && item.senderName ? <div className="mb-1 text-[11px] opacity-80">{item.senderName}</div> : null}
                          {item.content}
                        </div>
                      ))}
                      {isSending ? (
                        <div className="mr-auto max-w-[80%] rounded-2xl bg-muted px-3 py-2 text-sm text-muted-foreground">
                          Đang suy nghĩ...
                        </div>
                      ) : null}
                    </div>
                  ) : (
                    <div className="space-y-2">
                      <div className="text-sm text-muted-foreground">Ask something...</div>
                      {isSending ? (
                        <div className="mr-auto max-w-[80%] rounded-2xl bg-muted px-3 py-2 text-sm text-muted-foreground">
                          Đang suy nghĩ...
                        </div>
                      ) : null}
                    </div>
                  )}
                </div>
                <div className="border-t px-3 py-2">
                  {(pendingFile) ? (
                    <div className="mb-2 flex flex-wrap gap-2 text-xs">
                      {pendingFile ? <span className="rounded-full border px-2 py-1">File: {pendingFile.name} <button onClick={() => setPendingFile(null)}>x</button></span> : null}
                    </div>
                  ) : null}
                  <div className="flex items-center gap-2">
                  <input className="h-9 flex-1 rounded-xl border bg-background px-3 text-sm" placeholder="Ask something..." value={message} onChange={(e) => setMessage(e.target.value)} onKeyDown={(e) => { if (e.key === "Enter") { e.preventDefault(); void onSend(); } }} disabled={isSending} />
                  <button type="button" onClick={onSend} disabled={isSending} className="h-9 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 px-3 text-sm text-white">{isSending ? "..." : "Send"}</button>
                  <button type="button" onClick={onVoiceInput} className="h-9 rounded-xl border px-2 text-xs">Mic</button>
                  <button type="button" onClick={() => fileInputRef.current?.click()} className="h-9 rounded-xl border px-2 text-xs">File</button>
                  <input ref={fileInputRef} type="file" className="hidden" accept=".txt,.md,.json,.xml,.yml,.yaml,.csv,.log,.pdf,.docx" onChange={(e) => { const f = e.target.files?.[0]; if (f) setPendingFile(f); e.currentTarget.value = ""; }} />
                </div>
                </div>
              </div>
            </div>
          </div>
        </div>, document.body) : null}
    </>
  );
}
