package xyz.destiall.durableblocks.utils;

import org.bukkit.Material;

public class ToolCheck {

    public static boolean isPickaxe(Material material) {
        return material.name().contains("_PICKAXE");
    }

    public static boolean isAxe(Material material) {
        return material.name().contains("_AXE");
    }

    public static boolean isShovel(Material material) {
        return material.name().contains("_SPADE") || material.name().contains("_SHOVEL");
    }

    public static int getPickaxeLevel(Material material) {
        if (!isPickaxe(material)) return 1;
        switch (material) {
            case WOOD_PICKAXE:
                return 2;
            case STONE_PICKAXE:
                return 3;
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
                return 4;
            case DIAMOND_PICKAXE:
                return 5;
            default: return 6;
        }
    }

    public static int getAxeLevel(Material material) {
        if (!isAxe(material)) return 1;
        switch (material) {
            case WOOD_AXE:
                return 2;
            case STONE_AXE:
                return 3;
            case IRON_AXE:
            case GOLD_AXE:
                return 4;
            case DIAMOND_AXE:
                return 5;
            default: return 6;
        }
    }

    public static int getShovelLevel(Material material) {
        if (!isShovel(material)) return 1;
        switch (material) {
            case WOOD_SPADE:
                return 2;
            case STONE_SPADE:
                return 3;
            case IRON_SPADE:
            case GOLD_SPADE:
                return 4;
            case DIAMOND_SPADE:
                return 5;
            default: return 6;
        }
    }
}
