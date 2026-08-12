package org.ficha.domain.model.source;

import org.ficha.domain.model.ids.HealthcareWorkerId;

import java.util.Objects;

public record ProfessionalSource(HealthcareWorkerId healthcareWorkerId) implements EntrySource {
    public ProfessionalSource {
        Objects.requireNonNull(healthcareWorkerId, "healthcareWorkerId must not be null");
    }

    public static ProfessionalSource of(HealthcareWorkerId healthcareWorkerId) {
        return new ProfessionalSource(healthcareWorkerId);
    }
}
