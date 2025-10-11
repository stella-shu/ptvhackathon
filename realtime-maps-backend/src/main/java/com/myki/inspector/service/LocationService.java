package com.myki.inspector.service;

import com.myki.inspector.dto.LocationUpdateDto;
import com.myki.inspector.entity.LocationUpdate;
import com.myki.inspector.repository.LocationUpdateRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LocationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final LocationUpdateRepository locationRepository;

    public LocationService(SimpMessagingTemplate messagingTemplate,
                           LocationUpdateRepository locationRepository) {
        this.messagingTemplate = messagingTemplate;
        this.locationRepository = locationRepository;
    }

    /** Create & broadcast a new location update. Also clears caches. */
    @CacheEvict(value = { "activeLocations", "heatmapData" }, allEntries = true)
    public LocationUpdateDto updateLocation(LocationUpdateDto in) {
        if (in == null) {
            throw new IllegalArgumentException("Payload is required");
        }

        String inspectorId = Optional.ofNullable(in.getInspectorId())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("inspectorId is required"));

        Double latRaw = in.getLatitude();
        Double lngRaw = in.getLongitude();
        if (latRaw == null || lngRaw == null) {
            throw new IllegalArgumentException("latitude and longitude are required");
        }
        double lat = latRaw;
        double lng = lngRaw;
        if (!Double.isFinite(lat) || lat < -90 || lat > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (!Double.isFinite(lng) || lng < -180 || lng > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }

        Double accuracy = in.getAccuracy();
        if (accuracy != null && (accuracy < 0 || !Double.isFinite(accuracy))) {
            accuracy = null;
        }

        LocationUpdate l = new LocationUpdate();
        l.setInspectorId(inspectorId);
        l.setLatitude(lat);
        l.setLongitude(lng);
        l.setAccuracy(accuracy);
        l.setActive(in.isActive());

        LocationUpdate saved = locationRepository.save(l);
        LocationUpdateDto out = LocationUpdateDto.fromEntity(saved);

        // WS broadcast
        messagingTemplate.convertAndSend("/topic/locations", out);

        // simple proximity alert
        checkProximityAlerts(out);

        return out;
    }

    /** Active locations (last 10 min). Cached per default window. */
    @Cacheable(value = "activeLocations", key = "'m10'")
    public List<LocationUpdateDto> getActiveLocations() {
        Instant cutoff = Instant.now().minus(10, ChronoUnit.MINUTES);
        return locationRepository.findActiveLocationsSince(cutoff).stream()
                .map(LocationUpdateDto::fromEntity).toList();
    }

    /** Active locations (last N min). Cached per N. */
    @Cacheable(value = "activeLocations", key = "#minutes")
    public List<LocationUpdateDto> getActiveLocations(int minutes) {
        Instant cutoff = Instant.now().minus(minutes, ChronoUnit.MINUTES);
        return locationRepository.findActiveLocationsSince(cutoff).stream()
                .map(LocationUpdateDto::fromEntity).toList();
    }

    public List<LocationUpdateDto> getInspectorLocationHistory(String inspectorId) {
        return locationRepository.findByInspectorIdOrderByCreatedAtDesc(inspectorId).stream()
                .map(LocationUpdateDto::fromEntity).toList();
    }

    public Optional<LocationUpdateDto> getLatestLocation(String inspectorId) {
        return locationRepository.findLatestByInspectorId(inspectorId).map(LocationUpdateDto::fromEntity);
    }

    @CacheEvict(value = { "activeLocations", "heatmapData" }, allEntries = true)
    public LocationUpdateDto setInspectorOffline(String inspectorId) {
        Optional<LocationUpdate> latest = locationRepository.findLatestByInspectorId(inspectorId);
        if (latest.isPresent()) {
            LocationUpdate l = latest.get();
            l.setActive(false);
            LocationUpdate saved = locationRepository.save(l);
            LocationUpdateDto dto = LocationUpdateDto.fromEntity(saved);
            messagingTemplate.convertAndSend("/topic/locations", dto);
            return dto;
        }
        return null;
    }

    /* ---------- proximity alerts ---------- */

    private void checkProximityAlerts(LocationUpdateDto src) {
        Instant cutoff = Instant.now().minus(5, ChronoUnit.MINUTES);
        var nearby = locationRepository.findNearbyLocations(src.getLatitude(), src.getLongitude(),
                0.1, cutoff); // 100m

        for (LocationUpdate n : nearby) {
            if (!n.getInspectorId().equals(src.getInspectorId())) {
                sendProximityAlert(src, LocationUpdateDto.fromEntity(n));
            }
        }
    }

    private void sendProximityAlert(LocationUpdateDto a, LocationUpdateDto b) {
        String msg = "Proximity Alert: %s and %s are within 100m".formatted(
                a.getInspectorName(), b.getInspectorName());
        messagingTemplate.convertAndSend("/topic/alerts/proximity", Map.of(
                "message", msg, "inspector1", a, "inspector2", b, "timestamp", Instant.now()
        ));
    }
}
