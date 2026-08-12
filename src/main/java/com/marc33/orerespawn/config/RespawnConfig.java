package com.marc33.orerespawn.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Timing and safety settings for the respawn process itself. Lives in its own file
 * ({@code orerespawn-respawn.toml}) so it can be tuned without touching detection or
 * dimension settings.
 */
public final class RespawnConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue RESPAWN_DELAY_SECONDS;
    public static final ModConfigSpec.IntValue CHECK_INTERVAL_TICKS;
    public static final ModConfigSpec.BooleanValue REQUIRE_EMPTY_SPACE_OR_ORIGINAL_FILLER;
    public static final ModConfigSpec.IntValue RESPAWN_RADIUS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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

        SPEC = builder.build();
    }

    private RespawnConfig() {
    }
}
