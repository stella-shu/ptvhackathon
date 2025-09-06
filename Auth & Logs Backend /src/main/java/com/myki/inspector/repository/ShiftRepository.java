package com.myki.inspector.repository;

import com.myki.inspector.entity.Shift;
import com.myki.inspector.entity.Inspector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    List<Shift> findByInspector(Inspector inspector);
    List<Shift> findByInspector_InspectorId(String inspectorId);
    List<Shift> findByStatus(String status);
    List<Shift> findByStartTimeBetween(Instant from, Instant to);
}

