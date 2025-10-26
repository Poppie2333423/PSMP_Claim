package de.popcornsmp.claim;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    private final Set<ChunkPos> claims = new HashSet<>();
    private final Set<UUID> trusted = new HashSet<>();

    public Set<ChunkPos> getClaims() {
        return claims;
    }

    public Set<UUID> getTrusted() {
        return trusted;
    }

    public Set<ChunkPos> getClaimsView() {
        return Collections.unmodifiableSet(claims);
    }

    public Set<UUID> getTrustedView() {
        return Collections.unmodifiableSet(trusted);
    }
}
