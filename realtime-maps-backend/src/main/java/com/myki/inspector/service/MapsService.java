package com.myki.inspector.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

@Service
public class MapsService {

    @Autowired(required = false) // Optional since it depends on API key being set
    private GeoApiContext geoApiContext;

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
        return Map.of(
                "center", Map.of("lat", -37.8136, "lng", 144.9631), // Melbourne CBD
                "zoom", 13,
                "mapType", "roadmap",
                "apiAvailable", geoApiContext != null
        );
    }
}