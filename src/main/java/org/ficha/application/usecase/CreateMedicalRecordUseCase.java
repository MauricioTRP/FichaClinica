package org.ficha.application.usecase;

import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.repository.MedicalRecordRepository;

import java.util.Objects;

public final class CreateMedicalRecordUseCase
        implements UseCase<CreateMedicalRecordUseCase.Request, MedicalRecord> {
    private final MedicalRecordRepository repository;

    public CreateMedicalRecordUseCase(MedicalRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public MedicalRecord execute(Request request) {
        Objects.requireNonNull(request, "request must not be null");
        if (repository.existsByPatientId(request.patientId())) {
            throw new IllegalStateException(
                    "Patient already has a medical record: " + request.patientId().value()
            );
        }

        MedicalRecord medicalRecord = MedicalRecord.createFor(request.patientId());
        repository.save(medicalRecord);
        return medicalRecord;
    }

    public record Request(PatientId patientId) {
        public Request {
            Objects.requireNonNull(patientId, "patientId must not be null");
        }
    }
}
