import { urlBuilder } from "@/config/env";
// API client. Uses absolute base URL if provided; otherwise, relies on Vite dev proxy.
const NEW_KEY = "inspector_auth";
const OLD_KEY = "auth";

function getHeaders() {
  const headers = { "Content-Type": "application/json" };
  try {
    const raw = localStorage.getItem(NEW_KEY) || localStorage.getItem(OLD_KEY);
    if (raw) {
      const { token } = JSON.parse(raw);
      if (token) headers["Authorization"] = `Bearer ${token}`;
    }
  } catch (_error) {
    // noop: fall back to unauthenticated requests
  }
  return headers;
}

const buildAuthUrl = (path) => urlBuilder.auth(path);
const buildRealtimeUrl = (path) => urlBuilder.realtime(path);

// DTO mappers
export function mapIncidentDto(payload) {
  const lat = payload?.location?.lat ?? null;
  const lng = payload?.location?.lng ?? null;
  return {
    title: payload?.tag || "Incident",
    description: payload?.notes || "",
    severity: "MEDIUM",
    status: "OPEN",
    latitude: typeof lat === "number" ? lat : null,
    longitude: typeof lng === "number" ? lng : null,
    occurredAt: payload?.createdAt || new Date().toISOString(),
  };
}

export function mapShiftStartDto(payload) {
  return {
    startTime: new Date(payload?.startedAt || Date.now()).toISOString(),
    endTime: null,
    status: "OPEN",
    location: "",
    notes: "",
  };
}

export function mapShiftEndDto(payload) {
  return {
    startTime: new Date(payload?.startedAt || Date.now()).toISOString(),
    endTime: new Date(payload?.endedAt || Date.now()).toISOString(),
    status: "CLOSED",
    location: "",
    notes: "",
  };
}

// REST helpers
export async function createIncident(dto) {
  const res = await fetch(buildAuthUrl("/api/incidents"), {
    method: "POST",
    headers: getHeaders(),
    body: JSON.stringify(dto),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json().catch(() => null);
}

export async function createShift(dto) {
  const res = await fetch(buildAuthUrl("/api/shifts"), {
    method: "POST",
    headers: getHeaders(),
    body: JSON.stringify(dto),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json().catch(() => null);
}

export async function updateShift(id, dto) {
  const res = await fetch(buildAuthUrl(`/api/shifts/${id}`), {
    method: "PUT",
    headers: getHeaders(),
    body: JSON.stringify(dto),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json().catch(() => null);
}

export async function createBlitz({ latitude, longitude, description = "", blitzType = "GENERIC", scheduledEnd = null }) {
  // try read inspectorId from auth store persistence
  let inspectorId = undefined;
  try {
    const raw = localStorage.getItem(NEW_KEY) || localStorage.getItem(OLD_KEY);
    if (raw) inspectorId = JSON.parse(raw)?.user?.inspectorId;
  } catch (_error) {
    // noop: continue without inspector context
  }
  const body = {
    inspectorId,
    latitude,
    longitude,
    description,
    blitzType,
    scheduledEnd,
  };
  const res = await fetch(buildRealtimeUrl("/api/blitz"), {
    method: "POST",
    headers: getHeaders(),
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json().catch(() => null);
}
