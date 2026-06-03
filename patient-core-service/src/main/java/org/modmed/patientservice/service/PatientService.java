package org.modmed.patientservice.service;

import org.modmed.patientservice.dto.PatientRequestDTO;
import org.modmed.patientservice.dto.PatientResponseDTO;

import java.util.List;

public interface PatientService {
    PatientResponseDTO createPatient(PatientRequestDTO request);
    PatientResponseDTO getPatientById(Long id);
    List<PatientResponseDTO> getAllPatients();
    PatientResponseDTO updatePatient(Long id, PatientRequestDTO request);
    void deletePatient(Long id);
}
