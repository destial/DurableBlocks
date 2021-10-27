package xyz.destiall.durableblocks.api;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
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
    public DurableBlockImpl(Block block) {
        this.block = block;
        stage = -1;
        Map<String, Object> mapping = DurableBlocksAPI.getConfig().getMapping(block.getType());
        interval = (int) mapping.get("milliseconds-per-stage");
        expiry = (int) mapping.get("expiry-length-after-stop-mining");
        breakSound = Sound.valueOf((String) mapping.get("block-break-sound"));
        effect = Effect.valueOf((String) mapping.get("block-break-effect"));
        convert = Material.valueOf((String) mapping.get("block-break-type"));
        // TODO: Add drops
        drops = null;
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
}
