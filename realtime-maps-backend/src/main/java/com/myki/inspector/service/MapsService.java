package com.myki.inspector.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MapsService {

    @Autowired(required = false) // Optional since it depends on API key being set
    private GeoApiContext geoApiContext;

    @Value("${google.maps.browser-api-key:}")
    private String browserApiKey;

    public Optional<LatLng> geocodeAddress(String address) {
        if (geoApiContext == null) {
            return Optional.empty(); // No API key configured
        }

        try {
            GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, address).await();
            if (results.length > 0) {
                return Optional.of(results[0].geometry.location);
            }
        } catch (Exception e) {
            System.err.println("Geocoding failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    public String reverseGeocode(double lat, double lng) {
        if (geoApiContext == null) {
            return String.format("%.4f, %.4f", lat, lng); // Fallback to coordinates
        }

        try {
            LatLng location = new LatLng(lat, lng);
            GeocodingResult[] results = GeocodingApi.reverseGeocode(geoApiContext, location).await();
            if (results.length > 0) {
                return results[0].formattedAddress;
            }
        } catch (Exception e) {
            System.err.println("Reverse geocoding failed: " + e.getMessage());
        }
        return String.format("%.4f, %.4f", lat, lng);
    }

    public Map<String, Object> getMapConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("center", Map.of("lat", -37.8136, "lng", 144.9631)); // Melbourne CBD
        config.put("zoom", 13);
        config.put("mapType", "roadmap");
        config.put("apiAvailable", geoApiContext != null);
        if (StringUtils.hasText(browserApiKey)) {
            config.put("apiKey", browserApiKey.trim());
        }
        return config;
    }
}
