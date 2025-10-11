import { useMemo, useCallback, useEffect, useState } from "react";
import { GoogleMap, useJsApiLoader, Marker, HeatmapLayer } from "@react-google-maps/api";
import { useAppStore } from "../store/useAppStore";

const containerStyle = { width: "100%", height: "100%" };
const melbourne = { lat: -37.8136, lng: 144.9631 };

export default function MapDashboard() {
  const envKey = (import.meta.env.VITE_GOOGLE_MAPS_API_KEY || "").trim();
  const [apiKey, setApiKey] = useState(envKey);
  const [configStatus, setConfigStatus] = useState(envKey ? "ready" : "loading");
  const [configError, setConfigError] = useState("");

  useEffect(() => {
    if (envKey) return;
    let cancelled = false;
    setConfigStatus("loading");
    (async () => {
      try {
        const res = await fetch("/api/maps/config");
        if (!res.ok) {
          throw new Error(`HTTP ${res.status} while fetching map configuration`);
        }
        const data = await res.json();
        if (cancelled) return;
        const key = data?.apiKey ? String(data.apiKey).trim() : "";
        if (key) {
          setApiKey(key);
          setConfigStatus("ready");
        } else {
          setConfigError("Map API key not configured on server.");
          setConfigStatus("error");
        }
      } catch (err) {
        if (cancelled) return;
        setConfigError(err.message || String(err));
        setConfigStatus("error");
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [envKey]);

  if (!apiKey) {
    if (configStatus === "loading") {
      return (
        <div className="grid h-full w-full place-items-center bg-white/70 text-sm text-slate-500">
          <p>Checking Google Maps configuration…</p>
        </div>
      );
    }
    return (
      <div className="grid h-full w-full place-items-center bg-white/70 px-6 text-center text-sm text-rose-500">
        <div className="space-y-2">
          <p>We couldn’t find a Google Maps API key.</p>
          <p className="text-slate-500">
            Set <code>VITE_GOOGLE_MAPS_API_KEY</code> in the Vite app or configure{" "}
            <code>google.maps.browser-api-key</code> on the backend (env var{" "}
            <code>GOOGLE_MAPS_BROWSER_API_KEY</code>).
          </p>
          {configError && <p className="text-xs text-rose-400">Details: {configError}</p>}
        </div>
      </div>
    );
  }

  return <MapCanvas apiKey={apiKey} />;
}

function MapCanvas({ apiKey }) {
  const { isLoaded, loadError } = useJsApiLoader({
    id: "gmap-script",
    googleMapsApiKey: apiKey,
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

  if (loadError) {
    return (
      <div className="grid h-full w-full place-items-center bg-white/70 text-sm text-rose-500">
        <p>We couldn’t load Google Maps right now: {String(loadError)}</p>
      </div>
    );
  }

  if (!isLoaded) {
    return (
      <div className="grid h-full w-full place-items-center bg-white/70 text-lg text-slate-500">
        <p>Loading map magic…</p>
      </div>
    );
  }

  const { heatmapData, activeLocations } = useHeatAndActive(isLoaded, setRemotePins);
  const showHeatmap = useAppStore((s) => s.showHeatmap);
  const showActive = useAppStore((s) => s.showActive);
  const mapOptions = useMemo(
    () => ({
      disableDefaultUI: true,
      zoomControl: true,
      streetViewControl: false,
      mapTypeControl: false,
      backgroundColor: "#fef6fb",
      styles: CUTE_MAP_STYLES,
    }),
    []
  );

  return (
    <GoogleMap
      mapContainerStyle={containerStyle}
      mapContainerClassName="h-full w-full transition"
      center={center}
      zoom={12}
      onClick={onMapClick}
      options={mapOptions}
    >
      {showHeatmap && heatmapData && (
        <HeatmapLayer
          data={heatmapData}
          options={{ radius: 36, opacity: 0.6 }}
        />
      )}

      {showActive &&
        activeLocations.map((loc) => (
          <Marker
            key={`al_${loc.id}`}
            position={{ lat: loc.lat, lng: loc.lng }}
            icon={ACTIVE_ICON}
            label={{
              text: loc.name || String(loc.id),
              color: "#475569",
              fontSize: "12px",
              fontWeight: "600",
            }}
          />
        ))}

      {[...remotePins, ...pins].map((p, i) => (
        <Marker
          key={`pin_${p.id ?? i}`}
          position={{ lat: p.lat, lng: p.lng }}
          icon={p.type === "blitz" ? BLITZ_ICON : PIN_ICON}
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

const PIN_ICON = {
  url:
    "data:image/svg+xml;utf8," +
    encodeURIComponent(
      '<svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="none">\n' +
        '<path d="M12 22s7-6.4 7-11a7 7 0 10-14 0c0 4.6 7 11 7 11z" fill="#fbcfe8"/>\n' +
        '<circle cx="12" cy="10" r="3" fill="#fb7185"/>\n' +
      '</svg>'
    ),
  scaledSize: { width: 24, height: 24 },
};

const CUTE_MAP_STYLES = [
  {
    featureType: "water",
    elementType: "geometry.fill",
    stylers: [{ color: "#cdeffd" }],
  },
  {
    featureType: "landscape",
    elementType: "geometry",
    stylers: [{ color: "#fef7fb" }],
  },
  {
    featureType: "poi",
    elementType: "geometry",
    stylers: [{ color: "#fdebf5" }],
  },
  {
    featureType: "road",
    elementType: "geometry",
    stylers: [{ color: "#ffe8f0" }],
  },
  {
    featureType: "road",
    elementType: "geometry.stroke",
    stylers: [{ color: "#fbcfe8" }, { weight: 0.6 }],
  },
  {
    featureType: "administrative",
    elementType: "labels.text.fill",
    stylers: [{ color: "#9b8da1" }],
  },
  {
    featureType: "poi.park",
    elementType: "geometry",
    stylers: [{ color: "#e0f5d5" }],
  },
];

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
