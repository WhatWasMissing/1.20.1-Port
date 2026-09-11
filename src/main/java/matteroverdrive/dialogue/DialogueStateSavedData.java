package matteroverdrive.dialogue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Persistent per-player conversation memory for contemporary Matter Overdrive NPCs. */
public final class DialogueStateSavedData extends SavedData {
    private static final String ID = "matteroverdrive_dialogue_state";
    private static final int MAX_PLAYERS = 4096;
    private static final int MAX_FLAGS = 256;

    private final Map<UUID, PlayerState> players = new HashMap<>();

    private static final class PlayerState {
        final Map<String, Integer> visits = new HashMap<>();
        final Map<String, String> lastChoice = new HashMap<>();
        final Set<String> flags = new HashSet<>();
        int fieldTrust;
        int syntheticTrust;
        int archiveInsight;
    }

    public static DialogueStateSavedData get(ServerLevel level) {
        ServerLevel owner = level.getServer().overworld();
        return owner.getDataStorage().computeIfAbsent(DialogueStateSavedData::load, DialogueStateSavedData::new, ID);
    }

    private PlayerState state(UUID player) {
        if (players.size() >= MAX_PLAYERS && !players.containsKey(player)) return new PlayerState();
        return players.computeIfAbsent(player, ignored -> new PlayerState());
    }

    public int visits(UUID player, String dialogueId) {
        PlayerState state = players.get(player);
        return state == null ? 0 : state.visits.getOrDefault(dialogueId, 0);
    }

    public void markVisited(UUID player, String dialogueId) {
        PlayerState state = state(player);
        state.visits.put(dialogueId, Math.min(9999, state.visits.getOrDefault(dialogueId, 0) + 1));
        setDirty();
    }

    public String lastChoice(UUID player, String dialogueId) {
        PlayerState state = players.get(player);
        return state == null ? "" : state.lastChoice.getOrDefault(dialogueId, "");
    }

    public boolean hasFlag(UUID player, String flag) {
        PlayerState state = players.get(player);
        return state != null && state.flags.contains(flag);
    }

    public int fieldTrust(UUID player) {
        PlayerState state = players.get(player);
        return state == null ? 0 : state.fieldTrust;
    }

    public int syntheticTrust(UUID player) {
        PlayerState state = players.get(player);
        return state == null ? 0 : state.syntheticTrust;
    }

    public int archiveInsight(UUID player) {
        PlayerState state = players.get(player);
        return state == null ? 0 : state.archiveInsight;
    }

    public void recordChoice(UUID player, String dialogueId, String choiceId, String flag,
                             int fieldDelta, int syntheticDelta, int insightDelta) {
        PlayerState state = state(player);
        state.lastChoice.put(dialogueId, choiceId == null ? "" : choiceId);
        if (flag != null && !flag.isBlank() && state.flags.size() < MAX_FLAGS) state.flags.add(flag);
        state.fieldTrust = clamp(state.fieldTrust + fieldDelta, -20, 20);
        state.syntheticTrust = clamp(state.syntheticTrust + syntheticDelta, -20, 20);
        state.archiveInsight = clamp(state.archiveInsight + insightDelta, 0, 100);
        setDirty();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static DialogueStateSavedData load(CompoundTag tag) {
        DialogueStateSavedData data = new DialogueStateSavedData();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size() && i < MAX_PLAYERS; i++) {
            CompoundTag entry = players.getCompound(i);
            if (!entry.hasUUID("Player")) continue;
            PlayerState state = new PlayerState();
            state.fieldTrust = entry.getInt("FieldTrust");
            state.syntheticTrust = entry.getInt("SyntheticTrust");
            state.archiveInsight = entry.getInt("ArchiveInsight");

            ListTag dialogues = entry.getList("Dialogues", Tag.TAG_COMPOUND);
            for (int j = 0; j < dialogues.size(); j++) {
                CompoundTag d = dialogues.getCompound(j);
                String id = d.getString("Id");
                if (id.isBlank()) continue;
                state.visits.put(id, Math.max(0, d.getInt("Visits")));
                String last = d.getString("LastChoice");
                if (!last.isBlank()) state.lastChoice.put(id, last);
            }

            ListTag flags = entry.getList("Flags", Tag.TAG_STRING);
            for (int j = 0; j < flags.size() && state.flags.size() < MAX_FLAGS; j++) {
                String flag = flags.getString(j);
                if (!flag.isBlank()) state.flags.add(flag);
            }
            data.players.put(entry.getUUID("Player"), state);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag playersTag = new ListTag();
        players.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());
            PlayerState state = entry.getValue();
            playerTag.putInt("FieldTrust", state.fieldTrust);
            playerTag.putInt("SyntheticTrust", state.syntheticTrust);
            playerTag.putInt("ArchiveInsight", state.archiveInsight);

            ListTag dialogues = new ListTag();
            state.visits.keySet().stream().sorted().forEach(id -> {
                CompoundTag d = new CompoundTag();
                d.putString("Id", id);
                d.putInt("Visits", state.visits.getOrDefault(id, 0));
                String last = state.lastChoice.getOrDefault(id, "");
                if (!last.isBlank()) d.putString("LastChoice", last);
                dialogues.add(d);
            });
            playerTag.put("Dialogues", dialogues);

            ListTag flags = new ListTag();
            state.flags.stream().sorted().limit(MAX_FLAGS).forEach(flag -> flags.add(StringTag.valueOf(flag)));
            playerTag.put("Flags", flags);
            playersTag.add(playerTag);
        });
        tag.put("Players", playersTag);
        return tag;
    }
}
