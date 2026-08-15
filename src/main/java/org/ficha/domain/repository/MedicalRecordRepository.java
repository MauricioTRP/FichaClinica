package org.ficha.domain.repository;

import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.MedicalRecordId;
import org.ficha.domain.model.ids.PatientId;

import java.util.Optional;

public interface MedicalRecordRepository {
    Optional<MedicalRecord> findById(MedicalRecordId id);
    Optional<MedicalRecord> findByPatientId(PatientId patientId);
    boolean existsById(MedicalRecordId id);
    boolean existsByPatientId(PatientId patientId);
    void save(MedicalRecord medicalRecord);
}
