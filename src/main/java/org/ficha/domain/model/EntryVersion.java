package org.ficha.domain.model;

import org.ficha.domain.model.ids.ActorId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class EntryVersion {
    private final int versionNumber;
    private final EntryContent content;
    private final String amendmentReason;
    private final Instant recordedAt;
    private final ActorId recordedBy;

    private EntryVersion(
            int versionNumber,
            EntryContent content,
            String amendmentReason,
            Instant recordedAt,
            ActorId recordedBy
    ) {
        if (versionNumber < 1) {
            throw new IllegalArgumentException("versionNumber must be >= 1");
        }
        this.versionNumber = versionNumber;
        this.content = Objects.requireNonNull(content, "content must be not null");
        this.amendmentReason = amendmentReason;
        this.recordedAt = Objects.requireNonNull(recordedAt, "recordedAt must be not null");
        this.recordedBy = Objects.requireNonNull(recordedBy, "recordedBy must be not null");
    }

    public static EntryVersion initial(EntryContent content, ActorId recordedBy, Instant recordedAt) {
        return new EntryVersion(1, content, null, recordedAt, recordedBy);
    }

    public static EntryVersion amendment(
            int versionNumber,
            EntryContent content,
            String reason,
            ActorId recordedBy,
            Instant recordedAt
    ) {
        return new EntryVersion(versionNumber, content, reason, recordedAt, recordedBy);
    }

    public int versionNumber() { return versionNumber; }
    public EntryContent content() { return content; }
    public Optional<String> amendmentReason() { return Optional.ofNullable(amendmentReason); }
    public Instant recordedAt() { return recordedAt; }
    public ActorId recordedBy() { return recordedBy; }
}
