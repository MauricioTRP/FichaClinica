package org.ficha.domain.ids;

import org.ficha.domain.model.ids.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Ids Tests")
public class TypeIdTests {
    @Test
    @DisplayName("should create TypeIds from non-blank values")
    void shouldCreateTypeIdsFromNonBlankValues() {
        // Arrange
        String caregiverId = "caregiver-123";
        String patientId = "patient-123";
        String healthcareWorkerId = "hw-1";
        String medicalRecordId = "mr-01";
        String clinicalEntryId = "entry-01";
        String actorId = "actor-01";


        // Act & Assert
        assertThat(CaregiverId.of(caregiverId).value()).isEqualTo(caregiverId);
        assertThat(PatientId.of(patientId).value()).isEqualTo(patientId);
        assertThat(HealthcareWorkerId.of(healthcareWorkerId).value()).isEqualTo(healthcareWorkerId);
        assertThat(MedicalRecordId.of(medicalRecordId).value()).isEqualTo(medicalRecordId);
        assertThat(ClinicalEntryId.of(clinicalEntryId).value()).isEqualTo(clinicalEntryId);
        assertThat(ActorId.of(actorId).value()).isEqualTo(actorId);
    }

    @Test
    @DisplayName("Should reject blank or null id values")
    void shouldRejectBlankOrNullIdValues() {
        // AAA on one-liner lambda for every Id being tested

        // PatientId
        assertThatThrownBy(() -> PatientId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> PatientId.of("  "))
                .isInstanceOf(IllegalArgumentException.class);

        // CaregiverId
        assertThatThrownBy(() -> CaregiverId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CaregiverId.of("  "))
                .isInstanceOf(IllegalArgumentException.class);

        // MedicalRecordId
        assertThatThrownBy(() -> MedicalRecordId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> MedicalRecordId.of("  "))
                .isInstanceOf(IllegalArgumentException.class);

        // HealthcareWorkerId
        assertThatThrownBy(() -> HealthcareWorkerId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> HealthcareWorkerId.of("  "))
                .isInstanceOf(IllegalArgumentException.class);

        // ClinicalEntryId
        assertThatThrownBy(() -> ClinicalEntryId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ClinicalEntryId.of("  "))
                .isInstanceOf(IllegalArgumentException.class);

        // ActorId
        assertThatThrownBy(() -> ActorId.of(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ActorId.of("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should derive actorId from PartyIds")
    void shouldDeriveActorIdFromPartyIds() {
        // Arrange
        String patientId = "p-1";
        String caregiverId = "c-1";
        String healthcareWorkerId = "h-1";

        assertThat(ActorId.from(PatientId.of(patientId))).isEqualTo(ActorId.of(patientId));
        assertThat(ActorId.from(CaregiverId.of(caregiverId))).isEqualTo(ActorId.of(caregiverId));
        assertThat(ActorId.from(HealthcareWorkerId.of(healthcareWorkerId))).isEqualTo(ActorId.of(healthcareWorkerId));
    }
}
