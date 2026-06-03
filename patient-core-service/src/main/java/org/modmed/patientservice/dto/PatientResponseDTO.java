package org.modmed.patientservice.dto;

import java.time.LocalDateTime;

public record PatientResponseDTO(
        Long id,
        String firstName,
        String lastName,
        Integer age,
        String mobile,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
