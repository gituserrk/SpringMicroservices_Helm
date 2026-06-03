package org.modmed.appointmentservice.dto;

import org.modmed.appointmentservice.entity.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        Long patientId,
        String doctorName,
        LocalDate appointmentDate,
        AppointmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
