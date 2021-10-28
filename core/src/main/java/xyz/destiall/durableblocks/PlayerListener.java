package xyz.destiall.durableblocks;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;

final class PlayerListener implements Listener {
    private final DurableBlocksPlugin plugin;
    public PlayerListener(DurableBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        ConnectedPlayer cPlayer = DurableBlocksAPI.getManager().registerPlayer(e.getPlayer());
        if (DurableBlocksAPI.getConfig().getBool("always-fatigue")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                cPlayer.addFatigue(Integer.MAX_VALUE, 254);
            }, 10L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        DurableBlocksAPI.getManager().unregisterPlayer(e.getPlayer().getUniqueId());
    }
}
