package xyz.destiall.durableblocks.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Manager {
    private final HashMap<UUID, ConnectedPlayer> players;
    private final HashMap<Location, DurableBlock> blocks;
    private final HashMap<Integer, DurabilityBar> bars;
    public Manager() {
        players = new HashMap<>();
        blocks = new HashMap<>();
        bars = new HashMap<>();
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

    public DurabilityBar createDurabilityBar(int id, String message, DurabilityBar.Color color, float value) {
        if (bars.containsKey(id)) {
            DurabilityBar bar = bars.get(id);
            bar.setColor(color);
            bar.setMessage(message);
            bar.setValue(value);
            return bar;
        }
        DurabilityBar bar = new DurabilityBarImpl(color, message, value);
        bars.put(id, bar);
        return bar;
    }

    public DurabilityBar getDurabilityBar(int id) {
        return bars.get(id);
    }

    public void unregisterDurabilityBar(int id) {
        bars.remove(id);
    }
}
