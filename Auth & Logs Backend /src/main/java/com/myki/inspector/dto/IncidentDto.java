package com.myki.inspector.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class IncidentDto {
    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotBlank
    private String severity; // LOW/MEDIUM/HIGH

    @NotBlank
    private String status;   // OPEN/IN_PROGRESS/CLOSED

    @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "latitude must be <= 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "longitude must be <= 180")
    private Double longitude;

    @NotNull
    private Instant occurredAt;
}
