package xyz.destiall.durableblocks.nms.v1_11_R1;

import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.PacketPlayOutBlockBreakAnimation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.DurableBlock;
import xyz.destiall.durableblocks.api.NMS;

public class NMSImpl implements NMS {

    @Override
    public ConnectedPlayer registerPlayer(Player player) {
        CraftPlayer craftPlayer = ((CraftPlayer) player);
        return new ConnectedPlayerImpl(craftPlayer);
    }

    @Override
    public void sendBreakingAnimation(Player player, DurableBlock block) {
        Location location = block.getLocation();
        PacketPlayOutBlockBreakAnimation packet =
                new PacketPlayOutBlockBreakAnimation(block.getId(), new BlockPosition(location.getX(), location.getY(), location.getZ()), block.getStage());
        ((CraftWorld) location.getWorld()).getHandle()
                .getServer().getHandle()
                .sendPacketNearby(((CraftPlayer) player).getHandle(),
                        location.getX(), location.getY(), location.getZ(),
                        60,
                        ((CraftWorld) player.getWorld()).getHandle().dimension, packet);
    }

    @Override
    public void clearBreakingAnimation(DurableBlock block) {
        Location location = block.getLocation();
        PacketPlayOutBlockBreakAnimation packet =
                new PacketPlayOutBlockBreakAnimation(
                        block.getId(),
                        new BlockPosition(0, 0, 0),
                        block.getStage());
        ((CraftWorld) location.getWorld()).getHandle().getServer().getHandle()
                .sendAll(packet, ((CraftWorld) block.getWorld()).getHandle());
    }
}
