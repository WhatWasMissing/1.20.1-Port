package matteroverdrive.starmap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Legacy-style per-ship travel events: one ship, origin, destination, start and duration. */
public final class StarMapShipTravelData extends SavedData {
    public static final int SHIP_SCOUT = 1;
    public static final int SHIP_COLONIZER = 2;
    private static final String DATA_NAME = "matteroverdrive_starmap_ship_travel";
    private final List<TravelEvent> events = new ArrayList<>();

    public static StarMapShipTravelData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(StarMapShipTravelData::load, StarMapShipTravelData::new, DATA_NAME);
    }

    public boolean dispatch(UUID owner, long consolePos, int shipType, int fq, int fs, int fp, int tq, int ts, int tp, long now) {
        if (shipType != SHIP_SCOUT && shipType != SHIP_COLONIZER) return false;
        int duration = StarMapCatalog.travelTicks(fq, fs, fp, tq, ts, tp);
        if (duration <= 0) return false;
        events.add(new TravelEvent(owner, consolePos, shipType, fq, fs, fp, tq, ts, tp, now, duration));
        setDirty();
        return true;
    }

    public int countInTransit(UUID owner, long consolePos, int shipType) {
        int count = 0;
        for (TravelEvent event : events) if (event.owner.equals(owner) && event.consolePos == consolePos && event.shipType == shipType) count++;
        return count;
    }

    public List<Arrival> collectArrivals(ServerLevel level, UUID owner, long consolePos, long now) {
        List<Arrival> result = new ArrayList<>();
        Iterator<TravelEvent> it = events.iterator();
        while (it.hasNext()) {
            TravelEvent event = it.next();
            if (!event.owner.equals(owner) || event.consolePos != consolePos || now < event.start + event.duration) continue;
            result.add(new Arrival(event.shipType, event.toQ, event.toS, event.toP));
            it.remove();
            setDirty();
        }
        return result;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (TravelEvent event : events) {
            CompoundTag e = new CompoundTag();
            e.putUUID("Owner", event.owner); e.putLong("Console", event.consolePos); e.putInt("Ship", event.shipType);
            e.putInt("FromQ", event.fromQ); e.putInt("FromS", event.fromS); e.putInt("FromP", event.fromP);
            e.putInt("ToQ", event.toQ); e.putInt("ToS", event.toS); e.putInt("ToP", event.toP);
            e.putLong("Start", event.start); e.putInt("Duration", event.duration); list.add(e);
        }
        tag.put("Events", list); return tag;
    }

    private static StarMapShipTravelData load(CompoundTag tag) {
        StarMapShipTravelData data = new StarMapShipTravelData();
        ListTag list = tag.getList("Events", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            if (!e.hasUUID("Owner")) continue;
            int ship = e.getInt("Ship"); if (ship != SHIP_SCOUT && ship != SHIP_COLONIZER) continue;
            data.events.add(new TravelEvent(e.getUUID("Owner"), e.getLong("Console"), ship,
                    e.getInt("FromQ"), e.getInt("FromS"), e.getInt("FromP"), e.getInt("ToQ"), e.getInt("ToS"), e.getInt("ToP"),
                    e.getLong("Start"), Math.max(1, e.getInt("Duration"))));
        }
        return data;
    }

    private record TravelEvent(UUID owner, long consolePos, int shipType, int fromQ, int fromS, int fromP,
                               int toQ, int toS, int toP, long start, int duration) {}
    public record Arrival(int shipType, int q, int s, int p) {}
}
