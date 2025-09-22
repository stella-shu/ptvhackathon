package com.myki.inspector.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 64)
    private String actorInspectorId;

    @Column(nullable = false, length = 64)
    private String action; // LOGIN_SUCCESS, INCIDENT_CREATE, SHIFT_UPDATE, etc

    @Column(length = 64)
    private String entityType; // Incident, Shift, Inspector

    private String entityId; // UUID as string

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 64)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string or details
}

