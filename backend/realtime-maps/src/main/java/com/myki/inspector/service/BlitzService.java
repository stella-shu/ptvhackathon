package com.myki.inspector.service;

import com.myki.inspector.dto.BlitzMarkerDto;
import com.myki.inspector.entity.BlitzMarker;
import com.myki.inspector.repository.BlitzMarkerRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
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
        BlitzMarker m = new BlitzMarker();
        m.setInspectorId(in.getInspectorId());
        m.setLatitude(in.getLatitude());
        m.setLongitude(in.getLongitude());
        m.setDescription(in.getDescription());
        m.setBlitzType(in.getBlitzType());
        m.setScheduledEnd(in.getScheduledEnd());

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
        BlitzMarker m = repo.findById(id).orElseThrow();
        m.setActive(false);
        BlitzMarker saved = repo.save(m);
        BlitzMarkerDto dto = BlitzMarkerDto.fromEntity(saved);
        bus.convertAndSend("/topic/blitz/updated", dto);
        return dto;
    }
}
