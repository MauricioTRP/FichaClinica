package org.ficha.application.usecase;

import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateMedicalRecordUseCaseTest {
    private static final PatientId PATIENT = PatientId.of("patient-01");

    @Mock
    private MedicalRecordRepository repository;

    private CreateMedicalRecordUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateMedicalRecordUseCase(repository);
    }

    @Test
    void createsAndSavesAMedicalRecord() {
        when(repository.existsByPatientId(PATIENT)).thenReturn(false);

        MedicalRecord created = useCase.execute(new CreateMedicalRecordUseCase.Request(PATIENT));

        assertThat(created.patientId()).isEqualTo(PATIENT);
        ArgumentCaptor<MedicalRecord> captor = ArgumentCaptor.forClass(MedicalRecord.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().patientId()).isEqualTo(PATIENT);
    }

    @Test
    void rejectsCreatingDuplicateRecordForSamePatient() {
        when(repository.existsByPatientId(PATIENT)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new CreateMedicalRecordUseCase.Request(PATIENT)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has a medical record");

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsNullRequest() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullRepository() {
        assertThatThrownBy(() -> new CreateMedicalRecordUseCase(null))
                .isInstanceOf(NullPointerException.class);
    }
}
