import { create } from "zustand";
import {
  createIncident,
  createShift,
  updateShift,
  createBlitz,
  mapIncidentDto,
  mapShiftStartDto,
  mapShiftEndDto,
} from "../lib/api";

export const useAppStore = create((set, get) => ({
  showIncidentForm: false,
  showSummary: false,
  setShowIncidentForm: (v) => set({ showIncidentForm: v }),
  setShowSummary: (v) => set({ showSummary: v }),
  // UI toggles
  showHeatmap: true,
  showActive: true,
  showChat: false,
  setShowHeatmap: (v) => set({ showHeatmap: v }),
  setShowActive: (v) => set({ showActive: v }),
  setShowChat: (v) => set({ showChat: v }),

  // Shift
  shift: { active: false, startedAt: null, endedAt: null, localId: null },
  startShift: () => {
    const startedAt = Date.now();
    const localId = `sh_${startedAt}_${Math.random().toString(36).slice(2)}`;
    set({ shift: { active: true, startedAt, endedAt: null, localId } });
    // enqueue for backend sync
    get().enqueue({ type: "shiftStart", payload: { startedAt, localId } });
  },
  endShift: () => {
    const endedAt = Date.now();
    const s = get().shift;
    set({
      shift: { ...s, active: false, endedAt },
      showSummary: true,
    });
    // enqueue for backend sync, include basic stats
    get().enqueue({ type: "shiftEnd", payload: { startedAt: s.startedAt, endedAt, localId: s.localId } });
  },

  // Pins
  pins: [], // local pins {lat,lng,type:'pin'|'blitz'}
  remotePins: [], // server-sourced markers (e.g., blitz)
  setRemotePins: (pins) => set({ remotePins: pins }),
  addPin: (p) => set({ pins: [...get().pins, p] }),

  // UI mode for adding pin
  dropPinMode: false,
  setDropPinMode: (v) => set({ dropPinMode: v }),

  // Incidents
  incidents: [],
  addIncident: (inc) => set({ incidents: [...get().incidents, inc] }),

  // Offline queue (simple)
  enqueue: (item) => {
    try {
      const key = "offlineQueue";
      const q = JSON.parse(localStorage.getItem(key) || "[]");
      q.push(item);
      localStorage.setItem(key, JSON.stringify(q));
    } catch (e) {
      // ignore quota/JSON errors in this simple client
    }
  },
  flushQueue: async () => {
    const key = "offlineQueue";
    const q = JSON.parse(localStorage.getItem(key) || "[]");
    if (!q.length) return { sent: 0, failed: 0 };
    const remaining = [];
    let sent = 0;
    const idMapKey = "shiftIdMap";
    const idMap = JSON.parse(localStorage.getItem(idMapKey) || "{}");

    for (const item of q) {
      try {
        if (item.type === "incident") {
          await createIncident(mapIncidentDto(item.payload));
          sent += 1;
          continue;
        }
        if (item.type === "blitzCreate") {
          const { lat, lng, description, blitzType, scheduledEnd } = item.payload || {};
          await createBlitz({ latitude: lat, longitude: lng, description, blitzType, scheduledEnd });
          sent += 1;
          continue;
        }
        if (item.type === "shiftStart") {
          const res = await createShift(mapShiftStartDto(item.payload));
          const serverId = res?.id;
          const localId = item.payload?.localId;
          if (serverId && localId) {
            idMap[localId] = serverId;
            localStorage.setItem(idMapKey, JSON.stringify(idMap));
          }
          sent += 1;
          continue;
        }
        if (item.type === "shiftEnd") {
          const localId = item.payload?.localId;
          const serverId = localId ? idMap[localId] : null;
          if (!serverId) {
            // cannot update yet, keep for later
            remaining.push(item);
            continue;
          }
          await updateShift(serverId, mapShiftEndDto(item.payload));
          sent += 1;
          continue;
        }
        // unknown item type
        remaining.push(item);
      } catch (err) {
        remaining.push(item);
      }
    }
    localStorage.setItem(key, JSON.stringify(remaining));
    return { sent, failed: remaining.length };
  },
}));
