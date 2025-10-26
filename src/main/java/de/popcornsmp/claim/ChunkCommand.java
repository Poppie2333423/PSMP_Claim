package de.popcornsmp.claim;

import de.popcornsmp.claim.menu.MenuHandler;
import de.popcornsmp.claim.ChunkPos;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ChunkCommand implements CommandExecutor, TabCompleter {
    private final ChunkManager manager;
    private final MenuHandler menuHandler;

    public ChunkCommand(ChunkManager manager, MenuHandler menuHandler) {
        this.manager = manager;
        this.menuHandler = menuHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                Message.sendError(sender, "Dieser Befehl ist nur für Spieler verfügbar.");
                return true;
            }
            menuHandler.openMainMenu(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "claim":
                if (!(sender instanceof Player player)) {
                    Message.sendError(sender, "Nur Spieler können Chunks claimen.");
                    return true;
                }
                if (!manager.canClaimMore(player.getUniqueId())) {
                    Message.sendError(player, "Du hast bereits §625§c Claims erreicht.");
                    return true;
                }
                Chunk chunk = player.getLocation().getChunk();
                if (manager.getOwner(chunk).isPresent()) {
                    Message.sendError(player, "Dieser Chunk ist bereits geclaimed.");
                    return true;
                }
                if (manager.claimChunk(player, chunk)) {
                    Message.send(player, "Chunk §6" + chunk.getX() + "§7/§6" + chunk.getZ() + " §7gehört nun dir.");
                } else {
                    Message.sendError(player, "Der Chunk konnte nicht geclaimt werden.");
                }
                return true;
            case "unclaim":
                if (!(sender instanceof Player player)) {
                    Message.sendError(sender, "Nur Spieler können Claims entfernen.");
                    return true;
                }
                ChunkPos pos = ChunkPos.of(player.getLocation().getChunk());
                if (manager.unclaimChunk(player.getUniqueId(), pos)) {
                    Message.send(player, "Chunk §6" + pos.getX() + "§7/§6" + pos.getZ() + " §7wurde freigegeben.");
                } else {
                    Message.sendError(player, "Du besitzt diesen Chunk nicht.");
                }
                return true;
            case "list":
                if (!(sender instanceof Player player)) {
                    Message.sendError(sender, "Nur Spieler können Claims auflisten.");
                    return true;
                }
                if (manager.getClaims(player.getUniqueId()).isEmpty()) {
                    Message.send(player, "Du hast derzeit keine Chunks geclaimt.");
                    return true;
                }
                Message.send(player, "Deine Claims:");
                manager.getClaims(player.getUniqueId()).forEach(claim ->
                        Message.send(player, " - " + Message.highlight(claim.getWorld() + " §7| §6" + claim.getX() + "§7, §6" + claim.getZ())));
                return true;
            case "highlight":
                if (!(sender instanceof Player player)) {
                    Message.sendError(sender, "Nur Spieler können Chunks markieren.");
                    return true;
                }
                menuHandler.highlightCurrentChunk(player);
                return true;
            case "trust":
                if (!(sender instanceof Player player)) {
                    Message.sendError(sender, "Nur Spieler können Zugriffe vergeben.");
                    return true;
                }
                if (args.length < 2) {
                    Message.sendError(player, "Nutze §6/chunk trust <Spieler>§c.");
                    return true;
                }
                OfflinePlayer toTrust = Bukkit.getOfflinePlayer(args[1]);
                if (toTrust.getUniqueId() == null) {
                    Message.sendError(player, "Spieler konnte nicht gefunden werden.");
                    return true;
                }
                manager.addTrusted(player.getUniqueId(), toTrust.getUniqueId());
                Message.send(player, "§6" + (toTrust.getName() != null ? toTrust.getName() : toTrust.getUniqueId()) + "§7 hat nun Zugriff auf alle deine Claims.");
                return true;
            case "untrust":
                if (!(sender instanceof Player player)) {
                    Message.sendError(sender, "Nur Spieler können Zugriffe entziehen.");
                    return true;
                }
                if (args.length < 2) {
                    Message.sendError(player, "Nutze §6/chunk untrust <Spieler>§c.");
                    return true;
                }
                OfflinePlayer toUntrust = Bukkit.getOfflinePlayer(args[1]);
                if (toUntrust.getUniqueId() == null) {
                    Message.sendError(player, "Spieler konnte nicht gefunden werden.");
                    return true;
                }
                if (manager.removeTrusted(player.getUniqueId(), toUntrust.getUniqueId())) {
                    Message.send(player, "§6" + (toUntrust.getName() != null ? toUntrust.getName() : toUntrust.getUniqueId()) + "§7 hat keinen Zugriff mehr.");
                } else {
                    Message.sendError(player, "Dieser Spieler hatte keinen Zugriff.");
                }
                return true;
            case "trusted":
                if (!(sender instanceof Player player)) {
                    Message.sendError(sender, "Nur Spieler können Zugriffe anzeigen.");
                    return true;
                }
                if (manager.getTrustedPlayers(player.getUniqueId()).isEmpty()) {
                    Message.send(player, "Es sind keine Spieler eingetragen.");
                    return true;
                }
                List<String> names = manager.getTrustedPlayers(player.getUniqueId()).stream()
                        .map(uuid -> {
                            String name = Bukkit.getOfflinePlayer(uuid).getName();
                            return name != null ? name : uuid.toString();
                        })
                        .collect(Collectors.toList());
                Message.send(player, "Aktueller Zugriff: " + Message.highlight(String.join("§7, §6", names)));
                return true;
            default:
                Message.sendError(sender, "Unbekannter Unterbefehl.");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("claim", "unclaim", "list", "highlight", "trust", "untrust", "trusted");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("trust") || args[0].equalsIgnoreCase("untrust"))) {
            List<String> suggestions = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                suggestions.add(online.getName());
            }
            return suggestions;
        }
        return List.of();
    }
}
