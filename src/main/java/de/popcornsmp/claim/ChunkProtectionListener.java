package de.popcornsmp.claim;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.UUID;

public class ChunkProtectionListener implements Listener {
    private final ChunkManager manager;

    public ChunkProtectionListener(ChunkManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!canModify(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canModify(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!canModify(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
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
        if (!manager.isClaimed(to)) {
            return;
        }
        ChunkPos from = ChunkPos.of(event.getBlock().getChunk());
        UUID toOwner = manager.getOwner(to).orElse(null);
        UUID fromOwner = manager.getOwner(from).orElse(null);
        if (toOwner == null || !toOwner.equals(fromOwner)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> isProtected(block.getLocation()));
        if (event.blockList().isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> isProtected(block.getLocation()));
        if (event.blockList().isEmpty()) {
            event.setCancelled(true);
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
}
