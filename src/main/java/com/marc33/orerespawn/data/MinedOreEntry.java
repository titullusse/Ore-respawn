package com.marc33.orerespawn.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * A single ore block that was mined and is waiting to respawn.
 *
 * @param pos             the position of the mined ore, in world/chunk-relative absolute coordinates
 * @param oreBlockId      registry id of the ore block that was broken (what gets placed back on respawn)
 * @param fillerBlockId   registry id of the stone-like block the ore was embedded in (e.g. stone, deepslate),
 *                        used to tell an untouched hole apart from a player build on top of it
 * @param minedAtMillis   real-world timestamp (System.currentTimeMillis()) at which the ore was mined
 */
public record MinedOreEntry(BlockPos pos, ResourceLocation oreBlockId, ResourceLocation fillerBlockId, long minedAtMillis) {

    private static final String TAG_POS = "Pos";
    private static final String TAG_ORE = "Ore";
    private static final String TAG_FILLER = "Filler";
    private static final String TAG_TIME = "Time";

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(TAG_POS, pos.asLong());
        tag.putString(TAG_ORE, oreBlockId.toString());
        tag.putString(TAG_FILLER, fillerBlockId.toString());
        tag.putLong(TAG_TIME, minedAtMillis);
        return tag;
    }

    public static MinedOreEntry fromNbt(CompoundTag tag) {
        return new MinedOreEntry(
                BlockPos.of(tag.getLong(TAG_POS)),
                ResourceLocation.parse(tag.getString(TAG_ORE)),
                ResourceLocation.parse(tag.getString(TAG_FILLER)),
                tag.getLong(TAG_TIME)
        );
    }
}
