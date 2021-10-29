package xyz.destiall.durableblocks.api;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public interface DurableBlock {
    Block getBlock();
    int getStage();
    void setStage(int stage);
    void nextStage();
    long timePerStage();
    void setTimePerStage(long time);
    long getExpiryLength();
    void setExpiryLength(long expiry);
    Sound getBreakSound();
    Effect getBreakEffect();
    Material getBrokenBlock();
    ItemStack[] droppedItems();
    int getId();
    Location getLocation();
    double getX();
    double getY();
    double getZ();
    World getWorld();
    boolean needTool();
}
