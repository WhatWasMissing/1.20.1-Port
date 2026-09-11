package matteroverdrive.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Per-world ledger used to show the field briefing once to each player. */
public final class WorldOnboardingSavedData extends SavedData {
    private static final String ID = "matteroverdrive_world_onboarding";
    private static final int MAX_PLAYERS = 4096;
    private final Set<UUID> briefed = new HashSet<>();

    public static WorldOnboardingSavedData get(ServerLevel level) {
        ServerLevel root = level.getServer().getLevel(Level.OVERWORLD);
        if (root == null) root = level;
        return root.getDataStorage().computeIfAbsent(
                WorldOnboardingSavedData::load, WorldOnboardingSavedData::new, ID);
    }

    /** Returns true only the first time this player is marked in this world. */
    public boolean markBriefed(UUID player) {
        if (player == null || briefed.contains(player) || briefed.size() >= MAX_PLAYERS) return false;
        briefed.add(player);
        setDirty();
        return true;
    }

    public boolean hasBriefing(UUID player) {
        return player != null && briefed.contains(player);
    }

    public static WorldOnboardingSavedData load(CompoundTag tag) {
        WorldOnboardingSavedData data = new WorldOnboardingSavedData();
        ListTag list = tag.getList("Briefed", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < list.size() && i < MAX_PLAYERS; i++) {
            try { data.briefed.add(UUID.fromString(list.getString(i))); }
            catch (IllegalArgumentException ignored) { }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        briefed.stream().sorted().limit(MAX_PLAYERS)
                .forEach(value -> list.add(StringTag.valueOf(value.toString())));
        tag.put("Briefed", list);
        return tag;
    }
}
