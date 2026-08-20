package org.ficha.application.usecase;

import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.CaregiverId;
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
class RevokeCaregiverAccessUseCaseTest {
    private static final PatientId PATIENT = PatientId.of("patient-01");
    private static final CaregiverId CAREGIVER = CaregiverId.of("caregiver-01");

    @Mock
    private MedicalRecordRepository repository;

    private RevokeCaregiverAccessUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RevokeCaregiverAccessUseCase(repository);
    }

    @Test
    void revokesAccessAndSavesRecord() {
        MedicalRecord record = MedicalRecord.createFor(PATIENT);
        record.grantCaregiverAccess(PATIENT, CAREGIVER);
        when(repository.findByPatientId(PATIENT)).thenReturn(Optional.of(record));

        useCase.execute(new RevokeCaregiverAccessUseCase.Request(PATIENT, CAREGIVER));

        assertThat(record.hasCaregiverAccess(CAREGIVER)).isFalse();
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
        assertThatThrownBy(() -> new RevokeCaregiverAccessUseCase(null))
                .isInstanceOf(NullPointerException.class);
    }
}
