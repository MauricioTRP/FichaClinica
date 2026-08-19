package org.ficha.application.usecase;

import org.ficha.domain.model.ClinicalEntry;
import org.ficha.domain.model.EntryContent;
import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.HealthcareWorkerId;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.repository.MedicalRecordRepository;

import java.util.Objects;

public final class AddProfessionalEntryUseCase
        implements UseCase<AddProfessionalEntryUseCase.Request, ClinicalEntry> {
    private final MedicalRecordRepository repository;

    public AddProfessionalEntryUseCase(MedicalRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public ClinicalEntry execute(Request request) {
        Objects.requireNonNull(request, "request must not be null");
        MedicalRecord medicalRecord = new GetMedicalRecordUseCase(repository)
                .execute(new GetMedicalRecordUseCase.Request(request.patientId()));
        ClinicalEntry entry = medicalRecord.addProfessionalEntry(request.workerId(), request.content());
        repository.save(medicalRecord);
        return entry;
    }

    public record Request(PatientId patientId, HealthcareWorkerId workerId, EntryContent content) {
        public Request {
            Objects.requireNonNull(patientId, "patientId must not be null");
            Objects.requireNonNull(workerId, "workerId must not be null");
            Objects.requireNonNull(content, "content must not be null");
        }
    }
}
