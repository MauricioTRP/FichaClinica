package org.ficha.domain.model.ids;

import org.ficha.domain.DomainStrings;

public record CaregiverId(String value) {
    public CaregiverId {
        value = DomainStrings.requireNonBlank(value, "CaregiverId");
    }

    public static CaregiverId of(String value) {
        return new CaregiverId(value);
    }
}
