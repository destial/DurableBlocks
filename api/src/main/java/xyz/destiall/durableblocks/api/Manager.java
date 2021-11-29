package xyz.destiall.durableblocks.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class Manager {
    private final HashMap<UUID, ConnectedPlayer> players;
    private final HashMap<Location, DurableBlock> blocks;
    private final HashSet<String> enabledWorlds;
    public Manager() {
        players = new HashMap<>();
        blocks = new HashMap<>();
        enabledWorlds = new HashSet<>();
    }

    public ConnectedPlayer registerPlayer(Player player) {
        ConnectedPlayer connectedPlayer = DurableBlocksAPI.getNMS().registerPlayer(player);
        players.put(player.getUniqueId(), connectedPlayer);
        return connectedPlayer;
    }

    public void unregisterPlayer(UUID uuid) {
        ConnectedPlayer player = getPlayer(uuid);
        if (player != null) {
            player.unregister();
        }
        players.remove(uuid);
    }

    public DurableBlock registerBlock(Block block) {
        DurableBlock db = blocks.get(block.getLocation());
        if (db != null) return db;
        db = new DurableBlockImpl(block);
        blocks.put(block.getLocation(), db);
        return db;
    }

    public void unregisterBlock(Location location) {
        blocks.remove(location);
    }

    public ConnectedPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public DurableBlock getBlock(Location location) {
        return registerBlock(location.getBlock());
    }

    public void enableWorld(World world) {
        if (enabledWorlds.contains(world.getName())) return;
        enabledWorlds.add(world.getName());
    }

    public void disableWorld(World world) {
        enabledWorlds.remove(world.getName());
    }

    public boolean isEnabled(World world) {
        return enabledWorlds.contains(world.getName());
    }

    public void emptyWorlds() {
        enabledWorlds.clear();
    }

    public void clearBlocks() {
        blocks.clear();
    }
}
