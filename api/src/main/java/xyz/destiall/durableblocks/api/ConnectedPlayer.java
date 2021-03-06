package xyz.destiall.durableblocks.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ConnectedPlayer {
    void sendPacket(Object packet);
    void sendBlockBreakingAnimation(Block block, int stage);
    void sendBlockChange(Location from, Material to);
    void sendActionBar(String message);
    void addFatigue(int duration, int amplifier);
    void removeFatigue();
    void sendArmSwing();
    boolean isDigging();
    void setDigging(boolean digging);
    Player getBasePlayer();
    void breakItem(ItemStack hand);
    int getBreakingSpeed(ItemStack hand, Material block);
    void unregister();
}
