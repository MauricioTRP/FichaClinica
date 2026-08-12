package org.ficha.domain.model.ids;

import org.ficha.domain.DomainStrings;

public record ClinicalEntryId(String value) {
    public ClinicalEntryId {
        value = DomainStrings.requireNonBlank(value, "ClinicalEntryId");
    }

    public static ClinicalEntryId of(String value) {
        return new ClinicalEntryId(value);
    }
}
