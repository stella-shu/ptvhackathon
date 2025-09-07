import { create } from "zustand";

const base = import.meta.env.VITE_API_BASE_URL;
const NEW_KEY = "inspector_auth";
const OLD_KEY = "auth";

function buildUrl(path) {
  const p = path.startsWith("/") ? path : `/${path}`;
  if (!base) return p; // rely on Vite dev proxy when base not set
  return base.replace(/\/$/, "") + p;
}

function loadPersisted() {
  try {
    let raw = localStorage.getItem(NEW_KEY);
    if (!raw) {
      // migrate from old generic key if present
      const legacy = localStorage.getItem(OLD_KEY);
      if (legacy) {
        try {
          const obj = JSON.parse(legacy);
          localStorage.setItem(NEW_KEY, JSON.stringify(obj));
          localStorage.removeItem(OLD_KEY);
          raw = JSON.stringify(obj);
        } catch {}
      }
    }
    if (!raw) return { token: null, user: null };
    const data = JSON.parse(raw);
    return { token: data.token || null, user: data.user || null };
  } catch {
    return { token: null, user: null };
  }
}

function persist(token, user) {
  try {
    localStorage.setItem(NEW_KEY, JSON.stringify({ token, user }));
    // cleanup any legacy keys
    try { localStorage.removeItem(OLD_KEY); } catch {}
  } catch {}
}

export const useAuthStore = create((set, get) => ({
  ...loadPersisted(),
  loading: false,
  error: null,

  login: async ({ inspectorId, password }) => {
    set({ loading: true, error: null });
    try {
      const res = await fetch(buildUrl("/api/auth/login"), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ inspectorId, password }),
      });
      if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`HTTP ${res.status} ${res.statusText} ${text}`.trim());
      }
      const data = await res.json();
      const token = data.token;
      const user = {
        inspectorId: data.inspectorId,
        name: data.name,
        email: data.email,
      };
      persist(token, user);
      set({ token, user, loading: false, error: null });
      return user;
    } catch (e) {
      set({ loading: false, error: e.message || String(e) });
      throw e;
    }
  },

  logout: () => {
    try { localStorage.removeItem(NEW_KEY); } catch {}
    try { localStorage.removeItem(OLD_KEY); } catch {}
    set({ token: null, user: null, error: null });
  },
}));

export function getAuthToken() {
  try {
    let raw = localStorage.getItem(NEW_KEY) || localStorage.getItem(OLD_KEY);
    if (!raw) return null;
    const data = JSON.parse(raw);
    return data.token || null;
  } catch {
    return null;
  }
}
