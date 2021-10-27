package xyz.destiall.durableblocks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
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

    private final HashMap<UUID, HashMap<DurableBlock, Long>> blockExpiry;
    private final HashMap<UUID, HashMap<DurableBlock, Long>> blockNextPossible;
    private final HashMap<UUID, DurableBlock> miningBlocks;

    public BlockListener(DurableBlocksPlugin plugin) {
        this.plugin = plugin;
        blockExpiry = new HashMap<>();
        miningBlocks = new HashMap<>();
        blockNextPossible = new HashMap<>();
        Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin) DurableBlocksAPI.get(), () -> {
            for (Map.Entry<UUID, HashMap<DurableBlock, Long>> entry : blockExpiry.entrySet()) {
                HashSet<DurableBlock> clearingBlocks = new HashSet<>();
                ConnectedPlayer player = DurableBlocksAPI.getManager().getPlayer(entry.getKey());
                if (player == null) continue;
                for (Map.Entry<DurableBlock, Long> value : entry.getValue().entrySet()) {
                    if (value.getValue() <= System.currentTimeMillis()) {
                        player.sendBlockBreakingAnimation(value.getKey().getBlock(), -1);
                        clearingBlocks.add(value.getKey());
                    } else {
                        // player.sendBlockBreakingAnimation(value.getKey().getBlock(), value.getKey().getStage());
                    }
                }
                for (DurableBlock clearing : clearingBlocks) {
                    entry.getValue().entrySet().removeIf(e -> e.getKey() == clearing);
                    DurableBlocksAPI.getManager().unregisterBlock(clearing.getBlock().getLocation());
                    player.sendBlockBreakingAnimation(clearing.getBlock(), -1);
                }
                clearingBlocks.clear();
            }
            for (Map.Entry<UUID, DurableBlock> entry : miningBlocks.entrySet()) {
                ConnectedPlayer player = DurableBlocksAPI.getManager().getPlayer(entry.getKey());
                if (player == null) continue;
                HashMap<DurableBlock, Long> blocks = blockNextPossible.get(entry.getKey());
                Map.Entry<DurableBlock, Long> en = blocks.entrySet().stream().filter(e -> e.getKey() == entry.getValue()).findFirst().orElse(null);
                if (en == null) continue;
                Long time = en.getValue();
                if (time <= System.currentTimeMillis()) {
                    int prev = entry.getValue().getStage();
                    if (prev == 9) {
                        breakBlock(entry.getValue(), player);
                        continue;
                    }
                    entry.getValue().setStage(prev + 1);
                    player.sendBlockBreakingAnimation(entry.getValue().getBlock(), entry.getValue().getStage());
                    blocks.put(entry.getValue(), System.currentTimeMillis() + entry.getValue().timePerStage());
                } else {
                    // player.sendBlockBreakingAnimation(entry.getValue().getBlock(), entry.getValue().getStage());
                }
            }
        }, 0L, 1L);
    }

    @EventHandler
    public void onStartDigging(PlayerStartDiggingEvent e) {
        final Player player = e.getPlayer();
        if (player == null || !player.isOnline()) return;
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;

        final Location location = e.getBlock().getLocation();
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) return;

        final DurableBlock durableBlock = DurableBlocksAPI.getManager().getBlock(location);
        final ConnectedPlayer connectedPlayer = DurableBlocksAPI.getManager().getPlayer(player.getUniqueId());
        miningBlocks.put(player.getUniqueId(), durableBlock);

        connectedPlayer.addFatigue();
        e.setCancelled(true);
        HashMap<DurableBlock, Long> expiries = blockExpiry.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        expiries.entrySet().removeIf(en -> en.getKey() == durableBlock);
        HashMap<DurableBlock, Long> blocks = blockNextPossible.get(player.getUniqueId());
        if (blocks == null) {
            blocks = new HashMap<>();
        }
        blocks.put(durableBlock, System.currentTimeMillis() + durableBlock.timePerStage());
        blockNextPossible.put(player.getUniqueId(), blocks);
    }

    @EventHandler
    public void onStopDigging(PlayerStopDiggingEvent e) {
        miningBlocks.remove(e.getPlayer().getUniqueId());
        DurableBlock durableBlock = DurableBlocksAPI.getManager().getBlock(e.getBlock().getLocation());
        if (durableBlock.getStage() == 9) {
            // breakBlock(e.getBlock(), e.getBlock().getLocation(), e.getPlayer());
            return;
        }
        HashMap<DurableBlock, Long> expiries = blockExpiry.computeIfAbsent(e.getPlayer().getUniqueId(), k -> new HashMap<>());
        expiries.put(durableBlock, System.currentTimeMillis() + durableBlock.getExpiryLength());
        ConnectedPlayer connectedPlayer = DurableBlocksAPI.getManager().getPlayer(e.getPlayer().getUniqueId());
        // connectedPlayer.removeFatigue();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        miningBlocks.remove(e.getPlayer().getUniqueId());
        blockNextPossible.remove(e.getPlayer().getUniqueId());
        blockExpiry.remove(e.getPlayer().getUniqueId());
    }

    private void breakBlock(final DurableBlock block, final ConnectedPlayer player) {
        if (block.getBlock().getType() == Material.AIR) return;
        final BlockBreakEvent breakEvt = new BlockBreakEvent(block.getBlock(), player.getBasePlayer());
        Bukkit.getPluginManager().callEvent(breakEvt);
        if (breakEvt.isCancelled()) return;
        final DurableBlock durableBlock = DurableBlocksAPI.getManager().getBlock(block.getBlock().getLocation());
        DurableBlocksAPI.getManager().unregisterBlock(block.getBlock().getLocation());
        HashMap<DurableBlock, Long> expiries = blockExpiry.computeIfAbsent(player.getBasePlayer().getUniqueId(), k -> new HashMap<>());
        expiries.entrySet().removeIf(en -> en.getKey() == durableBlock);
        // block.getBlock().getWorld().dropItem(block.getBlock().getLocation(), new ItemStack(blockType, 1));
        final Material blockType = block.getBlock().getType();
        player.getBasePlayer().playSound(block.getBlock().getLocation(), Sound.BLOCK_STONE_BREAK, (float) 1.0, (float) 0.8);
        player.getBasePlayer().playEffect(block.getBlock().getLocation(), Effect.STEP_SOUND, blockType);
        block.getBlock().setType(Material.DIRT);
    }
}
