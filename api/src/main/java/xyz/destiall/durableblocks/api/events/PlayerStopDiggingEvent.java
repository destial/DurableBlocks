package xyz.destiall.durableblocks.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerStopDiggingEvent extends Event implements Cancellable {
    private final Block block;
    private final Player player;
    public PlayerStopDiggingEvent(Player who, Block block) {
        super(false);
        this.block = block;
        player = who;
    }

    public Player getPlayer() {
        return player;
    }

    public Block getBlock() {
        return block;
    }

    public static final HandlerList list = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return list;
    }

    public static HandlerList getHandlerList() {
        return list;
    }

    private boolean cancelled = false;
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
