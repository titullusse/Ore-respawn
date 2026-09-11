package com.marc33.orerespawn;

import com.marc33.orerespawn.config.DetectionConfig;
import com.marc33.orerespawn.config.DimensionConfig;
import com.marc33.orerespawn.config.RespawnConfig;
import com.marc33.orerespawn.config.SeasonConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * OreRespawn regenerates mined ore blocks server-side after a configurable delay.
 * The mod only ever needs to run on a dedicated server: it has no client-side content.
 */
@Mod(value = OreRespawnMod.MOD_ID, dist = Dist.DEDICATED_SERVER)
public class OreRespawnMod {

    public static final String MOD_ID = "orerespawn";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OreRespawnMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, RespawnConfig.SPEC, MOD_ID + "-respawn.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, DetectionConfig.SPEC, MOD_ID + "-detection.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, DimensionConfig.SPEC, MOD_ID + "-dimensions.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, SeasonConfig.SPEC, MOD_ID + "-seasons.toml");
        LOGGER.info("OreRespawn charge : les minerais mines reapparaitront apres le delai configure.");
    }
}
