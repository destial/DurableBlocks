package xyz.destiall.durableblocks.api;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.block.Block;

class DurableBlockImpl implements DurableBlock {
    private int stage;
    private long interval;
    private long expiry;
    private final Block block;
    private final Sound breakSound;
    private final Effect effect;
    public DurableBlockImpl(Block block) {
        this.block = block;
        stage = 0;
        interval = DurableBlocksAPI.getConfig().getInt("blocks." + block.getType().name() + ".milliseconds-per-stage");
        expiry = DurableBlocksAPI.getConfig().getInt("blocks." + block.getType().name() + ".expiry-length-after-stop-mining");
        breakSound = DurableBlocksAPI.getConfig().getSound("blocks." + block.getType().name() + ".block-break-sound");
        effect = DurableBlocksAPI.getConfig().getEffect("blocks." + block.getType().name() + ".block-break-effect");
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
            stage = 0;
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
}
