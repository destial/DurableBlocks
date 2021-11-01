package xyz.destiall.durableblocks;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;
import xyz.destiall.durableblocks.api.Manager;
import xyz.destiall.durableblocks.api.NMS;

import java.io.IOException;

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
            getLogger().warning("Unable to find a compatible version with this plugin! Shutting down!");
            getLogger().warning("Please use ProtocolLib for maximum compatibility!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        try {
            getDataFolder().mkdir();
            EnumList enumList = new EnumList();
            enumList.saveSoundList();
            enumList.saveMaterialList();
            enumList.saveEffectList();
            DurableBlocksAPI.getConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bukkit.getPluginManager().registerEvents(new BlockListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void reloadConfig() {
        getLogger().info("Reloading...");
        DurableBlocksAPI.reloadConfig();
        getLogger().info("Completed reloading...");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        DurableBlocksAPI.setManager(null);
        DurableBlocksAPI.setNMS(null);
        DurableBlocksAPI.set(null);
    }
}
