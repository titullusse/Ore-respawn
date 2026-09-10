package com.marc33.orerespawn.event;

import com.marc33.orerespawn.OreRespawnMod;
import com.marc33.orerespawn.config.DimensionConfig;
import com.marc33.orerespawn.config.RespawnConfig;
import com.marc33.orerespawn.data.MinedOreEntry;
import com.marc33.orerespawn.data.OreRespawnSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Periodically scans every enabled dimension's mined-ore log and respawns entries whose
 * delay has elapsed. Entries that can't be respawned yet (unloaded chunk, occupied space)
 * are simply left in place and retried on the next check.
 */
@EventBusSubscriber(modid = OreRespawnMod.MOD_ID)
public final class OreRespawnTicker {

    private static int ticksUntilNextCheck = 0;

    private OreRespawnTicker() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (--ticksUntilNextCheck > 0) {
            return;
        }
        ticksUntilNextCheck = RespawnConfig.CHECK_INTERVAL_TICKS.get();

        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            if (DimensionConfig.isDimensionEnabled(level.dimension())) {
                processLevel(level);
            }
        }
    }

    private static void processLevel(ServerLevel level) {
        OreRespawnSavedData data = OreRespawnSavedData.get(level);
        long now = System.currentTimeMillis();
        long delayMillis = RespawnConfig.effectiveDelayMillis();
        boolean requireOriginal = RespawnConfig.REQUIRE_EMPTY_SPACE_OR_ORIGINAL_FILLER.get();

        List<MinedOreEntry> due = new ArrayList<>();
        for (MinedOreEntry entry : data.getEntries()) {
            if (now - entry.minedAtMillis() >= delayMillis) {
                due.add(entry);
            }
        }

        for (MinedOreEntry entry : due) {
            tryRespawn(level, data, entry, requireOriginal);
        }
    }

    /**
     * Attempts to respawn a single mined ore entry, regardless of whether its delay has
     * elapsed. Also used by {@code /orerespawn respawn} to force an immediate respawn.
     *
     * @return true if the ore was respawned (or its entry dropped as stale), false if it was
     * left in place to be retried later (unloaded chunk, no player within respawnRadius, or
     * space occupied by a player build)
     */
    public static boolean tryRespawn(ServerLevel level, OreRespawnSavedData data, MinedOreEntry entry, boolean requireOriginal) {
        BlockPos pos = entry.pos();

        if (!level.isLoaded(pos)) {
            return false;
        }

        if (!isWithinRespawnRadius(level, pos)) {
            return false;
        }

        Optional<Block> oreBlock = BuiltInRegistries.BLOCK.getOptional(entry.oreBlockId());
        if (oreBlock.isEmpty()) {
            data.removeEntry(entry);
            return false;
        }

        if (requireOriginal) {
            BlockState current = level.getBlockState(pos);
            Block fillerBlock = BuiltInRegistries.BLOCK.getOptional(entry.fillerBlockId()).orElse(Blocks.STONE);
            boolean spaceIsSafe = current.isAir() || current.is(fillerBlock);
            if (!spaceIsSafe) {
                return false;
            }
        }

        level.setBlockAndUpdate(pos, oreBlock.get().defaultBlockState());
        data.removeEntry(entry);
        return true;
    }

    /**
     * True if {@code respawnRadius} is disabled (0), or if at least one player in the level
     * is within that many blocks of pos.
     */
    private static boolean isWithinRespawnRadius(ServerLevel level, BlockPos pos) {
        int radius = RespawnConfig.RESPAWN_RADIUS.get();
        if (radius <= 0) {
            return true;
        }

        double radiusSq = (double) radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().distSqr(pos) <= radiusSq) {
                return true;
            }
        }
        return false;
    }
}
