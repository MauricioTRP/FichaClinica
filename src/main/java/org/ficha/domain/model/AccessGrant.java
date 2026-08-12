package org.ficha.domain.model;

import org.ficha.domain.model.ids.ActorId;

import java.util.Objects;

public record AccessGrant(ActorId grantee, AccessRole role) {
    public AccessGrant {
        Objects.requireNonNull(grantee, "grantee must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }
}
