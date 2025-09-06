import { useMemo } from "react";
import { GoogleMap, useJsApiLoader, Marker } from "@react-google-maps/api";

const containerStyle = { width: "100%", height: "100%" };
const melbourne = { lat: -37.8136, lng: 144.9631 };

function MapDashboard() {
  const { isLoaded } = useJsApiLoader({
    id: "gmap-script",
    googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || "", // add to .env
  });

  const center = useMemo(() => melbourne, []);
  if (!isLoaded) {
    return (
      <div className="w-full h-full bg-gray-200 grid place-items-center">
        <p className="text-lg">[Loading Google Map...]</p>
      </div>
    );
  }

  return (
    <GoogleMap mapContainerStyle={containerStyle} center={center} zoom={12}>
      <Marker position={center} />
    </GoogleMap>
  );
}
export default MapDashboard;
