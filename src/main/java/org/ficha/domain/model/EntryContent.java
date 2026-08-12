package org.ficha.domain.model;

import org.ficha.domain.DomainStrings;

import java.util.List;
import java.util.Objects;

public record EntryContent(String summary, String notes, List<Observation> observations) {
    public EntryContent {
        summary = DomainStrings.requireNonBlank(summary, "EntryContent summary");
        Objects.requireNonNull(notes, "EntryContent notes can't be null");
        Objects.requireNonNull(observations, "EntryContent observations can't be null");
        observations = List.copyOf(observations);
    }

    public static EntryContent of(String summary, String notes, List<Observation> observations) {
        return new EntryContent(summary, notes, observations);
    }
}
