import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import { useAppStore } from "../store/useAppStore";

let client = null;

function wsUrl() {
  const base = import.meta.env.VITE_API_BASE_URL;
  if (!base) return "/ws"; // rely on dev proxy / same origin
  return base.replace(/\/$/, "") + "/ws";
}

export function startRealtime() {
  if (client && client.active) return client;
  client = new Client({
    webSocketFactory: () => new SockJS(wsUrl()),
    reconnectDelay: 3000,
    debug: () => {},
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
          if (idx >= 0) {
            next = [...existing];
            next[idx] = pin;
          } else {
            next = [...existing, pin];
          }
        }
        s.setRemotePins(next);
      } catch {}
    });
  };
  client.activate();
  return client;
}

export function stopRealtime() {
  if (client) {
    const c = client;
    client = null;
    try { c.deactivate(); } catch {}
  }
}

export function getClient() {
  if (!client || !client.active) startRealtime();
  return client;
}

function toPin(b) {
  if (typeof b?.latitude !== "number" || typeof b?.longitude !== "number") return null;
  return { id: b.id, type: "blitz", lat: b.latitude, lng: b.longitude };
}
