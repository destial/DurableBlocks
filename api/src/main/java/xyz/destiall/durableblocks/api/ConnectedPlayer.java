package xyz.destiall.durableblocks.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface ConnectedPlayer {
    void sendPacket(Object packet);
    void sendBlockBreakingAnimation(Block block, int stage);
    void sendBlockChange(Location from, Material to);
    void updateBlockNotify(Location location);
    void sendActionBar(String message);
    void sendDurabilityBar(DurabilityBar bar);
    void addFatigue();
    void removeFatigue();
    Player getBasePlayer();
}
