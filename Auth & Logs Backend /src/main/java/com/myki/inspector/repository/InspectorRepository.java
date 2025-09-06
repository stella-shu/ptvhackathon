package com.myki.inspector.repository;

import com.myki.inspector.entity.Inspector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InspectorRepository extends JpaRepository<Inspector, UUID> {
    Optional<Inspector> findByInspectorId(String inspectorId);
    Optional<Inspector> findByEmail(String email);
    boolean existsByInspectorId(String inspectorId);
}

