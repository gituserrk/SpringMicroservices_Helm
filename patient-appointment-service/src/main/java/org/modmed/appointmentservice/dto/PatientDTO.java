package org.modmed.appointmentservice.dto;

import java.time.LocalDateTime;

/**
 * Maps the response from PatientCore Service's GET /api/patients/{id} endpoint.
 */
public record PatientDTO(
        Long id,
        String firstName,
        String lastName,
        Integer age,
        String mobile,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
