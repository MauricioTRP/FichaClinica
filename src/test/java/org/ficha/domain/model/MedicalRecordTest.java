package org.ficha.domain.model;

import org.ficha.domain.exceptions.ClinicalEntryNotFoundException;
import org.ficha.domain.exceptions.UnauthorizedMedicalRecordAccess;
import org.ficha.domain.model.ids.*;
import org.ficha.domain.model.source.ExternalSource;
import org.ficha.domain.model.source.ProfessionalSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MedicalRecordTest {
    // Arrange
    private static final PatientId PATIENT = PatientId.of("patient-01");
    private static final CaregiverId CAREGIVER = CaregiverId.of("caregiver-01");
    private static final HealthcareWorkerId WORKER = HealthcareWorkerId.of("worker-01");
    private static final PatientId OTHER_PATIENT = PatientId.of("patient-02");
    private static final CaregiverId OTHER_CAREGIVER = CaregiverId.of("caregiver-02");
    private static final HealthcareWorkerId OTHER_WORKER = HealthcareWorkerId.of("worker-02");

    private MedicalRecord record;

    // Arrange for every test
    @BeforeEach
    void setUp() {
        record = MedicalRecord.createFor(PATIENT);
    }

    @Nested
    class CreateRecord {
        @Test
        @DisplayName("should create a medical record owned by a patient")
        void shouldCreateMedicalRecordOwnedByAPatient() {
            assertThat(record.patientId()).isEqualTo(PATIENT);
            assertThat(record.id()).isNotNull();
            assertThat(record.id().value()).isNotBlank();
            assertThat(record.entries()).isEmpty();
            assertThat(record.accessGrants()).isEmpty();
            assertThat(record.specialNeedsMarker().marked()).isFalse();
        }

        @Test
        @DisplayName("Should Reject Null Patient")
        void shouldRejectNullPatient() {
            assertThatThrownBy(() -> MedicalRecord.createFor(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("patientId must not be null");
        }
    }

    @Nested
    class CaregiverAccess {
        @Test
        @DisplayName("Should Allow Patient to grant Caregiver Access")
        void shouldAllowPatientToGrantCaregiverAccess() {
            // Act
            record.grantCaregiverAccess(PATIENT, CAREGIVER);

            // Assert
            assertThat(record.hasCaregiverAccess(CAREGIVER)).isTrue();
            assertThat(record.accessGrants())
                    .anySatisfy(grant -> {
                        assertThat(grant.grantee()).isEqualTo(ActorId.from(CAREGIVER));
                        assertThat(grant.role()).isEqualTo(AccessRole.CAREGIVER);
                    });
        }

        @Test
        @DisplayName("Should Allow Patient to Revoke Caregiver Access")
        void shouldAllowPatientToRevokeCaregiverAccess() {
            record.grantCaregiverAccess(PATIENT, CAREGIVER);

            record.revokeCaregiverAccess(PATIENT, CAREGIVER);

            assertThat(record.hasCaregiverAccess(CAREGIVER)).isFalse();
        }

        @Test
        @DisplayName("Should Reject Caregiver Grant From Non Owner")
        void shouldRejectCaregiverGrantFromNonOwner() {
            assertThatThrownBy(() -> record.grantCaregiverAccess(OTHER_PATIENT, CAREGIVER))
                    .isInstanceOf(UnauthorizedMedicalRecordAccess.class);
        }

        @Test
        @DisplayName("Should Reject Duplicate Caregiver Grant")
        void shouldRejectDuplicateCaregiverGrant() {
            record.grantCaregiverAccess(PATIENT, CAREGIVER);

            assertThatThrownBy(() -> record.grantCaregiverAccess(PATIENT, CAREGIVER))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class ShareWihHealthcareWorker {
        @BeforeEach
        void grantCaregiver() {
            record.grantCaregiverAccess(PATIENT, CAREGIVER);
        }

        @Test
        @DisplayName("Should Allow Caregiver to share with Healthcare Worker")
        void shouldAllowCaregiverToShareWithHealthcareWorker() {
            record.shareWithHealthcareWorker(CAREGIVER, WORKER);

            assertThat(record.hasHealthcareWorkerAccess(WORKER)).isTrue();
            assertThat(record.accessGrants())
                    .anySatisfy(grant -> {
                        assertThat(grant.grantee()).isEqualTo(ActorId.from(WORKER));
                        assertThat(grant.role()).isEqualTo(AccessRole.HEALTHCARE_WORKER);
                    });
        }

        @Test
        @DisplayName("Should Reject Share from Unauthorized Caregiver")
        void shouldRejectShareFromUnauthorizedCaregiver() {
            assertThatThrownBy(() -> record.shareWithHealthcareWorker(OTHER_CAREGIVER, WORKER))
                    .isInstanceOf(UnauthorizedMedicalRecordAccess.class);
        }

        @Test
        @DisplayName("Should Reject Share When Caregiver Has No Access")
        void shouldRejectShareWhenCaregiverHasNoAccess() {
            MedicalRecord freshMedicalRecord = MedicalRecord.createFor(PATIENT);

            assertThatThrownBy(() -> freshMedicalRecord.shareWithHealthcareWorker(CAREGIVER, WORKER))
                    .isInstanceOf(UnauthorizedMedicalRecordAccess.class);
        }
    }

    @Nested
    class ProfessionalEntries {
        @BeforeEach
        void shareWithWorker() {
            record.grantCaregiverAccess(PATIENT, CAREGIVER);
            record.shareWithHealthcareWorker(CAREGIVER, WORKER);
        }

        @Test
        @DisplayName("Should Allow Authorized Healthcare Worker To Add Professional Entry")
        void shouldAllowAuthorizedHealthcareWorkerToAddProfessionalEntry() {
            EntryContent content = EntryContent.of(
                    "Blood panel",
                    "Routine labs",
                    List.of(Observation.of("hemoglobin", "14.2", "g/dL"))
            );

            ClinicalEntry entry = record.addProfessionalEntry(WORKER, content);

            assertThat(record.entries()).hasSize(1);
            assertThat(entry.id()).isNotNull();
            assertThat(entry.source()).isInstanceOf(ProfessionalSource.class);
            assertThat(((ProfessionalSource) entry.source()).healthcareWorkerId()).isEqualTo(WORKER);
            assertThat(entry.versions()).hasSize(1);
            assertThat(entry.currentVersion().versionNumber()).isEqualTo(1);
            assertThat(entry.currentVersion().amendmentReason()).isEmpty();
            assertThat(entry.currentVersion().content().summary()).isEqualTo(content.summary());
            assertThat(entry.currentVersion().content().observations()).hasSize(1);
            assertThat(entry.currentVersion().recordedBy()).isEqualTo(ActorId.from(WORKER));
        }

        @Test
        @DisplayName("Should Reject Unauthorized Healthcare Worker From Adding Entry")
        void shouldRejectUnauthorizedHealthcareWorkerFromAddingEntry() {
            EntryContent content = EntryContent.of(
                    "Blood panel",
                    "Routine labs",
                    List.of(Observation.of("hemoglobin", "14.2", "g/dL"))
            );

            assertThatThrownBy(() -> record.addProfessionalEntry(OTHER_WORKER, content))
                    .isInstanceOf(UnauthorizedMedicalRecordAccess.class);
        }

        @Test
        @DisplayName("Should Reject Null Content For Professional Entry")
        void shouldRejectNullContentForProfessionalEntry() {
            assertThatThrownBy(() -> record.addProfessionalEntry(WORKER, null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class ExternalEntries {
        @BeforeEach
        void grantCaregiver() {
            record.grantCaregiverAccess(PATIENT, CAREGIVER);
        }

        @Test
        @DisplayName("Should Allow Patient to Add External Entry")
        void shouldAllowPatientToAddExternalEntry() {
            ExternalSource source = ExternalSource.of(
                    "Hospital discharge summary",
                    "discharge-2026-01.pdf",
                    ActorId.from(PATIENT)
            );

            EntryContent content = EntryContent.of(
                    "Discharge Summary",
                    "Patient discharged in good condition, follow up in 2 weeks",
                    List.of()
            );

            ClinicalEntry entry = record.addExternalEntry(ActorId.from(PATIENT), source, content);

            assertThat(entry.source()).isInstanceOf(ExternalSource.class);
            assertThat(((ExternalSource)  entry.source()).documentReference()).isEqualTo("discharge-2026-01.pdf");
            assertThat(entry.currentVersion().versionNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should Allow Caregiver to Add External Entry")
        void shouldAllowCaregiverToAddExternalEntry() {
            ExternalSource source = ExternalSource.of(
                    "Hospital discharge summary",
                    "discharge-2026-01.pdf",
                    ActorId.from(CAREGIVER)
            );

            EntryContent content = EntryContent.of(
                    "Discharge Summary",
                    "Patient discharged in good condition, follow up in 2 weeks",
                    List.of()
            );

            ClinicalEntry entry = record.addExternalEntry(ActorId.from(CAREGIVER), source, content);

            assertThat(entry.source()).isInstanceOf(ExternalSource.class);
            assertThat(((ExternalSource)  entry.source()).documentReference()).isEqualTo("discharge-2026-01.pdf");
            assertThat(entry.currentVersion().versionNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should Reject External Entry From Unauthorized Actor")
        void shouldRejectExternalEntryFromUnauthorizedActor() {
            ExternalSource source = ExternalSource.of(
                    "Hospital discharge summary",
                    "discharge-2026-01.pdf",
                    ActorId.from(OTHER_WORKER)
            );

            EntryContent content = EntryContent.of(
                    "Discharge Summary",
                    "Patient discharged in good condition, follow up in 2 weeks",
                    List.of()
            );

            assertThatThrownBy(() -> record.addExternalEntry(ActorId.from(OTHER_WORKER), source, content))
                    .isInstanceOf(UnauthorizedMedicalRecordAccess.class);
        }
    }

    @Nested
    class Amendments {
        private ClinicalEntryId entryId;

        @BeforeEach
        void createProfessionalEntry() {
            record.grantCaregiverAccess(PATIENT, CAREGIVER);
            record.shareWithHealthcareWorker(CAREGIVER, WORKER);

            ClinicalEntry entry = record.addProfessionalEntry(
                    WORKER,
                    EntryContent.of("Diagnosis", "Initial", List.of())
            );

            entryId = entry.id();
        }

        @Test
        @DisplayName("Should Append Amendment With Reason And Preserve Prior Versions")
        void shouldAppendAmendmentWithReasonAndPreservePriorVersions() {
            EntryContent amended = EntryContent.of("Diagnosis", "Amended", List.of());

            ClinicalEntry entry = record.amendEntry(WORKER, entryId, amended, "Typo in diagnosis name");

            assertThat(entry.versions()).hasSize(2);
            assertThat(entry.versions().get(0).versionNumber()).isEqualTo(1);
            assertThat(entry.versions().get(0).content().notes()).isEqualTo("Initial");
            assertThat(entry.currentVersion().versionNumber()).isEqualTo(2);
            assertThat(entry.currentVersion().content().notes()).isEqualTo("Amended");
            assertThat(entry.currentVersion().amendmentReason()).contains("Typo in diagnosis name");
            assertThat(entry.currentVersion().recordedBy()).isEqualTo(ActorId.from(WORKER));
        }

        @Test
        @DisplayName("Should Reject Amendment Without Reason")
        void shouldRejectAmendmentWithoutReason() {
            EntryContent amended = EntryContent.of("Diagnosis", "Amended", List.of());

            assertThatThrownBy(() -> record.amendEntry(WORKER, entryId, amended, "   "))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> record.amendEntry(WORKER, entryId, amended, null))
                    .isInstanceOf(IllegalArgumentException.class);

        }

        @Test
        @DisplayName("Should Reject Amendment From Unauthorized Worker")
        void shouldRejectAmendmentFromUnauthorizedWorker() {
            EntryContent amended = EntryContent.of("Diagnosis", "Corrected", List.of());

            assertThatThrownBy(() ->
                    record.amendEntry(OTHER_WORKER, entryId, amended, "fix"))
                    .isInstanceOf(UnauthorizedMedicalRecordAccess.class);
        }

        @Test
        @DisplayName("Should Reject Amendment For Unknown Entry")
        void shouldRejectAmendmentForUnknownEntry() {
            EntryContent amended = EntryContent.of("Diagnosis", "Corrected", List.of());

            assertThatThrownBy(() ->
                    record.amendEntry(WORKER, ClinicalEntryId.of("missing"), amended, "fix"))
                    .isInstanceOf(ClinicalEntryNotFoundException.class);
        }
    }

    @Nested
    class AuthorizationMatrix {
        @Test
        @DisplayName("Should Allow Patient And Granted Actors To Read")
        void shouldAllowPatientAndGrantedActorsToRead() {
            record.grantCaregiverAccess(PATIENT, CAREGIVER);
            record.shareWithHealthcareWorker(CAREGIVER, WORKER);

            assertThat(record.canRead(ActorId.from(PATIENT))).isTrue();
            assertThat(record.canRead(ActorId.from(CAREGIVER))).isTrue();
            assertThat(record.canRead(ActorId.from(WORKER))).isTrue();
            assertThat(record.canRead(ActorId.from(OTHER_WORKER))).isFalse();
        }

        @Test
        @DisplayName("Should Deny Mutations Without Grants")
        void shouldDenyMutationsWithoutGrants() {
            EntryContent content = EntryContent.of("Diagnosis", "Denied", List.of());

            assertThatThrownBy(() -> record.addProfessionalEntry(WORKER, content))
                    .isInstanceOf(UnauthorizedMedicalRecordAccess.class);
            assertThatThrownBy(() -> record.addProfessionalEntry(OTHER_WORKER, content))
                    .isInstanceOf(UnauthorizedMedicalRecordAccess.class);
        }
    }

    @Nested
    class SpecialNeedsHook {

        @Test
        void shouldAllowPatientToMarkAndClearSpecialNeeds() {
            record.markSpecialNeeds(ActorId.from(PATIENT), "Patient with atypical baseline labs");

            assertThat(record.specialNeedsMarker().marked()).isTrue();
            assertThat(record.specialNeedsMarker().notes())
                    .contains("Patient with atypical baseline labs");

            record.clearSpecialNeeds(ActorId.from(PATIENT));

            assertThat(record.specialNeedsMarker().marked()).isFalse();
            assertThat(record.specialNeedsMarker().notes()).isEmpty();
        }

        @Test
        void shouldAllowCaregiverToMarkSpecialNeeds() {
            record.grantCaregiverAccess(PATIENT, CAREGIVER);

            record.markSpecialNeeds(ActorId.from(CAREGIVER), "Special monitoring required");

            assertThat(record.specialNeedsMarker().marked()).isTrue();
        }

        @Test
        void shouldRejectSpecialNeedsChangesFromUnauthorizedActor() {
            assertThatThrownBy(() ->
                    record.markSpecialNeeds(ActorId.from(WORKER), "notes"))
                    .isInstanceOf(UnauthorizedMedicalRecordAccess.class);
            assertThatThrownBy(() ->
                    record.clearSpecialNeeds(ActorId.from(OTHER_CAREGIVER)))
                    .isInstanceOf(UnauthorizedMedicalRecordAccess.class);
        }
    }
}
