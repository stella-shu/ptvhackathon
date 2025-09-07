import { useAppStore } from "../store/useAppStore";
import { createBlitz } from "../lib/api";

export default function QuickActions() {
  const {
    shift, startShift, endShift,
    setShowIncidentForm, setDropPinMode, addPin
  } = useAppStore();

  return (
    <div className="absolute bottom-6 right-6 flex gap-3">
      {!shift.active ? (
        <button className="bg-blue-600 text-white px-4 py-2 rounded-full shadow" onClick={startShift}>
          Start Patrol
        </button>
      ) : (
        <button className="bg-blue-700 text-white px-4 py-2 rounded-full shadow" onClick={endShift}>
          End Patrol
        </button>
      )}

      <button
        className="bg-green-600 text-white px-4 py-2 rounded-full shadow"
        onClick={async () => {
          const lat = -37.8136, lng = 144.9631;
          addPin({ type: "blitz", lat, lng });
          try {
            await createBlitz({ latitude: lat, longitude: lng, description: "Quick blitz" });
          } catch {
            // offline or server error: queue for later sync
            useAppStore.getState().enqueue({ type: "blitzCreate", payload: { lat, lng, description: "Quick blitz" } });
          }
        }}
      >
        Mark Blitz
      </button>

      <button
        className="bg-slate-700 text-white px-4 py-2 rounded-full shadow"
        onClick={() => setDropPinMode(true)}
      >
        Drop Pin
      </button>

      <button
        className="bg-amber-600 text-white px-4 py-2 rounded-full shadow"
        onClick={() => setShowIncidentForm(true)}
        disabled={!shift.active}
        title={!shift.active ? "Start patrol to log incidents" : ""}
      >
        Log Incident
      </button>
    </div>
  );
}
