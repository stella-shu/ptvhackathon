import { useEffect, useState } from "react";
import { urlBuilder } from "@/config/env";

/**
 * Fetches supporting data for map overlays (heatmap + active locations).
 */
export function useMapLayersData() {
  const [heatPoints, setHeatPoints] = useState([]);
  const [activeLocations, setActiveLocations] = useState([]);

  useEffect(() => {
    let aborted = false;

    (async () => {
      try {
        const res = await fetch(urlBuilder.realtime("/api/location/snapshot"));
        if (!res.ok) return;
        const geo = await res.json();
        if (aborted || !geo?.features?.length) return;
        const next = [];
        for (const feature of geo.features) {
          try {
            const coords = feature?.geometry?.coordinates;
            const weight = Number(feature?.properties?.intensity) || 1;
            if (!Array.isArray(coords) || coords.length < 2) continue;
            const [lng, lat] = coords.map(Number);
            if (Number.isFinite(lat) && Number.isFinite(lng)) {
              next.push({ lat, lng, weight });
            }
          } catch (_error) {
            // noop: skip malformed geojson entry
          }
        }
        setHeatPoints(next);
      } catch (_error) {
        // noop: heat layer is optional
      }
    })();

    (async () => {
      try {
        const res = await fetch(urlBuilder.realtime("/api/location/active"));
        if (!res.ok) return;
        const items = await res.json();
        if (aborted) return;
        const next = (items || [])
          .filter((item) => Number.isFinite(item?.latitude) && Number.isFinite(item?.longitude))
          .map((item) => ({ id: item.id, lat: item.latitude, lng: item.longitude, name: item.name }));
        setActiveLocations(next);
      } catch (_error) {
        // noop: active overlays are optional
      }
    })();

    return () => {
      aborted = true;
    };
  }, []);

  return { heatPoints, activeLocations };
}
