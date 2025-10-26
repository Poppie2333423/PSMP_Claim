package de.popcornsmp.claim;

import org.bukkit.Color;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ChunkHighlighter {
    private static final Particle.DustOptions GOLD_DUST = new Particle.DustOptions(Color.fromRGB(255, 196, 66), 1.2f);

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
                for (int i = 0; i < 16; i++) {
                    spawnParticle(player, world, baseX + i, y, baseZ);
                    spawnParticle(player, world, baseX + i, y, baseZ + 15);
                    spawnParticle(player, world, baseX, y, baseZ + i);
                    spawnParticle(player, world, baseX + 15, y, baseZ + i);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void spawnParticle(Player player, World world, double x, double y, double z) {
        for (int h = 0; h <= 3; h++) {
            Location location = new Location(world, x + 0.5, y + h, z + 0.5);
            player.spawnParticle(Particle.REDSTONE, location, 1, 0.02, 0.0, 0.02, 0.0, GOLD_DUST);
            player.spawnParticle(Particle.END_ROD, location, 1, 0.02, 0.05, 0.02, 0.0);
        }
    }
}
