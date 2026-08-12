package org.ficha.domain.model.ids;

import org.ficha.domain.DomainStrings;

public record PatientId(String value) {
    public PatientId {
        value = DomainStrings.requireNonBlank(value, "PatientId");
    }

    public static PatientId of(String value) {
        return new PatientId(value);
    }
}
