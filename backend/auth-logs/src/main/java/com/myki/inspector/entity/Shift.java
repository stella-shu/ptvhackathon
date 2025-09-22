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
@Table(name = "shifts")
public class Shift {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private Inspector inspector;

    private Instant startTime;
    private Instant endTime;

    @Column(length = 64)
    private String status; // OPEN, CLOSED

    @Column(length = 255)
    private String location; // depot/route/etc

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
