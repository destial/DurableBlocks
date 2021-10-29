package xyz.destiall.durableblocks.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Manager {
    private final HashMap<UUID, ConnectedPlayer> players;
    private final HashMap<Location, DurableBlock> blocks;
    public Manager() {
        players = new HashMap<>();
        blocks = new HashMap<>();
    }

    public ConnectedPlayer registerPlayer(Player player) {
        ConnectedPlayer connectedPlayer = DurableBlocksAPI.getNMS().registerPlayer(player);
        players.put(player.getUniqueId(), connectedPlayer);
        return connectedPlayer;
    }

    public void unregisterPlayer(UUID uuid) {
        players.remove(uuid);
    }

    public DurableBlock registerBlock(Block block) {
        DurableBlock db = blocks.get(block.getLocation());
        if (db != null) return db;
        blocks.put(block.getLocation(), new DurableBlockImpl(block));
        return blocks.get(block.getLocation());
    }

    public void unregisterBlock(Location location) {
        blocks.remove(location);
    }

    public ConnectedPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public DurableBlock getBlock(Location location) {
        if (blocks.get(location) == null) {
            return registerBlock(location.getBlock());
        }
        return blocks.get(location);
    }
}
