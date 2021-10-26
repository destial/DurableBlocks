package xyz.destiall.durableblocks.api;

import org.bukkit.block.Block;

public interface DurableBlock {
    Block getBlock();
    int getStage();
    void setStage(int stage);
    int nextStage();
    default long timePerStage() {
        return 500;
    }
}
