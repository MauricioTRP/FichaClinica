package org.ficha.application.usecase;

import org.ficha.domain.model.ClinicalEntry;
import org.ficha.domain.model.EntryContent;
import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.CaregiverId;
import org.ficha.domain.model.ids.HealthcareWorkerId;
import org.ficha.domain.model.ids.PatientId;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendEntryUseCaseTest {
    private static final PatientId PATIENT = PatientId.of("patient-01");
    private static final CaregiverId CAREGIVER = CaregiverId.of("caregiver-01");
    private static final HealthcareWorkerId WORKER = HealthcareWorkerId.of("worker-01");

    @Mock
    private MedicalRecordRepository repository;

    private AmendEntryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AmendEntryUseCase(repository);
    }

    @Test
    void amendsEntryAndSavesRecord() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        record.grantCaregiverAccess(PATIENT, CAREGIVER);
        record.shareWithHealthcareWorker(CAREGIVER, WORKER);
        ClinicalEntry existing = record.addProfessionalEntry(
                WORKER,
                EntryContent.of("Diagnosis", "Initial diagnosis", List.of())
        );
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        ClinicalEntry amended = useCase.execute(
                new AmendEntryUseCase.Request(
                        PATIENT,
                        WORKER,
                        existing.id(),
                        EntryContent.of("Diagnosis", "Corrected diagnosis", List.of()),
                        "Fixed transcription error"
                )
        );

        assertThat(amended.versions()).hasSize(2);
        assertThat(amended.currentVersion().content().notes()).isEqualTo("Corrected diagnosis");
        ArgumentCaptor<MedicalRecord> captor = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(record);
    }

    @Test
    void rejectsNullRequest() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullRepository() {
        assertThatThrownBy(() -> new AmendEntryUseCase(null))
                .isInstanceOf(NullPointerException.class);
    }
}
