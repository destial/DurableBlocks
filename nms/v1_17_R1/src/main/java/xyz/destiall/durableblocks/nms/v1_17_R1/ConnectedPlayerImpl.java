package xyz.destiall.durableblocks.nms.v1_17_R1;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEffect;
import net.minecraft.network.protocol.game.PacketPlayOutRemoveEntityEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.events.PlayerStartDiggingEvent;
import xyz.destiall.durableblocks.api.events.PlayerStopDiggingEvent;

import java.util.UUID;

public class ConnectedPlayerImpl implements ConnectedPlayer {
    private final CraftPlayer player;
    private boolean digging;

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
                            synchronized (channel) {
                                Bukkit.getPluginManager().callEvent(e);
                                cancelled = e.isCancelled();
                                setDigging(true);
                            }
                        } else if (dig.d().equals(PacketPlayInBlockDig.EnumPlayerDigType.b) || dig.d().equals(PacketPlayInBlockDig.EnumPlayerDigType.c)) {
                            PlayerStopDiggingEvent e = new PlayerStopDiggingEvent(player, block);
                            synchronized (channel) {
                                Bukkit.getPluginManager().callEvent(e);
                                cancelled = e.isCancelled();
                                setDigging(false);
                            }
                        }
                        if (!player.getGameMode().equals(GameMode.CREATIVE))
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
        PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation((int) block.getLocation().length(), new BlockPosition(block.getX(), block.getY(), block.getZ()), stage);
        ((CraftWorld) block.getWorld()).getHandle().getCraftServer().getHandle().sendPacketNearby(player.getHandle(), block.getX(), block.getY(), block.getZ(), 60, ((CraftWorld) player.getWorld()).getHandle().getDimensionKey(), packet);
    }

    @Override
    public void sendBlockChange(Location from, Material to) {
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(new BlockPosition(from.getBlockX(), from.getBlockY(), from.getBlockZ()), CraftMagicNumbers.getBlock(to).getBlockData());
        sendPacket(packet);
    }

    @Override
    public void sendActionBar(String message) {
        ChatComponentText text = new ChatComponentText(message);
        PacketPlayOutChat packet = new PacketPlayOutChat(text, ChatMessageType.b, UUID.randomUUID());
        sendPacket(packet);
    }

    @Override
    public void addFatigue(int duration, int amplifier) {
        PacketPlayOutEntityEffect entityEffect = new PacketPlayOutEntityEffect(player.getEntityId(), new MobEffect(MobEffectList.fromId(PotionEffectType.SLOW_DIGGING.getId()), duration, amplifier, true, true));
        sendPacket(entityEffect);
    }

    @Override
    public void removeFatigue() {
        PacketPlayOutRemoveEntityEffect entityEffect = new PacketPlayOutRemoveEntityEffect(player.getEntityId(), MobEffectList.fromId(PotionEffectType.SLOW_DIGGING.getId()));
        sendPacket(entityEffect);
    }

    @Override
    public void sendArmSwing() {
        player.swingMainHand();
    }

    @Override
    public boolean isDigging() {
        return digging;
    }

    @Override
    public void setDigging(boolean digging) {
        this.digging = digging;
    }

    @Override
    public Player getBasePlayer() {
        return player;
    }
}
