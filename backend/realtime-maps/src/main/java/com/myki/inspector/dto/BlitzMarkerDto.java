package com.myki.inspector.dto;

import com.myki.inspector.entity.BlitzMarker;
import java.time.Instant;

public class BlitzMarkerDto {
    private Long id;
    private String inspectorId;
    private String inspectorName;  // optional display name
    private Double latitude;
    private Double longitude;
    private String description;
    private String blitzType;
    private boolean active = true;
    private Instant createdAt;
    private Instant scheduledEnd;

    public BlitzMarkerDto() {}

    public static BlitzMarkerDto fromEntity(BlitzMarker b) {
        BlitzMarkerDto dto = new BlitzMarkerDto();
        dto.id = b.getId();
        dto.inspectorId = b.getInspectorId();
        dto.inspectorName = "Inspector " + b.getInspectorId(); // replace with real name later
        dto.latitude = b.getLatitude();
        dto.longitude = b.getLongitude();
        dto.description = b.getDescription();
        dto.blitzType = b.getBlitzType();
        dto.active = b.isActive();
        dto.createdAt = b.getCreatedAt();
        dto.scheduledEnd = b.getScheduledEnd();
        return dto;
    }

    // Getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInspectorId() { return inspectorId; }
    public void setInspectorId(String inspectorId) { this.inspectorId = inspectorId; }
    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBlitzType() { return blitzType; }
    public void setBlitzType(String blitzType) { this.blitzType = blitzType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(Instant scheduledEnd) { this.scheduledEnd = scheduledEnd; }
}
