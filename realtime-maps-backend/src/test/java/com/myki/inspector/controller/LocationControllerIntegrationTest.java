package com.myki.inspector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myki.inspector.entity.LocationUpdate;
import com.myki.inspector.repository.LocationUpdateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
class LocationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LocationUpdateRepository locationUpdateRepository;

    @AfterEach
    void cleanUp() {
        locationUpdateRepository.deleteAll();
    }

    @Test
    void updateLocation_withValidPayload_returnsSavedDto() throws Exception {
        var payload = new LocationPayload("INS007", -37.8136, 144.9631, true, null);

        mockMvc.perform(post("/api/location/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inspectorId").value("INS007"))
                .andExpect(jsonPath("$.latitude").value(-37.8136))
                .andExpect(jsonPath("$.longitude").value(144.9631))
                .andExpect(jsonPath("$.active").value(true));

        List<LocationUpdate> all = locationUpdateRepository.findAll();
        assertThat(all).hasSize(1);
        LocationUpdate saved = all.get(0);
        assertThat(saved.getInspectorId()).isEqualTo("INS007");
        assertThat(saved.getLatitude()).isEqualTo(-37.8136);
    }

    @Test
    void updateLocation_withInvalidLatitude_returnsBadRequest() throws Exception {
        var payload = new LocationPayload("INS100", 123.0, 10.0, true, null);

        mockMvc.perform(post("/api/location/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"))
                .andExpect(jsonPath("$.details[0]").value("latitude: latitude must be <= 90"));
    }

    private record LocationPayload(String inspectorId, Double latitude, Double longitude, boolean active, Double accuracy) {}

    @TestConfiguration
    static class MessagingTemplateConfig {
        @Bean
        @Primary
        SimpMessagingTemplate brokerMessagingTemplate() {
            return new SimpMessagingTemplate(new ExecutorSubscribableChannel());
        }
    }
}
