import { useEffect, useMemo, useRef } from "react";
import L from "leaflet";
import { useAppStore } from "@/stores/appStore";
import { useBlitzPins } from "@/features/map/hooks/useBlitzPins";
import { useMapLayersData } from "@/features/map/hooks/useMapLayersData";

const containerStyle = { width: "100%", height: "100%" };
const MELBOURNE_COORDS = { lat: -37.8136, lng: 144.9631 };

export default function MapCanvas() {
  const mapRef = useRef(null);
  const mapElRef = useRef(null);
  const markersRef = useRef({ pins: [], remotePins: [], active: [], heat: [] });
  const center = useMemo(() => MELBOURNE_COORDS, []);

  const { pins, remotePins, setRemotePins, addPin, dropPinMode, setDropPinMode } = useAppStore();
  useBlitzPins(setRemotePins);

  useEffect(() => {
    if (mapRef.current || !mapElRef.current) return;
    const map = L.map(mapElRef.current, { center, zoom: 12 });
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "&copy; OpenStreetMap contributors",
      maxZoom: 19,
    }).addTo(map);

    map.on("click", (e) => {
      if (!dropPinMode) return;
      const { lat, lng } = e.latlng || {};
      if (Number.isFinite(lat) && Number.isFinite(lng)) addPin({ type: "pin", lat, lng });
      setDropPinMode(false);
    });

    mapRef.current = map;
  }, [center, dropPinMode, addPin, setDropPinMode]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    for (const marker of markersRef.current.pins) map.removeLayer(marker);
    markersRef.current.pins = [];
    for (const marker of markersRef.current.remotePins) map.removeLayer(marker);
    markersRef.current.remotePins = [];

    for (const pin of pins) {
      const marker = L.marker([pin.lat, pin.lng]);
      marker.addTo(map);
      markersRef.current.pins.push(marker);
    }

    for (const pin of remotePins) {
      const icon = pin.type === "blitz" ? L.icon({ iconUrl: BLITZ_ICON_URL, iconSize: [28, 28] }) : undefined;
      const marker = icon ? L.marker([pin.lat, pin.lng], { icon }) : L.marker([pin.lat, pin.lng]);
      marker.addTo(map);
      markersRef.current.remotePins.push(marker);
    }
  }, [pins, remotePins]);

  const { heatPoints, activeLocations } = useMapLayersData();
  const showHeatmap = useAppStore((state) => state.showHeatmap);
  const showActive = useAppStore((state) => state.showActive);

  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    for (const marker of markersRef.current.active) map.removeLayer(marker);
    markersRef.current.active = [];
    if (!showActive) return;
    for (const location of activeLocations) {
      const circle = L.circleMarker([location.lat, location.lng], {
        radius: 8,
        color: "#2563eb",
        fillColor: "#3b82f6",
        fillOpacity: 0.9,
        weight: 1,
      }).addTo(map);
      circle.bindTooltip(String(location.name || location.id), {
        permanent: true,
        direction: "top",
        offset: [0, -10],
      });
      markersRef.current.active.push(circle);
    }
  }, [activeLocations, showActive]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    for (const marker of markersRef.current.heat) map.removeLayer(marker);
    markersRef.current.heat = [];
    if (!showHeatmap) return;
    for (const point of heatPoints) {
      const radius = Math.max(6, Math.min(30, 6 + (point.weight || 1) * 6));
      const circle = L.circle([point.lat, point.lng], {
        radius,
        color: "#ef4444",
        weight: 0,
        fillColor: "#ef4444",
        fillOpacity: 0.15,
      }).addTo(map);
      markersRef.current.heat.push(circle);
    }
  }, [heatPoints, showHeatmap]);

  return <div ref={mapElRef} style={containerStyle} />;
}

const BLITZ_ICON_URL = `data:image/svg+xml;utf8,${encodeURIComponent([
  '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="none">',
  '<path d="M12 22s8-7 8-12a8 8 0 10-16 0c0 5 8 12 8 12z" fill="#f59e0b"/>',
  '<circle cx="12" cy="10" r="3.2" fill="#fff"/>',
  '</svg>',
].join("\n"))}`;
