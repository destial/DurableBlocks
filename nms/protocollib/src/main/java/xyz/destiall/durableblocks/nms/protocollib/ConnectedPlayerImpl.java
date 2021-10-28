package xyz.destiall.durableblocks.nms.protocollib;

import com.comphenix.packetwrapper.WrapperPlayClientArmAnimation;
import com.comphenix.packetwrapper.WrapperPlayServerAnimation;
import com.comphenix.packetwrapper.WrapperPlayServerBlockBreakAnimation;
import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerBoss;
import com.comphenix.packetwrapper.WrapperPlayServerEntityEffect;
import com.comphenix.packetwrapper.WrapperPlayServerRemoveEntityEffect;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.DurabilityBar;

import java.lang.reflect.InvocationTargetException;

public class ConnectedPlayerImpl implements ConnectedPlayer {
    private final Player player;
    public ConnectedPlayerImpl(Player player) {
        this.player = player;
    }

    @Override
    public void sendPacket(Object packet) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, PacketContainer.fromPacket(packet), true);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendBlockBreakingAnimation(Block block, int stage) {
        WrapperPlayServerBlockBreakAnimation packet = new WrapperPlayServerBlockBreakAnimation();
        BlockPosition bp = new BlockPosition(block.getX(), block.getY(), block.getZ());
        packet.setEntityID((int) block.getLocation().length());
        packet.setLocation(bp);
        packet.setDestroyStage(stage);
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet.getHandle(), block.getLocation(), 60);
    }

    @Override
    public void sendBlockChange(Location from, Material to) {
        WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange();
        packet.setLocation(new BlockPosition(from.toVector()));
        packet.setBlockData(WrappedBlockData.createData(to));
        packet.sendPacket(player);
    }

    @Override
    public void updateBlockNotify(Location location) {

    }

    @Override
    public void sendActionBar(String message) {

    }

    @Override
    public void sendDurabilityBar(DurabilityBar bar) {
        WrapperPlayServerBoss packet = new WrapperPlayServerBoss();
        packet.setAction(WrapperPlayServerBoss.Action.UPDATE_PCT);
        packet.setUniqueId(bar.getUUID());
        packet.setHealth(bar.getValue());
        packet.setStyle(WrapperPlayServerBoss.BarStyle.PROGRESS);
        packet.setTitle(WrappedChatComponent.fromText(bar.getMessage()));
        try {
            Class.forName("org.bukkit.boss.BarColor");
            packet.setColor(org.bukkit.boss.BarColor.valueOf(bar.getColor().name()));
        } catch (ClassNotFoundException ignored) {}
        packet.sendPacket(player);
    }

    @Override
    public void addFatigue(int duration, int amplifier) {
        WrapperPlayServerEntityEffect effect = new WrapperPlayServerEntityEffect();
        effect.setEffectID((byte) 0x04);
        effect.setDuration(duration);
        effect.setAmplifier((byte) amplifier);
        effect.setHideParticles(true);
        effect.setEntityID(player.getEntityId());
        effect.sendPacket(player);
    }

    @Override
    public void removeFatigue() {
        WrapperPlayServerRemoveEntityEffect effect = new WrapperPlayServerRemoveEntityEffect();
        effect.setEffect(PotionEffectType.SLOW_DIGGING);
        effect.setEntityID(player.getEntityId());
        effect.sendPacket(player);
    }

    @Override
    public void sendArmSwing() {
        WrapperPlayServerAnimation anim = new WrapperPlayServerAnimation();
        anim.setEntityID(player.getEntityId());
        anim.setAnimation(0);
        anim.sendPacket(player);
        WrapperPlayClientArmAnimation packet = new WrapperPlayClientArmAnimation();
        //packet.setAnimation(0);
        //packet.setEntityID(player.getEntityId());
        packet.receivePacket(player);
    }

    @Override
    public Player getBasePlayer() {
        return player;
    }
}
