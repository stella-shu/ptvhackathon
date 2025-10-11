package com.myki.inspector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class ShiftDto {
    @NotNull
    private Instant startTime;

    private Instant endTime;

    @NotBlank
    private String status; // OPEN/CLOSED

    @Size(max = 255)
    private String location;

    @Size(max = 1000)
    private String notes;
}
