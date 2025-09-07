package com.myki.inspector.entity;

import jakarta.persistence.*;
import lombok.*;
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
@Table(name = "inspectors")
public class Inspector {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false, length = 64)
    private String inspectorId; // external employee/inspector id

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 64)
    private String totpSecret;

    @Column(nullable = false)
    private boolean mfaEnabled = true;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
