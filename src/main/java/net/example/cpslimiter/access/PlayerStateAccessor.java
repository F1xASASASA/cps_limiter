package net.example.cpslimiter.access;

import java.util.UUID;

public interface PlayerStateAccessor {
    void cpslimiter$setUuid(UUID uuid);
    UUID cpslimiter$getUuid();
}