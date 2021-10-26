package xyz.destiall.durableblocks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;
import xyz.destiall.durableblocks.api.NMS;
import xyz.destiall.durableblocks.api.Manager;
import xyz.destiall.durableblocks.listeners.BlockListener;
import xyz.destiall.durableblocks.listeners.PlayerListener;

public final class DurableBlocksPlugin extends JavaPlugin implements DurableBlocksAPI {

    @Override
    public void onEnable() {
        DurableBlocksAPI.set(this);
        DurableBlocksAPI.setManager(new Manager());
        String version;
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            version = "protocollib";
        } else {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }
        getLogger().info("You are using " + version);
        try {
            Class<?> clazz = Class.forName("xyz.destiall.durableblocks.nms." + version + ".NMSImpl");
            NMS nms = (NMS) clazz.newInstance();
            DurableBlocksAPI.setNMS(nms);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
