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
class ClearSpecialNeedsUseCaseTest {
    private static final PatientId PATIENT = PatientId.of("patient-01");

    @Mock
    private MedicalRecordRepository repository;

    private ClearSpecialNeedsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ClearSpecialNeedsUseCase(repository);
    }

    @Test
    void clearsSpecialNeedsAndSavesRecord() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        record.markSpecialNeeds(ActorId.from(PATIENT), "Some notes");
        assertThat(record.specialNeedsMarker().marked()).isTrue();
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        useCase.execute(new ClearSpecialNeedsUseCase.Request(PATIENT, ActorId.from(PATIENT)));

        assertThat(record.specialNeedsMarker().marked()).isFalse();
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
        assertThatThrownBy(() -> new ClearSpecialNeedsUseCase(null))
                .isInstanceOf(NullPointerException.class);
    }
}
