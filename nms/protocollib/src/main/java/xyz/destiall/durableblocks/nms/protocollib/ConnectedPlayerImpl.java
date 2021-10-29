package xyz.destiall.durableblocks.nms.protocollib;

import com.comphenix.packetwrapper.WrapperPlayServerAnimation;
import com.comphenix.packetwrapper.WrapperPlayServerBlockBreakAnimation;
import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.NMS;

import java.lang.reflect.InvocationTargetException;

public class ConnectedPlayerImpl implements ConnectedPlayer {
    private final Player player;
    private final ConnectedPlayer nmsPlayer;
    private boolean digging;
    public ConnectedPlayerImpl(NMS nms, Player player) {
        this.player = player;
        this.nmsPlayer = nms.registerPlayer(player);
        digging = false;
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
    public void sendActionBar(String message) {
        nmsPlayer.sendActionBar(message);
    }

    @Override
    public void addFatigue(int duration, int amplifier) {
        nmsPlayer.addFatigue(duration, amplifier);
    }

    @Override
    public void removeFatigue() {
        nmsPlayer.removeFatigue();
    }

    @Override
    public void sendArmSwing() {
        WrapperPlayServerAnimation anim = new WrapperPlayServerAnimation();
        anim.setEntityID(player.getEntityId());
        anim.setAnimation(0);
        anim.sendPacket(player);
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
