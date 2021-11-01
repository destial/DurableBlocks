package xyz.destiall.durableblocks.api;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public interface DurableBlocksAPI {
    AtomicReference<DurableBlocksAPI> inst = new AtomicReference<>(null);
    AtomicReference<NMS> nmst = new AtomicReference<>(null);
    AtomicReference<Manager> player = new AtomicReference<>(null);
    AtomicReference<DurableConfig> config = new AtomicReference<>(null);

    static void setNMS(NMS nms) {
        if (getNMS() == null || nms == null) {
            nmst.set(nms);
        }
    }

    static void set(DurableBlocksAPI api) {
        if (get() == null || api == null) {
            inst.set(api);
        }
    }

    static void setManager(Manager manager) {
        if (getManager() == null || manager == null) {
            player.set(manager);
        }
    }

    static DurableBlocksAPI get() {
        return inst.get();
    }

    static NMS getNMS() {
        return nmst.get();
    }

    static Manager getManager() {
        return player.get();
    }

    static DurableConfig getConfig() {
        if (config.get() == null) {
            reloadConfig();
        }
        return config.get();
    }

    static void reloadConfig() {
        File configFile = new File(((Plugin) get()).getDataFolder(), "config.yml");
        try {
            FileConfiguration config;
            if (configFile.createNewFile()) {
                config = new YamlConfiguration();
                List<Map<String, Map<String, Object>>> materialMappings = new ArrayList<>();
                config.set("always-fatigue", true);
                config.set("enabled-worlds", Arrays.asList("world", "world_nether"));
                for (Material material : Material.values()) {
                    if (!material.isBlock()) continue;
                    HashMap<String, Map<String, Object>> path = new HashMap<>();
                    HashMap<String, Object> values = new HashMap<>();
                    values.put("milliseconds-per-stage", material.isBurnable() ? 200 : material.hasGravity() ? 200 : material.isTransparent() ? 100 : 500);
                    values.put("expiry-length-after-stop-mining", 5000);
                    values.put("need-tool-for-drops", true);
                    values.put("unbreakable", material == Material.BEDROCK);
                    values.put("block-break-sound", Arrays.stream(Sound.values()).filter(s -> s.name().equals("BLOCK_STONE_BREAK") || s.name().equals("DIG_STONE")).findFirst().get().name());
                    values.put("block-break-effect", Effect.STEP_SOUND.name());
                    values.put("block-break-type", Material.AIR.name());
                    values.put("exp-drops", material.name().contains("ORE") ? 5 : 1);
                    List<ItemStack> itemDrops = new ArrayList<>();
                    itemDrops.add(new ItemStack(material, 1));
                    values.put("item-drops", itemDrops);
                    path.put(material.name(), values);
                    materialMappings.add(path);
                }
                config.set("blocks", materialMappings);
                config.save(configFile);
            } else {
                config = YamlConfiguration.loadConfiguration(configFile);
            }
            DurableBlocksAPI.config.set(new DurableConfig(config));
            getManager().emptyWorlds();
            for (String name : config.getStringList("enabled-worlds")) {
                World world = Bukkit.getWorld(name);
                if (world == null) continue;
                getManager().enableWorld(world);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
