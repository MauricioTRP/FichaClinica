package org.ficha.application.usecase;

import org.ficha.domain.model.ClinicalEntry;
import org.ficha.domain.model.EntryContent;
import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.ActorId;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddExternalEntryUseCaseTest {
    private static final PatientId PATIENT = PatientId.of("patient-01");

    @Mock
    private MedicalRecordRepository repository;

    private AddExternalEntryUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddExternalEntryUseCase(repository);
    }

    @Test
    void addsExternalEntryAndSavesRecord() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));
        ExternalSource source = ExternalSource.of(
                "Discharge summary",
                "discharge.pdf",
                ActorId.from(PATIENT)
        );
        EntryContent content = EntryContent.of("Discharge", "Patient discharged", List.of());

        ClinicalEntry entry = useCase.execute(
                new AddExternalEntryUseCase.Request(PATIENT, ActorId.from(PATIENT), source, content)
        );

        assertThat(entry.source()).isEqualTo(source);
        assertThat(record.entries()).containsExactly(entry);
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
        assertThatThrownBy(() -> new AddExternalEntryUseCase(null))
                .isInstanceOf(NullPointerException.class);
    }
}
