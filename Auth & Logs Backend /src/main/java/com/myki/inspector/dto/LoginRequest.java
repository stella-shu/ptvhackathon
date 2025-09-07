package com.myki.inspector.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String inspectorId;
    @NotBlank
    private String password;
}
