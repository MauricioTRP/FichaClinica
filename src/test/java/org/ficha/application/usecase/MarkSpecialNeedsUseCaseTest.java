package org.ficha.application.usecase;

import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.ActorId;
import org.ficha.domain.model.ids.PatientId;
import org.ficha.domain.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkSpecialNeedsUseCaseTest {
    private static final PatientId PATIENT = PatientId.of("patient-01");

    @Mock
    private MedicalRecordRepository repository;

    private MarkSpecialNeedsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkSpecialNeedsUseCase(repository);
    }

    @Test
    void marksSpecialNeedsAndSavesRecord() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        useCase.execute(
                new MarkSpecialNeedsUseCase.Request(
                        PATIENT,
                        ActorId.from(PATIENT),
                        "Requires special monitoring"
                )
        );

        assertThat(record.specialNeedsMarker().marked()).isTrue();
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
        assertThatThrownBy(() -> new MarkSpecialNeedsUseCase(null))
                .isInstanceOf(NullPointerException.class);
    }
}
