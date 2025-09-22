package com.myki.inspector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Inspector inspector; // reporter/owner

    @Column(nullable = false, length = 140)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 32)
    private String severity; // LOW/MEDIUM/HIGH

    @Column(length = 32)
    private String status; // OPEN/IN_PROGRESS/CLOSED

    private Double latitude;
    private Double longitude;

    private Instant occurredAt;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
