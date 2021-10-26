package xyz.destiall.durableblocks.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        DurableBlocksAPI.getManager().registerPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        DurableBlocksAPI.getManager().unregisterPlayer(e.getPlayer().getUniqueId());
    }
}
