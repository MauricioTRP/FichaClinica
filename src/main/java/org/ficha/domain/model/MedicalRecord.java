package org.ficha.domain.model;

import org.ficha.domain.exceptions.ClinicalEntryNotFoundException;
import org.ficha.domain.exceptions.UnauthorizedMedicalRecordAccess;
import org.ficha.domain.model.ids.*;
import org.ficha.domain.model.source.ExternalSource;
import org.ficha.domain.model.source.ProfessionalSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class MedicalRecord {
    private final MedicalRecordId id;
    private final PatientId patientId;
    private final List<ClinicalEntry> entries;
    private final List<AccessGrant> accessGrants;
    private SpecialNeedsMarker specialNeedsMarker;


    private MedicalRecord(
            MedicalRecordId id,
            PatientId patientId
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.patientId = Objects.requireNonNull(patientId, "patientId must not be null");
        this.entries = new ArrayList<>();
        this.accessGrants = new ArrayList<>();
        this.specialNeedsMarker = SpecialNeedsMarker.unmarked();
    }

    public static MedicalRecord createFor(PatientId patientId) {
        return new MedicalRecord(MedicalRecordId.of(UUID.randomUUID().toString()), patientId);
    }

    public void grantCaregiverAccess(PatientId requestingPatientId, CaregiverId caregiverId) {
        requireOwner(requestingPatientId); // throws if requestingPatientId is not the owner of medical record
        Objects.requireNonNull(caregiverId, "caregiverId must not be null");
        ActorId grantee = ActorId.from(caregiverId);

        if (hasGrant(grantee, AccessRole.CAREGIVER)) {
            throw new IllegalStateException("Caregiver already has access: " + caregiverId.value());
        }

        accessGrants.add(new AccessGrant(grantee, AccessRole.CAREGIVER));
    }


    public void shareWithHealthcareWorker(CaregiverId caregiver, HealthcareWorkerId worker) {
        Objects.requireNonNull(caregiver, "caregiver must not be null");
        Objects.requireNonNull(worker, "worker must not be null");
        if (!hasCaregiverAccess(caregiver)) {
            throw new UnauthorizedMedicalRecordAccess(
                    "Caregiver is not authorized to share this medical record: " + caregiver.value()
            );
        }

        ActorId grantee = ActorId.from(worker);
        if (!hasGrant(grantee, AccessRole.HEALTHCARE_WORKER)) {
            accessGrants.add(new AccessGrant(grantee, AccessRole.HEALTHCARE_WORKER));
        }
    }

    public void revokeCaregiverAccess(PatientId requestingPatient, CaregiverId caregiver) {
        requireOwner(requestingPatient); // throws if requestingPatient id is not the owner of medical record
        Objects.requireNonNull(caregiver, "caregiver must not be null");
        ActorId grantee = ActorId.from(caregiver);

        if (!hasGrant(grantee, AccessRole.CAREGIVER)) {
            throw new IllegalStateException("Caregiver already is without access: " + caregiver.value());
        }

        // Remove access of caregiver
        accessGrants.remove(new AccessGrant(grantee, AccessRole.CAREGIVER));
    }

    public PatientId patientId() {
        return patientId;
    }

    public MedicalRecordId id() {
        return id;
    }

    public List<ClinicalEntry> entries() {
        return entries;
    }

    public List<AccessGrant> accessGrants() {
        return accessGrants;
    }

    public SpecialNeedsMarker specialNeedsMarker() {
        return specialNeedsMarker;
    }

    private boolean hasGrant(ActorId grantee, AccessRole role) {
        return accessGrants.stream()
                .anyMatch(grant -> grant.grantee().equals(grantee) && grant.role().equals(role));
    }

    private void requireOwner(PatientId requestingPatient) {
        Objects.requireNonNull(requestingPatient, "requestingPatient must not be null");
        if (!patientId.equals(requestingPatient)) {
            throw new UnauthorizedMedicalRecordAccess(
                    "Only the owning patient can perform this action: " + requestingPatient.value()
            );
        }
    }

    public boolean hasCaregiverAccess(CaregiverId caregiverId) {
        return hasGrant(ActorId.from(caregiverId), AccessRole.CAREGIVER);
    }

    public boolean hasHealthcareWorkerAccess(HealthcareWorkerId healthcareWorkerId) {
        return hasGrant(ActorId.from(healthcareWorkerId), AccessRole.HEALTHCARE_WORKER);
    }

    public ClinicalEntry addProfessionalEntry(HealthcareWorkerId worker, EntryContent content) {
        Objects.requireNonNull(worker, "healthcare worker must not be null");
        Objects.requireNonNull(content, "content must not be null");

        if(!hasHealthcareWorkerAccess(worker)) {
            throw new UnauthorizedMedicalRecordAccess(
                    "Healthcare worker is not authorized to add entries to this medical record: " + worker.value()
            );
        }

        ClinicalEntry entry = ClinicalEntry.create(
                ProfessionalSource.of(worker),
                content,
                ActorId.from(worker)
        );

        entries.add(entry);
        return entry;
    }

    public ClinicalEntry addExternalEntry(ActorId from, ExternalSource source, EntryContent content) {
        Objects.requireNonNull(from, "uploader must not be null");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(content, "content must not be null");
        requirePatientOrCaregiver(from);
        ClinicalEntry entry = ClinicalEntry.create(
                source,
                content,
                from
        );

        entries.add(entry);
        return entry;
    }

    private void requirePatientOrCaregiver(ActorId actorId) {
        Objects.requireNonNull(actorId, "actorId must not be null");

        if (isPatient(actorId) || hasGrant(actorId, AccessRole.CAREGIVER)) {
            return;
        }

        throw new UnauthorizedMedicalRecordAccess(
                "Actor is not authorized as patient or caregiver"
        );
    }

    private boolean isPatient(ActorId actorId) {
        return ActorId.from(patientId).equals(actorId);
    }

    public ClinicalEntry amendEntry(
            HealthcareWorkerId worker,
            ClinicalEntryId entryId,
            EntryContent content,
            String reason
    ) {
        Objects.requireNonNull(worker, "worker must not be null");
        Objects.requireNonNull(entryId, "entryId must not be null");
        Objects.requireNonNull(content, "content must not be null");

        if (!hasHealthcareWorkerAccess(worker)) {
            throw new UnauthorizedMedicalRecordAccess(
                    "Healthcare worker is not authorized to amend entry: " + entryId.value()
            );
        }

        ClinicalEntry entry = findEntry(entryId);
        entry.amend(content, reason, ActorId.from(worker));
        return entry;
    }

    private ClinicalEntry findEntry(ClinicalEntryId entryId) {
        return entries.stream()
                .filter(entry -> entry.id().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new ClinicalEntryNotFoundException(entryId.value()));
    }

    public boolean canRead(ActorId actorId) {
        Objects.requireNonNull(actorId, "from must not be null");

        return isPatient(actorId)
                || hasGrant(actorId, AccessRole.CAREGIVER)
                || hasGrant(actorId, AccessRole.HEALTHCARE_WORKER);
    }

    public void markSpecialNeeds(ActorId actorId, String notes) {
        requirePatientOrCaregiver(actorId);
        specialNeedsMarker = SpecialNeedsMarker.marked(notes);
    }


    public void clearSpecialNeeds(ActorId actorId) {
        requirePatientOrCaregiver(actorId);
        specialNeedsMarker = SpecialNeedsMarker.unmarked();
    }
}
