package de.popcornsmp.claim;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkProtectionListener implements Listener {
    private final ChunkManager manager;
    private final Map<String, UUID> placedTnt = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> primedTntOwners = new ConcurrentHashMap<>();
    private final Map<String, UUID> fluidSources = new ConcurrentHashMap<>();

    public ChunkProtectionListener(ChunkManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!canModify(event.getPlayer(), block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (block.getType() == Material.TNT) {
            placedTnt.remove(key(block.getLocation()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (!canModify(event.getPlayer(), block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        if (block.getType() == Material.TNT) {
            placedTnt.put(key(block.getLocation()), event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!canModify(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }
        Block clicked = event.getBlockClicked();
        Block target = clicked != null && event.getBlockFace() != null
                ? clicked.getRelative(event.getBlockFace())
                : event.getBlock();
        if (target != null) {
            fluidSources.put(key(target.getLocation()), event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!canModify(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!canModify(event.getPlayer(), event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!canModify(event.getPlayer(), event.getRightClicked().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!canModify(event.getPlayer(), event.getRightClicked().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!canModify(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) {
            if (isProtected(event.getEntity().getLocation())) {
                event.setCancelled(true);
            }
            return;
        }
        if (!canModify(player, event.getEntity().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (shouldCancelPiston(event.getBlock(), event.getBlocks(), event.getDirection(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (shouldCancelPiston(event.getBlock(), event.getBlocks(), event.getDirection(), true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        ChunkPos to = ChunkPos.of(event.getToBlock().getChunk());
        UUID sourcePlayer = fluidSources.get(key(event.getBlock().getLocation()));
        if (sourcePlayer != null) {
            fluidSources.put(key(event.getToBlock().getLocation()), sourcePlayer);
        }
        if (!manager.isClaimed(to)) {
            return;
        }
        ChunkPos from = ChunkPos.of(event.getBlock().getChunk());
        UUID toOwner = manager.getOwner(to).orElse(null);
        UUID fromOwner = manager.getOwner(from).orElse(null);
        if (toOwner != null && toOwner.equals(fromOwner)) {
            return;
        }
        if (sourcePlayer != null && manager.isTrusted(to, sourcePlayer)) {
            fluidSources.put(key(event.getToBlock().getLocation()), sourcePlayer);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        UUID responsible = resolveResponsiblePlayer(event.getEntity());
        event.blockList().removeIf(block -> !canExplosionAffect(block.getLocation(), responsible));
        if (event.blockList().isEmpty()) {
            event.setCancelled(true);
        }
        primedTntOwners.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        UUID responsible = null;
        if (event.getBlock().getType() == Material.TNT) {
            responsible = placedTnt.remove(key(event.getBlock().getLocation()));
        }
        event.blockList().removeIf(block -> !canExplosionAffect(block.getLocation(), responsible));
        if (event.blockList().isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof TNTPrimed tnt) {
            UUID source = placedTnt.remove(key(event.getLocation()));
            if (source != null) {
                primedTntOwners.put(tnt.getUniqueId(), source);
            }
        }
    }

    private boolean shouldCancelPiston(Block piston, java.util.List<Block> movedBlocks, BlockFace direction, boolean retract) {
        ChunkPos pistonPos = ChunkPos.of(piston.getChunk());
        UUID pistonOwner = manager.getOwner(pistonPos).orElse(null);
        for (Block block : movedBlocks) {
            ChunkPos from = ChunkPos.of(block.getChunk());
            Block targetBlock = retract ? block.getRelative(direction.getOppositeFace()) : block.getRelative(direction);
            ChunkPos to = ChunkPos.of(targetBlock.getChunk());
            if (isDifferentOwner(pistonOwner, from) || isDifferentOwner(pistonOwner, to)) {
                return true;
            }
        }
        if (!retract) {
            Block head = piston.getRelative(direction);
            ChunkPos headPos = ChunkPos.of(head.getChunk());
            return isDifferentOwner(pistonOwner, headPos);
        }
        return false;
    }

    private boolean isDifferentOwner(UUID pistonOwner, ChunkPos pos) {
        return manager.getOwner(pos)
                .filter(owner -> pistonOwner == null || !owner.equals(pistonOwner))
                .isPresent();
    }

    private boolean canModify(Player player, Location location) {
        Chunk chunk = location.getChunk();
        ChunkPos pos = ChunkPos.of(chunk);
        if (!manager.isClaimed(pos)) {
            return true;
        }
        if (manager.isTrusted(pos, player.getUniqueId())) {
            return true;
        }
        Message.sendError(player, "Du darfst hier nicht interagieren.");
        return false;
    }

    private boolean isProtected(Location location) {
        return manager.isClaimed(ChunkPos.of(location.getChunk()));
    }

    private boolean canExplosionAffect(Location location, UUID responsible) {
        ChunkPos pos = ChunkPos.of(location.getChunk());
        if (!manager.isClaimed(pos)) {
            return true;
        }
        if (responsible != null && manager.isTrusted(pos, responsible)) {
            return true;
        }
        return false;
    }

    private UUID resolveResponsiblePlayer(org.bukkit.entity.Entity entity) {
        if (entity instanceof Player player) {
            return player.getUniqueId();
        }
        if (entity instanceof TNTPrimed tnt) {
            org.bukkit.entity.Entity source = tnt.getSource();
            if (source instanceof Player player) {
                return player.getUniqueId();
            }
            UUID stored = primedTntOwners.get(tnt.getUniqueId());
            if (stored != null) {
                return stored;
            }
        }
        return null;
    }

    private String key(Location location) {
        return location.getWorld().getName() + ':' + location.getBlockX() + ':' + location.getBlockY() + ':' + location.getBlockZ();
    }
}
