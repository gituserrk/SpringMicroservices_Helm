package org.modmed.appointmentservice.service;

import org.modmed.appointmentservice.dto.AppointmentRequestDTO;
import org.modmed.appointmentservice.dto.AppointmentResponseDTO;

import java.util.List;

public interface AppointmentService {
    AppointmentResponseDTO createAppointment(AppointmentRequestDTO request);
    AppointmentResponseDTO getAppointmentById(Long id);
    List<AppointmentResponseDTO> getAllAppointments();
    AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO request);
    AppointmentResponseDTO cancelAppointment(Long id);
}
