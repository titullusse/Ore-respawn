package com.marc33.orerespawn.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Seasonal variation of the respawn delay, based on the real-world calendar date (the same
 * time source {@code respawnDelaySeconds} already uses). Lives in its own file
 * ({@code orerespawn-seasons.toml}) so it can be tuned without touching the rest of the
 * respawn settings.
 */
public final class SeasonConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_SEASONS;
    public static final ModConfigSpec.BooleanValue SOUTHERN_HEMISPHERE;
    public static final ModConfigSpec.DoubleValue SPRING_DELAY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue SUMMER_DELAY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue AUTUMN_DELAY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue WINTER_DELAY_MULTIPLIER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLE_SEASONS = builder
                .comment(
                        "Si vrai, respawnDelaySeconds est multiplie selon la saison reelle en cours,",
                        "basee sur la date du serveur (printemps/ete/automne/hiver).",
                        "Valeur par defaut : false."
                )
                .define("enableSeasons", false);

        SOUTHERN_HEMISPHERE = builder
                .comment(
                        "Si vrai, les saisons sont inversees pour correspondre a l'hemisphere sud",
                        "(ex: decembre-fevrier devient l'ete plutot que l'hiver)."
                )
                .define("southernHemisphere", false);

        SPRING_DELAY_MULTIPLIER = builder
                .comment("Multiplicateur applique a respawnDelaySeconds au printemps (mars-mai).")
                .defineInRange("springDelayMultiplier", 1.0, 0.01, 100.0);

        SUMMER_DELAY_MULTIPLIER = builder
                .comment(
                        "Multiplicateur applique a respawnDelaySeconds en ete (juin-aout).",
                        "Exemple : 0.75 fait reapparaitre les minerais 25% plus vite qu'en temps normal."
                )
                .defineInRange("summerDelayMultiplier", 0.75, 0.01, 100.0);

        AUTUMN_DELAY_MULTIPLIER = builder
                .comment("Multiplicateur applique a respawnDelaySeconds en automne (septembre-novembre).")
                .defineInRange("autumnDelayMultiplier", 1.0, 0.01, 100.0);

        WINTER_DELAY_MULTIPLIER = builder
                .comment(
                        "Multiplicateur applique a respawnDelaySeconds en hiver (decembre-fevrier).",
                        "Exemple : 1.5 fait reapparaitre les minerais 50% plus lentement qu'en temps normal."
                )
                .defineInRange("winterDelayMultiplier", 1.5, 0.01, 100.0);

        SPEC = builder.build();
    }

    private SeasonConfig() {
    }
}
