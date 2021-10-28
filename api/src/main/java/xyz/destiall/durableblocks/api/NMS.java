package xyz.destiall.durableblocks.api;

import org.bukkit.entity.Player;

public interface NMS {
    ConnectedPlayer registerPlayer(Player player);
    void sendBreakingAnimation(Player player, DurableBlock durableBlock);
    void clearBreakingAnimation(DurableBlock durableBlock);
}
