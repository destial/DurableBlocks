package xyz.destiall.durableblocks.nms.v1_8_R1;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumPlayerDigType;
import net.minecraft.server.v1_8_R1.NetworkManager;
import net.minecraft.server.v1_8_R1.Packet;
import net.minecraft.server.v1_8_R1.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R1.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_8_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.DurabilityBar;
import xyz.destiall.durableblocks.api.events.PlayerStartDiggingEvent;
import xyz.destiall.durableblocks.api.events.PlayerStopDiggingEvent;

import java.lang.reflect.Field;

public class ConnectedPlayerImpl implements ConnectedPlayer {
    private final CraftPlayer player;

    public ConnectedPlayerImpl(CraftPlayer player) {
        this.player = player;
        try {
            Field channelField = NetworkManager.class.getDeclaredField("i");
            channelField.setAccessible(true);
            Channel channel = (Channel) channelField.get(player.getHandle().playerConnection.networkManager);
            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof PacketPlayInBlockDig) {
                        PacketPlayInBlockDig dig = (PacketPlayInBlockDig) msg;
                        Block block = new Location(player.getWorld(), dig.a().getX(), dig.a().getY(), dig.a().getZ()).getBlock();
                        boolean cancelled = false;
                        if (dig.c().equals(EnumPlayerDigType.START_DESTROY_BLOCK)) {
                            PlayerStartDiggingEvent e = new PlayerStartDiggingEvent(player, block);
                            Bukkit.getPluginManager().callEvent(e);
                            cancelled = e.isCancelled();
                        } else if (dig.c().equals(EnumPlayerDigType.STOP_DESTROY_BLOCK) || dig.c().equals(EnumPlayerDigType.ABORT_DESTROY_BLOCK)) {
                            PlayerStopDiggingEvent e = new PlayerStopDiggingEvent(player, block);
                            Bukkit.getPluginManager().callEvent(e);
                            cancelled = e.isCancelled();
                        }
                        if (cancelled) return;
                    }
                    super.channelRead(ctx, msg);
                }
            };
            channel.pipeline().addBefore("packet_handler", player.getName(), handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPacket(Object packet) {
        player.getHandle().playerConnection.sendPacket((Packet) packet);
    }

    @Override
    public void sendBlockBreakingAnimation(Block block, int stage) {
        PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation((int) block.getLocation().length(), new BlockPosition(block.getX(), block.getY(), block.getZ()), stage);
        ((CraftWorld) block.getWorld()).getHandle().getServer().getHandle().sendPacketNearby(block.getX(), block.getY(), block.getZ(), 60, ((CraftWorld) player.getWorld()).getHandle().dimension, packet);
    }

    @Override
    public void sendBlockChange(Location from, Material to) {
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(((CraftWorld)from.getWorld()).getHandle(), new BlockPosition(from.getBlockX(), from.getBlockY(), from.getBlockZ()));
        packet.block = CraftMagicNumbers.getBlock(to).getBlockData();
        sendPacket(packet);
    }

    @Override
    public void updateBlockNotify(Location location) {
        ((CraftWorld) player.getWorld()).getHandle().notify(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    @Override
    public void sendActionBar(String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\": \"" + message + "\"}"));
        sendPacket(packet);
    }

    @Override
    public void sendDurabilityBar(DurabilityBar bar) {

    }

    @Override
    public void addFatigue() {
        player.addPotionEffect(PotionEffectType.SLOW_DIGGING.createEffect(25, 255));
    }

    @Override
    public void removeFatigue() {
        player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
    }

    @Override
    public Player getBasePlayer() {
        return player;
    }
}
