package de.popcornsmp.claim;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClaimStorage {
    private final JavaPlugin plugin;
    private final File file;

    public ClaimStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "claims.yml");
    }

    public Map<UUID, PlayerData> load() {
        if (!file.exists()) {
            return new HashMap<>();
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Map<UUID, PlayerData> data = new HashMap<>();
        if (config.contains("players")) {
            for (String id : config.getConfigurationSection("players").getKeys(false)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(id);
                } catch (IllegalArgumentException ex) {
                    continue;
                }
                PlayerData playerData = new PlayerData();
                List<String> claimList = config.getStringList("players." + id + ".claims");
                for (String claimKey : claimList) {
                    String[] split = claimKey.split(";");
                    if (split.length != 3) {
                        continue;
                    }
                    String world = split[0];
                    try {
                        int x = Integer.parseInt(split[1]);
                        int z = Integer.parseInt(split[2]);
                        playerData.getClaims().add(new ChunkPos(world, x, z));
                    } catch (NumberFormatException ex) {
                        // ignore malformed entry
                    }
                }
                List<String> trustedList = config.getStringList("players." + id + ".trusted");
                for (String trustedId : trustedList) {
                    try {
                        playerData.getTrusted().add(UUID.fromString(trustedId));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                data.put(uuid, playerData);
            }
        }
        return data;
    }

    public void save(Map<UUID, PlayerData> data) {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            plugin.getLogger().severe("Could not create data folder for claims.yml");
            return;
        }
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerData> entry : data.entrySet()) {
            String path = "players." + entry.getKey();
            Set<String> claims = new HashSet<>();
            for (ChunkPos pos : entry.getValue().getClaims()) {
                claims.add(pos.getWorld() + ";" + pos.getX() + ";" + pos.getZ());
            }
            config.set(path + ".claims", claims.stream().toList());
            Set<String> trusted = new HashSet<>();
            for (UUID trustedId : entry.getValue().getTrusted()) {
                trusted.add(trustedId.toString());
            }
            config.set(path + ".trusted", trusted.stream().toList());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save claims.yml: " + e.getMessage());
        }
    }
}
