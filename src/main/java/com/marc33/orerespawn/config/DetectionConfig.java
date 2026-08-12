package com.marc33.orerespawn.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Which blocks count as "ore" for OreRespawn. Lives in its own file
 * ({@code orerespawn-detection.toml}) so it can be tuned without touching respawn timing or
 * dimension settings.
 */
public final class DetectionConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue USE_VANILLA_ORE_TAG;
    public static final ModConfigSpec.BooleanValue USE_NEOFORGE_ORE_TAG;
    public static final ModConfigSpec.BooleanValue USE_CUSTOM_ORE_TAG;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        USE_VANILLA_ORE_TAG = builder
                .comment("Utiliser les tags vanilla #minecraft:*_ores (coal, copper, diamond, emerald, gold, iron, lapis, redstone) pour detecter les blocs de minerai.")
                .define("useVanillaOreTag", true);

        USE_NEOFORGE_ORE_TAG = builder
                .comment(
                        "Utiliser le tag de convention NeoForge #c:ores pour detecter les blocs de minerai.",
                        "Ce tag couvre deja tous les minerais vanilla (y compris le quartz du Nether et",
                        "les debris antiques) et c'est celui que la plupart des mods utilisent pour",
                        "rendre leurs propres minerais reconnaissables par les autres mods."
                )
                .define("useNeoForgeOreTag", true);

        USE_CUSTOM_ORE_TAG = builder
                .comment("Utiliser le tag personnalise #orerespawn:ores (data/orerespawn/tags/block/ores.json) pour detecter des minerais additionnels/moddes.")
                .define("useCustomOreTag", true);

        WHITELIST = builder
                .comment(
                        "Identifiants de blocs (ex: \"mymod:ruby_ore\") toujours traites comme des",
                        "minerais par OreRespawn, meme s'ils ne sont dans aucun des tags ci-dessus."
                )
                .defineListAllowEmpty("whitelist", List.of(), () -> "", DetectionConfig::isValidBlockId);

        BLACKLIST = builder
                .comment(
                        "Identifiants de blocs jamais traites comme des minerais par OreRespawn.",
                        "La blacklist est prioritaire sur les tags et sur la whitelist."
                )
                .defineListAllowEmpty("blacklist", List.of(), () -> "", DetectionConfig::isValidBlockId);

        SPEC = builder.build();
    }

    private DetectionConfig() {
    }

    private static boolean isValidBlockId(Object obj) {
        return obj instanceof String s && ResourceLocation.tryParse(s) != null;
    }
}
