package xyz.destiall.durableblocks.nms.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;
import xyz.destiall.durableblocks.api.NMS;
import xyz.destiall.durableblocks.api.events.PlayerStartDiggingEvent;
import xyz.destiall.durableblocks.api.events.PlayerStopDiggingEvent;

public class NMSImpl implements NMS {
    public NMSImpl() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter((Plugin) DurableBlocksAPI.get(), PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (packet.getType() == PacketType.Play.Client.BLOCK_DIG) {
                    BlockPosition position = packet.getBlockPositionModifier().getValues().stream().findFirst().get();
                    Location location = position.toLocation(event.getPlayer().getWorld());
                    if (packet.getPlayerDigTypes().read(0).equals(EnumWrappers.PlayerDigType.START_DESTROY_BLOCK)) {
                        PlayerStartDiggingEvent e = new PlayerStartDiggingEvent(event.getPlayer(), location.getBlock());
                        Bukkit.getPluginManager().callEvent(e);
                        event.setCancelled(e.isCancelled());
                    } else {
                        PlayerStopDiggingEvent e = new PlayerStopDiggingEvent(event.getPlayer(), location.getBlock());
                        Bukkit.getPluginManager().callEvent(e);
                        event.setCancelled(e.isCancelled());
                    }
                }
            }
        });
    }

    @Override
    public ConnectedPlayer registerPlayer(Player player) {
        return new ConnectedPlayerImpl(player);
    }
}
