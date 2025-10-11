package com.myki.inspector.service;

import com.myki.inspector.dto.LoginResponse;
import com.myki.inspector.entity.Inspector;
import com.myki.inspector.exception.AuthenticationException;
import com.myki.inspector.repository.InspectorRepository;
import com.myki.inspector.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final InspectorRepository inspectorRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;
    private final JwtService jwtService;
    private final AuditService auditService;

    public LoginResponse login(String inspectorId, String password, Integer otp, String ip, String userAgent) {
        String normalizedId = inspectorId == null ? null : inspectorId.trim();
        if (!StringUtils.hasText(normalizedId) || !StringUtils.hasText(password)) {
            throw new AuthenticationException("Inspector ID and password are required");
        }
        Optional<Inspector> inspectorOpt = inspectorRepository.findByInspectorId(normalizedId);
        if (inspectorOpt.isEmpty()) {
            auditService.log(normalizedId, "LOGIN_FAIL", "Inspector", null, ip, userAgent, "not_found");
            throw new AuthenticationException("Invalid credentials");
        }
        Inspector inspector = inspectorOpt.get();
        if (!passwordEncoder.matches(password, inspector.getPasswordHash())) {
            auditService.log(normalizedId, "LOGIN_FAIL", "Inspector", inspector.getId().toString(), ip, userAgent, "bad_password");
            throw new AuthenticationException("Invalid credentials");
        }
        if (inspector.isMfaEnabled()) {
            if (otp == null || !totpService.verifyCode(inspector.getTotpSecret(), otp)) {
                auditService.log(normalizedId, "LOGIN_FAIL", "Inspector", inspector.getId().toString(), ip, userAgent, "bad_otp");
                throw new AuthenticationException("Invalid OTP");
            }
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "INSPECTOR");
        claims.put("name", inspector.getName());
        String token = jwtService.generateToken(inspector.getInspectorId(), claims);
        auditService.log(inspector.getInspectorId(), "LOGIN_SUCCESS", "Inspector", inspector.getId().toString(), ip, userAgent, null);
        return new LoginResponse(token, inspector.getInspectorId(), inspector.getName(), inspector.getEmail());
    }

    @Transactional
    public Inspector ensureBootstrapInspector(String inspectorId, String name, String email, String rawPassword) {
        return inspectorRepository.findByInspectorId(inspectorId)
                .orElseGet(() -> {
                    String secret = totpService.generateSecret();
                    Inspector ins = Inspector.builder()
                            .inspectorId(inspectorId)
                            .name(name)
                            .email(email)
                            .passwordHash(passwordEncoder.encode(rawPassword))
                            .totpSecret(secret)
                            .mfaEnabled(true)
                            .build();
                    return inspectorRepository.save(ins);
                });
    }
}
