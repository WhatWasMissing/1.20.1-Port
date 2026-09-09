package matteroverdrive.world;

import matteroverdrive.MatterOverdrive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/** Persistent restoration progress for generated native technology facilities. */
public final class FacilityRestorationSavedData extends SavedData {
    private static final String DATA_NAME = "matteroverdrive_facility_restoration";
    public static final int STAGE_POWER = 0;
    public static final int STAGE_REPAIR = 1;
    public static final int STAGE_RESEARCH = 2;
    public static final int STAGE_COMPLETE = 3;

    public enum FacilityProfile {
        SYNTHETIC_MANUFACTURING_PLANT("synthetic_manufacturing_plant", "Synthetic Manufacturing Plant"),
        MATTER_REFINERY("matter_refinery", "Matter Refinery"),
        QUANTUM_RELAY_STATION("quantum_relay_station", "Quantum Relay Station"),
        ANDROID_COMMAND_BUNKER("android_command_bunker", "Android Command Bunker"),
        FUSION_RESEARCH_COMPLEX("fusion_research_complex", "Fusion Research Complex"),
        BLACK_SITE("black_site", "Black Site");

        private final String id;
        private final String displayName;
        private final ResourceKey<Structure> structureKey;

        FacilityProfile(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
            this.structureKey = ResourceKey.create(Registries.STRUCTURE,
                    new ResourceLocation(MatterOverdrive.MOD_ID, id));
        }

        public String id() { return id; }
        public String displayName() { return displayName; }
        public String archiveId() { return name(); }
        public ResourceKey<Structure> structureKey() { return structureKey; }
    }

    public record FacilityLocation(FacilityProfile profile, String stateKey) {}

    private final Map<String, Integer> stages = new LinkedHashMap<>();

    public FacilityRestorationSavedData() {}

    public static FacilityRestorationSavedData load(CompoundTag root) {
        FacilityRestorationSavedData data = new FacilityRestorationSavedData();
        ListTag list = root.getList("Facilities", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            String key = entry.getString("Key");
            if (!key.isBlank()) data.stages.put(key, Mth.clamp(entry.getInt("Stage"), STAGE_POWER, STAGE_COMPLETE));
        }
        return data;
    }

    public static FacilityRestorationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                FacilityRestorationSavedData::load,
                FacilityRestorationSavedData::new,
                DATA_NAME);
    }

    @Nullable
    public static FacilityLocation locate(ServerLevel level, BlockPos pos) {
        for (FacilityProfile profile : FacilityProfile.values()) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(pos, profile.structureKey());
            if (!start.isValid()) continue;
            String key = profile.id() + ":" + start.getChunkPos().x + "," + start.getChunkPos().z;
            return new FacilityLocation(profile, key);
        }
        return null;
    }

    public int stage(FacilityLocation location) {
        return stages.getOrDefault(location.stateKey(), STAGE_POWER);
    }

    public void setStage(FacilityLocation location, int stage) {
        int clamped = Mth.clamp(stage, STAGE_POWER, STAGE_COMPLETE);
        int previous = stages.getOrDefault(location.stateKey(), STAGE_POWER);
        if (previous == clamped && stages.containsKey(location.stateKey())) return;
        stages.put(location.stateKey(), clamped);
        setDirty();
    }

    public static int stageAt(ServerLevel level, BlockPos pos) {
        FacilityLocation location = locate(level, pos);
        return location == null ? -1 : get(level).stage(location);
    }

    /** Generated security shutters release after emergency power and control hardware are restored. */
    public static boolean isAccessUnlocked(ServerLevel level, BlockPos pos) {
        int stage = stageAt(level, pos);
        return stage < 0 || stage >= STAGE_RESEARCH;
    }

    /** Full restoration requires the recovered family dossier after secure access has been released. */
    public static boolean isFacilityRestored(ServerLevel level, BlockPos pos) {
        return stageAt(level, pos) >= STAGE_COMPLETE;
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        ListTag list = new ListTag();
        stages.forEach((key, stage) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Key", key);
            entry.putInt("Stage", stage);
            list.add(entry);
        });
        root.put("Facilities", list);
        return root;
    }
}
