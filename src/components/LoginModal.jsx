import { useState } from "react";
import { useAuthStore } from "../store/useAuthStore";
import { useAppStore } from "../store/useAppStore";

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
    <div className="absolute inset-0 bg-black/50 grid place-items-center z-50">
      <div className="w-[min(420px,92vw)] rounded-2xl bg-white p-5 shadow-xl">
        <h2 className="text-lg font-semibold mb-3">Sign in</h2>
        <form className="space-y-3" onSubmit={onSubmit}>
          <label className="flex flex-col text-sm">
            Inspector ID
            <input
              className="mt-1 border rounded p-2"
              name="inspectorId"
              value={form.inspectorId}
              onChange={onChange}
              autoFocus
              required
            />
          </label>
          <label className="flex flex-col text-sm">
            Password
            <input
              className="mt-1 border rounded p-2"
              type="password"
              name="password"
              value={form.password}
              onChange={onChange}
              required
            />
          </label>
          <label className="flex flex-col text-sm">
            One-time code (OTP)
            <input
              className="mt-1 border rounded p-2"
              type="number"
              name="otp"
              value={form.otp}
              onChange={onChange}
              required
            />
          </label>
          {error && <div className="text-xs text-red-600">{error}</div>}
          <div className="flex justify-end gap-2 pt-2">
            <button
              type="submit"
              className="px-4 py-2 rounded bg-blue-600 text-white disabled:opacity-60"
              disabled={loading}
              title={done ? "Signed in" : undefined}
            >
              {loading ? "Signing in..." : done ? "Signed in" : "Sign in"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

