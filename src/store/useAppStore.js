import { create } from "zustand";

export const useAppStore = create((set, get) => ({
  // UI
  showIncidentForm: false,
  showSummary: false,
  setShowIncidentForm: (v) => set({ showIncidentForm: v }),
  setShowSummary: (v) => set({ showSummary: v }),

  // Shift
  shift: { active: false, startedAt: null, endedAt: null },
  startShift: () => set({ shift: { active: true, startedAt: Date.now(), endedAt: null } }),
  endShift: () => set({ shift: { ...get().shift, active: false, endedAt: Date.now() }, showSummary: true }),

  // Incidents
  incidents: [],
  addIncident: (inc) => set({ incidents: [...get().incidents, inc] }),
}));
