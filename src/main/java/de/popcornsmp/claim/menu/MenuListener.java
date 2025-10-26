package de.popcornsmp.claim.menu;

import de.popcornsmp.claim.ChunkManager;
import de.popcornsmp.claim.ChunkPos;
import de.popcornsmp.claim.Message;
import de.popcornsmp.claim.PopcornSMPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class MenuListener implements Listener {
    private final PopcornSMPPlugin plugin;
    private final MenuHandler handler;
    private final ChunkManager manager;

    public MenuListener(PopcornSMPPlugin plugin, MenuHandler handler, ChunkManager manager) {
        this.plugin = plugin;
        this.handler = handler;
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (holder instanceof ChunkMainMenu) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= inventory.getSize()) {
                return;
            }
            if (slot == 11) {
                player.closeInventory();
                handler.highlightCurrentChunk(player);
            } else if (slot == 13) {
                event.getWhoClicked().closeInventory();
                handler.toggleClaim(player);
                handler.openMainMenu(player);
            } else if (slot == 15) {
                handler.openManageMenu(player);
            }
        } else if (holder instanceof ClaimManageMenu manageMenu) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= inventory.getSize()) {
                return;
            }
            if (slot == 11) {
                handler.openClaimListMenu(player);
            } else if (slot == 13) {
                player.closeInventory();
                handler.beginPrompt(player, MenuHandler.PromptType.ADD_TRUST);
            } else {
                UUID trusted = manageMenu.getTrustedAt(slot);
                if (trusted == null) {
                    return;
                }
                boolean removed = manager.removeTrusted(player.getUniqueId(), trusted);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(trusted);
                String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : trusted.toString();
                if (removed) {
                    Message.send(player, "§6" + name + "§7 hat keinen Zugriff mehr auf deine Claims.");
                } else {
                    Message.sendError(player, "Dieser Spieler hatte keinen Zugriff.");
                }
                handler.openManageMenu(player);
            }
        } else if (holder instanceof ClaimListMenu listMenu) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= inventory.getSize()) {
                return;
            }
            ChunkPos pos = listMenu.getChunkAt(slot);
            if (pos == null) {
                return;
            }
            player.closeInventory();
            handler.highlightChunk(player, pos);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (handler.hasPrompt(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            String message = event.getMessage();
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(plugin, () -> handler.handleChat(player, message));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (handler.hasPrompt(event.getPlayer().getUniqueId())) {
            handler.cancelPrompt(event.getPlayer());
        }
    }
}
