package xyz.destiall.durableblocks.api.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerStopDiggingEvent extends PlayerEvent implements Cancellable {
    private final Block block;
    public PlayerStopDiggingEvent(Player who, Block block) {
        super(who);
        this.block = block;
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
