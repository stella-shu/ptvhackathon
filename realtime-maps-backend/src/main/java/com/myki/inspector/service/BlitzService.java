package com.myki.inspector.service;

import com.myki.inspector.dto.BlitzMarkerDto;
import com.myki.inspector.entity.BlitzMarker;
import com.myki.inspector.repository.BlitzMarkerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class BlitzService {
    private final BlitzMarkerRepository repo;
    private final SimpMessagingTemplate bus;

    public BlitzService(BlitzMarkerRepository repo, SimpMessagingTemplate bus) {
        this.repo = repo;
        this.bus = bus;
    }

    public BlitzMarkerDto create(BlitzMarkerDto in) {
        String inspectorId = StringUtils.hasText(in.getInspectorId()) ? in.getInspectorId().trim() : "anonymous";
        Double latRaw = in.getLatitude();
        Double lngRaw = in.getLongitude();
        if (latRaw == null || !Double.isFinite(latRaw) || latRaw < -90 || latRaw > 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "latitude must be between -90 and 90");
        }
        if (lngRaw == null || !Double.isFinite(lngRaw) || lngRaw < -180 || lngRaw > 180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "longitude must be between -180 and 180");
        }

        BlitzMarker m = new BlitzMarker();
        m.setInspectorId(inspectorId);
        m.setLatitude(latRaw);
        m.setLongitude(lngRaw);
        m.setDescription(in.getDescription());
        m.setBlitzType(in.getBlitzType());
        Instant scheduledEnd = in.getScheduledEnd();
        if (scheduledEnd != null && scheduledEnd.isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scheduledEnd cannot be in the past");
        }
        m.setScheduledEnd(scheduledEnd);

        BlitzMarker saved = repo.save(m);
        BlitzMarkerDto out = BlitzMarkerDto.fromEntity(saved);

        // Broadcast to all connected clients
        bus.convertAndSend("/topic/blitz/created", out);
        return out;
    }

    public List<BlitzMarkerDto> listActive() {
        return repo.findCurrentlyValid(Instant.now())
                .stream().map(BlitzMarkerDto::fromEntity).toList();
    }

    public BlitzMarkerDto close(Long id) {
        BlitzMarker m = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blitz marker not found"));
        m.setActive(false);
        BlitzMarker saved = repo.save(m);
        BlitzMarkerDto dto = BlitzMarkerDto.fromEntity(saved);
        bus.convertAndSend("/topic/blitz/updated", dto);
        return dto;
    }
}
