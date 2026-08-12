package org.ficha.domain.model.ids;

import static org.ficha.domain.DomainStrings.requireNonBlank;

public record ActorId(String value) {
    public ActorId {
        value = requireNonBlank(value, "ActorId");
    }

    public static ActorId of(String value) {
        return new ActorId(value);
    }

    public static ActorId from(PatientId patientId) {
        return new ActorId(patientId.value());
    }

    public static ActorId from(CaregiverId caregiverId) {
        return new ActorId(caregiverId.value());
    }

    public static ActorId from(HealthcareWorkerId healthcareWorkerId) {
        return new ActorId(healthcareWorkerId.value());
    }
}
