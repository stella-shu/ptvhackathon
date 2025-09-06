import MapDashboard from "./components/MapDashboard";
import QuickActions from "./components/QuickActions";
import IncidentForm from "./components/IncidentForm";
import ShiftSummary from "./components/ShiftSummary";

function App() {
  return (
    <div className="w-screen h-screen relative">
      <MapDashboard />
      <QuickActions />
      <IncidentForm />
      <ShiftSummary />
    </div>
  );
}
export default App;
