package com.marc33.orerespawn.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Persists the ores a dimension is waiting to respawn.
 * <p>
 * One instance exists per dimension, but all of them are stored under the overworld's
 * data folder (saves/&lt;world&gt;/data/orerespawn_&lt;dimension&gt;.dat) so the files stay
 * together and don't depend on a non-overworld dimension having been loaded at least once.
 */
public class OreRespawnSavedData extends SavedData {

    private static final String DATA_NAME_PREFIX = "orerespawn_";
    private static final String TAG_ENTRIES = "MinedOres";

    private final List<MinedOreEntry> entries = new ArrayList<>();

    public static OreRespawnSavedData get(ServerLevel level) {
        ServerLevel storageLevel = level.getServer().overworld();
        String dimensionId = level.dimension().location().toString().replace(':', '_').replace('/', '_');

        return storageLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(OreRespawnSavedData::new, OreRespawnSavedData::load, null),
                DATA_NAME_PREFIX + dimensionId
        );
    }

    private static OreRespawnSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        OreRespawnSavedData data = new OreRespawnSavedData();
        ListTag list = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            data.entries.add(MinedOreEntry.fromNbt(list.getCompound(i)));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (MinedOreEntry entry : entries) {
            list.add(entry.toNbt());
        }
        tag.put(TAG_ENTRIES, list);
        return tag;
    }

    public void addEntry(MinedOreEntry entry) {
        entries.add(entry);
        setDirty();
    }

    public void removeEntry(MinedOreEntry entry) {
        if (entries.remove(entry)) {
            setDirty();
        }
    }

    public List<MinedOreEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}
