package com.myki.inspector.controller;

import com.google.maps.model.LatLng;
import com.myki.inspector.service.MapsService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/maps")
@CrossOrigin(origins = "*")
public class MapsController {

    private final MapsService mapsService;

    public MapsController(MapsService mapsService) {
        this.mapsService = mapsService;
    }

    @GetMapping("/config")
    public Map<String, Object> getMapConfig() {
        return mapsService.getMapConfig();
    }

    @GetMapping("/reverse")
    public Map<String, Object> reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        String address = mapsService.reverseGeocode(lat, lon);

        return Map.of(
                "success", true,
                "address", address,
                "latitude", lat,
                "longitude", lon
        );
    }

    @PostMapping("/geocode")
    public Map<String, Object> geocodeAddress(@RequestBody Map<String, String> request) {
        String address = request.get("address");
        Optional<LatLng> result = mapsService.geocodeAddress(address);

        if (result.isPresent()) {
            LatLng location = result.get();
            return Map.of(
                    "success", true,
                    "latitude", location.lat,
                    "longitude", location.lng,
                    "address", address
            );
        } else {
            return Map.of(
                    "success", false,
                    "error", "Could not geocode address",
                    "fallback", "Check API key configuration"
            );
        }
    }
}