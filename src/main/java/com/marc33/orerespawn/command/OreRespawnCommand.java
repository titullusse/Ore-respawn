package com.marc33.orerespawn.command;

import com.marc33.orerespawn.OreRespawnMod;
import com.marc33.orerespawn.config.OreRespawnConfig;
import com.marc33.orerespawn.data.MinedOreEntry;
import com.marc33.orerespawn.data.OreRespawnSavedData;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

/**
 * {@code /orerespawn list [page]} and {@code /orerespawn count}, scoped to the caller's
 * current dimension. Requires permission level 2 (server operator).
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

        long delayMillis = OreRespawnConfig.RESPAWN_DELAY_SECONDS.get() * 1000L;
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
}
