package xyz.destiall.durableblocks.api;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface NMS {
    ConnectedPlayer registerPlayer(Player player);
    DurableBlock registerBlock(Block block);
}
