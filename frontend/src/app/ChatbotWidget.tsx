import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import {
  createChatConversation,
  getChatHistory,
  listChatConversations,
  sendChatMessageToConversation,
  type ChatConversation,
} from "@/lib/chatbotApi";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";

type ChatItem = { role: "user" | "assistant"; content: string };

export default function ChatbotWidget() {
  const auth = useAuth();
  const toast = useToast();
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState("");
  const [items, setItems] = useState<ChatItem[]>([]);
  const [isSending, setIsSending] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [conversations, setConversations] = useState<ChatConversation[]>([]);
  const [activeConversationId, setActiveConversationId] = useState<number | null>(null);
  const sendingRef = useRef(false);

  async function onSend() {
    if (sendingRef.current) return;
    if (!activeConversationId) return;
    const text = message.trim();
    if (!text) return;
    sendingRef.current = true;
    setItems((prev) => [...prev, { role: "user", content: text }]);
    setMessage("");
    setIsSending(true);
    try {
      const res = await sendChatMessageToConversation(text, activeConversationId);
      setItems((prev) => [...prev, { role: "assistant", content: res.reply }]);
      setConversations((prev) => {
        const updated = prev.map((c) =>
          c.id === activeConversationId ? { ...c, updatedAt: new Date().toISOString() } : c,
        );
        return updated.sort((a, b) => String(b.updatedAt ?? "").localeCompare(String(a.updatedAt ?? "")));
      });
    } catch (e) {
      toast.push({ variant: "error", title: "Chatbot error", message: getErrorMessage(e, "Please try again.") });
    } finally {
      setIsSending(false);
      sendingRef.current = false;
    }
  }

  async function loadConversations() {
    try {
      const list = await listChatConversations();
      if (list && list.length > 0) {
        setConversations(list);
        setActiveConversationId((prev) => prev ?? list[0].id);
        return;
      }
      const created = await createChatConversation("General");
      setConversations([created]);
      setActiveConversationId(created.id);
    } catch {
      // ignore
    }
  }

  async function loadHistory(conversationId: number) {
    setIsLoading(true);
    try {
      const history = await getChatHistory(conversationId, 30);
      if (history?.length) {
        setItems(history.map((h) => ({ role: h.role, content: h.content })));
      } else {
        setItems([]);
      }
    } catch {
      // ignore
    } finally {
      setIsLoading(false);
    }
  }

  async function startNewChat() {
    try {
      const nextTitle = `Chat ${conversations.length + 1}`;
      const created = await createChatConversation(nextTitle);
      setConversations((prev) => [created, ...prev]);
      setActiveConversationId(created.id);
      setItems([]);
    } catch {
      // ignore
    }
  }

  useEffect(() => {
    if (!open) return;
    void loadConversations();
  }, [open]);

  useEffect(() => {
    if (!open || !activeConversationId) return;
    void loadHistory(activeConversationId);
  }, [open, activeConversationId]);

  if (!auth.isAuthenticated) {
    return (
      <button
        type="button"
        onClick={() => toast.push({ variant: "default", title: "Login required", message: "Please login to use the chatbot." })}
        className="fixed bottom-6 right-6 z-40 inline-flex h-14 w-14 items-center justify-center rounded-full border bg-background/80 text-base font-semibold shadow-lg backdrop-blur transition hover:-translate-y-1 hover:shadow-xl"
        aria-label="Open chatbot"
        title="Chatbot"
      >
        AI
      </button>
    );
  }

  return (
    <>
      <button
        type="button"
        onClick={() => {
          setOpen((v) => {
            const next = !v;
            return next;
          });
        }}
        className="fixed bottom-6 right-6 z-40 inline-flex h-14 w-14 items-center justify-center rounded-full border bg-background/80 text-base font-semibold shadow-lg backdrop-blur transition hover:-translate-y-1 hover:shadow-xl"
        aria-label="Open chatbot"
        title="Chatbot"
      >
        AI
      </button>
      {open
          ? createPortal(
            <div className="fixed bottom-24 right-6 z-50 w-[320px] sm:w-[520px]">
              <div className="rounded-2xl border bg-background/90 shadow-xl backdrop-blur">
                <div className="flex items-center justify-between border-b px-4 py-3">
                  <div className="text-sm font-semibold">Chatbot</div>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={startNewChat}
                      className="rounded-lg px-2 py-1 text-xs text-muted-foreground hover:bg-muted hover:text-foreground"
                    >
                      New chat
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setOpen(false);
                      }}
                      className="rounded-lg px-2 py-1 text-muted-foreground hover:bg-muted hover:text-foreground"
                      aria-label="Close"
                    >
                      x
                    </button>
                  </div>
                </div>

                <div className="flex min-h-[360px]">
                  <div className="hidden border-r px-3 py-3 sm:block sm:w-[170px]">
                    <div className="mb-2 text-xs font-semibold text-muted-foreground">History</div>
                    <div className="space-y-2">
                      {conversations.map((c) => (
                        <button
                          key={c.id}
                          type="button"
                          className={[
                            "w-full rounded-lg border px-2 py-1 text-left text-xs transition",
                            activeConversationId === c.id ? "border-primary bg-primary/10" : "bg-background hover:bg-muted",
                          ].join(" ")}
                          onClick={() => setActiveConversationId(c.id)}
                        >
                          <div className="line-clamp-1 font-medium">{c.title}</div>
                          {c.updatedAt ? (
                            <div className="text-[10px] text-muted-foreground">
                              {new Date(c.updatedAt).toLocaleDateString()}
                            </div>
                          ) : null}
                        </button>
                      ))}
                      {conversations.length === 0 ? (
                        <div className="text-xs text-muted-foreground">No chats yet.</div>
                      ) : null}
                    </div>
                  </div>

                  <div className="flex flex-1 flex-col">
                    <div className="border-b px-3 py-2 sm:hidden">
                      <div className="flex gap-2 overflow-x-auto">
                        {conversations.map((c) => (
                          <button
                            key={c.id}
                            type="button"
                            className={[
                              "flex-shrink-0 rounded-full border px-3 py-1 text-xs transition",
                              activeConversationId === c.id ? "border-primary bg-primary/10" : "bg-background hover:bg-muted",
                            ].join(" ")}
                            onClick={() => setActiveConversationId(c.id)}
                          >
                            {c.title}
                          </button>
                        ))}
                        {conversations.length === 0 ? (
                          <span className="text-xs text-muted-foreground">No chats yet.</span>
                        ) : null}
                      </div>
                    </div>
                    <div className="max-h-80 flex-1 overflow-auto px-4 py-3">
                      {isLoading ? (
                        <div className="text-sm text-muted-foreground">Loading...</div>
                      ) : items.length ? (
                        <div className="space-y-2">
                          {items.map((item, idx) => (
                            <div
                              key={`${item.role}-${idx}`}
                              className={[
                                "rounded-2xl px-3 py-2 text-sm whitespace-pre-wrap",
                                item.role === "user"
                                  ? "ml-auto max-w-[80%] bg-primary text-primary-foreground"
                                  : "mr-auto max-w-[80%] bg-muted text-foreground",
                              ].join(" ")}
                            >
                              {item.content}
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="text-sm text-muted-foreground">Ask about orders, refunds, or products.</div>
                      )}
                    </div>
                    <div className="flex items-center gap-2 border-t px-3 py-3">
                      <input
                        className="h-9 flex-1 rounded-xl border bg-background px-3 text-sm"
                        placeholder="Ask something..."
                        value={message}
                        onChange={(e) => setMessage(e.target.value)}
                        onKeyDown={(e) => {
                          if (e.key === "Enter") {
                            e.preventDefault();
                            void onSend();
                          }
                        }}
                        disabled={isSending}
                      />
                      <button
                        type="button"
                        onClick={onSend}
                        disabled={isSending}
                        className="h-9 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 px-3 text-sm text-white hover:opacity-95"
                      >
                        {isSending ? "..." : "Send"}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>,
            document.body,
          )
        : null}
    </>
  );
}
