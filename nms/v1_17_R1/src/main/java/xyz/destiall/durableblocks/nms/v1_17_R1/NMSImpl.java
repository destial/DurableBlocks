package xyz.destiall.durableblocks.nms.v1_17_R1;

import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.destiall.durableblocks.api.ConnectedPlayer;
import xyz.destiall.durableblocks.api.NMS;

public class NMSImpl implements NMS {

    @Override
    public ConnectedPlayer registerPlayer(Player player) {
        CraftPlayer craftPlayer = ((CraftPlayer) player);
        return new ConnectedPlayerImpl(craftPlayer);
    }
}
