package org.ficha.domain.model.source;

import org.ficha.domain.model.ids.ActorId;

public record ExternalSource(
        String description,
        String documentReference,
        ActorId uploadedBy
) implements EntrySource {
    public static ExternalSource of(String description, String documentReference, ActorId uploadedBy) {
        return new ExternalSource(description, documentReference, uploadedBy);
    }
}
