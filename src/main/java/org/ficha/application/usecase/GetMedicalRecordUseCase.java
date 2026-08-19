package org.ficha.application.usecase;

import org.ficha.application.MedicalRecordNotFoundException;
import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.repository.MedicalRecordRepository;

import java.util.Objects;

public final class GetMedicalRecordUseCase
        implements UseCase<GetMedicalRecordUseCase.Request, MedicalRecord> {
    private final MedicalRecordRepository repository;

    public GetMedicalRecordUseCase(MedicalRecordRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public MedicalRecord execute(Request request) {
        Objects.requireNonNull(request, "request must not be null");
        return repository.findByPatientId(request.patientId())
                .orElseThrow(() -> new MedicalRecordNotFoundException(request.patientId()));
    }

    public record Request(PatientId patientId) {
        public Request {
            Objects.requireNonNull(patientId, "patientId must not be null");
        }
    }
}
