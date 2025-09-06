import { useMemo, useCallback } from "react";
import { GoogleMap, useJsApiLoader, Marker } from "@react-google-maps/api";
import { useAppStore } from "../store/useAppStore";

const containerStyle = { width: "100%", height: "100%" };
const melbourne = { lat: -37.8136, lng: 144.9631 };

export default function MapDashboard() {
  const { isLoaded } = useJsApiLoader({
    id: "gmap-script",
    googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || "",
  });

  const center = useMemo(() => melbourne, []);
  const { pins, addPin, dropPinMode, setDropPinMode } = useAppStore();

  const onMapClick = useCallback((e) => {
    if (!dropPinMode) return;
    const lat = e.latLng?.lat();
    const lng = e.latLng?.lng();
    if (lat && lng) addPin({ type: "pin", lat, lng });
    setDropPinMode(false);
  }, [dropPinMode, addPin, setDropPinMode]);

  if (!isLoaded) {
    return <div className="w-full h-full bg-gray-200 grid place-items-center">
      <p className="text-lg">[Loading Google Map...]</p>
    </div>;
  }

  return (
    <GoogleMap mapContainerStyle={containerStyle} center={center} zoom={12} onClick={onMapClick}>
      {pins.map((p, i) => (
        <Marker key={i} position={{ lat: p.lat, lng: p.lng }}
          icon={p.type === "blitz" ? undefined /* use default for now */ : undefined} />
      ))}
    </GoogleMap>
  );
}
