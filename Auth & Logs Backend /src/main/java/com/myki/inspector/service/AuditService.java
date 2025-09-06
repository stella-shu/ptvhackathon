package com.myki.inspector.service;

import com.myki.inspector.entity.AuditLog;
import com.myki.inspector.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public void log(String actorInspectorId, String action, String entityType, String entityId, String ip, String userAgent, String metadata) {
        AuditLog log = AuditLog.builder()
                .actorInspectorId(actorInspectorId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(ip)
                .userAgent(userAgent)
                .timestamp(Instant.now())
                .metadata(metadata)
                .build();
        auditLogRepository.save(log);
    }
}

