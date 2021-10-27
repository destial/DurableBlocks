package xyz.destiall.durableblocks.api;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DurableConfig implements Map<String, Object> {
    public FileConfiguration config;

    public DurableConfig(FileConfiguration config) {
        this.config = config;
    }

    public DurableConfig(File file) {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public Sound getSound(String path) {
        String name = getString(path);
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (Exception e) {
            ((Plugin) DurableBlocksAPI.get()).getLogger().warning("Unable to find sound of " + name + "! Please refer to the sounds.txt file for reference");
        }
        return null;
    }

    public Map<String, Object> getMapping(String path) {
        return (Map<String, Object>) get(path);
    }

    public Material getMaterial(String path) {
        String name = getString(path);
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (Exception e) {
            ((Plugin) DurableBlocksAPI.get()).getLogger().warning("Unable to find material of " + name + "! Please refer to the materials.txt file for reference");
        }
        return null;
    }

    public Effect getEffect(String path) {
        String name = getString(path);
        try {
            return Effect.valueOf(name.toUpperCase());
        } catch (Exception e) {
            ((Plugin) DurableBlocksAPI.get()).getLogger().warning("Unable to find effect of " + name + "! Please refer to the effects.txt file for reference");
        }
        return null;
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