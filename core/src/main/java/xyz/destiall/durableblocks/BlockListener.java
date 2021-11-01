package xyz.destiall.durableblocks;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.DurableBlock;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;
import xyz.destiall.durableblocks.api.ToolCheck;
import xyz.destiall.durableblocks.api.events.PlayerStartDiggingEvent;
import xyz.destiall.durableblocks.api.events.PlayerStopDiggingEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

final class BlockListener implements Listener {
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
            // Clear all expired blocks
            HashSet<Object> clearing = new HashSet<>();
            for (Entry<DurableBlock, Long> entry : blockExpiry.entrySet()) {
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

            // Advance all mining blocks and clear all broken blocks
            for (Entry<UUID, DurableBlock> entry : miningBlocks.entrySet()) {
                ConnectedPlayer player = DurableBlocksAPI.getManager().getPlayer(entry.getKey());
                if (player == null) {
                    clearing.add(entry.getKey());
                    continue;
                }
                Long time = blockNextPossible.get(entry.getValue());
                if (time == null) continue;
                if (time <= System.currentTimeMillis()) {
                    int prev = entry.getValue().getStage();
                    if (prev == 9) {
                        // Block can now be broken, so break it
                        entry.getValue().setStage(-1);
                        breakBlock(entry.getValue(), player);
                        clearing.add(entry.getKey());
                        continue;
                    } else {
                        // Block can now advance, so set next stage
                        entry.getValue().nextStage();
                        DurableBlocksAPI.getNMS().sendBreakingAnimation(null, entry.getValue());
                        ItemStack inHand = player.getBasePlayer().getItemInHand();

                        // Dynamic breaking speed based on tool and player attributes
                        long multiplier = 1;
                        if (inHand != null) {
                            if (!entry.getValue().getBlock().getDrops(inHand).isEmpty()) {
                                multiplier = player.getBreakingSpeed(inHand, entry.getValue().getBlock().getType());
                            }
                        }
                        blockNextPossible.put(entry.getValue(), System.currentTimeMillis() + (entry.getValue().timePerStage() * (1 / multiplier)));
                    }
                } else {
                    DurableBlocksAPI.getNMS().sendBreakingAnimation(null, entry.getValue());
                }

                // Send block break progress
                player.sendActionBar(ChatColor.translateAlternateColorCodes('&', "&aProgress: " + getProgress(entry.getValue().getStage())));
            }
            for (Object clear : clearing) {
                miningBlocks.remove(clear);
            }
            clearing.clear();
        }, 0L, 1L);
    }

    @EventHandler
    public void onStartDigging(PlayerStartDiggingEvent e) {
        if (!DurableBlocksAPI.getManager().isEnabled(e.getBlock().getWorld())) return;

        // Put block as mining block
        final Player player = e.getPlayer();
        if (player == null || !player.isOnline() || player.getGameMode().equals(GameMode.CREATIVE)) return;
        final ConnectedPlayer connectedPlayer = DurableBlocksAPI.getManager().getPlayer(player.getUniqueId());
        final Location location = e.getBlock().getLocation();
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) return;
        final DurableBlock durableBlock = DurableBlocksAPI.getManager().getBlock(location);
        if (durableBlock.isUnbreakable()) return;
        if (durableBlock.timePerStage() <= 0) return;
        miningBlocks.put(player.getUniqueId(), durableBlock);
        ItemStack inHand = e.getPlayer().getItemInHand();

        long multiplier = 1;
        if (inHand != null) {
            if (!durableBlock.getBlock().getDrops(inHand).isEmpty()) {
                multiplier = connectedPlayer.getBreakingSpeed(inHand, durableBlock.getBlock().getType());
            }
        }
        blockNextPossible.put(durableBlock, System.currentTimeMillis() + (durableBlock.timePerStage() * (1 / multiplier)));
        blockExpiry.remove(durableBlock);
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        if (player == null || !player.isOnline() || player.getGameMode().equals(GameMode.CREATIVE)) return;
        if (!DurableBlocksAPI.getManager().isEnabled(player.getWorld())) return;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            DurableBlock block = DurableBlocksAPI.getManager().getBlock(e.getClickedBlock().getLocation());
            if (block.timePerStage() <= 0) {
                DurableBlocksAPI.getManager().unregisterBlock(block.getLocation());
                return;
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onStopDigging(PlayerStopDiggingEvent e) {
        // Remove block from mining and add expiration
        if (miningBlocks.remove(e.getPlayer().getUniqueId()) != null) {
            DurableBlock durableBlock = DurableBlocksAPI.getManager().getBlock(e.getBlock().getLocation());
            blockExpiry.put(durableBlock, System.currentTimeMillis() + durableBlock.getExpiryLength());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        miningBlocks.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDamage(BlockDamageEvent e) {
        final Player player = e.getPlayer();
        if (player == null || !player.isOnline() || player.getGameMode().equals(GameMode.CREATIVE)) return;
        if (!DurableBlocksAPI.getManager().isEnabled(player.getWorld())) return;
        DurableBlock block = DurableBlocksAPI.getManager().getBlock(e.getBlock().getLocation());
        if (block.timePerStage() <= 0) {
            DurableBlocksAPI.getManager().unregisterBlock(block.getLocation());
            return;
        }
        // No no insta break
        e.setInstaBreak(false);
        e.setCancelled(true);
    }

    private void breakBlock(final DurableBlock block, final ConnectedPlayer player) {
        if (block.getBlock().getType() == Material.AIR) return;
        final BlockBreakEvent breakEvt = new BlockBreakEvent(block.getBlock(), player.getBasePlayer());
        Bukkit.getPluginManager().callEvent(breakEvt);
        if (breakEvt.isCancelled()) return;
        blockExpiry.remove(block);
        blockNextPossible.remove(block);
        miningBlocks.remove(player.getBasePlayer().getUniqueId());
        DurableBlocksAPI.getManager().unregisterBlock(block.getBlock().getLocation());
        final Material blockType = block.getBlock().getType();
        boolean drop = true;
        ItemStack inHand = player.getBasePlayer().getItemInHand();
        if (inHand != null) {
            if (block.droppedItems() != null) {
                long multiplier = ToolCheck.getToolSpeedAgainstBlock(blockType, inHand.getType());
                if (multiplier == 1 && block.needTool()) {
                    drop = false;
                }
            }
            if (drop) {
                int fortune = inHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                if (block.droppedItems() != null) {
                    for (int i = 0; i <= fortune; ++i) {
                        for (ItemStack d : block.droppedItems()) {
                            block.getBlock().getWorld().dropItemNaturally(block.getLocation(), d);
                        }
                    }
                }
            }
            player.breakItem(inHand);
        }
        block.getBlock().setType(block.getBrokenBlock());
        player.getBasePlayer().playSound(block.getBlock().getLocation(), block.getBreakSound(), (float) 1.0, (float) 0.8);
        player.getBasePlayer().playEffect(block.getBlock().getLocation(), block.getBreakEffect(), blockType);
        block.setStage(-1);
        DurableBlocksAPI.getNMS().clearBreakingAnimation(block);
        if (block.getBrokenBlock().isBlock() && block.getBrokenBlock().isSolid()) {
            // If the broken block can be broken, start digging that block
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isDigging())
                    onStartDigging(new PlayerStartDiggingEvent(player.getBasePlayer(), block.getBlock()));
            },1L);
        }
    }

    private String getProgress(int stage) {
        StringBuilder progress = new StringBuilder("&a");
        for (int i = 0; i < (stage + 1); ++i) {
            progress.append("||");
        }
        progress.append("&c");
        for (int i = 0; i < (9 - stage); ++i) {
            progress.append("||");
        }
        return progress.toString();
    }
}
