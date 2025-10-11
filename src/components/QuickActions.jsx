import { useAppStore } from "../store/useAppStore";
import { createBlitz } from "../lib/api";
import { mergeClasses, pillButton, glassPanel, panelPadding } from "../lib/theme";

export default function QuickActions() {
  const {
    shift, startShift, endShift,
    setShowIncidentForm, setDropPinMode, addPin
  } = useAppStore();

  return (
    <div
      className={mergeClasses(
        "absolute bottom-6 right-5 sm:bottom-10 sm:right-10 inline-flex flex-col sm:flex-row gap-3 sm:gap-4 items-end sm:items-center z-40",
        glassPanel,
        "rounded-[28px]",
        panelPadding
      )}
    >
      {!shift.active ? (
        <button
          className={mergeClasses(
            pillButton,
            "bg-gradient-to-r from-rose-300 via-amber-200 to-lime-200 text-slate-800"
          )}
          onClick={startShift}
        >
          🌼 Start Patrol
        </button>
      ) : (
        <button
          className={mergeClasses(
            pillButton,
            "bg-gradient-to-r from-sky-200 via-indigo-200 to-purple-200 text-slate-800"
          )}
          onClick={endShift}
        >
          🌙 End Patrol
        </button>
      )}

      <button
        className={mergeClasses(
          pillButton,
          "bg-gradient-to-r from-emerald-200 via-teal-200 to-cyan-200 text-slate-800"
        )}
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
        ⚡ Mark Blitz
      </button>

      <button
        className={mergeClasses(
          pillButton,
          "bg-gradient-to-r from-amber-200 via-rose-200 to-pink-200 text-slate-800"
        )}
        onClick={() => setDropPinMode(true)}
      >
        📍 Drop Pin
      </button>

      <button
        className={mergeClasses(
          pillButton,
          "bg-gradient-to-r from-fuchsia-200 via-rose-300 to-amber-200 text-slate-800 disabled:opacity-60"
        )}
        onClick={() => setShowIncidentForm(true)}
        disabled={!shift.active}
        title={!shift.active ? "Start patrol to log incidents" : ""}
      >
        📝 Log Incident
      </button>
    </div>
  );
}
