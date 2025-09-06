import { useAppStore } from "../store/useAppStore";

export default function QuickActions() {
  const { shift, startShift, endShift, setShowIncidentForm } = useAppStore();

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
      <button className="bg-green-600 text-white px-4 py-2 rounded-full shadow" onClick={() => alert("Blitz marked ✅")}>
        Mark Blitz
      </button>
      <button className="bg-yellow-600 text-white px-4 py-2 rounded-full shadow" onClick={() => setShowIncidentForm(true)}>
        Log Incident
      </button>
      <button className="bg-slate-700 text-white px-4 py-2 rounded-full shadow" onClick={() => alert("Open photo picker")}>
        Upload Photo
      </button>
    </div>
  );
}