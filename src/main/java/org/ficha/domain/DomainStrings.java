package org.ficha.domain;

import java.util.Objects;

/**
 * # DomainStrings
 *
 * Several utils to manage nullable and blank of strings
 */
public final class DomainStrings {
    public static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name + " value can't be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(name + " value can't be blank");
        }
        return trimmed;
    }
}
