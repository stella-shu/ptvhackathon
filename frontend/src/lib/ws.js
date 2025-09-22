// src/lib/ws.js
import { useAppStore } from "../store/useAppStore";

let client = null;

function wsUrl() {
  const base = import.meta.env.VITE_API_BASE_URL;
  if (!base) return "/ws"; // rely on same-origin or dev proxy
  return base.replace(/\/$/, "") + "/ws";
}

export async function startRealtime() {
  if (client && client.active) return client;

  try {
    // ⬇️ Lazy-load SockJS & STOMP so they don't run at bundle-eval time
    const [{ default: SockJS }, { Client }] = await Promise.all([
      import("sockjs-client"),
      import("@stomp/stompjs"),
    ]);

    client = new Client({
      webSocketFactory: () => new SockJS(wsUrl()),
      reconnectDelay: 3000,
      debug: () => {}, // silence
    });

    client.onConnect = () => {
      // Blitz created
      client.subscribe("/topic/blitz/created", (msg) => {
        try {
          const b = JSON.parse(msg.body);
          const s = useAppStore.getState();
          const existing = s.remotePins || [];
          const next = [...existing, toPin(b)].filter(Boolean);
          s.setRemotePins(next);
        } catch {}
      });

      // Blitz updated (e.g., closed)
      client.subscribe("/topic/blitz/updated", (msg) => {
        try {
          const b = JSON.parse(msg.body);
          const s = useAppStore.getState();
          const existing = s.remotePins || [];
          let next = existing;

          if (b.active === false) {
            next = existing.filter((p) => p.id !== b.id);
          } else {
            const pin = toPin(b);
            const idx = existing.findIndex((p) => p.id === b.id);
            next = idx >= 0 ? Object.assign([...existing], { [idx]: pin }) : [...existing, pin];
          }
          s.setRemotePins(next);
        } catch {}
      });
    };

    client.activate();
    return client;
  } catch (e) {
    // If SockJS/STOMP (or global/polyfill) fails, don't kill the app
    console.warn("Realtime disabled:", e);
    return null;
  }
}

export function stopRealtime() {
  if (client) {
    const c = client;
    client = null;
    try { c.deactivate(); } catch {}
  }
}

export function getClient() {
  if (!client || !client.active) {
    // fire and forget; caller doesn't need to await
    startRealtime();
  }
  return client;
}

function toPin(b) {
  if (typeof b?.latitude !== "number" || typeof b?.longitude !== "number") return null;
  return { id: b.id, type: "blitz", lat: b.latitude, lng: b.longitude };
}
