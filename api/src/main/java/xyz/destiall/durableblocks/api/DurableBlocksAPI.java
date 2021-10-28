package xyz.destiall.durableblocks.api;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public interface DurableBlocksAPI {
    AtomicReference<DurableBlocksAPI> inst = new AtomicReference<>(null);
    AtomicReference<NMS> nmst = new AtomicReference<>(null);
    AtomicReference<Manager> player = new AtomicReference<>(null);
    AtomicReference<DurableConfig> config = new AtomicReference<>(null);

    static NMS getNMS() {
        return nmst.get();
    }

    static void setNMS(NMS nms) {
        if (getNMS() == null) {
            nmst.set(nms);
        }
    }

    static void set(DurableBlocksAPI api) {
        if (get() == null) {
            inst.set(api);
        }
    }

    static DurableBlocksAPI get() {
        return inst.get();
    }

    static void setManager(Manager manager) {
        if (getManager() == null) {
            player.set(manager);
        }
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
                for (Material material : Material.values()) {
                    if (!material.isBlock() && !material.isSolid()) continue;
                    HashMap<String, Map<String, Object>> path = new HashMap<>();
                    HashMap<String, Object> values = new HashMap<>();
                    values.put("milliseconds-per-stage", material.isBurnable() ? 200 : material.hasGravity() ? 200 : material.isTransparent() ? 100 : 500);
                    values.put("expiry-length-after-stop-mining", 5000);
                    values.put("block-break-sound", Arrays.stream(Sound.values()).filter(s -> s.name().equals("BLOCK_STONE_BREAK") || s.name().equals("DIG_STONE")).findFirst().get().name());
                    values.put("block-break-effect", Effect.STEP_SOUND.name());
                    values.put("block-break-type", Material.AIR.name());
                    List<Map<String, Object>> itemDrops = new ArrayList<>();
                    Map<String, Object> itemMapping = new HashMap<>();
                    itemMapping.put(material.name(), new ItemStack(material, 1));
                    itemDrops.add(itemMapping);
                    values.put("item-drops", itemDrops);
                    path.put(material.name(), values);
                    materialMappings.add(path);
                }
                config.set("blocks", materialMappings);
                config.set("always-fatigue", true);
                config.save(configFile);
            } else {
                config = YamlConfiguration.loadConfiguration(configFile);
            }
            DurableBlocksAPI.config.set(new DurableConfig(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
