package com.marc33.orerespawn.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class OreRespawnConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue RESPAWN_DELAY_SECONDS;
    public static final ModConfigSpec.IntValue CHECK_INTERVAL_TICKS;
    public static final ModConfigSpec.BooleanValue REQUIRE_EMPTY_SPACE_OR_ORIGINAL_FILLER;
    public static final ModConfigSpec.IntValue RESPAWN_RADIUS;

    public static final ModConfigSpec.BooleanValue USE_VANILLA_ORE_TAG;
    public static final ModConfigSpec.BooleanValue USE_NEOFORGE_ORE_TAG;
    public static final ModConfigSpec.BooleanValue USE_CUSTOM_ORE_TAG;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENABLED_DIMENSIONS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("respawn");

        RESPAWN_DELAY_SECONDS = builder
                .comment(
                        "Delai avant qu'un minerai mine ne reapparaisse, en secondes.",
                        "Ce delai est base sur le temps reel (horloge systeme), pas sur le temps in-game.",
                        "Valeur par defaut : 86400 (24 heures)."
                )
                .defineInRange("respawnDelaySeconds", 86400, 1, Integer.MAX_VALUE);

        CHECK_INTERVAL_TICKS = builder
                .comment(
                        "Intervalle, en ticks serveur (20 ticks = 1 seconde), entre deux passages",
                        "du planificateur qui regenere les minerais dont le delai est ecoule.",
                        "Valeur par defaut : 200 (10 secondes)."
                )
                .defineInRange("checkIntervalTicks", 200, 1, Integer.MAX_VALUE);

        REQUIRE_EMPTY_SPACE_OR_ORIGINAL_FILLER = builder
                .comment(
                        "Si vrai, un minerai ne reapparait que si son emplacement est toujours vide (air)",
                        "ou contient encore la roche d'origine (stone/deepslate/etc). Protege les",
                        "constructions des joueurs contre un ecrasement par la regeneration."
                )
                .define("requireEmptySpaceOrOriginalFiller", true);

        RESPAWN_RADIUS = builder
                .comment(
                        "Rayon (en blocs) autour d'un joueur dans lequel un minerai est autorise a",
                        "reapparaitre. Un minerai hors de portee de tout joueur de la dimension est",
                        "laisse en attente et reessaye au prochain passage.",
                        "0 desactive cette restriction (comportement par defaut : toute la dimension)."
                )
                .defineInRange("respawnRadius", 0, 0, Integer.MAX_VALUE);

        builder.pop();

        builder.push("detection");

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
                .defineListAllowEmpty("whitelist", List.of(), () -> "", OreRespawnConfig::isValidBlockId);

        BLACKLIST = builder
                .comment(
                        "Identifiants de blocs jamais traites comme des minerais par OreRespawn.",
                        "La blacklist est prioritaire sur les tags et sur la whitelist."
                )
                .defineListAllowEmpty("blacklist", List.of(), () -> "", OreRespawnConfig::isValidBlockId);

        builder.pop();

        builder.push("dimensions");

        ENABLED_DIMENSIONS = builder
                .comment(
                        "Identifiants des dimensions (ex: \"minecraft:overworld\") dans lesquelles",
                        "OreRespawn est actif."
                )
                .defineListAllowEmpty(
                        "enabledDimensions",
                        List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"),
                        () -> "minecraft:overworld",
                        OreRespawnConfig::isValidBlockId
                );

        builder.pop();

        SPEC = builder.build();
    }

    private OreRespawnConfig() {
    }

    private static boolean isValidBlockId(Object obj) {
        return obj instanceof String s && ResourceLocation.tryParse(s) != null;
    }

    public static boolean isDimensionEnabled(ResourceKey<Level> dimension) {
        String id = dimension.location().toString();
        for (String enabled : ENABLED_DIMENSIONS.get()) {
            if (enabled.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
