package org.ficha.application.usecase;

import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.ActorId;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.repository.MedicalRecordRepository;

import java.util.Objects;

public final class ClearSpecialNeedsUseCase
        implements UseCase<ClearSpecialNeedsUseCase.Request, Void> {
    private final MedicalRecordRepository repository;

    public ClearSpecialNeedsUseCase(MedicalRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public Void execute(Request request) {
        Objects.requireNonNull(request, "request must not be null");
        MedicalRecord medicalRecord = new GetMedicalRecordUseCase(repository)
                .execute(new GetMedicalRecordUseCase.Request(request.patientId()));
        medicalRecord.clearSpecialNeeds(request.actorId());
        repository.save(medicalRecord);
        return null;
    }

    public record Request(PatientId patientId, ActorId actorId) {
        public Request {
            Objects.requireNonNull(patientId, "patientId must not be null");
            Objects.requireNonNull(actorId, "actorId must not be null");
        }
    }
}
