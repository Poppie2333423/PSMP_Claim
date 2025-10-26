package de.popcornsmp.claim.menu;

import de.popcornsmp.claim.ChunkHighlighter;
import de.popcornsmp.claim.ChunkManager;
import de.popcornsmp.claim.ChunkPos;
import de.popcornsmp.claim.Message;
import de.popcornsmp.claim.PopcornSMPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuHandler {
    private final PopcornSMPPlugin plugin;
    private final ChunkManager manager;
    private final ChunkHighlighter highlighter;
    private final Map<UUID, PromptType> prompts = new ConcurrentHashMap<>();

    public MenuHandler(PopcornSMPPlugin plugin, ChunkManager manager, ChunkHighlighter highlighter) {
        this.plugin = plugin;
        this.manager = manager;
        this.highlighter = highlighter;
    }

    public void openMainMenu(Player player) {
        player.openInventory(new ChunkMainMenu(player, manager).getInventory());
    }

    public void openManageMenu(Player player) {
        player.openInventory(new ClaimManageMenu(player, manager).getInventory());
    }

    public void openClaimListMenu(Player player) {
        player.openInventory(new ClaimListMenu(player, manager).getInventory());
    }

    public void highlightCurrentChunk(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        highlightChunk(player, ChunkPos.of(chunk));
    }

    public void highlightChunk(Player player, ChunkPos pos) {
        World world = Bukkit.getWorld(pos.getWorld());
        if (world == null) {
            Message.sendError(player, "Die Welt §6" + pos.getWorld() + "§c ist nicht geladen.");
            return;
        }
        Chunk chunk = world.getChunkAt(pos.getX(), pos.getZ());
        highlighter.highlight(player, chunk, 15);
        Message.send(player, "Chunk §6" + pos.getX() + "§7/§6" + pos.getZ() + " §7wurde markiert.");
        if (!player.getWorld().equals(world)) {
            Message.send(player, "§7Hinweis: Du befindest dich nicht in §6" + world.getName() + "§7.");
        }
    }

    public boolean toggleClaim(Player player) {
        Chunk chunk = player.getLocation().getChunk();
        ChunkPos pos = ChunkPos.of(chunk);
        Optional<UUID> owner = manager.getOwner(pos);
        if (owner.isPresent() && owner.get().equals(player.getUniqueId())) {
            manager.unclaimChunk(player.getUniqueId(), pos);
            Message.send(player, "Chunk §6" + chunk.getX() + "§7/§6" + chunk.getZ() + " §7wurde freigegeben.");
            return true;
        }
        if (owner.isPresent()) {
            OfflinePlayer offlineOwner = Bukkit.getOfflinePlayer(owner.get());
            Message.sendError(player, "Dieser Chunk gehört bereits §6" + (offlineOwner.getName() != null ? offlineOwner.getName() : "jemandem") + "§c.");
            return false;
        }
        if (!manager.canClaimMore(player.getUniqueId())) {
            Message.sendError(player, "Du hast bereits die maximalen §625§c Claims erreicht.");
            return false;
        }
        boolean claimed = manager.claimChunk(player, chunk);
        if (claimed) {
            Message.send(player, "Chunk §6" + chunk.getX() + "§7/§6" + chunk.getZ() + " §7gehört nun dir.");
            return true;
        }
        Message.sendError(player, "Dieser Chunk kann nicht geclaimt werden.");
        return false;
    }

    public void beginPrompt(Player player, PromptType type) {
        prompts.put(player.getUniqueId(), type);
        if (type == PromptType.ADD_TRUST) {
            Message.send(player, "Gib den §6Spielernamen§7 im Chat ein, den du hinzufügen möchtest.");
        }
        Message.send(player, "Sende §6abbrechen§7, um den Vorgang zu stoppen.");
    }

    public boolean hasPrompt(UUID playerId) {
        return prompts.containsKey(playerId);
    }

    public void cancelPrompt(Player player) {
        prompts.remove(player.getUniqueId());
        Message.send(player, "Aktion abgebrochen.");
    }

    public boolean handleChat(Player player, String message) {
        PromptType type = prompts.remove(player.getUniqueId());
        if (type == null) {
            return false;
        }
        if (message.equalsIgnoreCase("abbrechen")) {
            Message.send(player, "Aktion abgebrochen.");
            Bukkit.getScheduler().runTask(plugin, () -> openManageMenu(player));
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(message);
        if (target.getUniqueId() == null) {
            Message.sendError(player, "Spieler konnte nicht gefunden werden.");
            Bukkit.getScheduler().runTask(plugin, () -> openManageMenu(player));
            return true;
        }
        if (type == PromptType.ADD_TRUST) {
            manager.addTrusted(player.getUniqueId(), target.getUniqueId());
            Message.send(player, "§6" + (target.getName() != null ? target.getName() : target.getUniqueId()) + "§7 hat nun Zugriff auf alle deine Claims.");
        }
        Bukkit.getScheduler().runTask(plugin, () -> openManageMenu(player));
        return true;
    }

    public enum PromptType {
        ADD_TRUST
    }
}
