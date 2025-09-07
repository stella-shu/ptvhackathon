import { useMemo, useCallback, useEffect, useState } from "react";
import { GoogleMap, useJsApiLoader, Marker, HeatmapLayer } from "@react-google-maps/api";
import { useAppStore } from "../store/useAppStore";

const containerStyle = { width: "100%", height: "100%" };
const melbourne = { lat: -37.8136, lng: 144.9631 };

export default function MapDashboard() {
  const apiKey = import.meta.env.VITE_GOOGLE_MAPS_API_KEY;
  const { isLoaded, loadError } = useJsApiLoader({
    id: "gmap-script",
    googleMapsApiKey: apiKey || undefined,
    libraries: ["visualization"],
  });

  const center = useMemo(() => melbourne, []);
  const { pins, remotePins, setRemotePins, addPin, dropPinMode, setDropPinMode } = useAppStore();
  useLoadBlitz(setRemotePins);

  const onMapClick = useCallback(
    (e) => {
      if (!dropPinMode) return;
      const lat = e?.latLng?.lat?.();
      const lng = e?.latLng?.lng?.();
      if (typeof lat === "number" && typeof lng === "number") {
        addPin({ type: "pin", lat, lng });
      }
      setDropPinMode(false);
    },
    [dropPinMode, addPin, setDropPinMode]
  );

  if (!apiKey) {
    return (
      <div className="w-full h-full bg-gray-200 grid place-items-center">
        <p className="text-sm text-slate-700">[Missing Google Maps API key: set VITE_GOOGLE_MAPS_API_KEY]</p>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="w-full h-full bg-gray-200 grid place-items-center">
        <p className="text-sm text-red-700">[Failed to load Google Maps: {String(loadError)}]</p>
      </div>
    );
  }

  if (!isLoaded) {
    return (
      <div className="w-full h-full bg-gray-200 grid place-items-center">
        <p className="text-lg">[Loading Google Map...]</p>
      </div>
    );
  }

  const { heatmapData, activeLocations } = useHeatAndActive(isLoaded, setRemotePins);
  const showHeatmap = useAppStore((s) => s.showHeatmap);
  const showActive = useAppStore((s) => s.showActive);

  return (
    <GoogleMap mapContainerStyle={containerStyle} center={center} zoom={12} onClick={onMapClick}>
      {showHeatmap && heatmapData && <HeatmapLayer data={heatmapData} options={{ radius: 24 }} />}

      {showActive && activeLocations.map((loc) => (
        <Marker
          key={`al_${loc.id}`}
          position={{ lat: loc.lat, lng: loc.lng }}
          icon={ACTIVE_ICON}
          label={{ text: loc.name || String(loc.id), color: "#1f2937", fontSize: "12px" }}
        />
      ))}

      {[...remotePins, ...pins].map((p, i) => (
        <Marker
          key={`pin_${p.id ?? i}`}
          position={{ lat: p.lat, lng: p.lng }}
          icon={p.type === "blitz" ? BLITZ_ICON : undefined}
        />
      ))}
    </GoogleMap>
  );
}

// simple orange pin SVG as data URL (distinct for blitz)
const BLITZ_ICON = {
  url:
    "data:image/svg+xml;utf8,"
    + encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="none">\n' +
        '<path d="M12 22s8-7 8-12a8 8 0 10-16 0c0 5 8 12 8 12z" fill="#f59e0b"/>\n' +
        '<circle cx="12" cy="10" r="3.2" fill="#fff"/>\n' +
      '</svg>'
    ),
  scaledSize: { width: 28, height: 28 },
};

const ACTIVE_ICON = {
  url:
    "data:image/svg+xml;utf8," +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="none">\n' +
        '<circle cx="12" cy="12" r="6" fill="#3b82f6" fill-opacity="0.9"/>\n' +
      '</svg>'
    ),
  scaledSize: { width: 18, height: 18 },
};

// Load active blitz markers from backend once on mount
function useLoadBlitz(setRemotePins) {
  useEffect(() => {
    let aborted = false;
    (async () => {
      try {
        const res = await fetch("/api/blitz/active");
        if (!res.ok) return;
        const items = await res.json();
        if (aborted) return;
        const pins = (items || [])
          .filter((b) => typeof b?.latitude === "number" && typeof b?.longitude === "number")
          .map((b) => ({ type: "blitz", lat: b.latitude, lng: b.longitude, id: b.id }));
        setRemotePins(pins);
      } catch {}
    })();
    return () => {
      aborted = true;
    };
  }, [setRemotePins]);
}

function useHeatAndActive(enabled, setRemotePins) {
  const [heatmapData, setHeatmapData] = useState(null);
  const [activeLocations, setActiveLocations] = useState([]);
  useEffect(() => {
    if (!enabled) return;
    let aborted = false;
    (async () => {
      try {
        // Heatmap snapshot
        const res = await fetch("/api/location/snapshot");
        if (res.ok) {
          const geo = await res.json();
          if (!aborted && geo?.features?.length) {
            const arr = [];
            for (const f of geo.features) {
              try {
                const coords = f?.geometry?.coordinates;
                const weight = Number(f?.properties?.intensity) || 1;
                if (Array.isArray(coords) && coords.length >= 2) {
                  const lng = Number(coords[0]);
                  const lat = Number(coords[1]);
                  if (Number.isFinite(lat) && Number.isFinite(lng)) {
                    arr.push({ location: new google.maps.LatLng(lat, lng), weight });
                  }
                }
              } catch {}
            }
            setHeatmapData(arr);
          }
        }
      } catch {}
    })();
    (async () => {
      try {
        // Active locations
        const res = await fetch("/api/location/active");
        if (res.ok) {
          const items = await res.json();
          if (!aborted) {
            const list = (items || [])
              .filter((i) => Number.isFinite(i?.latitude) && Number.isFinite(i?.longitude))
              .map((i) => ({ id: i.id, lat: i.latitude, lng: i.longitude }));
            setActiveLocations(list);
          }
        }
      } catch {}
    })();
    return () => {
      aborted = true;
    };
  }, [enabled]);
  return { heatmapData, activeLocations };
}
