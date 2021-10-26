package xyz.destiall.durableblocks.nms.v1_8_R2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.MobEffect;
import net.minecraft.server.v1_8_R2.Packet;
import net.minecraft.server.v1_8_R2.PacketPlayInBlockDig;
import net.minecraft.server.v1_8_R2.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_8_R2.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R2.PacketPlayOutEntityEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.events.PlayerStartDiggingEvent;
import xyz.destiall.durableblocks.api.events.PlayerStopDiggingEvent;

public class ConnectedPlayerImpl implements ConnectedPlayer {
    private final CraftPlayer player;

    public ConnectedPlayerImpl(CraftPlayer player) {
        this.player = player;
        try {
            Channel channel = player.getHandle().playerConnection.networkManager.k;
            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof PacketPlayInBlockDig) {
                        PacketPlayInBlockDig dig = (PacketPlayInBlockDig) msg;
                        Block block = new Location(player.getWorld(), dig.a().getX(), dig.a().getY(), dig.a().getZ()).getBlock();
                        boolean cancelled = false;
                        if (dig.c().equals(PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK)) {
                            PlayerStartDiggingEvent e = new PlayerStartDiggingEvent(player, block);
                            Bukkit.getPluginManager().callEvent(e);
                            cancelled = e.isCancelled();
                        } else if (dig.c().equals(PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) || dig.c().equals(PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK)) {
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
        player.getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }

    @Override
    public void sendBlockBreakingAnimation(Block block, int stage) {
        sendPacket(new PacketPlayOutBlockBreakAnimation(player.getEntityId(), new BlockPosition(block.getX(), block.getY(), block.getZ()), stage));
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
    public void addFatigue() {
        PacketPlayOutEntityEffect packet = new PacketPlayOutEntityEffect(player.getEntityId(), new MobEffect(4, 20, 255, false, false));
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
