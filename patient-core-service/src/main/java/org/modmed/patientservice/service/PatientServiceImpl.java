package org.modmed.patientservice.service;

import org.modmed.patientservice.dto.PatientRequestDTO;
import org.modmed.patientservice.dto.PatientResponseDTO;
import org.modmed.patientservice.entity.Patient;
import org.modmed.patientservice.exception.PatientNotFoundException;
import org.modmed.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public PatientResponseDTO createPatient(PatientRequestDTO request) {
        Patient patient = new Patient();
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setAge(request.age());
        patient.setMobile(request.mobile());
        patient.setEmail(request.email());
        return toResponseDTO(patientRepository.save(patient));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id) {
        return patientRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public PatientResponseDTO updatePatient(Long id, PatientRequestDTO request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setAge(request.age());
        patient.setMobile(request.mobile());
        patient.setEmail(request.email());
        return toResponseDTO(patientRepository.save(patient));
    }

    @Override
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        patientRepository.deleteById(id);
    }

    private PatientResponseDTO toResponseDTO(Patient patient) {
        return new PatientResponseDTO(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getAge(),
                patient.getMobile(),
                patient.getEmail(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }
}
