package xyz.destiall.durableblocks.nms.protocollib;

import com.comphenix.packetwrapper.WrapperPlayServerBlockBreakAnimation;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.DurableBlock;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;
import xyz.destiall.durableblocks.api.NMS;
import xyz.destiall.durableblocks.api.events.PlayerStartDiggingEvent;
import xyz.destiall.durableblocks.api.events.PlayerStopDiggingEvent;

public class NMSImpl implements NMS {
    private NMS nms;
    public NMSImpl() {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> clazz = Class.forName("xyz.destiall.durableblocks.nms." + version + ".NMSImpl");
            nms = (NMS) clazz.newInstance();
        }  catch (Exception e) {
            e.printStackTrace();
        }
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
                        Player player = e.getPlayer();
                        e.setCancelled(!player.getGameMode().equals(GameMode.CREATIVE));
                        DurableBlocksAPI.getManager().getPlayer(event.getPlayer().getUniqueId()).setDigging(true);
                    } else {
                        PlayerStopDiggingEvent e = new PlayerStopDiggingEvent(event.getPlayer(), location.getBlock());
                        Bukkit.getPluginManager().callEvent(e);
                        DurableBlocksAPI.getManager().getPlayer(event.getPlayer().getUniqueId()).setDigging(false);
                    }
                }
            }
        });
    }

    @Override
    public ConnectedPlayer registerPlayer(Player player) {
        return new ConnectedPlayerImpl(nms, player);
    }

    @Override
    public void sendBreakingAnimation(Player player, DurableBlock block) {
        try {
            WrapperPlayServerBlockBreakAnimation packet = new WrapperPlayServerBlockBreakAnimation();
            BlockPosition bp = new BlockPosition((int) block.getX(), (int) block.getY(), (int) block.getZ());
            packet.setEntityID(block.getId());
            packet.setLocation(bp);
            packet.setDestroyStage(block.getStage());
            ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet.getHandle(), block.getLocation(), 60);
        } catch (Exception e) {
            nms.sendBreakingAnimation(player, block);
        }
    }

    @Override
    public void clearBreakingAnimation(DurableBlock block) {
        try {
            WrapperPlayServerBlockBreakAnimation packet = new WrapperPlayServerBlockBreakAnimation();
            BlockPosition bp = new BlockPosition(0, 0, 0);
            packet.setEntityID(block.getId());
            packet.setLocation(bp);
            packet.setDestroyStage(-1);
            ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet.getHandle());
        } catch (Exception e) {
            nms.clearBreakingAnimation(block);
        }
    }
}
