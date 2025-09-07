package com.myki.inspector.controller;

import com.myki.inspector.dto.LoginRequest;
import com.myki.inspector.dto.LoginResponse;
import com.myki.inspector.entity.Inspector;
import com.myki.inspector.repository.InspectorRepository;
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
    private final InspectorRepository inspectorRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpReq) {
        String token = authService.login(
                request.getInspectorId(),
                request.getPassword(),
                httpReq.getRemoteAddr(),
                httpReq.getHeader("User-Agent")
        );
        Inspector inspector = inspectorRepository.findByInspectorId(request.getInspectorId()).orElse(null);
        return ResponseEntity.ok(new LoginResponse(token, inspector.getInspectorId(), inspector.getName(), inspector.getEmail()));
    }

    @GetMapping("/bootstrap-info")
    public ResponseEntity<?> bootstrapInfo() {
        // For local/dev visibility only
        return ResponseEntity.ok(Map.of("message", "Use /actuator/health to verify service. POST /api/auth/login to sign in."));
    }
}
