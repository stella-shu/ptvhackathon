import { useAuthStore } from "../store/useAuthStore";

export default function AuthStatus() {
  const { token, user, logout } = useAuthStore();
  if (!token || !user) return null;
  return (
    <div className="absolute top-4 right-4 bg-white/90 border rounded-full px-3 py-1 shadow text-sm flex items-center gap-2 z-[1200]">
      <span className="text-slate-700">{user.name || user.inspectorId}</span>
      <button className="text-blue-600 hover:underline" onClick={logout}>Logout</button>
    </div>
  );
}
