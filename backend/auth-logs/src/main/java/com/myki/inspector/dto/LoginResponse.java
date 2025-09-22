package com.myki.inspector.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String inspectorId;
    private String name;
    private String email;
}

