package com.myki.inspector.config;

import com.myki.inspector.entity.Inspector;
import com.myki.inspector.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BootstrapConfig {

    private final AuthService authService;

    @Bean
    @Profile({"default","dev"})
    public CommandLineRunner bootstrapAdmin() {
        return args -> {
            Inspector admin = authService.ensureBootstrapInspector(
                    "INSPECTOR1",
                    "Admin Inspector",
                    "admin@example.com",
                    "ChangeMe123!"
            );
            log.info("Bootstrap inspector created/ensured. InspectorId=INSPECTOR1 Password=ChangeMe123! (OTP disabled)");
        };
    }
}
