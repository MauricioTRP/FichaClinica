package org.ficha.application.usecase;

import org.ficha.domain.model.ClinicalEntry;
import org.ficha.domain.model.EntryContent;
import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.ActorId;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.model.source.ExternalSource;
import org.ficha.domain.repository.MedicalRecordRepository;

import java.util.Objects;

public final class AddExternalEntryUseCase
        implements UseCase<AddExternalEntryUseCase.Request, ClinicalEntry> {
    private final MedicalRecordRepository repository;

    public AddExternalEntryUseCase(MedicalRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public ClinicalEntry execute(Request request) {
        Objects.requireNonNull(request, "request must not be null");
        MedicalRecord medicalRecord = new GetMedicalRecordUseCase(repository)
                .execute(new GetMedicalRecordUseCase.Request(request.patientId()));
        ClinicalEntry entry = medicalRecord.addExternalEntry(request.uploaderId(), request.source(), request.content());
        repository.save(medicalRecord);
        return entry;
    }

    public record Request(PatientId patientId, ActorId uploaderId, ExternalSource source, EntryContent content) {
        public Request {
            Objects.requireNonNull(patientId, "patientId must not be null");
            Objects.requireNonNull(uploaderId, "uploaderId must not be null");
            Objects.requireNonNull(source, "source must not be null");
            Objects.requireNonNull(content, "content must not be null");
        }
    }
}
