package com.myki.inspector.service;

import com.myki.inspector.dto.LocationUpdateDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HotspotService {
    private static final double CLUSTER_RADIUS_KM = 0.1; // ~100m
    private static final int MIN_HOTSPOT_COUNT = 3;      // >=3 people
    private static final int ACTIVE_WINDOW_MIN = 5;      // cluster window
    private static final int HEATMAP_WINDOW_MIN = 10;    // heatmap window

    private final LocationService locationService;
    private final SimpMessagingTemplate messagingTemplate;

    public HotspotService(LocationService locationService,
                          SimpMessagingTemplate messagingTemplate) {
        this.locationService = locationService;
        this.messagingTemplate = messagingTemplate;
    }

    /** Every minute: find clusters & broadcast alerts to /topic/hotspots/detected */
    @Scheduled(fixedRate = 60_000)
    public void detectHotspots() {
        List<LocationUpdateDto> recent = locationService.getActiveLocations(ACTIVE_WINDOW_MIN);
        List<HotspotCluster> clusters = cluster(recent);

        for (HotspotCluster c : clusters) {
            if (c.size() >= MIN_HOTSPOT_COUNT) {
                var payload = Map.of(
                        "type", "HOTSPOT_DETECTED",
                        "latitude", c.centerLat(),
                        "longitude", c.centerLng(),
                        "count", c.size(),
                        "radius", (int) Math.round(CLUSTER_RADIUS_KM * 1000),
                        "timestamp", Instant.now(),
                        "severity", c.size() >= 5 ? "HIGH" : "MEDIUM"
                );
                messagingTemplate.convertAndSend("/topic/hotspots/detected", payload);
            }
        }
    }

    /** Heatmap snapshot (cached) for `/api/hotspots/heatmap` */
    @Cacheable(value = "heatmapData", key = "'current'")
    public List<Map<String, Object>> getHeatmapData() {
        List<LocationUpdateDto> pts = locationService.getActiveLocations(HEATMAP_WINDOW_MIN);
        List<Map<String, Object>> out = new ArrayList<>(pts.size());
        for (LocationUpdateDto p : pts) {
            double intensity = intensity(p, pts);
            out.add(Map.of(
                    "lat", p.getLatitude(),
                    "lng", p.getLongitude(),
                    "intensity", intensity
            ));
        }
        return out;
    }

    /* ----------------- helpers ----------------- */

    private List<HotspotCluster> cluster(List<LocationUpdateDto> pts) {
        List<HotspotCluster> clusters = new ArrayList<>();
        for (LocationUpdateDto p : pts) {
            boolean added = false;
            for (HotspotCluster c : clusters) {
                if (haversineKm(c.centerLat(), c.centerLng(), p.getLatitude(), p.getLongitude()) <= CLUSTER_RADIUS_KM) {
                    c.add(p);
                    added = true;
                    break;
                }
            }
            if (!added) clusters.add(new HotspotCluster(p));
        }
        return clusters;
    }

    private double intensity(LocationUpdateDto target, List<LocationUpdateDto> all) {
        int nearby = 0;
        for (LocationUpdateDto p : all) {
            double d = haversineKm(target.getLatitude(), target.getLongitude(),
                    p.getLatitude(), p.getLongitude());
            if (d < 0.05) nearby++; // within 50m
        }
        return Math.min(1.0, nearby / 5.0); // cap at 5 -> 1.0
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    /* simple in-memory cluster */
    private static class HotspotCluster {
        private final List<LocationUpdateDto> list = new ArrayList<>();
        private double lat, lng;

        HotspotCluster(LocationUpdateDto first) { add(first); }
        void add(LocationUpdateDto p) { list.add(p); recompute(); }
        int size() { return list.size(); }
        double centerLat() { return lat; }
        double centerLng() { return lng; }

        private void recompute() {
            lat = list.stream().mapToDouble(LocationUpdateDto::getLatitude).average().orElse(0);
            lng = list.stream().mapToDouble(LocationUpdateDto::getLongitude).average().orElse(0);
        }
    }
}
