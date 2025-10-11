import { useEffect, useRef, useState } from "react";
import { useAppStore } from "../store/useAppStore";
import { useAuthStore } from "../store/useAuthStore";
import { getClient, startRealtime } from "../lib/ws";
import { glassPanel, mergeClasses, softInput, pillButton } from "../lib/theme";

export default function ChatPanel() {
  const { showChat, setShowChat } = useAppStore();
  const { user } = useAuthStore();
  const [channel, setChannel] = useState("general");
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const subRef = useRef(null);
  const listRef = useRef(null);

  useEffect(() => {
    if (!showChat) return;
    startRealtime();
  }, [showChat]);

  useEffect(() => {
    if (!showChat) return;
    let aborted = false;
    // load history
    (async () => {
      try {
        const res = await fetch(`/api/channels/${encodeURIComponent(channel)}/messages`);
        if (!res.ok) return;
        const items = await res.json();
        if (!aborted) setMessages(items || []);
        scrollToBottom();
      } catch {}
    })();
    // subscribe
    const c = getClient();
    if (c && c.connected) {
      resubscribe();
    } else {
      const interval = setInterval(() => {
        const ci = getClient();
        if (ci && ci.connected) {
          clearInterval(interval);
          resubscribe();
        }
      }, 500);
      return () => clearInterval(interval);
    }
    return () => {
      aborted = true;
      if (subRef.current) {
        try { subRef.current.unsubscribe(); } catch {}
        subRef.current = null;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [channel, showChat]);

  const resubscribe = () => {
    if (subRef.current) {
      try { subRef.current.unsubscribe(); } catch {}
      subRef.current = null;
    }
    const c = getClient();
    if (!c || !c.connected) return;
    // join notification (optional)
    try {
      c.publish({
        destination: `/app/channels/${channel}/join`,
        body: JSON.stringify({ senderId: user?.inspectorId, senderName: user?.name || user?.inspectorId || "Guest" }),
      });
    } catch {}
    subRef.current = c.subscribe(`/topic/channels/${channel}`, (msg) => {
      try {
        const data = JSON.parse(msg.body);
        setMessages((prev) => [...prev, data]);
        scrollToBottom();
      } catch {}
    });
  };

  const send = () => {
    const content = input.trim();
    if (!content) return;
    setInput("");
    const c = getClient();
    const payload = {
      channelId: channel,
      senderId: user?.inspectorId || "guest",
      senderName: user?.name || user?.inspectorId || "Guest",
      content,
      type: "CHAT",
    };
    if (c && c.connected) {
      c.publish({ destination: `/app/channels/${channel}/send`, body: JSON.stringify(payload) });
    } else {
      // fallback REST
      fetch(`/api/channels/${encodeURIComponent(channel)}/messages`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      }).catch(() => {});
    }
  };

  const onKey = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  };

  const scrollToBottom = () => {
    requestAnimationFrame(() => {
      try {
        const el = listRef.current;
        if (el) el.scrollTop = el.scrollHeight;
      } catch {}
    });
  };

  if (!showChat) return null;

  return (
    <div
      className={mergeClasses(
        "absolute bottom-6 left-5 sm:bottom-10 sm:left-10 z-50 flex max-h-[70vh] w-[min(420px,92vw)] flex-col",
        glassPanel,
        "rounded-[28px] overflow-hidden"
      )}
    >
      <div className="flex items-center gap-2 border-b border-white/60 bg-white/60 px-4 py-3 text-sm font-semibold text-slate-600">
        <span>Team Chat</span>
        <input
          className={mergeClasses(softInput, "ml-auto w-28")}
          value={channel}
          onChange={(e) => setChannel(e.target.value)}
        />
        <button
          className="rounded-full bg-white/80 px-2 py-1 text-xs text-slate-400 transition hover:text-slate-600"
          onClick={() => setShowChat(false)}
        >
          ✕
        </button>
      </div>
      <div ref={listRef} className="flex-1 space-y-2 overflow-auto px-4 py-3 text-sm">
        {messages.length === 0 && (
          <div className="rounded-3xl bg-white/70 px-4 py-3 text-center text-xs font-medium text-slate-400 shadow-inner shadow-rose-100/40">
            No messages yet — say hi! ✨
          </div>
        )}
        {messages.map((m, i) => (
          <div
            key={m.id || i}
            className="rounded-3xl bg-white/70 px-3 py-2 shadow-inner shadow-rose-100/40"
          >
            <span className="text-[11px] uppercase tracking-wide text-slate-400">
              {formatTime(m.createdAt)}
            </span>
            <div className="font-semibold text-slate-700">{m.senderName || m.senderId}</div>
            <p className="text-slate-600 whitespace-pre-wrap">{m.content}</p>
          </div>
        ))}
      </div>
      <div className="flex items-end gap-2 border-t border-white/60 bg-white/55 px-4 py-3">
        <textarea
          className={mergeClasses(softInput, "flex-1 h-[60px] resize-none")}
          placeholder="Send a little hello…"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={onKey}
        />
        <button
          className={mergeClasses(
            pillButton,
            "bg-gradient-to-r from-sky-200 via-indigo-200 to-rose-200 text-slate-800"
          )}
          onClick={send}
        >
          Send
        </button>
      </div>
    </div>
  );
}

function formatTime(iso) {
  if (!iso) return "";
  try {
    const d = new Date(iso);
    return d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  } catch {
    return "";
  }
}
