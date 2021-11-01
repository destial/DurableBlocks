package xyz.destiall.durableblocks.api;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;

class DurableBlockImpl implements DurableBlock {
    private int stage;
    private long interval;
    private long expiry;
    private final Block block;
    private final Sound breakSound;
    private final Effect effect;
    private final Material convert;
    private final ItemStack[] drops;
    private final int id;
    private final boolean need;
    private final boolean unbreakable;
    public DurableBlockImpl(Block block) {
        id = (int) (Math.random() * 500);
        this.block = block;
        stage = -1;
        Map<String, Object> mapping = DurableBlocksAPI.getConfig().getMapping(block.getType());
        interval = mapping != null ? (int) mapping.getOrDefault("milliseconds-per-stage", 500) : 0;
        expiry = mapping != null ? (int) mapping.getOrDefault("expiry-length-after-stop-mining", 5000) : 0;
        need = mapping != null && (boolean) mapping.getOrDefault("need-tool-for-drops", true);
        unbreakable = mapping != null && (boolean) mapping.getOrDefault("unbreakable", false);
        breakSound = mapping != null ? DurableBlocksAPI.getConfig().getSound(block.getType()) : Arrays.stream(Sound.values()).filter(s -> s.name().equals("BLOCK_STONE_BREAK") || s.name().equals("DIG_STONE")).findFirst().get();
        effect = mapping != null ? DurableBlocksAPI.getConfig().getEffect(block.getType()) : Effect.STEP_SOUND;
        convert = mapping != null ? DurableBlocksAPI.getConfig().getConvert(block.getType()) : Material.AIR;
        drops = mapping != null ? DurableBlocksAPI.getConfig().getStacks(block.getType()).toArray(new ItemStack[0]) : null;
    }
    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public int getStage() {
        return stage;
    }

    @Override
    public void setStage(int stage) {
        this.stage = stage;
    }

    @Override
    public void nextStage() {
        if (stage > 9) {
            stage = -1;
        }
        stage++;
    }

    @Override
    public long timePerStage() {
        return interval;
    }

    @Override
    public void setTimePerStage(long time) {
        interval = time;
    }

    @Override
    public long getExpiryLength() {
        return expiry;
    }

    @Override
    public void setExpiryLength(long expiry) {
        this.expiry = expiry;
    }

    @Override
    public Sound getBreakSound() {
        return breakSound;
    }

    @Override
    public Effect getBreakEffect() {
        return effect;
    }

    @Override
    public Material getBrokenBlock() {
        return convert;
    }

    @Override
    public ItemStack[] droppedItems() {
        return drops;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Location getLocation() {
        return block.getLocation();
    }

    @Override
    public double getX() {
        return block.getX();
    }

    @Override
    public double getY() {
        return block.getY();
    }

    @Override
    public double getZ() {
        return block.getZ();
    }

    @Override
    public World getWorld() {
        return block.getWorld();
    }

    @Override
    public boolean needTool() {
        return need;
    }

    @Override
    public boolean isUnbreakable() {
        return unbreakable;
    }
}
