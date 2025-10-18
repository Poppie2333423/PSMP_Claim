package de.popcornsmp.claim.menu;

import de.popcornsmp.claim.ChunkManager;
import de.popcornsmp.claim.ItemBuilder;
import de.popcornsmp.claim.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ClaimManageMenu implements InventoryHolder {
    public static final String TITLE = "§6§lClaim-Verwaltung";
    private final Inventory inventory;

    public ClaimManageMenu(Player player, ChunkManager manager) {
        this.inventory = Bukkit.createInventory(this, 27, TITLE);
        int count = manager.getClaimCount(player.getUniqueId());

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
        if (manager.getTrustedPlayers(player.getUniqueId()).isEmpty()) {
            trustedLine = Message.highlight("Keine");
        } else {
            trustedLine = manager.getTrustedPlayers(player.getUniqueId()).stream()
                    .map(uuid -> {
                        String name = Bukkit.getOfflinePlayer(uuid).getName();
                        return name != null ? name : "Unbekannt";
                    })
                    .collect(Collectors.joining("§7, §6", "§6", "")) + "§7";
        }

        inventory.setItem(15, new ItemBuilder(Material.REDSTONE)
                .name("§6Spieler entfernen")
                .lore(Arrays.asList(
                        "§7Entziehe einem Spieler den Zugriff.",
                        "§7Klicke und gib danach den Namen im Chat ein.",
                        "",
                        "§7Aktuelle Zugänge: " + trustedLine
                ))
                .build());

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
}
