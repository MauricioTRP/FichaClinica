package org.ficha.application;

import org.ficha.domain.model.ids.PatientId;

public final class MedicalRecordNotFoundException extends RuntimeException {
    public MedicalRecordNotFoundException(PatientId patientId) {
        super("Medical record not found for patient: " + patientId.value());
    }
}
