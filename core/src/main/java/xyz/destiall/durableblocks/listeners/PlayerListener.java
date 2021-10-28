package xyz.destiall.durableblocks.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        DurableBlocksAPI.getManager().registerPlayer(e.getPlayer());
        if (DurableBlocksAPI.getConfig().getBool("always-fatigue")) {
            PotionEffect effect = new PotionEffect(PotionEffectType.SLOW_DIGGING, 37523, 250, true, false);
            e.getPlayer().addPotionEffect(effect);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        DurableBlocksAPI.getManager().unregisterPlayer(e.getPlayer().getUniqueId());
    }
}
