function QuickActions() {
  return (
    <div className="absolute bottom-6 right-6 space-y-2">
      <button className="bg-blue-600 text-white px-4 py-2 rounded-full shadow">
        Start Patrol
      </button>
      <button className="bg-green-600 text-white px-4 py-2 rounded-full shadow">
        Mark Blitz
      </button>
      <button className="bg-yellow-600 text-white px-4 py-2 rounded-full shadow">
        Log Incident
      </button>
    </div>
  );
}
export default QuickActions;
