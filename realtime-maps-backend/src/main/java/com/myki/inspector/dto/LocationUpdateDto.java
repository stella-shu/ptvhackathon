package com.myki.inspector.dto;

import com.myki.inspector.entity.LocationUpdate;
import java.time.Instant;

public class LocationUpdateDto {
    private Long id;
    private String inspectorId;
    private String inspectorName; // For display purposes
    private double latitude;
    private double longitude;
    private Double accuracy;
    private boolean active;
    private Instant createdAt;

    public LocationUpdateDto() {
        this.createdAt = Instant.now();
        this.active = true;
    }

    // Mapper from entity -> DTO
    public static LocationUpdateDto fromEntity(LocationUpdate location) {
        LocationUpdateDto dto = new LocationUpdateDto();
        dto.id = location.getId();
        dto.inspectorId = location.getInspectorId();
        dto.inspectorName = "Inspector " + location.getInspectorId(); // TODO: Get real name from auth service
        dto.latitude = location.getLatitude();
        dto.longitude = location.getLongitude();
        dto.accuracy = location.getAccuracy();
        dto.active = location.isActive();
        dto.createdAt = location.getCreatedAt();
        return dto;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInspectorId() { return inspectorId; }
    public void setInspectorId(String inspectorId) { this.inspectorId = inspectorId; }

    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}