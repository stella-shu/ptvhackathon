const trim = (value) => (typeof value === "string" ? value.trim().replace(/\/$/, "") : "");

const API_BASE = trim(import.meta.env.VITE_API_BASE_URL);
const AUTH_BASE = trim(import.meta.env.VITE_AUTH_BASE_URL) || API_BASE;
const REALTIME_BASE = trim(import.meta.env.VITE_REALTIME_BASE_URL) || API_BASE;

const prefixPath = (path) => (path.startsWith("/") ? path : `/${path}`);

const buildUrl = (base, path) => {
  if (!path) return base || "";
  const normalized = prefixPath(path);
  return base ? `${base}${normalized}` : normalized;
};

export const urlBuilder = {
  api: (path) => buildUrl(API_BASE, path),
  auth: (path) => buildUrl(AUTH_BASE, path),
  realtime: (path) => buildUrl(REALTIME_BASE, path),
};

export const serviceOrigins = {
  api: API_BASE,
  auth: AUTH_BASE,
  realtime: REALTIME_BASE,
};
