package com.myki.inspector.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class IncidentDto {
    private String title;
    private String description;
    private String severity; // LOW/MEDIUM/HIGH
    private String status;   // OPEN/IN_PROGRESS/CLOSED
    private Double latitude;
    private Double longitude;
    private Instant occurredAt;
}

