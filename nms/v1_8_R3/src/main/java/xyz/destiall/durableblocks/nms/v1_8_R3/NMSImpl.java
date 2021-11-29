package xyz.destiall.durableblocks.nms.v1_8_R3;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.DurableBlock;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;
import xyz.destiall.durableblocks.api.NMS;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;


public class NMSImpl implements NMS {

    @Override
    public ConnectedPlayer registerPlayer(Player player) {
        CraftPlayer craftPlayer = ((CraftPlayer) player);
        return new ConnectedPlayerImpl(craftPlayer);
    }

    @Override
    public void sendBreakingAnimation(Player player, DurableBlock block) {
        block.getWorld().getNearbyEntities(block.getLocation(), 60, 60, 60)
                .stream().filter(e -> e instanceof Player).map(p -> DurableBlocksAPI.getManager().getPlayer(p.getUniqueId()))
                .forEach(cp -> cp.sendBlockBreakingAnimation(block.getBlock(), block.getStage()));
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
