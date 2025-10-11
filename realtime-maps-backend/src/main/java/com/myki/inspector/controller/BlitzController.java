package com.myki.inspector.controller;

import com.myki.inspector.dto.BlitzMarkerDto;
import com.myki.inspector.service.BlitzService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blitz")
@CrossOrigin(origins = "*")
public class BlitzController {
    private final BlitzService service;
    public BlitzController(BlitzService service) { this.service = service; }

    @PostMapping
    public BlitzMarkerDto create(@Valid @RequestBody BlitzMarkerDto dto) {
        return service.create(dto);
    }

    @GetMapping("/active")
    public List<BlitzMarkerDto> active() {
        return service.listActive();
    }

    @PostMapping("/{id}/close")
    public BlitzMarkerDto close(@PathVariable Long id) {
        return service.close(id);
    }
}
