package com.marc33.orerespawn.data;

import com.marc33.orerespawn.OreRespawnMod;
import com.marc33.orerespawn.config.OreRespawnConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Decides whether a broken block should be tracked by OreRespawn, and what stone-like
 * block it was embedded in (used later to detect whether a player has built over the hole).
 */
public final class OreDetector {

    public static final TagKey<Block> CUSTOM_ORE_TAG =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(OreRespawnMod.MOD_ID, "ores"));

    private OreDetector() {
    }

    public static boolean isOre(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (isListed(id, OreRespawnConfig.BLACKLIST.get())) {
            return false;
        }

        if (isListed(id, OreRespawnConfig.WHITELIST.get())) {
            return true;
        }

        if (OreRespawnConfig.USE_VANILLA_ORE_TAG.get() && state.is(BlockTags.ORES)) {
            return true;
        }

        return OreRespawnConfig.USE_CUSTOM_ORE_TAG.get() && state.is(CUSTOM_ORE_TAG);
    }

    /**
     * Best-effort guess at the stone-like block an ore was embedded in, based on naming
     * conventions shared by vanilla and most modded ores (e.g. "deepslate_iron_ore" -> deepslate).
     */
    public static Block guessFillerBlock(ResourceLocation oreBlockId) {
        String path = oreBlockId.getPath();

        if (path.contains("deepslate")) {
            return Blocks.DEEPSLATE;
        }
        if (path.contains("blackstone")) {
            return Blocks.BLACKSTONE;
        }
        if (path.contains("nether")) {
            return Blocks.NETHERRACK;
        }
        if (path.contains("end_stone")) {
            return Blocks.END_STONE;
        }
        return Blocks.STONE;
    }

    private static boolean isListed(ResourceLocation id, java.util.List<? extends String> list) {
        String s = id.toString();
        for (String entry : list) {
            if (entry.equals(s)) {
                return true;
            }
        }
        return false;
    }
}
