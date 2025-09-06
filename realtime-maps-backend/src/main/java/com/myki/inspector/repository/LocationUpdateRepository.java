package com.myki.inspector.repository;

import com.myki.inspector.entity.LocationUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationUpdateRepository extends JpaRepository<LocationUpdate, Long> {

    @Query("SELECT l FROM LocationUpdate l WHERE l.inspectorId = ?1 ORDER BY l.createdAt DESC")
    List<LocationUpdate> findByInspectorIdOrderByCreatedAtDesc(String inspectorId);

    @Query("SELECT l FROM LocationUpdate l WHERE l.active = true AND l.createdAt > ?1 ORDER BY l.createdAt DESC")
    List<LocationUpdate> findActiveLocationsSince(Instant since);

    @Query(value = "SELECT * FROM location_updates WHERE inspector_id = ?1 ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<LocationUpdate> findLatestByInspectorId(String inspectorId);

    // Find nearby inspectors within radius (in kilometers)
    @Query(value = """
        SELECT * FROM location_updates l1 
        WHERE l1.is_active = true 
        AND l1.created_at > ?4
        AND (6371 * acos(cos(radians(?1)) * cos(radians(l1.latitude)) * 
             cos(radians(l1.longitude) - radians(?2)) + 
             sin(radians(?1)) * sin(radians(l1.latitude)))) < ?3
        """, nativeQuery = true)
    List<LocationUpdate> findNearbyLocations(double latitude, double longitude, double radiusKm, Instant since);
}