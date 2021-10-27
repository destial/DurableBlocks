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

    private final HashMap<UUID, HashMap<DurableBlock, Long>> blockExpiry;
    private final HashMap<UUID, HashMap<DurableBlock, Long>> blockNextPossible;
    private final HashMap<UUID, DurableBlock> miningBlocks;

    public BlockListener(DurableBlocksPlugin plugin) {
        this.plugin = plugin;
        blockExpiry = new HashMap<>();
        miningBlocks = new HashMap<>();
        blockNextPossible = new HashMap<>();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<UUID, HashMap<DurableBlock, Long>> entry : blockExpiry.entrySet()) {
                HashSet<DurableBlock> clearingBlocks = new HashSet<>();
                ConnectedPlayer player = DurableBlocksAPI.getManager().getPlayer(entry.getKey());
                if (player == null) continue;
                for (Map.Entry<DurableBlock, Long> value : entry.getValue().entrySet()) {
                    if (value.getValue() <= System.currentTimeMillis()) {
                        player.sendBlockBreakingAnimation(value.getKey().getBlock(), -1);
                        clearingBlocks.add(value.getKey());
                    } else {
                        player.sendBlockBreakingAnimation(value.getKey().getBlock(), value.getKey().getStage());
                    }
                }
                for (DurableBlock clearing : clearingBlocks) {
                    entry.getValue().remove(clearing);
                    DurableBlocksAPI.getManager().unregisterBlock(clearing.getBlock().getLocation());
                    player.sendBlockBreakingAnimation(clearing.getBlock(), -1);
                }
                clearingBlocks.clear();
            }
            HashSet<DurableBlock> clearingBlocks = new HashSet<>();
            for (Map.Entry<UUID, DurableBlock> entry : miningBlocks.entrySet()) {
                ConnectedPlayer player = DurableBlocksAPI.getManager().getPlayer(entry.getKey());
                if (player == null) continue;
                if (!DurableBlocksAPI.getConfig().getBool("always-fatigue")) player.addFatigue();

                HashMap<DurableBlock, Long> blocks = blockNextPossible.get(entry.getKey());
                if (blocks == null) continue;
                Map.Entry<DurableBlock, Long> en = blocks.entrySet().stream().filter(e -> e.getKey() == entry.getValue()).findFirst().orElse(null);
                if (en == null) continue;
                Long time = en.getValue();
                if (time <= System.currentTimeMillis()) {
                    int prev = entry.getValue().getStage();
                    if (prev == 9) {
                        breakBlock(entry.getValue(), player);
                        clearingBlocks.add(entry.getValue());
                        continue;
                    }
                    entry.getValue().setStage(prev + 1);
                    player.sendBlockBreakingAnimation(entry.getValue().getBlock(), entry.getValue().getStage());
                    blocks.put(entry.getValue(), System.currentTimeMillis() + entry.getValue().timePerStage());
                }
            }
            miningBlocks.entrySet().removeIf((en) -> clearingBlocks.contains(en.getValue()));
            clearingBlocks.clear();
        }, 0L, 1L);
    }

    @EventHandler
    public void onStartDigging(PlayerStartDiggingEvent e) {
        System.out.println("Start digging");
        final Player player = e.getPlayer();
        if (player == null || !player.isOnline() || player.getGameMode().equals(GameMode.CREATIVE)) return;
        final Location location = e.getBlock().getLocation();
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) return;
        final DurableBlock durableBlock = DurableBlocksAPI.getManager().getBlock(location);
        miningBlocks.put(player.getUniqueId(), durableBlock);
        HashMap<DurableBlock, Long> blocks = blockNextPossible.get(player.getUniqueId());
        if (blocks == null) {
            blocks = new HashMap<>();
        }
        blocks.put(durableBlock, System.currentTimeMillis() + durableBlock.timePerStage());
        blockNextPossible.put(player.getUniqueId(), blocks);
        e.setCancelled(true);
        HashMap<DurableBlock, Long> expiries = blockExpiry.get(player.getUniqueId());
        if (expiries != null) {
            expiries.remove(durableBlock);
        }
    }

    @EventHandler
    public void onStopDigging(PlayerStopDiggingEvent e) {
        System.out.println("Stop digging");
        if (miningBlocks.remove(e.getPlayer().getUniqueId()) != null) {
            DurableBlock durableBlock = DurableBlocksAPI.getManager().getBlock(e.getBlock().getLocation());
            HashMap<DurableBlock, Long> expiries = blockExpiry.computeIfAbsent(e.getPlayer().getUniqueId(), k -> new HashMap<>());
            if (durableBlock.getStage() == 9) {
                expiries.remove(durableBlock);
                return;
            }
            expiries.put(durableBlock, System.currentTimeMillis() + durableBlock.getExpiryLength());
        }
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
        DurableBlocksAPI.getManager().unregisterBlock(block.getBlock().getLocation());
        HashMap<DurableBlock, Long> expiries = blockExpiry.get(player.getBasePlayer().getUniqueId());
        if (expiries != null) {
            expiries.remove(block);
        }
        miningBlocks.remove(player.getBasePlayer().getUniqueId());
        final Material blockType = block.getBlock().getType();
        // TODO: Add drops
        // block.getBlock().getWorld().dropItem(block.getBlock().getLocation(), new ItemStack(block.get, 1));
        player.getBasePlayer().playSound(block.getBlock().getLocation(), block.getBreakSound(), (float) 1.0, (float) 0.8);
        player.getBasePlayer().playEffect(block.getBlock().getLocation(), block.getBreakEffect(), blockType);
        block.getBlock().setType(block.getBrokenBlock());
        player.sendBlockBreakingAnimation(block.getBlock(), -1);
        if (block.getBrokenBlock().isBlock() && block.getBrokenBlock().isSolid()) {
            onStartDigging(new PlayerStartDiggingEvent(player.getBasePlayer(), block.getBlock()));
        }
    }
}
