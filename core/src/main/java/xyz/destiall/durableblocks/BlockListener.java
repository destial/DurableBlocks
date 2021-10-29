package xyz.destiall.durableblocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import java.util.Map;
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
                Long time = blockNextPossible.get(entry.getValue());
                if (time == null) continue;
                // player.sendArmSwing();
                if (time <= System.currentTimeMillis()) {
                    int prev = entry.getValue().getStage();
                    if (prev == 9) {
                        entry.getValue().setStage(-1);
                        breakBlock(entry.getValue(), player);
                        clearing.add(entry.getKey());
                        continue;
                    } else {
                        entry.getValue().nextStage();
                        DurableBlocksAPI.getNMS().sendBreakingAnimation(null, entry.getValue());
                        ItemStack inHand = player.getBasePlayer().getItemInHand();
                        long multiplier = 1;
                        if (inHand != null) {
                            if (!entry.getValue().getBlock().getDrops(inHand).isEmpty()) {
                                multiplier = ToolCheck.getToolSpeedAgainstBlock(entry.getValue().getBlock().getType(), inHand.getType());
                                if (multiplier != 1) {
                                    if (ToolCheck.isPickaxe(inHand.getType())) {
                                        multiplier *= ToolCheck.getPickaxeLevel(inHand.getType());
                                    } else if (ToolCheck.isAxe(inHand.getType())) {
                                        multiplier *= ToolCheck.getAxeLevel(inHand.getType());
                                    } else if (ToolCheck.isShovel(inHand.getType())) {
                                        multiplier *= ToolCheck.getShovelLevel(inHand.getType());
                                    }
                                    if (inHand.getEnchantments() != null && inHand.getEnchantments().containsKey(Enchantment.DIG_SPEED)) {
                                        multiplier *= inHand.getEnchantmentLevel(Enchantment.DIG_SPEED);
                                    }
                                }
                            }
                        }
                        blockNextPossible.put(entry.getValue(), System.currentTimeMillis() + (entry.getValue().timePerStage() * (1 / multiplier)));
                    }
                } else {
                    DurableBlocksAPI.getNMS().sendBreakingAnimation(null, entry.getValue());
                }
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
        final Player player = e.getPlayer();
        if (player == null || !player.isOnline() || player.getGameMode().equals(GameMode.CREATIVE)) return;
        final Location location = e.getBlock().getLocation();
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) return;
        final DurableBlock durableBlock = DurableBlocksAPI.getManager().getBlock(location);
        miningBlocks.put(player.getUniqueId(), durableBlock);
        ItemStack inHand = e.getPlayer().getItemInHand();
        long multiplier = 1;
        if (inHand != null) {
            if (!durableBlock.getBlock().getDrops(inHand).isEmpty()) {
                multiplier = ToolCheck.getToolSpeedAgainstBlock(durableBlock.getBlock().getType(), inHand.getType());
                if (multiplier != 1) {
                    if (ToolCheck.isPickaxe(inHand.getType())) {
                        multiplier *= ToolCheck.getPickaxeLevel(inHand.getType());
                    } else if (ToolCheck.isAxe(inHand.getType())) {
                        multiplier *= ToolCheck.getAxeLevel(inHand.getType());
                    } else if (ToolCheck.isShovel(inHand.getType())) {
                        multiplier *= ToolCheck.getShovelLevel(inHand.getType());
                    }
                }
                if (inHand.getEnchantments() != null && inHand.getEnchantments().containsKey(Enchantment.DIG_SPEED)) {
                    multiplier *= inHand.getEnchantmentLevel(Enchantment.DIG_SPEED);
                }
            }
        }
        blockNextPossible.put(durableBlock, System.currentTimeMillis() + (durableBlock.timePerStage() * (1 / multiplier)));
        // e.setCancelled(true);
        blockExpiry.remove(durableBlock);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        if (player == null || !player.isOnline() || player.getGameMode().equals(GameMode.CREATIVE)) return;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) e.setCancelled(true);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDamage(BlockDamageEvent e) {
        final Player player = e.getPlayer();
        if (player == null || !player.isOnline() || player.getGameMode().equals(GameMode.CREATIVE)) return;
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
        if (block.droppedItems() != null) {
            if (inHand != null) {
                long multiplier = ToolCheck.getToolSpeedAgainstBlock(blockType, inHand.getType());
                if (multiplier == 1 && block.needTool()) {
                    drop = false;
                }
                if (ToolCheck.isTool(inHand.getType())) {
                    int unbreaking = inHand.getEnchantmentLevel(Enchantment.DURABILITY);
                    if (unbreaking == 0) {
                        inHand.setDurability((short) (inHand.getDurability() + 1));
                    } else {
                        double rand = unbreaking - (Math.random() * unbreaking);
                        if (rand <= unbreaking / 2.f) {
                            inHand.setDurability((short) (inHand.getDurability() + 1));
                        }
                    }
                }
            }
        }
        if (drop) {
            if (inHand != null) {
                int fortune = inHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                for (int i = 0; i <= fortune; ++i) {
                    for (ItemStack d : block.droppedItems()) {
                        block.getBlock().getWorld().dropItemNaturally(block.getLocation(), d);
                    }
                }
            }
        }
        block.getBlock().setType(block.getBrokenBlock());
        player.getBasePlayer().playSound(block.getBlock().getLocation(), block.getBreakSound(), (float) 1.0, (float) 0.8);
        player.getBasePlayer().playEffect(block.getBlock().getLocation(), block.getBreakEffect(), blockType);
        block.setStage(-1);
        DurableBlocksAPI.getNMS().clearBreakingAnimation(block);
        if (block.getBrokenBlock().isBlock() && block.getBrokenBlock().isSolid()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isDigging())
                    onStartDigging(new PlayerStartDiggingEvent(player.getBasePlayer(), block.getBlock()));
            },1L);
        }
    }

    private String getProgress(int stage) {
        String progress = "&a";
        for (int i = 0; i < (stage + 1); ++i) {
            progress += "||";
        }
        progress += "&c";
        for (int i = 0; i < (10 - stage); ++i) {
            progress += "||";
        }
        return progress;
    }
}
