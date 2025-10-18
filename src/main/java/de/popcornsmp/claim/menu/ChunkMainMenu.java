package de.popcornsmp.claim.menu;

import de.popcornsmp.claim.ChunkManager;
import de.popcornsmp.claim.ChunkPos;
import de.popcornsmp.claim.ItemBuilder;
import de.popcornsmp.claim.Message;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;

public class ChunkMainMenu implements InventoryHolder {
    public static final String TITLE = "§6§lChunk-Menü";
    private final Inventory inventory;

    public ChunkMainMenu(Player player, ChunkManager manager) {
        this.inventory = Bukkit.createInventory(this, 27, TITLE);
        Chunk chunk = player.getLocation().getChunk();
        ChunkPos pos = ChunkPos.of(chunk);

        inventory.setItem(11, new ItemBuilder(Material.MAP)
                .name("§6Aktueller Chunk")
                .lore(Arrays.asList(
                        "§7Welt: " + Message.highlight(pos.getWorld()),
                        "§7Koordinaten: " + Message.highlight(pos.getX() + ", " + pos.getZ()),
                        "",
                        "§7Klicke, um den Chunk für 15 Sekunden zu markieren."
                ))
                .build());

        boolean owned = manager.getOwner(chunk).map(owner -> owner.equals(player.getUniqueId())).orElse(false);
        boolean claimed = manager.isClaimed(chunk);
        if (owned) {
            inventory.setItem(13, new ItemBuilder(Material.LIME_BANNER)
                    .name("§6Chunk freigeben")
                    .lore(Arrays.asList(
                            "§7Dieser Chunk gehört dir.",
                            "§7Klicke, um den Claim zu entfernen."
                    ))
                    .build());
        } else if (claimed) {
            inventory.setItem(13, new ItemBuilder(Material.BARRIER)
                    .name("§cChunk bereits geclaimed")
                    .lore(Arrays.asList(
                            "§7Besitzer: " + manager.getOwner(chunk)
                                    .map(uuid -> {
                                        String name = Bukkit.getOfflinePlayer(uuid).getName();
                                        return name != null ? name : "Unbekannt";
                                    })
                                    .orElse("Unbekannt"),
                            "§7Du kannst diesen Chunk nicht claimen."
                    ))
                    .build());
        } else {
            inventory.setItem(13, new ItemBuilder(Material.ORANGE_BANNER)
                    .name("§6Chunk claimen")
                    .lore(Arrays.asList(
                            "§7Beanspruche diesen Chunk.",
                            "§7Noch verfügbar: " + Message.highlight((Math.max(0, 25 - manager.getClaimCount(player.getUniqueId()))) + " / 25")
                    ))
                    .build());
        }

        inventory.setItem(15, new ItemBuilder(Material.CHEST)
                .name("§6Verwaltung")
                .lore(Arrays.asList(
                        "§7Du hast derzeit " + Message.highlight(manager.getClaimCount(player.getUniqueId()) + " / 25") + "§7 Chunks.",
                        "§7Verwalte Claims und Zugriffsrechte."
                ))
                .build());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
