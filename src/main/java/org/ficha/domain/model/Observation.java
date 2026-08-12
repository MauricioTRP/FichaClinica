package org.ficha.domain.model;

import org.ficha.domain.DomainStrings;

/**
 * Represents a clinical observation in the FichaClinica domain.
 *
 * <pre>{@code
 * // Example for a fever observation:
 * Observation obs = Observation.of("fever", "38.9", "°C");
 * }</pre>
 *
 * @param name the name of the clinical parameter of the observation
 * @param value the actual value of the observation
 * @param unit the unit of measure for the observation
 */
public record Observation(String name, String value, String unit) {
    public Observation {
        name = DomainStrings.requireNonBlank(name, "Observation name");
        value = DomainStrings.requireNonBlank(value, "Observation value");
        unit = DomainStrings.requireNonBlank(unit, "Observation unit");
    }

    public static Observation of(String name, String value, String unit) {
        return new Observation(name, value, unit);
    }
}
