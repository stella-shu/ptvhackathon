package com.myki.inspector.service;

import com.myki.inspector.entity.Inspector;
import com.myki.inspector.repository.InspectorRepository;
import com.myki.inspector.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public String login(String inspectorId, String password, Integer otp, String ip, String userAgent) {
        Optional<Inspector> inspectorOpt = inspectorRepository.findByInspectorId(inspectorId);
        if (inspectorOpt.isEmpty()) {
            auditService.log(inspectorId, "LOGIN_FAIL", "Inspector", null, ip, userAgent, "not_found");
            throw new RuntimeException("Invalid credentials");
        }
        Inspector inspector = inspectorOpt.get();
        if (!passwordEncoder.matches(password, inspector.getPasswordHash())) {
            auditService.log(inspectorId, "LOGIN_FAIL", "Inspector", inspector.getId().toString(), ip, userAgent, "bad_password");
            throw new RuntimeException("Invalid credentials");
        }
        if (inspector.isMfaEnabled()) {
            if (otp == null || !totpService.verifyCode(inspector.getTotpSecret(), otp)) {
                auditService.log(inspectorId, "LOGIN_FAIL", "Inspector", inspector.getId().toString(), ip, userAgent, "bad_otp");
                throw new RuntimeException("Invalid OTP");
            }
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "INSPECTOR");
        claims.put("name", inspector.getName());
        String token = jwtService.generateToken(inspector.getInspectorId(), claims);
        auditService.log(inspector.getInspectorId(), "LOGIN_SUCCESS", "Inspector", inspector.getId().toString(), ip, userAgent, null);
        return token;
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

