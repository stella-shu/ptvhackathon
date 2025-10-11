import { useAuthStore } from "../store/useAuthStore";
import { glassPanel, mergeClasses, panelPadding } from "../lib/theme";

export default function AuthStatus() {
  const { token, user, logout } = useAuthStore();
  if (!token || !user) return null;
  return (
    <div
      className={mergeClasses(
        "absolute top-6 right-5 sm:top-10 sm:right-10 z-50 flex items-center gap-3 rounded-full text-sm",
        glassPanel,
        panelPadding
      )}
    >
      <span className="font-semibold text-slate-700">
        👋 {user.name || user.inspectorId}
      </span>
      <button
        className="rounded-full bg-white/80 px-3 py-1 text-xs font-semibold text-rose-500 transition hover:bg-rose-100/80"
        onClick={logout}
      >
        Logout
      </button>
    </div>
  );
}
