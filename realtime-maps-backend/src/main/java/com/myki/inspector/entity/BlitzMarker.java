package com.myki.inspector.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.Instant;

@Entity
@Table(name = "blitz_markers", indexes = {
        @Index(name = "idx_blitz_active_created", columnList = "is_active,created_at")
})
public class BlitzMarker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspector_id", nullable = false)
    private String inspectorId;

    @DecimalMin("-90.0")  @DecimalMax("90.0")
    @Column(nullable = false)
    private Double latitude;

    @DecimalMin("-180.0") @DecimalMax("180.0")
    @Column(nullable = false)
    private Double longitude;

    @Column(length = 500)
    private String description;

    @Column(name = "blitz_type")
    private String blitzType; // "operation", "checkpoint", "sweep"

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "scheduled_end")
    private Instant scheduledEnd;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = Instant.now(); }

    public BlitzMarker() {}
    public BlitzMarker(String inspectorId, Double latitude, Double longitude,
                       String description, String blitzType) {
        this.inspectorId = inspectorId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.blitzType = blitzType;
    }

    // Getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInspectorId() { return inspectorId; }
    public void setInspectorId(String inspectorId) { this.inspectorId = inspectorId; }
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
