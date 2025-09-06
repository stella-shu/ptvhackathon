package com.myki.inspector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String inspectorId;
    @NotBlank
    private String password;
    @NotNull
    private Integer otp;
}

