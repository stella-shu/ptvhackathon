package com.myki.inspector.config;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleMapsConfig {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(value = "google.maps.api-key")
    public GeoApiContext geoApiContext(@Value("${google.maps.api-key}") String apiKey) {
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .queryRateLimit(10)
                .build();
    }
}