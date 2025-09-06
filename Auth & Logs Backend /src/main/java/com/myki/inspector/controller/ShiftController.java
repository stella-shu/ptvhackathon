package com.myki.inspector.controller;

import com.myki.inspector.dto.ShiftDto;
import com.myki.inspector.entity.Shift;
import com.myki.inspector.service.ShiftService;
import com.myki.inspector.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {
    private final ShiftService shiftService;

    @PostMapping
    public ResponseEntity<Shift> create(@Valid @RequestBody ShiftDto body) {
        String inspectorId = SecurityUtils.currentInspectorId();
        Shift shift = new Shift();
        shift.setStartTime(body.getStartTime());
        shift.setEndTime(body.getEndTime());
        shift.setStatus(body.getStatus());
        shift.setLocation(body.getLocation());
        shift.setNotes(body.getNotes());
        Shift saved = shiftService.createShift(inspectorId, shift);
        return ResponseEntity.created(URI.create("/api/shifts/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Shift> update(@PathVariable UUID id, @RequestBody ShiftDto body) {
        String inspectorId = SecurityUtils.currentInspectorId();
        Shift update = new Shift();
        update.setStartTime(body.getStartTime());
        update.setEndTime(body.getEndTime());
        update.setStatus(body.getStatus());
        update.setLocation(body.getLocation());
        update.setNotes(body.getNotes());
        return ResponseEntity.ok(shiftService.updateShift(inspectorId, id, update));
    }

    @GetMapping
    public ResponseEntity<List<Shift>> list(@RequestParam(value = "mine", required = false) Boolean mine) {
        if (Boolean.TRUE.equals(mine)) {
            return ResponseEntity.ok(shiftService.listMine(SecurityUtils.currentInspectorId()));
        }
        return ResponseEntity.ok(shiftService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shift> get(@PathVariable UUID id) {
        return shiftService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}

