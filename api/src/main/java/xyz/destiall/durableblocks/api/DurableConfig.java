package xyz.destiall.durableblocks.api;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DurableConfig implements Map<String, Object> {
    private final FileConfiguration config;

    public DurableConfig(FileConfiguration config) {
        this.config = config;
    }

    public Sound getSound(Material material) {
        Map<String, Object> mapping = getMapping(material);
        if (mapping == null) return null;
        String name = (String) mapping.get("block-break-sound");
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (Exception e) {
            ((Plugin) DurableBlocksAPI.get()).getLogger().warning("Unable to find sound of " + name + "! Please refer to the sounds.txt file for reference");
        }
        return null;
    }

    public List<ItemStack> getStacks(Material material) {
        Map<String, Object> mapping = getMapping(material);
        if (mapping == null) return null;
        List<Map<String, Object>> itemDrops = (List<Map<String, Object>>) mapping.get("item-drops");
        List<ItemStack> stacks = new ArrayList<>();
        for (Map<String, Object> items : itemDrops) {
            for (Object oitemStackMapping : items.values())
            stacks.add((ItemStack) oitemStackMapping);
        }
        return stacks;
    }

    public Map<String, Object> getMapping(Material material) {
        List<Map<String,Object>> list = ((List<Map<String, Object>>) config.getList("blocks"));
        Map<String, Object> materialMap = list.stream().filter(m -> m.containsKey(material.name())).findFirst().orElse(null);
        if (materialMap == null) return null;
        return (Map<String, Object>) materialMap.get(material.name());
    }

    public Material getConvert(Material material) {
        Map<String, Object> mapping = getMapping(material);
        if (mapping == null) return null;
        String name = (String) mapping.get("block-break-type");
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (Exception e) {
            ((Plugin) DurableBlocksAPI.get()).getLogger().warning("Unable to find material of " + name + "! Please refer to the materials.txt file for reference");
        }
        return null;
    }

    public Effect getEffect(Material material) {
        Map<String, Object> mapping = getMapping(material);
        if (mapping == null) return null;
        String name = (String) mapping.get("block-break-effect");
        try {
            return Effect.valueOf(name.toUpperCase());
        } catch (Exception e) {
            ((Plugin) DurableBlocksAPI.get()).getLogger().warning("Unable to find effect of " + name + "! Please refer to the effects.txt file for reference");
        }
        return null;
    }

    public boolean getBool(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public boolean getBool(String path) {
        return config.getBoolean(path);
    }

    public float getFloat(String path, float def) {
        return (float) getDouble(path, def);
    }

    public float getFloat(String path) {
        return (float) getDouble(path);
    }

    public List<String> getList(String path) {
        if (config.contains(path) && config.isList(path)) {
            return config.getStringList(path);
        }
        return new ArrayList<>();
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    @Override
    public int size() {
        return config.getKeys(true).size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object path) {
        return config.contains((String) path);
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Object get(Object key) {
        return config.get((String) key);
    }

    @Override
    public Object put(String key, Object value) {
        config.set(key, value);
        return value;
    }

    @Override
    public Object remove(Object key) {
        Object ob = get(key);
        config.set((String) key, null);
        return ob;
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {

    }

    @Override
    public Set<String> keySet() {
        return config.getKeys(false);
    }

    @Override
    public Collection<Object> values() {
        Collection<Object> objects = new HashSet<>();
        for (String path : keySet()) {
            objects.add(get(path));
        }
        return objects;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Map<String, Object> objects = new HashMap<>();
        for (String path : keySet()) {
            objects.put(path, get(path));
        }
        return objects.entrySet();
    }
}
