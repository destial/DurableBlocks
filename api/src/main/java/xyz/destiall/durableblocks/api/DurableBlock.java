package xyz.destiall.durableblocks.api;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public interface DurableBlock {
    Block getBlock();
    int getStage();
    void setStage(int stage);
    int nextStage();
    long timePerStage();
    void setTimePerStage(long time);
    long getExpiryLength();
    void setExpiryLength(long expiry);
    Sound getBreakSound();
    Effect getBreakEffect();
}
