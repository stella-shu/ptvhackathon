package com.myki.inspector.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myki.inspector.dto.LocationUpdateDto;
import com.myki.inspector.dto.PatrolZoneDto;
import com.myki.inspector.entity.PatrolZone;
import com.myki.inspector.repository.PatrolZoneRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class PatrolZoneService {
    private final PatrolZoneRepository patrolZoneRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public PatrolZoneService(PatrolZoneRepository patrolZoneRepository,
                             SimpMessagingTemplate messagingTemplate) {
        this.patrolZoneRepository = patrolZoneRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<PatrolZoneDto> getAllActiveZones() {
        var zones = patrolZoneRepository.findByActiveTrue();
        return zones.stream().map(PatrolZoneDto::fromEntity).toList();
    }

    /** Create a zone and persist its polygon (normalizing keys to {lat,lon}). */
    public PatrolZoneDto createZone(PatrolZoneDto zoneDto) {
        if (zoneDto.getName() == null || zoneDto.getName().isBlank())
            throw new IllegalArgumentException("Zone name is required");
        if (zoneDto.getBoundary() == null || zoneDto.getBoundary().size() < 3)
            throw new IllegalArgumentException("Polygon requires at least 3 points");

        String polygonJson = toPolygonJson(zoneDto.getBoundary());

        PatrolZone zone = new PatrolZone();
        zone.setName(zoneDto.getName());
        zone.setDescription(zoneDto.getDescription());
        zone.setZoneType(zoneDto.getZoneType());
        zone.setBoundaryCoordinates(polygonJson);
        zone.setActive(true);

        PatrolZone saved = patrolZoneRepository.save(zone);

        PatrolZoneDto responseDto = PatrolZoneDto.fromEntity(saved);
        // Broadcast creation
        messagingTemplate.convertAndSend("/topic/zones/created", responseDto);
        return responseDto;
    }

    /** Call this from LocationService.updateLocation(...) after saving a location. */
    public void checkZoneEntry(LocationUpdateDto location) {
        List<PatrolZone> activeZones = patrolZoneRepository.findByActiveTrue();
        for (PatrolZone zone : activeZones) {
            if (isLocationInZone(location.getLatitude(), location.getLongitude(), zone)) {
                sendZoneAlert(location, zone, "ENTERED");
            }
        }
    }

    /* ---------------- helpers ---------------- */

    private String toPolygonJson(List<Map<String, Double>> boundary) {
        try {
            // Normalize keys to {lat,lon} and optionally close the ring
            List<Map<String, Double>> norm = new ArrayList<>();
            for (Map<String, Double> p : boundary) {
                Double lat = p.get("lat");
                Double lon = p.containsKey("lon") ? p.get("lon") : p.get("lng");
                if (lat == null || lon == null) continue;
                Map<String, Double> m = new LinkedHashMap<>();
                m.put("lat", lat);
                m.put("lon", lon);
                norm.add(m);
            }
            if (norm.size() >= 3) {
                Map<String, Double> first = norm.get(0);
                Map<String, Double> last = norm.get(norm.size() - 1);
                if (!first.get("lat").equals(last.get("lat")) || !first.get("lon").equals(last.get("lon"))) {
                    norm.add(new LinkedHashMap<>(first));
                }
            }
            return mapper.writeValueAsString(norm);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize polygon", e);
        }
    }

    private boolean isLocationInZone(double lat, double lon, PatrolZone zone) {
        List<Map<String, Double>> ring = parse(zone.getBoundaryCoordinates());
        if (ring.size() < 3) return false;
        // Ray-casting; treat x=lon, y=lat
        boolean inside = false;
        for (int i = 0, j = ring.size() - 1; i < ring.size(); j = i++) {
            double yi = ring.get(i).get("lat");
            double xi = ring.get(i).get("lon");
            double yj = ring.get(j).get("lat");
            double xj = ring.get(j).get("lon");
            boolean intersect = ((yi > lat) != (yj > lat)) &&
                    (lon < (xj - xi) * (lat - yi) / ((yj - yi) == 0 ? 1e-12 : (yj - yi)) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }

    private List<Map<String, Double>> parse(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<Map<String, Object>> raw = mapper.readValue(json, new TypeReference<>() {});
            List<Map<String, Double>> out = new ArrayList<>(raw.size());
            for (Map<String, Object> p : raw) {
                Double lat = toD(p.get("lat"));
                Double lon = p.containsKey("lon") ? toD(p.get("lon"))
                        : p.containsKey("lng") ? toD(p.get("lng"))
                        : null;
                if (lat == null || lon == null) continue;
                Map<String, Double> m = new LinkedHashMap<>();
                m.put("lat", lat);
                m.put("lon", lon);
                out.add(m);
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    private Double toD(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        return Double.valueOf(String.valueOf(v));
    }

    private void sendZoneAlert(LocationUpdateDto location, PatrolZone zone, String action) {
        Map<String, Object> alert = Map.of(
                "type", "ZONE_ALERT",
                "action", action,
                "inspector", location,
                "zone", PatrolZoneDto.fromEntity(zone),
                "timestamp", Instant.now()
        );
        messagingTemplate.convertAndSend("/topic/zones/alerts", alert);
    }
}
