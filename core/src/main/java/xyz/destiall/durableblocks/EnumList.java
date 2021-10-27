package xyz.destiall.durableblocks;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import xyz.destiall.durableblocks.api.DurableBlocksAPI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

final class EnumList {
    public EnumList() {}
    public void saveSoundList() throws IOException {
        Plugin plugin = (Plugin) DurableBlocksAPI.get();
        File sounds = new File(plugin.getDataFolder(), "sounds.txt");
        if (!sounds.createNewFile()) {
            FileWriter write = new FileWriter("sounds.txt");
            for (Sound sound : Sound.values()) {
                write.append(sound.name()).append("\n");
            }
            write.close();
        } else {
            FileWriter write = new FileWriter("sounds.txt");
            StringBuilder builder = new StringBuilder();
            for (Sound sound : Sound.values()) {
                builder.append(sound.name()).append("\n");
            }
            write.write(builder.toString());
            write.close();
        }
    }

    public void saveMaterialList() throws IOException {
        Plugin plugin = (Plugin) DurableBlocksAPI.get();
        File materials = new File(plugin.getDataFolder(), "materials.txt");

        if (!materials.createNewFile()) {
            FileWriter write = new FileWriter("materials.txt");
            for (Material material : Material.values()) {
                write.append(material.name()).append("\n");
            }
            write.close();
        } else {
            FileWriter write = new FileWriter("materials.txt");
            StringBuilder builder = new StringBuilder();
            for (Material material : Material.values()) {
                builder.append(material.name()).append("\n");
            }
            write.write(builder.toString());
            write.close();
        }
    }

    public void saveEffectList() throws IOException {
        Plugin plugin = (Plugin) DurableBlocksAPI.get();
        File effects = new File(plugin.getDataFolder(), "effects.txt");
        if (!effects.createNewFile()) {
            FileWriter write = new FileWriter("effects.txt");
            for (Effect effect : Effect.values()) {
                write.append(effect.name()).append("\n");
            }
            write.close();
        } else {
            FileWriter write = new FileWriter("effects.txt");
            StringBuilder builder = new StringBuilder();
            for (Effect effect : Effect.values()) {
                builder.append(effect.name()).append("\n");
            }
            write.write(builder.toString());
            write.close();
        }
    }
}
