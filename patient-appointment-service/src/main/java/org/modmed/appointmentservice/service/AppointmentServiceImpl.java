package org.modmed.appointmentservice.service;

import feign.FeignException;
import org.modmed.appointmentservice.dto.AppointmentRequestDTO;
import org.modmed.appointmentservice.dto.AppointmentResponseDTO;
import org.modmed.appointmentservice.entity.Appointment;
import org.modmed.appointmentservice.entity.AppointmentStatus;
import org.modmed.appointmentservice.exception.AppointmentNotFoundException;
import org.modmed.appointmentservice.exception.PatientNotFoundException;
import org.modmed.appointmentservice.feign.PatientClient;
import org.modmed.appointmentservice.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientClient patientClient;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, PatientClient patientClient) {
        this.appointmentRepository = appointmentRepository;
        this.patientClient = patientClient;
    }

    @Override
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request) {
        validatePatientExists(request.patientId());

        Appointment appointment = new Appointment();
        appointment.setPatientId(request.patientId());
        appointment.setDoctorName(request.doctorName());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStatus(request.status() != null ? request.status() : AppointmentStatus.BOOKED);

        return toResponseDTO(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        // Re-validate patient only if patientId is being changed
        if (!appointment.getPatientId().equals(request.patientId())) {
            validatePatientExists(request.patientId());
        }

        appointment.setPatientId(request.patientId());
        appointment.setDoctorName(request.doctorName());
        appointment.setAppointmentDate(request.appointmentDate());
        if (request.status() != null) {
            appointment.setStatus(request.status());
        }

        return toResponseDTO(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponseDTO cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponseDTO(appointmentRepository.save(appointment));
    }

    private void validatePatientExists(Long patientId) {
        try {
            patientClient.getPatientById(patientId);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new PatientNotFoundException(patientId);
            }
            throw new RuntimeException("PatientCore Service unavailable: " + e.getMessage(), e);
        }
    }

    private AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorName(),
                appointment.getAppointmentDate(),
                appointment.getStatus(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
