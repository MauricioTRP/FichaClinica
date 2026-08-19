package org.ficha.application.usecase;

import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.ActorId;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.repository.MedicalRecordRepository;

import java.util.Objects;

public final class MarkSpecialNeedsUseCase
        implements UseCase<MarkSpecialNeedsUseCase.Request, Void> {
    private final MedicalRecordRepository repository;

    public MarkSpecialNeedsUseCase(MedicalRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public Void execute(Request request) {
        Objects.requireNonNull(request, "request must not be null");
        MedicalRecord medicalRecord = new GetMedicalRecordUseCase(repository)
                .execute(new GetMedicalRecordUseCase.Request(request.patientId()));
        medicalRecord.markSpecialNeeds(request.actorId(), request.notes());
        repository.save(medicalRecord);
        return null;
    }

    public record Request(PatientId patientId, ActorId actorId, String notes) {
        public Request {
            Objects.requireNonNull(patientId, "patientId must not be null");
            Objects.requireNonNull(actorId, "actorId must not be null");
            Objects.requireNonNull(notes, "notes must not be null");
        }
    }
}
