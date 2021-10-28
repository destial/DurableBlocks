package xyz.destiall.durableblocks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.destiall.durableblocks.DurableBlocksPlugin;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.DurableBlock;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;
import xyz.destiall.durableblocks.api.events.PlayerStartDiggingEvent;
import xyz.destiall.durableblocks.api.events.PlayerStopDiggingEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class BlockListener implements Listener {
    private final DurableBlocksPlugin plugin;

    private final HashMap<DurableBlock, Long> blockExpiry;
    private final HashMap<DurableBlock, Long> blockNextPossible;
    private final HashMap<UUID, DurableBlock> miningBlocks;

    public BlockListener(DurableBlocksPlugin plugin) {
        this.plugin = plugin;
        blockExpiry = new HashMap<>();
        miningBlocks = new HashMap<>();
        blockNextPossible = new HashMap<>();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            HashSet<Object> clearing = new HashSet<>();
            for (Map.Entry<DurableBlock, Long> entry : blockExpiry.entrySet()) {
                if (entry.getValue() <= System.currentTimeMillis()) {
                    entry.getKey().setStage(-1);
                    DurableBlocksAPI.getNMS().clearBreakingAnimation(entry.getKey());
                    DurableBlocksAPI.getManager().unregisterBlock(entry.getKey().getBlock().getLocation());
                    clearing.add(entry.getKey());
                } else {
                    DurableBlocksAPI.getNMS().sendBreakingAnimation(null, entry.getKey());
                }
            }
            for (Object clear : clearing) {
                blockExpiry.remove(clear);
            }
            clearing.clear();

            for (Map.Entry<UUID, DurableBlock> entry : miningBlocks.entrySet()) {
                ConnectedPlayer player = DurableBlocksAPI.getManager().getPlayer(entry.getKey());
                if (player == null) {
                    clearing.add(entry.getKey());
                    continue;
                }
                if (!DurableBlocksAPI.getConfig().getBool("always-fatigue")) player.addFatigue();
                Long time = blockNextPossible.get(entry.getValue());
                if (time <= System.currentTimeMillis()) {
                    int prev = entry.getValue().getStage();
                    if (prev == 9) {
                        entry.getValue().setStage(-1);
                        breakBlock(entry.getValue(), player);
                        clearing.add(entry.getKey());
                        continue;
                    }
                    entry.getValue().nextStage();
                    DurableBlocksAPI.getNMS().sendBreakingAnimation(null, entry.getValue());
                    blockNextPossible.put(entry.getValue(), System.currentTimeMillis() + entry.getValue().timePerStage());
                } else {
                    DurableBlocksAPI.getNMS().sendBreakingAnimation(null, entry.getValue());
                }
            }
            for (Object clear : clearing) {
                miningBlocks.remove(clear);
            }
            clearing.clear();
        }, 0L, 1L);
    }

    @EventHandler
    public void onStartDigging(PlayerStartDiggingEvent e) {
        final Player player = e.getPlayer();
        if (player == null || !player.isOnline() || player.getGameMode().equals(GameMode.CREATIVE)) return;
        final Location location = e.getBlock().getLocation();
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) return;
        final DurableBlock durableBlock = DurableBlocksAPI.getManager().getBlock(location);
        miningBlocks.put(player.getUniqueId(), durableBlock);
        blockNextPossible.put(durableBlock, System.currentTimeMillis() + durableBlock.timePerStage());
        e.setCancelled(true);
        blockExpiry.remove(durableBlock);
    }

    @EventHandler
    public void onStopDigging(PlayerStopDiggingEvent e) {
        if (miningBlocks.remove(e.getPlayer().getUniqueId()) != null) {
            DurableBlock durableBlock = DurableBlocksAPI.getManager().getBlock(e.getBlock().getLocation());
            blockExpiry.put(durableBlock, System.currentTimeMillis() + durableBlock.getExpiryLength());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        miningBlocks.remove(e.getPlayer().getUniqueId());
    }

    private void breakBlock(final DurableBlock block, final ConnectedPlayer player) {
        if (block.getBlock().getType() == Material.AIR) return;
        final BlockBreakEvent breakEvt = new BlockBreakEvent(block.getBlock(), player.getBasePlayer());
        Bukkit.getPluginManager().callEvent(breakEvt);
        if (breakEvt.isCancelled()) return;
        DurableBlocksAPI.getManager().unregisterBlock(block.getBlock().getLocation());
        blockExpiry.remove(block);
        blockNextPossible.remove(block);
        miningBlocks.remove(player.getBasePlayer().getUniqueId());
        final Material blockType = block.getBlock().getType();
        // TODO: Add drops
        // block.getBlock().getWorld().dropItem(block.getBlock().getLocation(), new ItemStack(block.get, 1));
        player.getBasePlayer().playSound(block.getBlock().getLocation(), block.getBreakSound(), (float) 1.0, (float) 0.8);
        player.getBasePlayer().playEffect(block.getBlock().getLocation(), block.getBreakEffect(), blockType);
        block.getBlock().setType(block.getBrokenBlock());
        block.setStage(-1);
        DurableBlocksAPI.getNMS().clearBreakingAnimation(block);
        if (block.getBrokenBlock().isBlock() && block.getBrokenBlock().isSolid()) {
            onStartDigging(new PlayerStartDiggingEvent(player.getBasePlayer(), block.getBlock()));
        }
    }
}
