package com.marc33.orerespawn.event;

import com.marc33.orerespawn.OreRespawnMod;
import com.marc33.orerespawn.config.OreRespawnConfig;
import com.marc33.orerespawn.data.MinedOreEntry;
import com.marc33.orerespawn.data.OreDetector;
import com.marc33.orerespawn.data.OreRespawnSavedData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Records ores as they are mined, so {@link OreRespawnTicker} can bring them back later.
 */
@EventBusSubscriber(modid = OreRespawnMod.MOD_ID)
public final class OreBreakListener {

    private OreBreakListener() {
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel level)) {
            return;
        }
        if (!OreRespawnConfig.isDimensionEnabled(level.dimension())) {
            return;
        }

        BlockState state = event.getState();
        if (!OreDetector.isOre(state)) {
            return;
        }

        ResourceLocation oreId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        ResourceLocation fillerId = BuiltInRegistries.BLOCK.getKey(OreDetector.guessFillerBlock(oreId));

        MinedOreEntry entry = new MinedOreEntry(event.getPos().immutable(), oreId, fillerId, System.currentTimeMillis());
        OreRespawnSavedData.get(level).addEntry(entry);
    }
}
