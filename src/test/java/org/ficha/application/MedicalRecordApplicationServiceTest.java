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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalRecordApplicationServiceTest {
    private static final PatientId PATIENT = PatientId.of("patient-01");
    private static final CaregiverId CAREGIVER = CaregiverId.of("caregiver-01");
    private static final HealthcareWorkerId WORKER = HealthcareWorkerId.of("worker-01");

    @Mock
    private MedicalRecordRepository repository;

    private MedicalRecordApplicationService service;

    @BeforeEach
    void setUp() {
        service = new MedicalRecordApplicationService(repository);
    }

    @Test
    void createsAMedicalRecordAndSavesIt() {
        when(repository.existsByPatientId(PATIENT)).thenReturn(false);

        MedicalRecord created = service.createFor(PATIENT);

        assertThat(created.patientId()).isEqualTo(PATIENT);
        verify(repository).existsByPatientId(PATIENT);
        verify(repository).save(created);
    }

    @Test
    void rejectsCreatingMoreThanOneMedicalRecordForAPatient() {
        when(repository.existsByPatientId(PATIENT)).thenReturn(true);

        assertThatThrownBy(() -> service.createFor(PATIENT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has a medical record");

        verify(repository, never()).save(any());
    }

    @Test
    void returnsTheMedicalRecordWhenItExists() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        MedicalRecord found = service.getByPatientId(PATIENT);

        assertThat(found).isSameAs(record);
        verify(repository).findByPatientId(PATIENT);
    }

    @Test
    void reportsWhenThePatientsMedicalRecordDoesNotExist() {
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByPatientId(PATIENT))
                .isInstanceOf(MedicalRecordNotFoundException.class)
                .hasMessageContaining(PATIENT.value());

        verify(repository).findByPatientId(PATIENT);
    }

    @Test
    void grantsCaregiverAccessAndSavesTheAggregate() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        service.grantCaregiverAccess(PATIENT, CAREGIVER);

        assertThat(record.hasCaregiverAccess(CAREGIVER)).isTrue();
        verify(repository).save(record);
    }

    @Test
    void revokesCaregiverAccessAndSavesTheAggregate() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        record.grantCaregiverAccess(PATIENT, CAREGIVER);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        service.revokeCaregiverAccess(PATIENT, CAREGIVER);

        assertThat(record.hasCaregiverAccess(CAREGIVER)).isFalse();
        verify(repository).save(record);
    }

    @Test
    void sharesWithHealthcareWorkerAndSavesTheAggregate() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        record.grantCaregiverAccess(PATIENT, CAREGIVER);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        service.shareWithHealthcareWorker(PATIENT, CAREGIVER, WORKER);

        assertThat(record.hasHealthcareWorkerAccess(WORKER)).isTrue();
        verify(repository).save(record);
    }

    @Test
    void addsAProfessionalEntryAndSavesTheAggregate() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        record.grantCaregiverAccess(PATIENT, CAREGIVER);
        record.shareWithHealthcareWorker(CAREGIVER, WORKER);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));
        EntryContent content = EntryContent.of("Blood panel", "Routine labs", List.of());

        ClinicalEntry entry = service.addProfessionalEntry(PATIENT, WORKER, content);

        assertThat(entry.currentVersion().content().summary()).isEqualTo("Blood panel");
        assertThat(record.entries()).containsExactly(entry);
        verify(repository).save(record);
    }

    @Test
    void addsAnExternalEntryAndSavesTheAggregate() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));
        ExternalSource source = ExternalSource.of(
                "Discharge summary",
                "discharge-summary.pdf",
                ActorId.from(PATIENT)
        );
        EntryContent content = EntryContent.of("Discharge summary", "Recovered", List.of());

        ClinicalEntry entry = service.addExternalEntry(
                PATIENT,
                ActorId.from(PATIENT),
                source,
                content
        );

        assertThat(entry.source()).isEqualTo(source);
        assertThat(record.entries()).containsExactly(entry);
        verify(repository).save(record);
    }

    @Test
    void amendsAnEntryAndSavesTheAggregate() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        record.grantCaregiverAccess(PATIENT, CAREGIVER);
        record.shareWithHealthcareWorker(CAREGIVER, WORKER);
        ClinicalEntry existing = record.addProfessionalEntry(
                WORKER,
                EntryContent.of("Diagnosis", "Initial diagnosis", List.of())
        );
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        ClinicalEntry amended = service.amendEntry(
                PATIENT,
                WORKER,
                existing.id(),
                EntryContent.of("Diagnosis", "Corrected diagnosis", List.of()),
                "Corrected a transcription error"
        );

        assertThat(amended.versions()).hasSize(2);
        assertThat(amended.currentVersion().content().notes()).isEqualTo("Corrected diagnosis");
        verify(repository).save(record);
    }

    @Test
    void marksAndClearsSpecialNeedsAndSavesTheAggregate() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        service.markSpecialNeeds(PATIENT, ActorId.from(PATIENT), "Requires special monitoring");
        assertThat(record.specialNeedsMarker().marked()).isTrue();

        service.clearSpecialNeeds(PATIENT, ActorId.from(PATIENT));
        assertThat(record.specialNeedsMarker().marked()).isFalse();

        verify(repository, times(2)).save(record);
    }

    @Test
    void preservesDomainAuthorizationRulesAndDoesNotSaveOnFailure() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> service.addProfessionalEntry(
                PATIENT,
                WORKER,
                EntryContent.of("Diagnosis", "Unauthorized", List.of())
        )).isInstanceOf(UnauthorizedMedicalRecordAccess.class);

        assertThat(record.entries()).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void executesACompleteWorkflowThroughTheMockedRepository() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        when(repository.existsByPatientId(PATIENT)).thenReturn(false);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        MedicalRecord created = service.createFor(PATIENT);
        ArgumentCaptor<MedicalRecord> savedOnCreate = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(repository).save(savedOnCreate.capture());
        assertThat(savedOnCreate.getValue().patientId()).isEqualTo(PATIENT);
        assertThat(created.patientId()).isEqualTo(PATIENT);

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
        service.addExternalEntry(
                PATIENT,
                ActorId.from(PATIENT),
                ExternalSource.of(
                        "Discharge summary",
                        "discharge-summary.pdf",
                        ActorId.from(PATIENT)
                ),
                EntryContent.of("Discharge summary", "Recovered", List.of())
        );
        service.markSpecialNeeds(
                PATIENT,
                ActorId.from(CAREGIVER),
                "Requires special monitoring"
        );
        service.clearSpecialNeeds(PATIENT, ActorId.from(PATIENT));
        service.revokeCaregiverAccess(PATIENT, CAREGIVER);

        MedicalRecord finalState = service.getByPatientId(PATIENT);
        assertThat(finalState.entries()).hasSize(2);
        assertThat(amendedEntry.versions()).hasSize(2);
        assertThat(finalState.specialNeedsMarker().marked()).isFalse();
        assertThat(finalState.hasCaregiverAccess(CAREGIVER)).isFalse();
        assertThat(finalState.hasHealthcareWorkerAccess(WORKER)).isTrue();
        // createFor saves a newly created aggregate; the other eight commands save the stubbed one
        verify(repository, times(9)).save(any(MedicalRecord.class));
        verify(repository, times(8)).save(record);
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
