import { create } from "zustand";

const base = import.meta.env.VITE_API_BASE_URL;

function loadPersisted() {
  try {
    const raw = localStorage.getItem("auth");
    if (!raw) return { token: null, user: null };
    const data = JSON.parse(raw);
    return { token: data.token || null, user: data.user || null };
  } catch {
    return { token: null, user: null };
  }
}

function persist(token, user) {
  try {
    localStorage.setItem("auth", JSON.stringify({ token, user }));
    if (token) localStorage.setItem("authToken", token);
    else localStorage.removeItem("authToken");
  } catch {}
}

export const useAuthStore = create((set, get) => ({
  ...loadPersisted(),
  loading: false,
  error: null,

  login: async ({ inspectorId, password, otp }) => {
    if (!base) throw new Error("VITE_API_BASE_URL not set");
    set({ loading: true, error: null });
    try {
      const res = await fetch(base.replace(/\/$/, "") + "/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ inspectorId, password, otp: Number(otp) }),
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
    persist(null, null);
    set({ token: null, user: null });
  },
}));

export function getAuthToken() {
  try {
    const raw = localStorage.getItem("auth");
    if (!raw) return null;
    const data = JSON.parse(raw);
    return data.token || null;
  } catch {
    return null;
  }
}

