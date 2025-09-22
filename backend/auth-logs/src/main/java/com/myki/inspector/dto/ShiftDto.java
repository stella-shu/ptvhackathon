package com.myki.inspector.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ShiftDto {
    private Instant startTime;
    private Instant endTime;
    private String status; // OPEN/CLOSED
    private String location;
    private String notes;
}

