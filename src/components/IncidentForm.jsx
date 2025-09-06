import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useAppStore } from "../store/useAppStore";

export default function IncidentForm() {
  const { showIncidentForm, setShowIncidentForm, addIncident } = useAppStore();
  const { register, handleSubmit, reset } = useForm();
  const [coords, setCoords] = useState(null);

  useEffect(() => {
    if (!showIncidentForm) return;
    if ("geolocation" in navigator) {
      navigator.geolocation.getCurrentPosition(
        (pos) => setCoords({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
        () => setCoords(null),
        { enableHighAccuracy: true, timeout: 5000 }
      );
    }
  }, [showIncidentForm]);

  const onSubmit = async (data) => {
  const photoFile = data.photo?.[0] || null;
  const photoPreview = photoFile ? URL.createObjectURL(photoFile) : null;

  const payload = {
    ...data,
    createdAt: new Date().toISOString(),
    location: coords,
    photoPreview,
  };

  addIncident(payload);

  if (!navigator.onLine) {
    // queue for later sync
    useAppStore.getState().enqueue({ type: "incident", payload });
  }

  reset();
  setShowIncidentForm(false);
  setTimeout(() => alert("Incident logged 🎉"), 50);
};

  if (!showIncidentForm) return null;

  return (
    <div className="absolute inset-0 bg-black/40 grid place-items-center">
      <div className="w-[min(520px,92vw)] rounded-2xl bg-white p-5 shadow-xl">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-xl font-semibold">Log Incident</h2>
          <button className="text-slate-500" onClick={() => setShowIncidentForm(false)}>✕</button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <label className="flex flex-col text-sm">
              Mode
              <select className="mt-1 border rounded p-2" {...register("mode")} defaultValue="train">
                <option value="train">Train</option>
                <option value="tram">Tram</option>
                <option value="bus">Bus</option>
              </select>
            </label>
            <label className="flex flex-col text-sm">
              Type/Tag
              <select className="mt-1 border rounded p-2" {...register("tag")} defaultValue="fare_evasion">
                <option value="fare_evasion">Fare evasion</option>
                <option value="aggression">Aggression</option>
                <option value="faulty_gate">Faulty gate</option>
                <option value="other">Other</option>
              </select>
            </label>
          </div>

          <label className="flex flex-col text-sm">
            Notes
            <textarea className="mt-1 border rounded p-2" rows={3} placeholder="Optional details..." {...register("notes")} />
          </label>

          <label className="flex flex-col text-sm">
            Photo (optional)
            <input type="file" accept="image/*" className="mt-1" {...register("photo")} />
          </label>

          <div className="text-xs text-slate-500">
            {coords ? `Location: ${coords.lat.toFixed(5)}, ${coords.lng.toFixed(5)}` : "Location: pending / blocked"}
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button type="button" className="px-4 py-2 rounded border" onClick={() => setShowIncidentForm(false)}>Cancel</button>
            <button type="submit" className="px-4 py-2 rounded bg-blue-600 text-white">Save</button>
          </div>
        </form>
      </div>
    </div>
  );
}
