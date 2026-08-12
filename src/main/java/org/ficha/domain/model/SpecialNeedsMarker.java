package org.ficha.domain.model;

import java.util.Objects;
import java.util.Optional;

public final class SpecialNeedsMarker {
    private final boolean marked;
    private final String notes;

    private SpecialNeedsMarker(boolean marked, String notes) {
        this.marked = marked;
        this.notes = notes;
    }

    public static SpecialNeedsMarker unmarked() {
        return new SpecialNeedsMarker(false, null);
    }

    public static SpecialNeedsMarker marked(String notes) {
        Objects.requireNonNull(notes, "notes must not be null");
        return new SpecialNeedsMarker(true, notes.trim());
    }

    public boolean marked() {
        return marked;
    }

    public Optional<String> notes() {
        return Optional.ofNullable(notes).filter(value -> !value.isBlank());
    }
}
