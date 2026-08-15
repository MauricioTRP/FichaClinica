package org.ficha.infrastructure.persistence;

import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.MedicalRecordId;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.repository.MedicalRecordRepository;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryMedicalRecordRepository implements MedicalRecordRepository {
    private final Map<MedicalRecordId, MedicalRecord> byId = new ConcurrentHashMap<>();
    private final Map<PatientId, MedicalRecordId> idByPatient = new ConcurrentHashMap<>();

    @Override
    public Optional<MedicalRecord> findById(MedicalRecordId id) {
        Objects.requireNonNull(id, "MedicalRecordId cannot be null");
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<MedicalRecord> findByPatientId(PatientId patientId) {
        Objects.requireNonNull(patientId, "PatientId cannot be null");
        MedicalRecordId id = idByPatient.get(patientId);
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public boolean existsById(MedicalRecordId id) {
        Objects.requireNonNull(id, "MedicalRecordId cannot be null");
        return byId.containsKey(id);
    }

    @Override
    public boolean existsByPatientId(PatientId patientId) {
        Objects.requireNonNull(patientId, "PatientId cannot be null");
        return idByPatient.containsKey(patientId);
    }

    @Override
    public synchronized void save(MedicalRecord medicalRecord) {
        Objects.requireNonNull(medicalRecord, "medicalRecord must not be null");

        MedicalRecordId id = medicalRecord.id();
        PatientId patientId = medicalRecord.patientId();

        MedicalRecordId existingForPatient = idByPatient.get(patientId);
        if (existingForPatient != null && !existingForPatient.equals(id)) {
            throw new IllegalStateException(
                    "Patient already has a medical record: " + patientId.value()
            );
        }

        MedicalRecord existingForId = byId.get(id);
        if (existingForId != null && !existingForId.patientId().equals(patientId)) {
            throw new IllegalStateException(
                    "Medical record id already belongs to another patient: " + id.value()
            );
        }

        byId.put(id, medicalRecord);
        idByPatient.put(patientId, id);
    }
}
