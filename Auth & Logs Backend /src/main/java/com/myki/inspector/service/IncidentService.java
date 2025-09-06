package com.myki.inspector.service;

import com.myki.inspector.entity.Incident;
import com.myki.inspector.entity.Inspector;
import com.myki.inspector.repository.IncidentRepository;
import com.myki.inspector.repository.InspectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final InspectorRepository inspectorRepository;
    private final AuditService auditService;

    public Incident createIncident(String inspectorId, Incident incident) {
        Inspector inspector = inspectorRepository.findByInspectorId(inspectorId)
                .orElseThrow(() -> new RuntimeException("Inspector not found"));
        incident.setInspector(inspector);
        if (incident.getStatus() == null) incident.setStatus("OPEN");
        if (incident.getOccurredAt() == null) incident.setOccurredAt(Instant.now());
        Incident saved = incidentRepository.save(incident);
        auditService.log(inspectorId, "INCIDENT_CREATE", "Incident", saved.getId().toString(), null, null, null);
        return saved;
    }

    public Optional<Incident> get(UUID id) {
        return incidentRepository.findById(id);
    }

    public List<Incident> listAll() { return incidentRepository.findAll(); }

    public List<Incident> listMine(String inspectorId) { return incidentRepository.findByInspector_InspectorId(inspectorId); }

    public Incident updateIncident(String inspectorId, UUID id, Incident update) {
        Incident existing = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));
        if (update.getTitle() != null) existing.setTitle(update.getTitle());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getSeverity() != null) existing.setSeverity(update.getSeverity());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        if (update.getLatitude() != null) existing.setLatitude(update.getLatitude());
        if (update.getLongitude() != null) existing.setLongitude(update.getLongitude());
        if (update.getOccurredAt() != null) existing.setOccurredAt(update.getOccurredAt());
        Incident saved = incidentRepository.save(existing);
        auditService.log(inspectorId, "INCIDENT_UPDATE", "Incident", saved.getId().toString(), null, null, null);
        return saved;
    }
}

