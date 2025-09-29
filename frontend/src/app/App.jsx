import { useEffect } from "react";
import MapDashboardPage from "@/pages/map-dashboard";
import { useAppStore } from "@/stores/appStore";
import { startRealtime, stopRealtime } from "@/services/realtime/client";

export default function App() {
  const flushQueue = useAppStore((state) => state.flushQueue);

  useEffect(() => {
    const onOnline = () => flushQueue();
    window.addEventListener("online", onOnline);
    flushQueue();
    startRealtime();
    return () => {
      window.removeEventListener("online", onOnline);
      try {
        stopRealtime();
      } catch (_error) {
        // noop: cleanup failures are non-fatal
      }
    };
  }, [flushQueue]);

  return <MapDashboardPage />;
}
