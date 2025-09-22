package com.myki.inspector.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myki.inspector.entity.PatrolZone;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** DTO exposing polygon as a list of points: [{lat, lon}, ...] */
public class PatrolZoneDto {
    private Long id;
    private String name;
    private String description;
    private List<Map<String, Double>> boundary; // [{lat, lon}]
    private String zoneType;
    private boolean active;
    private Instant createdAt;

    public PatrolZoneDto() {}

    public static PatrolZoneDto fromEntity(PatrolZone zone) {
        PatrolZoneDto dto = new PatrolZoneDto();
        dto.id = zone.getId();
        dto.name = zone.getName();
        dto.description = zone.getDescription();
        dto.zoneType = zone.getZoneType();
        dto.active = zone.isActive();
        dto.createdAt = zone.getCreatedAt();
        dto.boundary = parseCoordinates(zone.getBoundaryCoordinates());
        return dto;
    }

    /** Accepts JSON with keys {lat, lon} or {lat, lng}; normalizes to {lat, lon}. */
    private static List<Map<String, Double>> parseCoordinates(String json) {
        List<Map<String, Double>> out = new ArrayList<>();
        if (json == null || json.isBlank()) return out;
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> raw = mapper.readValue(json, new TypeReference<>() {});
            for (Map<String, Object> p : raw) {
                if (!p.containsKey("lat")) continue;
                double lat = toD(p.get("lat"));
                double lon = p.containsKey("lon") ? toD(p.get("lon"))
                        : p.containsKey("lng") ? toD(p.get("lng"))
                        : Double.NaN;
                if (!Double.isNaN(lon)) {
                    Map<String, Double> m = new LinkedHashMap<>();
                    m.put("lat", lat);
                    m.put("lon", lon); // normalized key
                    out.add(m);
                }
            }
        } catch (Exception ignore) {}
        return out;
    }

    private static double toD(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(v));
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Map<String, Double>> getBoundary() { return boundary; }
    public void setBoundary(List<Map<String, Double>> boundary) { this.boundary = boundary; }

    public String getZoneType() { return zoneType; }
    public void setZoneType(String zoneType) { this.zoneType = zoneType; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
