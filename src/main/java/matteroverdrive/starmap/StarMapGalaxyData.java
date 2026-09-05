package matteroverdrive.starmap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-global persistent ownership/building state for deterministic Star Map planets. */
public final class StarMapGalaxyData extends SavedData {
    private static final String DATA_NAME = "matteroverdrive_starmap_galaxy";
    private final Map<String, PlanetState> planets = new HashMap<>();

    public static StarMapGalaxyData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(StarMapGalaxyData::load, StarMapGalaxyData::new, DATA_NAME);
    }

    public PlanetState planet(int quadrant, int star, int planet) {
        return planets.computeIfAbsent(key(quadrant, star, planet), ignored -> new PlanetState());
    }

    public boolean ensureHomeworld(int quadrant, int star, int planet, UUID owner) {
        PlanetState state = planet(quadrant, star, planet);
        if (state.owner != null && !state.owner.equals(owner)) return false;
        boolean changed = state.owner == null || !state.base || !state.shipFactory;
        state.owner = owner;
        state.base = true;
        state.shipFactory = true;
        if (changed) setDirty();
        return true;
    }

    public boolean colonize(int quadrant, int star, int planet, UUID owner) {
        PlanetState state = planet(quadrant, star, planet);
        if (state.owner != null || state.base) return false;
        state.owner = owner;
        state.base = true;
        setDirty();
        return true;
    }

    public boolean addShipFactory(int quadrant, int star, int planet, UUID owner) {
        PlanetState state = planet(quadrant, star, planet);
        if (!state.isOwner(owner) || !state.base || state.shipFactory) return false;
        state.shipFactory = true;
        setDirty();
        return true;
    }

    public boolean addHangar(int quadrant, int star, int planet, UUID owner) {
        PlanetState state = planet(quadrant, star, planet);
        if (!state.isOwner(owner) || !state.base || state.hangars >= 8) return false;
        state.hangars++;
        setDirty();
        return true;
    }

    private static String key(int q, int s, int p) { return q + ":" + s + ":" + p; }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (var entry : planets.entrySet()) {
            PlanetState state = entry.getValue();
            CompoundTag e = new CompoundTag();
            e.putString("Key", entry.getKey());
            if (state.owner != null) e.putUUID("Owner", state.owner);
            e.putBoolean("Base", state.base);
            e.putBoolean("ShipFactory", state.shipFactory);
            e.putInt("Hangars", state.hangars);
            list.add(e);
        }
        tag.put("Planets", list);
        return tag;
    }

    private static StarMapGalaxyData load(CompoundTag tag) {
        StarMapGalaxyData data = new StarMapGalaxyData();
        ListTag list = tag.getList("Planets", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            String key = e.getString("Key");
            if (key.isEmpty()) continue;
            PlanetState state = new PlanetState();
            state.owner = e.hasUUID("Owner") ? e.getUUID("Owner") : null;
            state.base = e.getBoolean("Base");
            state.shipFactory = e.getBoolean("ShipFactory");
            state.hangars = Math.max(0, Math.min(8, e.getInt("Hangars")));
            data.planets.put(key, state);
        }
        return data;
    }

    public static final class PlanetState {
        @Nullable private UUID owner;
        private boolean base;
        private boolean shipFactory;
        private int hangars;
        public PlanetState() {}
        @Nullable public UUID owner() { return owner; }
        public boolean hasOwner() { return owner != null; }
        public boolean isOwner(UUID uuid) { return owner != null && owner.equals(uuid); }
        public boolean hasBase() { return base; }
        public boolean hasShipFactory() { return shipFactory; }
        public int hangars() { return hangars; }
        /** Legacy hangars add two fleet spaces. A colonized base has two baseline berths. */
        public int fleetCapacity() { return 2 + hangars * 2; }
    }
}
