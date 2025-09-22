package com.myki.inspector.controller;

import com.myki.inspector.service.HotspotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hotspots")
@CrossOrigin(origins = "*")
public class HotspotController {
    private final HotspotService hotspotService;

    public HotspotController(HotspotService hotspotService) {
        this.hotspotService = hotspotService;
    }

    /** Heatmap points with intensity (0..1) */
    @GetMapping("/heatmap")
    public List<Map<String, Object>> heatmap() {
        return hotspotService.getHeatmapData();
    }

    /** GeoJSON FeatureCollection snapshot (Points with intensity) */
    @GetMapping("/snapshot")
    public Map<String, Object> snapshot() {
        var pts = hotspotService.getHeatmapData();
        var features = pts.stream().map(p -> Map.of(
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
