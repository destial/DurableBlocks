package xyz.destiall.durableblocks.nms.protocollib;

import org.bukkit.block.Block;
import xyz.destiall.durableblocks.api.DurableBlock;

public class DurableBlockImpl implements DurableBlock {
    private int stage;
    private final Block block;
    public DurableBlockImpl(Block block) {
        this.block = block;
        stage = 0;
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
}
