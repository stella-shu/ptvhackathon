package com.myki.inspector.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "patrol_zones")
public class PatrolZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "boundary_coordinates", columnDefinition = "TEXT")
    private String boundaryCoordinates; // JSON array: [{"lat":-37.8136,"lng":144.9631},...]

    @Column(name = "zone_type")
    private String zoneType; // "patrol", "restricted", "high_priority"

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    // Constructors
    public PatrolZone() {}

    public PatrolZone(String name, String description, String boundaryCoordinates, String zoneType) {
        this.name = name;
        this.description = description;
        this.boundaryCoordinates = boundaryCoordinates;
        this.zoneType = zoneType;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBoundaryCoordinates() { return boundaryCoordinates; }
    public void setBoundaryCoordinates(String boundaryCoordinates) { this.boundaryCoordinates = boundaryCoordinates; }

    public String getZoneType() { return zoneType; }
    public void setZoneType(String zoneType) { this.zoneType = zoneType; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}