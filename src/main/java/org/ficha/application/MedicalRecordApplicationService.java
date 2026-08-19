package org.ficha.application;

import org.ficha.application.usecase.AddExternalEntryUseCase;
import org.ficha.application.usecase.AddProfessionalEntryUseCase;
import org.ficha.application.usecase.AmendEntryUseCase;
import org.ficha.application.usecase.ClearSpecialNeedsUseCase;
import org.ficha.application.usecase.CreateMedicalRecordUseCase;
import org.ficha.application.usecase.GetMedicalRecordUseCase;
import org.ficha.application.usecase.GrantCaregiverAccessUseCase;
import org.ficha.application.usecase.MarkSpecialNeedsUseCase;
import org.ficha.application.usecase.RevokeCaregiverAccessUseCase;
import org.ficha.application.usecase.ShareWithHealthcareWorkerUseCase;
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
 * Thin application façade that delegates each command to a dedicated UseCase.
 * This keeps the public API stable while the application layer becomes more
 * aligned with Clean Architecture and the UseCase pattern.
 */
public final class MedicalRecordApplicationService {
    private final CreateMedicalRecordUseCase createMedicalRecordUseCase;
    private final GetMedicalRecordUseCase getMedicalRecordUseCase;
    private final GrantCaregiverAccessUseCase grantCaregiverAccessUseCase;
    private final RevokeCaregiverAccessUseCase revokeCaregiverAccessUseCase;
    private final ShareWithHealthcareWorkerUseCase shareWithHealthcareWorkerUseCase;
    private final AddProfessionalEntryUseCase addProfessionalEntryUseCase;
    private final AddExternalEntryUseCase addExternalEntryUseCase;
    private final AmendEntryUseCase amendEntryUseCase;
    private final MarkSpecialNeedsUseCase markSpecialNeedsUseCase;
    private final ClearSpecialNeedsUseCase clearSpecialNeedsUseCase;

    public MedicalRecordApplicationService(MedicalRecordRepository repo) {
        MedicalRecordRepository repository = Objects.requireNonNull(repo, "repository must not be null");
        this.createMedicalRecordUseCase = new CreateMedicalRecordUseCase(repository);
        this.getMedicalRecordUseCase = new GetMedicalRecordUseCase(repository);
        this.grantCaregiverAccessUseCase = new GrantCaregiverAccessUseCase(repository);
        this.revokeCaregiverAccessUseCase = new RevokeCaregiverAccessUseCase(repository);
        this.shareWithHealthcareWorkerUseCase = new ShareWithHealthcareWorkerUseCase(repository);
        this.addProfessionalEntryUseCase = new AddProfessionalEntryUseCase(repository);
        this.addExternalEntryUseCase = new AddExternalEntryUseCase(repository);
        this.amendEntryUseCase = new AmendEntryUseCase(repository);
        this.markSpecialNeedsUseCase = new MarkSpecialNeedsUseCase(repository);
        this.clearSpecialNeedsUseCase = new ClearSpecialNeedsUseCase(repository);
    }

    public MedicalRecord createFor(PatientId patientId) {
        return createMedicalRecordUseCase.execute(new CreateMedicalRecordUseCase.Request(patientId));
    }

    public MedicalRecord getByPatientId(PatientId patientId) {
        return getMedicalRecordUseCase.execute(new GetMedicalRecordUseCase.Request(patientId));
    }

    public void grantCaregiverAccess(PatientId patientId, CaregiverId caregiverId) {
        grantCaregiverAccessUseCase.execute(new GrantCaregiverAccessUseCase.Request(patientId, caregiverId));
    }

    public void revokeCaregiverAccess(PatientId patientId, CaregiverId caregiverId) {
        revokeCaregiverAccessUseCase.execute(new RevokeCaregiverAccessUseCase.Request(patientId, caregiverId));
    }

    public void shareWithHealthcareWorker(
            PatientId patientId,
            CaregiverId caregiverId,
            HealthcareWorkerId workerId
    ) {
        shareWithHealthcareWorkerUseCase.execute(
                new ShareWithHealthcareWorkerUseCase.Request(patientId, caregiverId, workerId)
        );
    }

    public ClinicalEntry addProfessionalEntry(
            PatientId patientId,
            HealthcareWorkerId workerId,
            EntryContent content
    ) {
        return addProfessionalEntryUseCase.execute(
                new AddProfessionalEntryUseCase.Request(patientId, workerId, content)
        );
    }

    public ClinicalEntry addExternalEntry(
            PatientId patientId,
            ActorId uploaderId,
            ExternalSource source,
            EntryContent content
    ) {
        return addExternalEntryUseCase.execute(
                new AddExternalEntryUseCase.Request(patientId, uploaderId, source, content)
        );
    }

    public ClinicalEntry amendEntry(
            PatientId patientId,
            HealthcareWorkerId workerId,
            ClinicalEntryId entryId,
            EntryContent content,
            String reason
    ) {
        return amendEntryUseCase.execute(
                new AmendEntryUseCase.Request(patientId, workerId, entryId, content, reason)
        );
    }

    public void markSpecialNeeds(PatientId patientId, ActorId actorId, String notes) {
        markSpecialNeedsUseCase.execute(new MarkSpecialNeedsUseCase.Request(patientId, actorId, notes));
    }

    public void clearSpecialNeeds(PatientId patientId, ActorId actorId) {
        clearSpecialNeedsUseCase.execute(new ClearSpecialNeedsUseCase.Request(patientId, actorId));
    }
}
