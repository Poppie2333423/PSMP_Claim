package de.popcornsmp.claim;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ChunkHighlighter {
    private final PopcornSMPPlugin plugin;

    public ChunkHighlighter(PopcornSMPPlugin plugin) {
        this.plugin = plugin;
    }

    public void highlight(Player player, Chunk chunk, int seconds) {
        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        double y = player.getLocation().getY();
        World world = chunk.getWorld();
        new BukkitRunnable() {
            private int ticks = seconds * 20;

            @Override
            public void run() {
                if (ticks <= 0 || !player.isOnline()) {
                    cancel();
                    return;
                }
                ticks -= 10;
                for (int i = 0; i <= 16; i++) {
                    spawnParticle(player, world, baseX + i, y, baseZ);
                    spawnParticle(player, world, baseX + i, y, baseZ + 16);
                    spawnParticle(player, world, baseX, y, baseZ + i);
                    spawnParticle(player, world, baseX + 16, y, baseZ + i);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void spawnParticle(Player player, World world, double x, double y, double z) {
        for (int h = 0; h <= 3; h++) {
            Location location = new Location(world, x + 0.5, y + h, z + 0.5);
            player.spawnParticle(Particle.VILLAGER_HAPPY, location, 1, 0, 0, 0, 0);
        }
    }
}
