package org.modmed.appointmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.modmed.appointmentservice.entity.AppointmentStatus;

import java.time.LocalDate;

public record AppointmentRequestDTO(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        @NotBlank(message = "Doctor name is required")
        @Size(max = 150, message = "Doctor name must not exceed 150 characters")
        String doctorName,

        @NotNull(message = "Appointment date is required")
        LocalDate appointmentDate,

        // Nullable on create — defaults to BOOKED; used on update to change status
        AppointmentStatus status
) {}
