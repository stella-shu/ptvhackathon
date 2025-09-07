import { useEffect, useRef, useState } from "react";
import { useAppStore } from "../store/useAppStore";
import { useAuthStore } from "../store/useAuthStore";
import { getClient, startRealtime } from "../lib/ws";

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
    <div className="absolute bottom-4 left-4 z-50 w-[min(420px,92vw)] bg-white/95 backdrop-blur border rounded-xl shadow-xl flex flex-col max-h-[70vh]">
      <div className="px-3 py-2 border-b flex items-center gap-2">
        <span className="font-medium">Chat</span>
        <input className="ml-auto border rounded px-2 py-1 text-sm w-32" value={channel} onChange={(e) => setChannel(e.target.value)} />
        <button className="text-slate-600" onClick={() => setShowChat(false)}>✕</button>
      </div>
      <div ref={listRef} className="px-3 py-2 overflow-auto flex-1 space-y-1">
        {messages.map((m, i) => (
          <div key={m.id || i} className="text-sm">
            <span className="text-slate-500">[{formatTime(m.createdAt)}]</span>{" "}
            <span className="font-medium">{m.senderName || m.senderId}</span>: {m.content}
          </div>
        ))}
      </div>
      <div className="p-2 border-t flex gap-2">
        <textarea
          className="flex-1 border rounded px-2 py-1 text-sm resize-none h-[40px]"
          placeholder="Message..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={onKey}
        />
        <button className="px-3 py-1 bg-blue-600 text-white rounded" onClick={send}>Send</button>
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

