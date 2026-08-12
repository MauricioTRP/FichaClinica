package org.ficha.domain.model.ids;

import org.ficha.domain.DomainStrings;

public record MedicalRecordId(String value) {
    public MedicalRecordId {
        value = DomainStrings.requireNonBlank(value, "MedicalRecordId");
    }

    public static MedicalRecordId of(String value) {
        return new MedicalRecordId(value);
    }
}
