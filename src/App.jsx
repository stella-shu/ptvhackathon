import MapDashboard from "./components/MapDashboard";
import QuickActions from "./components/QuickActions";

function App() {
  return (
    <div className="w-screen h-screen relative">
      {/* Main map area */}
      <MapDashboard />

      {/* Floating action buttons */}
      <QuickActions />
    </div>
  );
}

export default App;