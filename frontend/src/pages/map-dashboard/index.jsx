import { AuthStatus, LoginModal } from "@/features/auth";
import { ChatPanel } from "@/features/chat";
import { IncidentForm } from "@/features/incidents";
import { MapCanvas, MapControls, QuickActions } from "@/features/map";
import { ShiftSummary } from "@/features/shifts";

export default function MapDashboardPage() {
  return (
    <div className="relative h-screen w-screen overflow-hidden bg-slate-100">
      <div className="absolute inset-0">
        <MapCanvas />
      </div>

      <LoginModal />
      <AuthStatus />
      <MapControls />
      <QuickActions />
      <IncidentForm />
      <ShiftSummary />
      <ChatPanel />
    </div>
  );
}
