package xyz.destiall.durableblocks.nms.v1_17_R1;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.DurabilityBar;
import xyz.destiall.durableblocks.api.events.PlayerStartDiggingEvent;
import xyz.destiall.durableblocks.api.events.PlayerStopDiggingEvent;

import java.util.UUID;

public class ConnectedPlayerImpl implements ConnectedPlayer {
    private final CraftPlayer player;

    public ConnectedPlayerImpl(CraftPlayer player) {
        this.player = player;
        try {
            Channel channel = player.getHandle().b.a.k;
            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof PacketPlayInBlockDig) {
                        PacketPlayInBlockDig dig = (PacketPlayInBlockDig) msg;
                        Block block = new Location(player.getWorld(), dig.b().getX(), dig.b().getY(), dig.b().getZ()).getBlock();
                        boolean cancelled = false;
                        if (dig.d().equals(PacketPlayInBlockDig.EnumPlayerDigType.a)) {
                            PlayerStartDiggingEvent e = new PlayerStartDiggingEvent(player, block);
                            Bukkit.getPluginManager().callEvent(e);
                            cancelled = e.isCancelled();
                        } else if (dig.d().equals(PacketPlayInBlockDig.EnumPlayerDigType.b) || dig.d().equals(PacketPlayInBlockDig.EnumPlayerDigType.c)) {
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
        player.getHandle().b.sendPacket((Packet<?>) packet);
    }

    @Override
    public void sendBlockBreakingAnimation(Block block, int stage) {
        sendPacket(new PacketPlayOutBlockBreakAnimation(player.getEntityId(), new BlockPosition(block.getX(), block.getY(), block.getZ()), stage));
    }

    @Override
    public void sendBlockChange(Location from, Material to) {
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(new BlockPosition(from.getBlockX(), from.getBlockY(), from.getBlockZ()), CraftMagicNumbers.getBlock(to).getBlockData());
        sendPacket(packet);
    }

    @Override
    public void updateBlockNotify(Location location) {
        ((CraftWorld) player.getWorld()).getHandle().a(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), true);
    }

    @Override
    public void sendActionBar(String message) {

    }

    @Override
    public void sendDurabilityBar(DurabilityBar bar) {

    }

    @Override
    public void addFatigue() {
        PacketPlayOutEntityEffect packet = new PacketPlayOutEntityEffect(player.getEntityId(), new MobEffect(MobEffectList.fromId(4), 20, 255, false, false));
        sendPacket(packet);
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
