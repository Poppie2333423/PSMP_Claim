package de.popcornsmp.claim;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ChunkManager {
    private static final int MAX_CLAIMS = 25;

    private final Map<UUID, PlayerData> playerData;
    private final Map<ChunkPos, UUID> claims = new HashMap<>();
    private final ClaimStorage storage;

    public ChunkManager(ClaimStorage storage) {
        this.storage = storage;
        this.playerData = storage.load();
        rebuildIndex();
    }

    private void rebuildIndex() {
        claims.clear();
        for (Map.Entry<UUID, PlayerData> entry : playerData.entrySet()) {
            for (ChunkPos pos : entry.getValue().getClaims()) {
                claims.put(pos, entry.getKey());
            }
        }
    }

    public void save() {
        storage.save(playerData);
    }

    public Optional<UUID> getOwner(Chunk chunk) {
        return Optional.ofNullable(claims.get(ChunkPos.of(chunk)));
    }

    public Optional<UUID> getOwner(ChunkPos pos) {
        return Optional.ofNullable(claims.get(pos));
    }

    public boolean isClaimed(Chunk chunk) {
        return claims.containsKey(ChunkPos.of(chunk));
    }

    public boolean isClaimed(ChunkPos pos) {
        return claims.containsKey(pos);
    }

    public boolean isTrusted(ChunkPos pos, UUID playerId) {
        UUID owner = claims.get(pos);
        if (owner == null) {
            return false;
        }
        if (owner.equals(playerId)) {
            return true;
        }
        PlayerData data = playerData.get(owner);
        return data != null && data.getTrusted().contains(playerId);
    }

    public int getClaimCount(UUID playerId) {
        return getOrCreateData(playerId).getClaims().size();
    }

    public Collection<ChunkPos> getClaims(UUID playerId) {
        return Collections.unmodifiableCollection(getOrCreateData(playerId).getClaims());
    }

    public Collection<UUID> getTrustedPlayers(UUID playerId) {
        return Collections.unmodifiableCollection(getOrCreateData(playerId).getTrusted());
    }

    public boolean canClaimMore(UUID playerId) {
        return getClaimCount(playerId) < MAX_CLAIMS;
    }

    public boolean claimChunk(Player player, Chunk chunk) {
        UUID owner = claims.get(ChunkPos.of(chunk));
        if (owner != null) {
            return false;
        }
        if (!canClaimMore(player.getUniqueId())) {
            return false;
        }
        ChunkPos pos = ChunkPos.of(chunk);
        getOrCreateData(player.getUniqueId()).getClaims().add(pos);
        claims.put(pos, player.getUniqueId());
        save();
        return true;
    }

    public boolean unclaimChunk(UUID playerId, ChunkPos pos) {
        UUID owner = claims.get(pos);
        if (owner == null || !owner.equals(playerId)) {
            return false;
        }
        claims.remove(pos);
        getOrCreateData(playerId).getClaims().remove(pos);
        save();
        return true;
    }

    public void addTrusted(UUID owner, UUID trusted) {
        getOrCreateData(owner).getTrusted().add(trusted);
        save();
    }

    public boolean removeTrusted(UUID owner, UUID trusted) {
        boolean removed = getOrCreateData(owner).getTrusted().remove(trusted);
        if (removed) {
            save();
        }
        return removed;
    }

    public void unclaimAll(UUID playerId) {
        PlayerData data = getOrCreateData(playerId);
        for (ChunkPos pos : new HashSet<>(data.getClaims())) {
            claims.remove(pos);
        }
        data.getClaims().clear();
        save();
    }

    private PlayerData getOrCreateData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, id -> new PlayerData());
    }
}
