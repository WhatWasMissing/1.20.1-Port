package matteroverdrive.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * World-persistent side policy used by Matter Overdrive's own cables/networks.
 * This lets old machines gain side configuration without rewriting every legacy block entity at once.
 */
public class MachineSideConfigurationData extends SavedData {
    public static final String DATA_NAME = "matteroverdrive_machine_sides";
    public enum Resource { ENERGY, MATTER, ITEMS }
    public enum Mode {
        DISABLED, INPUT, OUTPUT, BOTH;
        public Mode next() { return values()[(ordinal() + 1) % values().length]; }
        public boolean input() { return this == INPUT || this == BOTH; }
        public boolean output() { return this == OUTPUT || this == BOTH; }
    }

    private final Map<Long, Entry> entries = new HashMap<>();

    public static MachineSideConfigurationData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(MachineSideConfigurationData::load, MachineSideConfigurationData::new, DATA_NAME);
    }
    public static Mode mode(Level level, BlockPos pos, Direction side, Resource resource) {
        if (!(level instanceof ServerLevel server)) return Mode.BOTH;
        return get(server).mode(pos, side, resource);
    }
    public static boolean allowsInput(Level level, BlockPos pos, Direction side, Resource resource) { return mode(level, pos, side, resource).input(); }
    public static boolean allowsOutput(Level level, BlockPos pos, Direction side, Resource resource) { return mode(level, pos, side, resource).output(); }

    public Mode mode(BlockPos pos, Direction side, Resource resource) {
        Entry entry = entries.get(pos.asLong());
        return entry == null ? Mode.BOTH : unpack(entry.packed(resource), side);
    }
    public Mode cycle(BlockPos pos, Direction side, Resource resource) {
        Entry entry = entries.computeIfAbsent(pos.asLong(), ignored -> new Entry(defaultPacked(), defaultPacked(), defaultPacked()));
        Mode next = mode(pos, side, resource).next();
        int packed = setPacked(entry.packed(resource), side, next);
        entry.setPacked(resource, packed);
        if (entry.isDefault()) entries.remove(pos.asLong());
        setDirty();
        return next;
    }
    public void copy(BlockPos from, BlockPos to) {
        Entry source = entries.get(from.asLong());
        if (source == null) entries.remove(to.asLong()); else entries.put(to.asLong(), source.copy());
        setDirty();
    }
    public void clear(BlockPos pos) { if (entries.remove(pos.asLong()) != null) setDirty(); }

    private static int defaultPacked() {
        int packed = 0;
        for (Direction direction : Direction.values()) packed = setPacked(packed, direction, Mode.BOTH);
        return packed;
    }
    private static Mode unpack(int packed, Direction side) { return Mode.values()[(packed >> (side.get3DDataValue() * 2)) & 3]; }
    private static int setPacked(int packed, Direction side, Mode mode) {
        int shift = side.get3DDataValue() * 2;
        return (packed & ~(3 << shift)) | ((mode.ordinal() & 3) << shift);
    }

    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        entries.forEach((pos, entry) -> {
            CompoundTag row = new CompoundTag(); row.putLong("Pos", pos); row.putInt("Energy", entry.energy); row.putInt("Matter", entry.matter); row.putInt("Items", entry.items); list.add(row);
        });
        tag.put("Entries", list); return tag;
    }
    public static MachineSideConfigurationData load(CompoundTag tag) {
        MachineSideConfigurationData data = new MachineSideConfigurationData();
        if (tag.contains("Entries", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag row = list.getCompound(i);
                data.entries.put(row.getLong("Pos"), new Entry(row.getInt("Energy"), row.getInt("Matter"), row.getInt("Items")));
            }
        }
        return data;
    }

    private static final class Entry {
        int energy, matter, items;
        Entry(int energy, int matter, int items) { this.energy = energy; this.matter = matter; this.items = items; }
        int packed(Resource resource) { return switch (resource) { case ENERGY -> energy; case MATTER -> matter; case ITEMS -> items; }; }
        void setPacked(Resource resource, int value) { switch (resource) { case ENERGY -> energy = value; case MATTER -> matter = value; case ITEMS -> items = value; } }
        boolean isDefault() { int d = defaultPacked(); return energy == d && matter == d && items == d; }
        Entry copy() { return new Entry(energy, matter, items); }
    }
}