package org.modmed.appointmentservice.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modmed.appointmentservice.dto.AppointmentRequestDTO;
import org.modmed.appointmentservice.dto.AppointmentResponseDTO;
import org.modmed.appointmentservice.dto.PatientDTO;
import org.modmed.appointmentservice.entity.Appointment;
import org.modmed.appointmentservice.entity.AppointmentStatus;
import org.modmed.appointmentservice.exception.AppointmentNotFoundException;
import org.modmed.appointmentservice.exception.PatientNotFoundException;
import org.modmed.appointmentservice.feign.PatientClient;
import org.modmed.appointmentservice.repository.AppointmentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientClient patientClient;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Appointment appointment;
    private AppointmentRequestDTO requestDTO;
    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setPatientId(1L);
        appointment.setDoctorName("Dr. Smith");
        appointment.setAppointmentDate(LocalDate.of(2026, 12, 15));
        appointment.setStatus(AppointmentStatus.BOOKED);

        requestDTO = new AppointmentRequestDTO(1L, "Dr. Smith", LocalDate.of(2026, 12, 15), null);
        patientDTO = new PatientDTO(1L, "John", "Doe", 35, "9876543210", "john@example.com", null, null);
    }

    @Test
    void createAppointment_whenPatientExists_shouldCreateWithBookedStatus() {
        when(patientClient.getPatientById(1L)).thenReturn(patientDTO);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponseDTO result = appointmentService.createAppointment(requestDTO);

        assertThat(result.patientId()).isEqualTo(1L);
        assertThat(result.doctorName()).isEqualTo("Dr. Smith");
        assertThat(result.status()).isEqualTo(AppointmentStatus.BOOKED);
        verify(patientClient).getPatientById(1L);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void createAppointment_whenPatientNotFound_shouldThrowPatientNotFoundException() {
        FeignException mockFeign = mock(FeignException.class);
        when(mockFeign.status()).thenReturn(404);
        when(patientClient.getPatientById(99L)).thenThrow(mockFeign);

        AppointmentRequestDTO badRequest = new AppointmentRequestDTO(99L, "Dr. Smith", LocalDate.of(2026, 12, 15), null);

        assertThatThrownBy(() -> appointmentService.createAppointment(badRequest))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createAppointment_whenPatientServiceUnavailable_shouldThrowRuntimeException() {
        FeignException mockFeign = mock(FeignException.class);
        when(mockFeign.status()).thenReturn(503);
        when(patientClient.getPatientById(1L)).thenThrow(mockFeign);

        assertThatThrownBy(() -> appointmentService.createAppointment(requestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    void getAppointmentById_whenExists_shouldReturnAppointment() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        AppointmentResponseDTO result = appointmentService.getAppointmentById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.doctorName()).isEqualTo("Dr. Smith");
    }

    @Test
    void getAppointmentById_whenNotFound_shouldThrowException() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(99L))
                .isInstanceOf(AppointmentNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllAppointments_shouldReturnList() {
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment));

        List<AppointmentResponseDTO> result = appointmentService.getAllAppointments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).doctorName()).isEqualTo("Dr. Smith");
    }

    @Test
    void updateAppointment_whenSamePatient_shouldNotCallPatientService() {
        AppointmentRequestDTO updateRequest = new AppointmentRequestDTO(1L, "Dr. Jones", LocalDate.of(2026, 12, 20), AppointmentStatus.COMPLETED);
        Appointment updatedAppointment = new Appointment();
        updatedAppointment.setId(1L);
        updatedAppointment.setPatientId(1L);
        updatedAppointment.setDoctorName("Dr. Jones");
        updatedAppointment.setAppointmentDate(LocalDate.of(2026, 12, 20));
        updatedAppointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(updatedAppointment);

        AppointmentResponseDTO result = appointmentService.updateAppointment(1L, updateRequest);

        assertThat(result.doctorName()).isEqualTo("Dr. Jones");
        assertThat(result.status()).isEqualTo(AppointmentStatus.COMPLETED);
        verifyNoInteractions(patientClient);
    }

    @Test
    void updateAppointment_whenNotFound_shouldThrowException() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.updateAppointment(99L, requestDTO))
                .isInstanceOf(AppointmentNotFoundException.class);
    }

    @Test
    void cancelAppointment_whenExists_shouldSetStatusCancelled() {
        Appointment cancelled = new Appointment();
        cancelled.setId(1L);
        cancelled.setPatientId(1L);
        cancelled.setDoctorName("Dr. Smith");
        cancelled.setAppointmentDate(LocalDate.of(2026, 12, 15));
        cancelled.setStatus(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(cancelled);

        AppointmentResponseDTO result = appointmentService.cancelAppointment(1L);

        assertThat(result.status()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void cancelAppointment_whenNotFound_shouldThrowException() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.cancelAppointment(99L))
                .isInstanceOf(AppointmentNotFoundException.class);
    }
}
