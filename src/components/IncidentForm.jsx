import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useAppStore } from "../store/useAppStore";
import { glassPanel, panelPadding, softInput, mergeClasses, pillButton } from "../lib/theme";

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
    // keep payload small for queue; skip embedding blob URL
    const payload = {
      ...data,
      createdAt: new Date().toISOString(),
      location: coords,
      hasPhoto: Boolean(photoFile),
    };

    addIncident(payload);

    // always enqueue; will send immediately if online and authed via flush
    useAppStore.getState().enqueue({ type: "incident", payload });

    // Clean up any temporary object URLs if created
    try {
      // no preview URL created; nothing to revoke
    } catch {}

    reset();
    setShowIncidentForm(false);
    setTimeout(() => alert("Incident logged 🎉"), 50);
  };

  if (!showIncidentForm) return null;

  return (
    <div className="absolute inset-0 z-50 grid place-items-center bg-gradient-to-br from-slate-900/40 via-slate-900/30 to-transparent backdrop-blur-sm px-4">
      <div className={mergeClasses("w-[min(560px,96vw)] rounded-[32px]", glassPanel, panelPadding)}>
        <div className="flex items-start justify-between mb-4">
          <div>
            <h2 className="text-2xl font-bold text-slate-800">Log an Incident</h2>
            <p className="text-sm text-slate-500 mt-1">Capture quick notes to keep your patrol story adorable and accurate.</p>
          </div>
          <button
            className="rounded-full bg-white/70 px-3 py-1 text-slate-400 hover:text-slate-600 hover:bg-white transition"
            onClick={() => setShowIncidentForm(false)}
            type="button"
            aria-label="Close"
          >
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <label className="flex flex-col text-sm font-medium text-slate-600">
              Mode
              <select className={softInput} {...register("mode")} defaultValue="train">
                <option value="train">Train</option>
                <option value="tram">Tram</option>
                <option value="bus">Bus</option>
              </select>
            </label>
            <label className="flex flex-col text-sm font-medium text-slate-600">
              Type/Tag
              <select className={softInput} {...register("tag")} defaultValue="fare_evasion">
                <option value="fare_evasion">Fare evasion</option>
                <option value="aggression">Aggression</option>
                <option value="faulty_gate">Faulty gate</option>
                <option value="other">Other</option>
              </select>
            </label>
          </div>

          <label className="flex flex-col text-sm font-medium text-slate-600">
            Notes
            <textarea
              className={mergeClasses(softInput, "min-h-[110px] resize-y")}
              placeholder="Optional details..."
              {...register("notes")}
            />
          </label>

          <label className="flex flex-col text-sm font-medium text-slate-600">
            Photo (optional)
            <input type="file" accept="image/*" className={mergeClasses(softInput, "file:mr-3 file:rounded-full file:border-0 file:bg-rose-200/80 file:px-3 file:py-1 file:text-xs file:font-semibold file:text-slate-700")} {...register("photo")} />
          </label>

          <div className="text-xs text-slate-500 rounded-2xl bg-white/80 px-3 py-2 shadow-inner shadow-rose-100/60">
            {coords ? `Location locked at ${coords.lat.toFixed(5)}, ${coords.lng.toFixed(5)}` : "Location pending… we’ll keep trying politely."}
          </div>

          <div className="flex justify-end gap-3 pt-3">
            <button
              type="button"
              className={mergeClasses(
                pillButton,
                "bg-white text-slate-500 shadow-none hover:bg-slate-100"
              )}
              onClick={() => setShowIncidentForm(false)}
            >
              Cancel
            </button>
            <button
              type="submit"
              className={mergeClasses(
                pillButton,
                "bg-gradient-to-r from-rose-300 via-fuchsia-200 to-sky-200 text-slate-800"
              )}
            >
              Save Incident
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
