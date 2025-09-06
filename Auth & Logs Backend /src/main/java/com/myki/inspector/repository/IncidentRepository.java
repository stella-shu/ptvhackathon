package com.myki.inspector.repository;

import com.myki.inspector.entity.Incident;
import com.myki.inspector.entity.Inspector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
    List<Incident> findByInspector(Inspector inspector);
    List<Incident> findByInspector_InspectorId(String inspectorId);
    List<Incident> findByStatus(String status);
    List<Incident> findByCreatedAtBetween(Instant from, Instant to);
}

