import { useEffect } from "react";
import MapDashboard from "./components/MapDashboard";
import QuickActions from "./components/QuickActions";
import IncidentForm from "./components/IncidentForm";
import ShiftSummary from "./components/ShiftSummary";
import { useAppStore } from "./store/useAppStore";
import LoginModal from "./components/LoginModal";
import AuthStatus from "./components/AuthStatus";
import { startRealtime, stopRealtime } from "./lib/ws";
import MapControls from "./components/MapControls";
import ChatPanel from "./components/ChatPanel";

function App() {
  const flushQueue = useAppStore((s) => s.flushQueue);

  useEffect(() => {
    const onOnline = () => flushQueue();
    window.addEventListener("online", onOnline);
    // best-effort flush once on mount
    flushQueue();
    // start realtime subscriptions (blitz)
    startRealtime();
    return () => {
      window.removeEventListener("online", onOnline);
      try { stopRealtime(); } catch {}
    };
  }, [flushQueue]);

  return (
    <div className="w-screen h-screen relative">
      <LoginModal />
      <AuthStatus />
      <MapControls />
      <MapDashboard />
      <QuickActions />
      <IncidentForm />
      <ShiftSummary />
      <ChatPanel />
    </div>
  );
}
export default App;
