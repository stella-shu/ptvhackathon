import { useState } from "react";
import { useAuthStore } from "../store/useAuthStore";
import { useAppStore } from "../store/useAppStore";
import { glassPanel, mergeClasses, panelPadding, softInput, pillButton } from "../lib/theme";

export default function LoginModal() {
  const { token, loading, error, login } = useAuthStore();
  const flushQueue = useAppStore((s) => s.flushQueue);
  const [form, setForm] = useState({ inspectorId: "", password: "", otp: "" });
  const [done, setDone] = useState(false);

  if (token) return null;

  const onChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  const onSubmit = async (e) => {
    e.preventDefault();
    try {
      await login({
        inspectorId: form.inspectorId.trim(),
        password: form.password,
        otp: form.otp,
      });
      setDone(true);
      // try flush queued items after successful login
      try { await flushQueue(); } catch {}
    } catch {}
  };

  return (
    <div className="absolute inset-0 z-50 grid place-items-center bg-slate-900/45 backdrop-blur-sm px-4">
      <div className={mergeClasses("w-[min(420px,94vw)] rounded-[32px]", glassPanel, panelPadding)}>
        <h2 className="text-2xl font-bold text-slate-800 mb-2">Welcome back, inspector!</h2>
        <p className="text-sm text-slate-500 mb-5">Log in to sync your patrol adventures and collaborate with your team.</p>
        <form className="space-y-4" onSubmit={onSubmit}>
          <label className="flex flex-col text-sm font-medium text-slate-600">
            Inspector ID
            <input
              className={softInput}
              name="inspectorId"
              value={form.inspectorId}
              onChange={onChange}
              autoFocus
              required
            />
          </label>
          <label className="flex flex-col text-sm font-medium text-slate-600">
            Password
            <input
              className={softInput}
              type="password"
              name="password"
              value={form.password}
              onChange={onChange}
              required
            />
          </label>
          <label className="flex flex-col text-sm font-medium text-slate-600">
            One-time code (OTP)
            <input
              className={softInput}
              type="number"
              name="otp"
              value={form.otp}
              onChange={onChange}
              required
            />
          </label>
          {error && (
            <div className="rounded-2xl bg-rose-100/80 px-3 py-2 text-xs text-rose-600 shadow-inner shadow-rose-200/60">
              {error}
            </div>
          )}
          <div className="flex justify-end gap-3 pt-2">
            <button
              type="submit"
              className={mergeClasses(
                pillButton,
                "bg-gradient-to-r from-rose-300 via-amber-200 to-sky-200 text-slate-800 disabled:opacity-60"
              )}
              disabled={loading}
              title={done ? "Signed in" : undefined}
            >
              {loading ? "Signing in…" : done ? "Signed in ✨" : "Sign in"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
