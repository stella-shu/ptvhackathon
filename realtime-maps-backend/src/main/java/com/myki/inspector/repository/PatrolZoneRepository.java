package com.myki.inspector.repository;

import com.myki.inspector.entity.PatrolZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatrolZoneRepository extends JpaRepository<PatrolZone, Long> {

    List<PatrolZone> findByActiveTrue();

    List<PatrolZone> findByZoneTypeAndActiveTrue(String zoneType);

    @Query("SELECT p FROM PatrolZone p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<PatrolZone> findAllActiveOrderByCreatedAtDesc();
}