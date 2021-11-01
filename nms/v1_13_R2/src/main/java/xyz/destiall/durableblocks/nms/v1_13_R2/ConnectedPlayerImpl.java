package xyz.destiall.durableblocks.nms.v1_13_R2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.ChatComponentText;
import net.minecraft.server.v1_13_R2.ChatMessageType;
import net.minecraft.server.v1_13_R2.MobEffect;
import net.minecraft.server.v1_13_R2.MobEffectList;
import net.minecraft.server.v1_13_R2.Packet;
import net.minecraft.server.v1_13_R2.PacketPlayInBlockDig;
import net.minecraft.server.v1_13_R2.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_13_R2.PacketPlayOutBlockChange;
import net.minecraft.server.v1_13_R2.PacketPlayOutChat;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityEffect;
import net.minecraft.server.v1_13_R2.PacketPlayOutRemoveEntityEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.ToolCheck;
import xyz.destiall.durableblocks.api.events.PlayerStartDiggingEvent;
import xyz.destiall.durableblocks.api.events.PlayerStopDiggingEvent;

public class ConnectedPlayerImpl implements ConnectedPlayer {
    private final CraftPlayer player;
    private boolean digging;

    public ConnectedPlayerImpl(CraftPlayer player) {
        this.player = player;
        try {
            Channel channel = player.getHandle().playerConnection.networkManager.channel;
            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof PacketPlayInBlockDig) {
                        PacketPlayInBlockDig dig = (PacketPlayInBlockDig) msg;
                        Block block = new Location(player.getWorld(), dig.b().getX(), dig.b().getY(), dig.b().getZ()).getBlock();
                        boolean cancelled = false;
                        if (dig.d().equals(PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK)) {
                            PlayerStartDiggingEvent e = new PlayerStartDiggingEvent(player, block);
                            synchronized (channel) {
                                Bukkit.getPluginManager().callEvent(e);
                                cancelled = e.isCancelled();
                                setDigging(true);
                            }
                        } else if (dig.d().equals(PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) || dig.d().equals(PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK)) {
                            PlayerStopDiggingEvent e = new PlayerStopDiggingEvent(player, block);
                            synchronized (channel) {
                                Bukkit.getPluginManager().callEvent(e);
                                setDigging(false);
                            }
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
        PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation((int) block.getLocation().length(), new BlockPosition(block.getX(), block.getY(), block.getZ()), stage);
        ((CraftWorld) block.getWorld()).getHandle().getServer().getHandle().sendPacketNearby(player.getHandle(), block.getX(), block.getY(), block.getZ(), 60, ((CraftWorld) player.getWorld()).getHandle().dimension, packet);
    }

    @Override
    public void sendBlockChange(Location from, Material to) {
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(((CraftWorld)from.getWorld()).getHandle(), new BlockPosition(from.getBlockX(), from.getBlockY(), from.getBlockZ()));
        packet.block = CraftMagicNumbers.getBlock(to).getBlockData();
        sendPacket(packet);
    }

    @Override
    public void sendActionBar(String message) {
        ChatComponentText text = new ChatComponentText(message);
        PacketPlayOutChat packet = new PacketPlayOutChat(text, ChatMessageType.GAME_INFO);
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

    }

    @Override
    public void breakItem(ItemStack hand) {
        if (ToolCheck.isTool(hand.getType())) {
            int unbreaking = hand.getEnchantmentLevel(Enchantment.DURABILITY);
            if (unbreaking == 0) {
                hand.setDurability((short) (hand.getDurability() + 1));
            } else {
                double rand = unbreaking - (Math.random() * unbreaking);
                if (rand <= unbreaking / 2.f) {
                    hand.setDurability((short) (hand.getDurability() + 1));
                }
            }
            if ((int)hand.getDurability() > (int)hand.getType().getMaxDurability()) {
                player.setItemInHand(null);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            }
        }
    }

    @Override
    public int getBreakingSpeed(ItemStack hand, Material block) {
        int multiplier = ToolCheck.getToolSpeedAgainstBlock(block, hand.getType());
        if (multiplier != 1) {
            if (ToolCheck.isPickaxe(hand.getType())) {
                multiplier *= ToolCheck.getPickaxeLevel(hand.getType());
            } else if (ToolCheck.isAxe(hand.getType())) {
                multiplier *= ToolCheck.getAxeLevel(hand.getType());
            } else if (ToolCheck.isShovel(hand.getType())) {
                multiplier *= ToolCheck.getShovelLevel(hand.getType());
            }
            hand.getEnchantments();
            if (hand.getEnchantments().containsKey(Enchantment.DIG_SPEED)) {
                multiplier *= hand.getEnchantmentLevel(Enchantment.DIG_SPEED);
            }
        }
        return multiplier;
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
