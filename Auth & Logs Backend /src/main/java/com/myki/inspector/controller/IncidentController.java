package com.myki.inspector.controller;

import com.myki.inspector.dto.IncidentDto;
import com.myki.inspector.entity.Incident;
import com.myki.inspector.service.IncidentService;
import com.myki.inspector.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {
    private final IncidentService incidentService;

    @PostMapping
    public ResponseEntity<Incident> create(@Valid @RequestBody IncidentDto body) {
        String inspectorId = SecurityUtils.currentInspectorId();
        Incident incident = new Incident();
        incident.setTitle(body.getTitle());
        incident.setDescription(body.getDescription());
        incident.setSeverity(body.getSeverity());
        incident.setStatus(body.getStatus());
        incident.setLatitude(body.getLatitude());
        incident.setLongitude(body.getLongitude());
        incident.setOccurredAt(body.getOccurredAt());
        Incident saved = incidentService.createIncident(inspectorId, incident);
        return ResponseEntity.created(URI.create("/api/incidents/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Incident> update(@PathVariable UUID id, @Valid @RequestBody IncidentDto body) {
        String inspectorId = SecurityUtils.currentInspectorId();
        Incident update = new Incident();
        update.setTitle(body.getTitle());
        update.setDescription(body.getDescription());
        update.setSeverity(body.getSeverity());
        update.setStatus(body.getStatus());
        update.setLatitude(body.getLatitude());
        update.setLongitude(body.getLongitude());
        update.setOccurredAt(body.getOccurredAt());
        return ResponseEntity.ok(incidentService.updateIncident(inspectorId, id, update));
    }

    @GetMapping
    public ResponseEntity<List<Incident>> list(@RequestParam(value = "mine", required = false) Boolean mine) {
        if (Boolean.TRUE.equals(mine)) {
            return ResponseEntity.ok(incidentService.listMine(SecurityUtils.currentInspectorId()));
        }
        return ResponseEntity.ok(incidentService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Incident> get(@PathVariable UUID id) {
        return incidentService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
