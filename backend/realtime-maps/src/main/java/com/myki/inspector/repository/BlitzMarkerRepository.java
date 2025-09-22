package com.myki.inspector.repository;

import com.myki.inspector.entity.BlitzMarker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface BlitzMarkerRepository extends JpaRepository<BlitzMarker, Long> {
    List<BlitzMarker> findByActiveTrue();

    @Query("SELECT b FROM BlitzMarker b WHERE b.active = true ORDER BY b.createdAt DESC")
    List<BlitzMarker> findActiveOrderByCreatedAtDesc();

    @Query("SELECT b FROM BlitzMarker b WHERE b.active = true AND (b.scheduledEnd IS NULL OR b.scheduledEnd > ?1)")
    List<BlitzMarker> findCurrentlyValid(Instant now);
}
