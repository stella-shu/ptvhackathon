package com.myki.inspector.controller;

import com.myki.inspector.dto.LoginRequest;
import com.myki.inspector.dto.LoginResponse;
import com.myki.inspector.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpReq) {
        LoginResponse response = authService.login(
                request.getInspectorId(),
                request.getPassword(),
                request.getOtp(),
                httpReq.getRemoteAddr(),
                httpReq.getHeader("User-Agent")
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bootstrap-info")
    public ResponseEntity<?> bootstrapInfo() {
        // For local/dev visibility only
        return ResponseEntity.ok(Map.of("message", "Use /actuator/health to verify service. POST /api/auth/login to sign in."));
    }
}
