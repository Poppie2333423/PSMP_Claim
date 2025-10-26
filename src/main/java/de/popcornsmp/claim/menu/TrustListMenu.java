package de.popcornsmp.claim.menu;

import de.popcornsmp.claim.ChunkManager;
import de.popcornsmp.claim.ItemBuilder;
import de.popcornsmp.claim.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrustListMenu implements InventoryHolder {
    public static final String TITLE = "§6§lZugriffsverwaltung";
    private static final int[] TRUSTED_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 12, 14, 16, 17,
            18, 19, 20, 21, 23, 24, 25, 26
    };

    private final Inventory inventory;
    private final Map<Integer, UUID> trustedSlots = new HashMap<>();

    public TrustListMenu(Player player, ChunkManager manager) {
        this.inventory = Bukkit.createInventory(this, 27, TITLE);

        inventory.setItem(18, new ItemBuilder(Material.ARROW)
                .name("§6Zurück")
                .lore(Collections.singletonList("§7Klicke, um zur Claim-Verwaltung zurückzukehren."))
                .build());

        List<UUID> trustedPlayers = new ArrayList<>(manager.getTrustedPlayers(player.getUniqueId()));
        if (trustedPlayers.isEmpty()) {
            inventory.setItem(4, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("§7Keine vertrauenswürdigen Spieler")
                    .lore(Collections.singletonList("§7Füge Spieler hinzu, um sie hier zu verwalten."))
                    .build());
        } else {
            int index = 0;
            for (UUID trusted : trustedPlayers) {
                if (index >= TRUSTED_SLOTS.length) {
                    break;
                }
                int slot = TRUSTED_SLOTS[index++];
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(trusted);
                String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : trusted.toString();
                inventory.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                        .skullOwner(trusted)
                        .name("§6" + name)
                        .lore(Collections.singletonList("§7Klicke, um den Zugriff zu entziehen."))
                        .build());
                trustedSlots.put(slot, trusted);
            }
        }

        inventory.setItem(22, new ItemBuilder(Material.BOOK)
                .name("§6Status")
                .lore(Arrays.asList(
                        "§7Vertrauenswürdige Spieler: " + Message.highlight(String.valueOf(trustedPlayers.size())),
                        "§7Zugriff gilt für alle Claims."
                ))
                .build());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public UUID getTrustedAt(int slot) {
        return trustedSlots.get(slot);
    }
}
