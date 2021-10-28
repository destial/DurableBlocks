package xyz.destiall.durableblocks.api;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

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
    public DurableBlockImpl(Block block) {
        id = (int) (Math.random() * 500);
        this.block = block;
        stage = -1;
        Map<String, Object> mapping = DurableBlocksAPI.getConfig().getMapping(block.getType());
        interval = (int) mapping.get("milliseconds-per-stage");
        expiry = (int) mapping.get("expiry-length-after-stop-mining");
        breakSound = Sound.valueOf((String) mapping.get("block-break-sound"));
        effect = Effect.valueOf((String) mapping.get("block-break-effect"));
        convert = Material.valueOf((String) mapping.get("block-break-type"));
        drops = DurableBlocksAPI.getConfig().getStacks(block.getType()).toArray(new ItemStack[0]);
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
    public int nextStage() {
        if (stage > 9) {
            stage = -1;
        }
        return stage++;
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
}
