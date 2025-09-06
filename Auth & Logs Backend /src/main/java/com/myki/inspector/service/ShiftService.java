package com.myki.inspector.service;

import com.myki.inspector.entity.Inspector;
import com.myki.inspector.entity.Shift;
import com.myki.inspector.repository.InspectorRepository;
import com.myki.inspector.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftService {
    private final ShiftRepository shiftRepository;
    private final InspectorRepository inspectorRepository;
    private final AuditService auditService;

    public Shift createShift(String inspectorId, Shift shift) {
        Inspector inspector = inspectorRepository.findByInspectorId(inspectorId)
                .orElseThrow(() -> new RuntimeException("Inspector not found"));
        shift.setInspector(inspector);
        if (shift.getStartTime() == null) shift.setStartTime(Instant.now());
        if (shift.getStatus() == null) shift.setStatus("OPEN");
        Shift saved = shiftRepository.save(shift);
        auditService.log(inspectorId, "SHIFT_CREATE", "Shift", saved.getId().toString(), null, null, null);
        return saved;
    }

    public Optional<Shift> get(UUID id) { return shiftRepository.findById(id); }
    public List<Shift> listMine(String inspectorId) { return shiftRepository.findByInspector_InspectorId(inspectorId); }
    public List<Shift> listAll() { return shiftRepository.findAll(); }

    public Shift updateShift(String inspectorId, UUID id, Shift update) {
        Shift existing = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        if (update.getStartTime() != null) existing.setStartTime(update.getStartTime());
        if (update.getEndTime() != null) existing.setEndTime(update.getEndTime());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        if (update.getLocation() != null) existing.setLocation(update.getLocation());
        if (update.getNotes() != null) existing.setNotes(update.getNotes());
        Shift saved = shiftRepository.save(existing);
        auditService.log(inspectorId, "SHIFT_UPDATE", "Shift", saved.getId().toString(), null, null, null);
        return saved;
    }
}

