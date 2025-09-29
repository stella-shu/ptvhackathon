import { useEffect } from "react";
import { urlBuilder } from "@/config/env";

/**
 * Loads active blitz markers once and updates the provided setter when data arrives.
 */
export function useBlitzPins(setRemotePins) {
  useEffect(() => {
    let aborted = false;
    (async () => {
      try {
        const res = await fetch(urlBuilder.realtime("/api/blitz/active"));
        if (!res.ok) return;
        const items = await res.json();
        if (aborted) return;
        const pins = (items || [])
          .filter((b) => typeof b?.latitude === "number" && typeof b?.longitude === "number")
          .map((b) => ({ type: "blitz", lat: b.latitude, lng: b.longitude, id: b.id }));
        setRemotePins(pins);
      } catch (_error) {
        // noop: realtime stream will sync pins when available
      }
    })();
    return () => {
      aborted = true;
    };
  }, [setRemotePins]);
}
