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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClaimManageMenu implements InventoryHolder {
    public static final String TITLE = "§6§lClaim-Verwaltung";
    private static final int[] TRUSTED_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 12, 14, 16, 17,
            18, 19, 20, 21, 23, 24, 25, 26
    };
    private final Inventory inventory;
    private final Map<Integer, UUID> trustedSlots = new HashMap<>();

    public ClaimManageMenu(Player player, ChunkManager manager) {
        this.inventory = Bukkit.createInventory(this, 27, TITLE);
        int count = manager.getClaimCount(player.getUniqueId());
        List<UUID> trustedPlayers = new ArrayList<>(manager.getTrustedPlayers(player.getUniqueId()));

        inventory.setItem(11, new ItemBuilder(Material.PAPER)
                .name("§6Alle Claims anzeigen")
                .lore(Arrays.asList(
                        "§7Zeigt eine Liste deiner Claims.",
                        "§7Klicke, um zu verwalten."
                ))
                .build());

        inventory.setItem(13, new ItemBuilder(Material.PLAYER_HEAD)
                .name("§6Spieler hinzufügen")
                .lore(Arrays.asList(
                        "§7Gewähre einem Spieler Zugriff auf alle deine Claims.",
                        "§7Klicke und gib danach den Namen im Chat ein."
                ))
                .build());

        String trustedLine;
        if (trustedPlayers.isEmpty()) {
            trustedLine = Message.highlight("Keine");
        } else {
            trustedLine = trustedPlayers.stream()
                    .map(uuid -> {
                        String name = Bukkit.getOfflinePlayer(uuid).getName();
                        return name != null ? name : "Unbekannt";
                    })
                    .collect(Collectors.joining("§7, §6", "§6", "")) + "§7";
        }

        inventory.setItem(15, new ItemBuilder(trustedPlayers.isEmpty() ? Material.BARRIER : Material.BLAZE_ROD)
                .name("§6Zugriffe verwalten")
                .lore(Arrays.asList(
                        "§7Klicke auf einen Kopf, um einen Spieler zu entfernen.",
                        "",
                        "§7Aktuelle Zugänge: " + trustedLine
                ))
                .build());

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
                    .lore(Arrays.asList(
                            "§7Klicke, um den Zugriff zu entziehen.",
                            "§7Gilt für alle deine Claims."
                    ))
                    .build());
            trustedSlots.put(slot, trusted);
        }

        if (trustedPlayers.isEmpty()) {
            inventory.setItem(4, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                    .name("§7Keine vertrauenswürdigen Spieler")
                    .lore(Arrays.asList(
                            "§7Füge Spieler hinzu, um sie hier zu verwalten."
                    ))
                    .build());
        }

        inventory.setItem(22, new ItemBuilder(Material.BOOK)
                .name("§6Status")
                .lore(Arrays.asList(
                        "§7Geclaimte Chunks: " + Message.highlight(count + " / 25"),
                        "§7Vertrauenswürdige Spieler: " + Message.highlight(String.valueOf(manager.getTrustedPlayers(player.getUniqueId()).size()))
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
