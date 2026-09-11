package com.marc33.orerespawn.command;

import com.marc33.orerespawn.OreRespawnMod;
import com.marc33.orerespawn.config.RespawnConfig;
import com.marc33.orerespawn.data.MinedOreEntry;
import com.marc33.orerespawn.data.OreRespawnSavedData;
import com.marc33.orerespawn.event.OreRespawnTicker;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code /orerespawn list [page]}, {@code /orerespawn count} and {@code /orerespawn respawn},
 * scoped to the caller's current dimension. Requires permission level 2 (server operator).
 */
@EventBusSubscriber(modid = OreRespawnMod.MOD_ID)
public final class OreRespawnCommand {

    private static final int ENTRIES_PER_PAGE = 10;
    private static final int OP_PERMISSION_LEVEL = 2;

    private OreRespawnCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("orerespawn")
                        .requires(source -> source.hasPermission(OP_PERMISSION_LEVEL))
                        .then(Commands.literal("list")
                                .executes(ctx -> listOres(ctx.getSource(), 1))
                                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                        .executes(ctx -> listOres(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))))
                        .then(Commands.literal("count")
                                .executes(ctx -> countOres(ctx.getSource())))
                        .then(Commands.literal("respawn")
                                .executes(ctx -> forceRespawn(ctx.getSource())))
        );
    }

    private static int listOres(CommandSourceStack source, int page) {
        ServerLevel level = source.getLevel();
        List<MinedOreEntry> entries = OreRespawnSavedData.get(level).getEntries();

        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Aucun minerai en attente de reapparition dans cette dimension."), false);
            return 0;
        }

        int totalPages = (entries.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE;
        int clampedPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = (clampedPage - 1) * ENTRIES_PER_PAGE;
        int toIndex = Math.min(fromIndex + ENTRIES_PER_PAGE, entries.size());

        long delayMillis = RespawnConfig.effectiveDelayMillis();
        long now = System.currentTimeMillis();

        int finalClampedPage = clampedPage;
        source.sendSuccess(() -> Component.literal(String.format(
                "Minerais en attente (page %d/%d, %d au total) :", finalClampedPage, totalPages, entries.size()
        )), false);

        for (int i = fromIndex; i < toIndex; i++) {
            MinedOreEntry entry = entries.get(i);
            BlockPos pos = entry.pos();
            long remainingSeconds = Math.max(0, (delayMillis - (now - entry.minedAtMillis())) / 1000L);
            source.sendSuccess(() -> Component.literal(String.format(
                    "- %s en (%d, %d, %d), reapparait dans %ds",
                    entry.oreBlockId(), pos.getX(), pos.getY(), pos.getZ(), remainingSeconds
            )), false);
        }

        return entries.size();
    }

    private static int countOres(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        int count = OreRespawnSavedData.get(level).getEntries().size();
        source.sendSuccess(() -> Component.literal("Minerais en attente de reapparition dans cette dimension : " + count), false);
        return count;
    }

    /**
     * Forces every mined ore in the caller's dimension to respawn immediately, ignoring the
     * configured delay. Still honours {@code requireEmptySpaceOrOriginalFiller} and skips
     * entries in unloaded chunks, which are left in the log to be retried normally.
     */
    private static int forceRespawn(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        OreRespawnSavedData data = OreRespawnSavedData.get(level);
        boolean requireOriginal = RespawnConfig.REQUIRE_EMPTY_SPACE_OR_ORIGINAL_FILLER.get();

        List<MinedOreEntry> entries = new ArrayList<>(data.getEntries());
        int respawned = 0;
        for (MinedOreEntry entry : entries) {
            if (OreRespawnTicker.tryRespawn(level, data, entry, requireOriginal)) {
                respawned++;
            }
        }

        int skipped = entries.size() - respawned;
        int finalRespawned = respawned;
        source.sendSuccess(() -> Component.literal(String.format(
                "%d minerai(s) regenere(s) immediatement. %d laisse(s) en attente (chunk non charge ou emplacement occupe).",
                finalRespawned, skipped
        )), true);

        return respawned;
    }
}
