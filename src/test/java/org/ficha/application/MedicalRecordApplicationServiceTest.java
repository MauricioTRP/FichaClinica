package org.ficha.application;

import org.ficha.domain.exceptions.UnauthorizedMedicalRecordAccess;
import org.ficha.domain.model.ClinicalEntry;
import org.ficha.domain.model.EntryContent;
import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.ActorId;
import org.ficha.domain.model.ids.CaregiverId;
import org.ficha.domain.model.ids.HealthcareWorkerId;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.model.source.ExternalSource;
import org.ficha.domain.repository.MedicalRecordRepository;
import org.ficha.infrastructure.persistence.InMemoryMedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MedicalRecordApplicationServiceTest {
    private static final PatientId PATIENT = PatientId.of("patient-01");
    private static final CaregiverId CAREGIVER = CaregiverId.of("caregiver-01");
    private static final HealthcareWorkerId WORKER = HealthcareWorkerId.of("worker-01");

    private InMemoryMedicalRecordRepository repository;
    private MedicalRecordApplicationService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMedicalRecordRepository();
        service = new MedicalRecordApplicationService(repository);
    }

    @Test
    void executesACompleteInMemoryMedicalRecordWorkflow() {
        MedicalRecord created = service.createFor(PATIENT);

        service.grantCaregiverAccess(PATIENT, CAREGIVER);
        service.shareWithHealthcareWorker(PATIENT, CAREGIVER, WORKER);

        ClinicalEntry professionalEntry = service.addProfessionalEntry(
                PATIENT,
                WORKER,
                EntryContent.of("Diagnosis", "Initial diagnosis", List.of())
        );

        ClinicalEntry amendedEntry = service.amendEntry(
                PATIENT,
                WORKER,
                professionalEntry.id(),
                EntryContent.of("Diagnosis", "Corrected diagnosis", List.of()),
                "Corrected a transcription error"
        );

        ExternalSource source = ExternalSource.of(
                "Discharge summary",
                "discharge-summary.pdf",
                ActorId.from(PATIENT)
        );
        service.addExternalEntry(
                PATIENT,
                ActorId.from(PATIENT),
                source,
                EntryContent.of("Discharge summary", "Recovered", List.of())
        );

        service.markSpecialNeeds(
                PATIENT,
                ActorId.from(CAREGIVER),
                "Requires special monitoring"
        );

        MedicalRecord updated = service.getByPatientId(PATIENT);
        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.entries()).hasSize(2);
        assertThat(amendedEntry.versions()).hasSize(2);
        assertThat(updated.specialNeedsMarker().marked()).isTrue();

        service.clearSpecialNeeds(PATIENT, ActorId.from(PATIENT));
        service.revokeCaregiverAccess(PATIENT, CAREGIVER);

        MedicalRecord finalState = repository.findByPatientId(PATIENT).orElseThrow();
        assertThat(finalState.specialNeedsMarker().marked()).isFalse();
        assertThat(finalState.hasCaregiverAccess(CAREGIVER)).isFalse();
        assertThat(finalState.hasHealthcareWorkerAccess(WORKER)).isTrue();
    }

    @Test
    void rejectsCreatingMoreThanOneMedicalRecordForAPatient() {
        service.createFor(PATIENT);

        assertThatThrownBy(() -> service.createFor(PATIENT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has a medical record");
    }

    @Test
    void reportsWhenThePatientsMedicalRecordDoesNotExist() {
        assertThatThrownBy(() -> service.getByPatientId(PATIENT))
                .isInstanceOf(MedicalRecordNotFoundException.class)
                .hasMessageContaining(PATIENT.value());
    }

    @Test
    void preservesDomainAuthorizationRules() {
        service.createFor(PATIENT);

        assertThatThrownBy(() -> service.addProfessionalEntry(
                PATIENT,
                WORKER,
                EntryContent.of("Diagnosis", "Unauthorized", List.of())
        )).isInstanceOf(UnauthorizedMedicalRecordAccess.class);

        assertThat(repository.findByPatientId(PATIENT).orElseThrow().entries()).isEmpty();
    }

    @Test
    void savesTheAggregateAfterAddingAClinicalEntry() {
        MedicalRecordRepository repositoryPort = mock(MedicalRecordRepository.class);
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        record.grantCaregiverAccess(PATIENT, CAREGIVER);
        record.shareWithHealthcareWorker(CAREGIVER, WORKER);
        when(repositoryPort.findByPatientId(PATIENT)).thenReturn(Optional.of(record));
        MedicalRecordApplicationService applicationService =
                new MedicalRecordApplicationService(repositoryPort);

        applicationService.addProfessionalEntry(
                PATIENT,
                WORKER,
                EntryContent.of("Blood panel", "Routine labs", List.of())
        );

        verify(repositoryPort).save(record);
        assertThat(record.entries()).hasSize(1);
    }

    @Test
    void rejectsNullDependenciesAndPatientIdentifiers() {
        assertThatThrownBy(() -> new MedicalRecordApplicationService(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> service.createFor(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> service.getByPatientId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
