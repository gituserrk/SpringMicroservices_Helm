package org.modmed.patientservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modmed.patientservice.dto.PatientRequestDTO;
import org.modmed.patientservice.dto.PatientResponseDTO;
import org.modmed.patientservice.entity.Patient;
import org.modmed.patientservice.exception.PatientNotFoundException;
import org.modmed.patientservice.repository.PatientRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient patient;
    private PatientRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        patient = new Patient(1L, "John", "Doe", 35, "9876543210", "john@example.com");
        requestDTO = new PatientRequestDTO("John", "Doe", 35, "9876543210", "john@example.com");
    }

    @Test
    void createPatient_shouldReturnCreatedPatient() {
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientResponseDTO result = patientService.createPatient(requestDTO);

        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.email()).isEqualTo("john@example.com");
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void getPatientById_whenExists_shouldReturnPatient() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        PatientResponseDTO result = patientService.getPatientById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("John");
    }

    @Test
    void getPatientById_whenNotFound_shouldThrowException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(99L))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllPatients_shouldReturnList() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        List<PatientResponseDTO> result = patientService.getAllPatients();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).firstName()).isEqualTo("John");
    }

    @Test
    void updatePatient_whenExists_shouldReturnUpdatedPatient() {
        PatientRequestDTO updateRequest = new PatientRequestDTO("Jane", "Doe", 30, "9876543210", "jane@example.com");
        Patient updatedPatient = new Patient(1L, "Jane", "Doe", 30, "9876543210", "jane@example.com");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);

        PatientResponseDTO result = patientService.updatePatient(1L, updateRequest);

        assertThat(result.firstName()).isEqualTo("Jane");
        assertThat(result.email()).isEqualTo("jane@example.com");
    }

    @Test
    void updatePatient_whenNotFound_shouldThrowException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.updatePatient(99L, requestDTO))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deletePatient_whenExists_shouldDelete() {
        when(patientRepository.existsById(1L)).thenReturn(true);
        doNothing().when(patientRepository).deleteById(1L);

        assertThatCode(() -> patientService.deletePatient(1L)).doesNotThrowAnyException();
        verify(patientRepository).deleteById(1L);
    }

    @Test
    void deletePatient_whenNotFound_shouldThrowException() {
        when(patientRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> patientService.deletePatient(99L))
                .isInstanceOf(PatientNotFoundException.class)
                .hasMessageContaining("99");
    }
}
