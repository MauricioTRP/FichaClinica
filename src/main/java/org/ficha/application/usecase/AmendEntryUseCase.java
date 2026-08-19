package org.ficha.application.usecase;

import org.ficha.domain.model.ClinicalEntry;
import org.ficha.domain.model.EntryContent;
import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.ClinicalEntryId;
import org.ficha.domain.model.ids.HealthcareWorkerId;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.repository.MedicalRecordRepository;

import java.util.Objects;

public final class AmendEntryUseCase
        implements UseCase<AmendEntryUseCase.Request, ClinicalEntry> {
    private final MedicalRecordRepository repository;

    public AmendEntryUseCase(MedicalRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public ClinicalEntry execute(Request request) {
        Objects.requireNonNull(request, "request must not be null");
        MedicalRecord medicalRecord = new GetMedicalRecordUseCase(repository)
                .execute(new GetMedicalRecordUseCase.Request(request.patientId()));
        ClinicalEntry entry = medicalRecord.amendEntry(
                request.workerId(),
                request.entryId(),
                request.content(),
                request.reason()
        );
        repository.save(medicalRecord);
        return entry;
    }

    public record Request(
            PatientId patientId,
            HealthcareWorkerId workerId,
            ClinicalEntryId entryId,
            EntryContent content,
            String reason
    ) {
        public Request {
            Objects.requireNonNull(patientId, "patientId must not be null");
            Objects.requireNonNull(workerId, "workerId must not be null");
            Objects.requireNonNull(entryId, "entryId must not be null");
            Objects.requireNonNull(content, "content must not be null");
            Objects.requireNonNull(reason, "reason must not be null");
        }
    }
}
