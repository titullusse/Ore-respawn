package com.marc33.orerespawn.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Which dimensions OreRespawn is active in. Lives in its own file
 * ({@code orerespawn-dimensions.toml}) so it can be tuned without touching respawn timing or
 * ore detection settings.
 */
public final class DimensionConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_ALL_DIMENSIONS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENABLED_DIMENSIONS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_DIMENSIONS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLE_ALL_DIMENSIONS = builder
                .comment(
                        "Si vrai, OreRespawn est actif dans TOUTES les dimensions chargees sur le",
                        "serveur, y compris celles ajoutees par d'autres mods, sans avoir a les lister",
                        "une par une. enabledDimensions est alors ignore ; disabledDimensions reste",
                        "applique pour exclure des cas particuliers.",
                        "Valeur par defaut : true."
                )
                .define("enableAllDimensions", true);

        ENABLED_DIMENSIONS = builder
                .comment(
                        "Utilise seulement si enableAllDimensions est desactive : liste explicite des",
                        "identifiants de dimensions (ex: \"mymod:custom_dimension\") dans lesquelles",
                        "OreRespawn est actif."
                )
                .defineListAllowEmpty(
                        "enabledDimensions",
                        List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"),
                        () -> "minecraft:overworld",
                        DimensionConfig::isValidDimensionId
                );

        DISABLED_DIMENSIONS = builder
                .comment(
                        "Identifiants de dimensions toujours exclues, meme si enableAllDimensions est",
                        "actif (ex: une dimension \"lobby\" ou une dimension d'extraction jetable ajoutee",
                        "par un autre mod)."
                )
                .defineListAllowEmpty("disabledDimensions", List.of(), () -> "", DimensionConfig::isValidDimensionId);

        SPEC = builder.build();
    }

    private DimensionConfig() {
    }

    private static boolean isValidDimensionId(Object obj) {
        return obj instanceof String s && ResourceLocation.tryParse(s) != null;
    }

    public static boolean isDimensionEnabled(ResourceKey<Level> dimension) {
        String id = dimension.location().toString();

        if (isListed(id, DISABLED_DIMENSIONS.get())) {
            return false;
        }

        if (ENABLE_ALL_DIMENSIONS.get()) {
            return true;
        }

        return isListed(id, ENABLED_DIMENSIONS.get());
    }

    private static boolean isListed(String id, List<? extends String> list) {
        for (String entry : list) {
            if (entry.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
