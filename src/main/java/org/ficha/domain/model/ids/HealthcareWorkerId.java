package org.ficha.domain.model.ids;

public record HealthcareWorkerId(String value) {
    public HealthcareWorkerId {
        value = org.ficha.domain.DomainStrings.requireNonBlank(value, "HealthcareWorkerId");
    }

    public static HealthcareWorkerId of(String value) {
        return new HealthcareWorkerId(value);
    }
}
