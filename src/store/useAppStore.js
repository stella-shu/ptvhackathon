import { create } from "zustand";

export const useAppStore = create((set, get) => ({
  showIncidentForm: false,
  showSummary: false,
  setShowIncidentForm: (v) => set({ showIncidentForm: v }),
  setShowSummary: (v) => set({ showSummary: v }),

  // Shift
  shift: { active: false, startedAt: null, endedAt: null },
  startShift: () => set({ shift: { active: true, startedAt: Date.now(), endedAt: null } }),
  endShift: () =>
    set({
      shift: { ...get().shift, active: false, endedAt: Date.now() },
      showSummary: true,
    }),

  // Pins & blitz
  pins: [], // {lat,lng,type:'pin'|'blitz'}
  addPin: (p) => set({ pins: [...get().pins, p] }),

  // UI mode for adding pin
  dropPinMode: false,
  setDropPinMode: (v) => set({ dropPinMode: v }),

  // Incidents
  incidents: [],
  addIncident: (inc) => set({ incidents: [...get().incidents, inc] }),

  // Offline queue (simple)
  enqueue: (item) => {
    const key = "offlineQueue";
    const q = JSON.parse(localStorage.getItem(key) || "[]");
    q.push(item);
    localStorage.setItem(key, JSON.stringify(q));
  },
  flushQueue: () => {
    const key = "offlineQueue";
    const q = JSON.parse(localStorage.getItem(key) || "[]");
    if (q.length) {
      // send to backend later; for now just clear
      localStorage.setItem(key, "[]");
    }
  },
}));
