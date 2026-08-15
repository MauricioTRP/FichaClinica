package org.ficha.application;

import org.ficha.domain.model.ClinicalEntry;
import org.ficha.domain.model.EntryContent;
import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.ActorId;
import org.ficha.domain.model.ids.CaregiverId;
import org.ficha.domain.model.ids.ClinicalEntryId;
import org.ficha.domain.model.ids.HealthcareWorkerId;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.model.source.ExternalSource;
import org.ficha.domain.repository.MedicalRecordRepository;

import java.util.Objects;

/**
 * Application boundary for medical-record commands.
 *
 * Each command loads the aggregate, delegates validation and mutation to it,
 * and saves the resulting state through the repository.
 */
public final class MedicalRecordApplicationService {
    private final MedicalRecordRepository repository;

    public MedicalRecordApplicationService(MedicalRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    public MedicalRecord createFor(PatientId patientId) {
        Objects.requireNonNull(patientId, "patientId must not be null");
        if (repository.existsByPatientId(patientId)) {
            throw new IllegalStateException(
                    "Patient already has a medical record: " + patientId.value()
            );
        }

        MedicalRecord medicalRecord = MedicalRecord.createFor(patientId);
        repository.save(medicalRecord);
        return medicalRecord;
    }

    public MedicalRecord getByPatientId(PatientId patientId) {
        Objects.requireNonNull(patientId, "patientId must not be null");
        return repository.findByPatientId(patientId)
                .orElseThrow(() -> new MedicalRecordNotFoundException(patientId));
    }

    public void grantCaregiverAccess(
            PatientId patientId,
            CaregiverId caregiverId
    ) {
        MedicalRecord medicalRecord = getByPatientId(patientId);
        medicalRecord.grantCaregiverAccess(patientId, caregiverId);
        repository.save(medicalRecord);
    }

    public void revokeCaregiverAccess(
            PatientId patientId,
            CaregiverId caregiverId
    ) {
        MedicalRecord medicalRecord = getByPatientId(patientId);
        medicalRecord.revokeCaregiverAccess(patientId, caregiverId);
        repository.save(medicalRecord);
    }

    public void shareWithHealthcareWorker(
            PatientId patientId,
            CaregiverId caregiverId,
            HealthcareWorkerId workerId
    ) {
        MedicalRecord medicalRecord = getByPatientId(patientId);
        medicalRecord.shareWithHealthcareWorker(caregiverId, workerId);
        repository.save(medicalRecord);
    }

    public ClinicalEntry addProfessionalEntry(
            PatientId patientId,
            HealthcareWorkerId workerId,
            EntryContent content
    ) {
        MedicalRecord medicalRecord = getByPatientId(patientId);
        ClinicalEntry entry = medicalRecord.addProfessionalEntry(workerId, content);
        repository.save(medicalRecord);
        return entry;
    }

    public ClinicalEntry addExternalEntry(
            PatientId patientId,
            ActorId uploaderId,
            ExternalSource source,
            EntryContent content
    ) {
        MedicalRecord medicalRecord = getByPatientId(patientId);
        ClinicalEntry entry = medicalRecord.addExternalEntry(uploaderId, source, content);
        repository.save(medicalRecord);
        return entry;
    }

    public ClinicalEntry amendEntry(
            PatientId patientId,
            HealthcareWorkerId workerId,
            ClinicalEntryId entryId,
            EntryContent content,
            String reason
    ) {
        MedicalRecord medicalRecord = getByPatientId(patientId);
        ClinicalEntry entry = medicalRecord.amendEntry(workerId, entryId, content, reason);
        repository.save(medicalRecord);
        return entry;
    }

    public void markSpecialNeeds(
            PatientId patientId,
            ActorId actorId,
            String notes
    ) {
        MedicalRecord medicalRecord = getByPatientId(patientId);
        medicalRecord.markSpecialNeeds(actorId, notes);
        repository.save(medicalRecord);
    }

    public void clearSpecialNeeds(
            PatientId patientId,
            ActorId actorId
    ) {
        MedicalRecord medicalRecord = getByPatientId(patientId);
        medicalRecord.clearSpecialNeeds(actorId);
        repository.save(medicalRecord);
    }
}
