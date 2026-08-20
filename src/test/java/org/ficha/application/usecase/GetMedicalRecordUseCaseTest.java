package org.ficha.application.usecase;

import org.ficha.application.MedicalRecordNotFoundException;
import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMedicalRecordUseCaseTest {
    private static final PatientId PATIENT = PatientId.of("patient-01");

    @Mock
    private MedicalRecordRepository repository;

    private GetMedicalRecordUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetMedicalRecordUseCase(repository);
    }

    @Test
    void retrievesExistingMedicalRecord() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        MedicalRecord found = useCase.execute(new GetMedicalRecordUseCase.Request(PATIENT));

        assertThat(found).isSameAs(record);
        verify(repository).findByPatientId(PATIENT);
    }

    @Test
    void throwsWhenRecordDoesNotExist() {
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetMedicalRecordUseCase.Request(PATIENT)))
                .isInstanceOf(MedicalRecordNotFoundException.class)
                .hasMessageContaining(PATIENT.value());

        verify(repository).findByPatientId(PATIENT);
    }

    @Test
    void rejectsNullRequest() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullRepository() {
        assertThatThrownBy(() -> new GetMedicalRecordUseCase(null))
                .isInstanceOf(NullPointerException.class);
    }
}
