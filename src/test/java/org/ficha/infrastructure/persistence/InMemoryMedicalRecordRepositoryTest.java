package org.ficha.infrastructure.persistence;

import org.ficha.domain.model.EntryContent;
import org.ficha.domain.model.MedicalRecord;
import org.ficha.domain.model.ids.CaregiverId;
import org.ficha.domain.model.ids.HealthcareWorkerId;
import org.ficha.domain.model.ids.MedicalRecordId;
import org.ficha.domain.model.ids.PatientId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryMedicalRecordRepositoryTest {
    private static final PatientId PATIENT = PatientId.of("patient-01");
    private static final CaregiverId CAREGIVER = CaregiverId.of("caregiver-01");
    private static final HealthcareWorkerId WORKER = HealthcareWorkerId.of("worker-01");

    private InMemoryMedicalRecordRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMedicalRecordRepository();
    }

    @Test
    void savesAndFindsMedicalRecordUsingBothIdentifiers() {
        MedicalRecord medicalRecord = MedicalRecord.createFor(PATIENT);

        repository.save(medicalRecord);

        assertThat(repository.findById(medicalRecord.id())).containsSame(medicalRecord);
        assertThat(repository.findByPatientId(PATIENT)).containsSame(medicalRecord);
        assertThat(repository.existsById(medicalRecord.id())).isTrue();
        assertThat(repository.existsByPatientId(PATIENT)).isTrue();
    }

    @Test
    void returnsEmptyForUnknownIdentifiers() {
        assertThat(repository.findById(MedicalRecordId.of("missing"))).isEmpty();
        assertThat(repository.findByPatientId(PATIENT)).isEmpty();
    }

    @Test
    void savesAnUpdatedMedicalRecordWithANewClinicalEntry() {
        MedicalRecord medicalRecord = MedicalRecord.createFor(PATIENT);
        medicalRecord.grantCaregiverAccess(PATIENT, CAREGIVER);
        medicalRecord.shareWithHealthcareWorker(CAREGIVER, WORKER);
        repository.save(medicalRecord);

        MedicalRecord loaded = repository.findByPatientId(PATIENT).orElseThrow();
        loaded.addProfessionalEntry(
                WORKER,
                EntryContent.of("Blood panel", "Routine labs", List.of())
        );
        repository.save(loaded);

        assertThat(repository.findById(medicalRecord.id()).orElseThrow().entries())
                .singleElement()
                .satisfies(entry ->
                        assertThat(entry.currentVersion().content().summary())
                                .isEqualTo("Blood panel")
                );
    }

    @Test
    void rejectsASecondMedicalRecordForTheSamePatient() {
        repository.save(MedicalRecord.createFor(PATIENT));

        assertThatThrownBy(() -> repository.save(MedicalRecord.createFor(PATIENT)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has a medical record");
    }

    @Test
    void rejectsNullArguments() {
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> repository.findByPatientId(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> repository.existsById(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> repository.existsByPatientId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
