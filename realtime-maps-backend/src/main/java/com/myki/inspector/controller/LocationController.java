package com.myki.inspector.controller;

import com.myki.inspector.dto.LocationUpdateDto;
import com.myki.inspector.service.LocationService;
import com.myki.inspector.service.HotspotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/location")
@CrossOrigin(origins = "*")
class LocationRestController {

    private final LocationService locationService;
    private final HotspotService hotspotService;

    public LocationRestController(LocationService locationService, HotspotService hotspotService) {
        this.locationService = locationService;
        this.hotspotService = hotspotService;
    }

    @PostMapping("/update")
    public ResponseEntity<LocationUpdateDto> update(@Valid @RequestBody LocationUpdateDto body) {
        LocationUpdateDto result = locationService.updateLocation(body);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    public List<LocationUpdateDto> getActive(@RequestParam(name = "minutes", required = false) Integer minutes) {
        if (minutes == null) return locationService.getActiveLocations();
        return locationService.getActiveLocations(minutes);
    }

    @GetMapping("/history/{inspectorId}")
    public List<LocationUpdateDto> history(@PathVariable String inspectorId) {
        return locationService.getInspectorLocationHistory(inspectorId);
    }

    @GetMapping("/snapshot")
    public Map<String, Object> getLocationSnapshot() {
        var points = hotspotService.getHeatmapData(); // [{lat, lng, intensity}, ...]
        var features = points.stream().map(p -> Map.of(
                "type", "Feature",
                "geometry", Map.of(
                        "type", "Point",
                        "coordinates", List.of(p.get("lng"), p.get("lat"))
                ),
                "properties", Map.of("intensity", p.get("intensity"))
        )).collect(Collectors.toList());

        return Map.of("type", "FeatureCollection", "features", features);
    }
}
