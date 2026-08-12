package org.ficha.domain.model;

import org.ficha.domain.model.ids.ActorId;
import org.ficha.domain.model.ids.ClinicalEntryId;
import org.ficha.domain.model.source.EntrySource;

import java.time.Instant;
import java.util.*;

public final class ClinicalEntry {
    private final ClinicalEntryId id;
    private final EntrySource source;
    private final List<EntryVersion> versions;

    private ClinicalEntry(ClinicalEntryId id, EntrySource source, List<EntryVersion> versions) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.versions = new ArrayList<>(Objects.requireNonNull(versions, "versions must not be null"));
        if (this.versions.isEmpty()) {
            throw new IllegalArgumentException("ClinicalEntry must have at least one version");
        }
    }

    public static ClinicalEntry create(EntrySource source, EntryContent content, ActorId recordedBy) {
        EntryVersion initial = EntryVersion.initial(content, recordedBy, Instant.now());
        return new ClinicalEntry(ClinicalEntryId.of(UUID.randomUUID().toString()), source, List.of(initial));
    }

    // Getters
    public ClinicalEntryId id() { return id; }
    public EntrySource source() { return source; }
    public List<EntryVersion> versions() { return Collections.unmodifiableList(versions); }
    public EntryVersion currentVersion() { return versions.getLast(); }

    void amend(EntryContent content, String reason, ActorId recordedBy) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Amendment reason is required");
        }

        EntryVersion nextVersion = EntryVersion.amendment(
                currentVersion().versionNumber() + 1,
                content,
                reason.trim(),
                recordedBy,
                Instant.now()
        );

        versions.add(nextVersion);
    }


}
