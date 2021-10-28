package xyz.destiall.durableblocks.api;

import org.bukkit.Material;

import java.util.stream.Stream;

public final class ToolCheck {

    public static int getToolSpeedAgainstBlock(Material block, Material tool) {
        if (ToolCheck.isShovel(tool)) {
            if (Stream.of(
                    "SAND", "SNOW", "DIRT", "CONCRETE_POWDER", "CLAY", "GRAVEL", "SOUL",
                    "PODZOL", "FARMLAND", "GRASS", "MYCEL")
                    .anyMatch(a -> block.name().contains(a))) return 3;
        } else if (ToolCheck.isPickaxe(tool)) {
            if (Stream.of(
                    "STONE", "ORE", "ICE", "LOG", "BRICK", "POLISHED", "TERRACOTTA",
                    "PRISMARINE", "SANDSTONE", "IRON", "RAIL", "DIAMOND", "COAL", "EMERALD",
                    "REDSTONE_BLOCK", "GOLD", "NETHERITE", "SHULKER", "AMETHYST", "ANVIL",
                    "BELL", "BREWING", "COPPER", "ANDESITE", "DIORITE", "GRANITE", "CONCRETE",
                    "TERRACOTTA", "BONE_BLOCK", "CONDUIT", "BASALT", "FURNACE", "QUARTZ",
                    "DROPPER", "DISPENSER", "OBSERVER", "OBSIDIAN", "NETHER", "SPAWNER",
                    "SMOKER", "SMOOTH", "LANTERN", "ENCHANT", "DEBRIS", "RESPAWN")
                    .anyMatch(a -> block.name().contains(a))) return 3;
        } else if (ToolCheck.isAxe(tool)) {
            if (Stream.of(
                    "WOOD", "SIGN", "LOG", "OAK", "BIRCH", "ACACIA", "JUNGLE", "SPRUCE",
                    "PLANKS", "BOOKSHELF", "COCOA", "JACK_O_LANTERN", "PUMPKIN", "MELON",
                    "BEE", "MUSHROOM", "BANNER", "BARREL", "CHEST", "BOOK", "CAMPFIRE",
                    "WORKBENCH", "TABLE", "COMPOSTER", "DAYLIGHT", "LADDER", "JUKEBOX",
                    "LOOM", "HYPHAE")
                    .anyMatch(a -> block.name().contains(a))) return 3;
        }
        return 1;
    }

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
