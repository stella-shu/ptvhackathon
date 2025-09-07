import { useEffect, useMemo, useRef, useState, useCallback } from "react";
import { useAppStore } from "../store/useAppStore";
import L from "leaflet";

const containerStyle = { width: "100%", height: "100%" };
const melbourne = { lat: -37.8136, lng: 144.9631 };

export default function MapDashboard() {
  const mapRef = useRef(null);
  const mapElRef = useRef(null);
  const markersRef = useRef({ pins: [], remotePins: [], active: [], heat: [] });
  const center = useMemo(() => melbourne, []);

  const { pins, remotePins, setRemotePins, addPin, dropPinMode, setDropPinMode } = useAppStore();
  useLoadBlitz(setRemotePins);

  // Init map once
  useEffect(() => {
    if (mapRef.current || !mapElRef.current) return;
    const map = L.map(mapElRef.current, { center, zoom: 12 });
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "&copy; OpenStreetMap contributors",
      maxZoom: 19,
    }).addTo(map);

    // click handler for drop-pin mode
    map.on("click", (e) => {
      if (!dropPinMode) return;
      const { lat, lng } = e.latlng || {};
      if (Number.isFinite(lat) && Number.isFinite(lng)) addPin({ type: "pin", lat, lng });
      setDropPinMode(false);
    });

    mapRef.current = map;
  }, [center, dropPinMode, addPin, setDropPinMode]);

  // Render local + remote pins
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    // clear existing
    for (const m of markersRef.current.pins) map.removeLayer(m);
    markersRef.current.pins = [];
    for (const m of markersRef.current.remotePins) map.removeLayer(m);
    markersRef.current.remotePins = [];

    // local pins
    for (const p of pins) {
      const m = L.marker([p.lat, p.lng]);
      m.addTo(map);
      markersRef.current.pins.push(m);
    }

    // remote pins (blitz etc.) with special icon
    for (const p of remotePins) {
      const icon = p.type === "blitz" ? L.icon({ iconUrl: BLITZ_ICON_URL, iconSize: [28, 28] }) : undefined;
      const m = icon ? L.marker([p.lat, p.lng], { icon }) : L.marker([p.lat, p.lng]);
      m.addTo(map);
      markersRef.current.remotePins.push(m);
    }
  }, [pins, remotePins]);

  const { heatPoints, activeLocations } = useHeatAndActive(setRemotePins);
  const showHeatmap = useAppStore((s) => s.showHeatmap);
  const showActive = useAppStore((s) => s.showActive);

  // Render active locations as blue circles with label tooltips
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    for (const m of markersRef.current.active) map.removeLayer(m);
    markersRef.current.active = [];
    if (!showActive) return;
    for (const loc of activeLocations) {
      const c = L.circleMarker([loc.lat, loc.lng], {
        radius: 8,
        color: "#2563eb",
        fillColor: "#3b82f6",
        fillOpacity: 0.9,
        weight: 1,
      }).addTo(map);
      c.bindTooltip(String(loc.name || loc.id), { permanent: true, direction: "top", offset: [0, -10] });
      markersRef.current.active.push(c);
    }
  }, [activeLocations, showActive]);

  // Render fake heatmap via translucent circles (no plugin)
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    for (const m of markersRef.current.heat) map.removeLayer(m);
    markersRef.current.heat = [];
    if (!showHeatmap) return;
    for (const pt of heatPoints) {
      const r = Math.max(6, Math.min(30, 6 + (pt.weight || 1) * 6));
      const c = L.circle([pt.lat, pt.lng], {
        radius: r,
        color: "#ef4444",
        weight: 0,
        fillColor: "#ef4444",
        fillOpacity: 0.15,
      }).addTo(map);
      markersRef.current.heat.push(c);
    }
  }, [heatPoints, showHeatmap]);

  return <div ref={mapElRef} style={containerStyle} />;
}

// orange pin SVG data URL for blitz
const BLITZ_ICON_URL =
  "data:image/svg+xml;utf8," +
  encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="none">\n' +
      '<path d="M12 22s8-7 8-12a8 8 0 10-16 0c0 5 8 12 8 12z" fill="#f59e0b"/>\n' +
      '<circle cx="12" cy="10" r="3.2" fill="#fff"/>\n' +
    '</svg>'
  );

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

function useHeatAndActive(setRemotePins) {
  const [heatPoints, setHeatPoints] = useState([]);
  const [activeLocations, setActiveLocations] = useState([]);
  useEffect(() => {
    let aborted = false;
    (async () => {
      try {
        // Heatmap snapshot to weighted points
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
                  if (Number.isFinite(lat) && Number.isFinite(lng)) arr.push({ lat, lng, weight });
                }
              } catch {}
            }
            setHeatPoints(arr);
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
              .map((i) => ({ id: i.id, lat: i.latitude, lng: i.longitude, name: i.name }));
            setActiveLocations(list);
          }
        }
      } catch {}
    })();
    return () => {
      aborted = true;
    };
  }, [setRemotePins]);
  return { heatPoints, activeLocations };
}
