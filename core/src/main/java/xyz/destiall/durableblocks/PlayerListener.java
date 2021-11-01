package xyz.destiall.durableblocks;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
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
        Bukkit.getScheduler().runTaskLater(plugin, cPlayer::removeFatigue, 10L);
        if (DurableBlocksAPI.getConfig().getBool("always-fatigue")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> cPlayer.addFatigue(9999 * 20, 5), 50L);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        DurableBlocksAPI.getManager().unregisterPlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        ConnectedPlayer player = DurableBlocksAPI.getManager().getPlayer(e.getPlayer().getUniqueId());
        if (player == null) return;
        player.setDigging(false);
    }
}
