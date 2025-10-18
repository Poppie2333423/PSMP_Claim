package de.popcornsmp.claim.menu;

import de.popcornsmp.claim.ChunkManager;
import de.popcornsmp.claim.ChunkPos;
import de.popcornsmp.claim.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClaimListMenu implements InventoryHolder {
    public static final String TITLE = "§6§lDeine Claims";

    private final Inventory inventory;
    private final Map<Integer, ChunkPos> slotMapping = new HashMap<>();

    public ClaimListMenu(Player player, ChunkManager manager) {
        List<ChunkPos> claims = new ArrayList<>(manager.getClaims(player.getUniqueId()));
        int size = Math.max(27, ((claims.size() / 9) + 1) * 9);
        this.inventory = Bukkit.createInventory(this, Math.min(size, 54), TITLE);

        if (claims.isEmpty()) {
            inventory.setItem(13, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("§6Keine Claims")
                    .lore(List.of("§7Du hast derzeit keine geclaimten Chunks."))
                    .build());
            return;
        }

        int slot = 0;
        for (ChunkPos pos : claims) {
            if (slot >= inventory.getSize()) {
                break;
            }
            inventory.setItem(slot, new ItemBuilder(Material.GRASS_BLOCK)
                    .name("§6" + pos.getWorld() + " §7| §6" + pos.getX() + "§7, §6" + pos.getZ())
                    .lore(List.of(
                            "§7Links-Klick: Chunk markieren",
                            "§7Rechts-Klick: Chunk freigeben"
                    ))
                    .build());
            slotMapping.put(slot, pos);
            slot++;
        }
    }

    public ChunkPos getChunkAt(int slot) {
        return slotMapping.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
