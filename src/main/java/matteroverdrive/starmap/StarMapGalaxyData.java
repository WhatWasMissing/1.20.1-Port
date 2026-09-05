package matteroverdrive.starmap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-global persistent ownership, building, production and stationed-fleet state for deterministic Star Map planets. */
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

    public boolean addShipFactory(int q,int s,int p,UUID owner){PlanetState state=planet(q,s,p);if(!state.isOwner(owner)||!state.base||state.shipFactory)return false;state.shipFactory=true;setDirty();return true;}
    public boolean addHangar(int q,int s,int p,UUID owner){PlanetState state=planet(q,s,p);if(!state.isOwner(owner)||!state.base||state.hangars>=8)return false;state.hangars++;setDirty();return true;}
    public boolean addMatterExtractor(int q,int s,int p,UUID owner){PlanetState state=planet(q,s,p);if(!state.isOwner(owner)||!state.base||state.matterExtractors>=16)return false;state.matterExtractors++;setDirty();return true;}
    public boolean addPowerGenerator(int q,int s,int p,UUID owner){PlanetState state=planet(q,s,p);if(!state.isOwner(owner)||!state.base||state.powerGenerators>=16)return false;state.powerGenerators++;setDirty();return true;}
    public boolean addResidential(int q,int s,int p,UUID owner){PlanetState state=planet(q,s,p);if(!state.isOwner(owner)||!state.base||state.residential>=16)return false;state.residential++;setDirty();return true;}

    public boolean addShip(int q,int s,int p,UUID owner,int shipType){
        PlanetState state=planet(q,s,p);if(!state.isOwner(owner)||!state.base||state.shipCount()>=state.fleetCapacity())return false;
        if(shipType==StarMapShipTravelData.SHIP_SCOUT)state.scoutShips++;
        else if(shipType==StarMapShipTravelData.SHIP_COLONIZER)state.colonizerShips++;
        else return false;
        setDirty();return true;
    }
    public boolean addArrivingShip(int q,int s,int p,UUID owner,int shipType){
        PlanetState state=planet(q,s,p);if(state.hasOwner()&&!state.isOwner(owner))return false;
        if(shipType==StarMapShipTravelData.SHIP_SCOUT)state.scoutShips++;
        else if(shipType==StarMapShipTravelData.SHIP_COLONIZER)state.colonizerShips++;
        else return false;
        setDirty();return true;
    }
    public boolean removeShip(int q,int s,int p,UUID owner,int shipType){
        PlanetState state=planet(q,s,p);if(!state.isOwner(owner))return false;
        if(shipType==StarMapShipTravelData.SHIP_SCOUT){if(state.scoutShips<=0)return false;state.scoutShips--;}
        else if(shipType==StarMapShipTravelData.SHIP_COLONIZER){if(state.colonizerShips<=0)return false;state.colonizerShips--;}
        else return false;
        setDirty();return true;
    }
    /** One-time bridge for saves from the console-local ship-counter implementation. */
    public void migrateLegacyShips(int q,int s,int p,UUID owner,int scouts,int colonizers){
        if(scouts<=0&&colonizers<=0)return;PlanetState state=planet(q,s,p);if(!state.isOwner(owner))return;
        state.scoutShips=Math.max(0,state.scoutShips+scouts);state.colonizerShips=Math.max(0,state.colonizerShips+colonizers);setDirty();
    }

    private static String key(int q,int s,int p){return q+":"+s+":"+p;}

    @Override public CompoundTag save(CompoundTag tag){
        ListTag list=new ListTag();
        for(var entry:planets.entrySet()){
            PlanetState state=entry.getValue();CompoundTag e=new CompoundTag();e.putString("Key",entry.getKey());
            if(state.owner!=null)e.putUUID("Owner",state.owner);e.putBoolean("Base",state.base);e.putBoolean("ShipFactory",state.shipFactory);
            e.putInt("Hangars",state.hangars);e.putInt("MatterExtractors",state.matterExtractors);e.putInt("PowerGenerators",state.powerGenerators);e.putInt("Residential",state.residential);
            e.putInt("ScoutShips",state.scoutShips);e.putInt("ColonizerShips",state.colonizerShips);list.add(e);
        }
        tag.put("Planets",list);return tag;
    }

    private static StarMapGalaxyData load(CompoundTag tag){
        StarMapGalaxyData data=new StarMapGalaxyData();ListTag list=tag.getList("Planets",10);
        for(int i=0;i<list.size();i++){
            CompoundTag e=list.getCompound(i);String key=e.getString("Key");if(key.isEmpty())continue;PlanetState state=new PlanetState();
            state.owner=e.hasUUID("Owner")?e.getUUID("Owner"):null;state.base=e.getBoolean("Base");state.shipFactory=e.getBoolean("ShipFactory");
            state.hangars=clamp(e.getInt("Hangars"),0,8);state.matterExtractors=clamp(e.getInt("MatterExtractors"),0,16);state.powerGenerators=clamp(e.getInt("PowerGenerators"),0,16);state.residential=clamp(e.getInt("Residential"),0,16);
            state.scoutShips=Math.max(0,e.getInt("ScoutShips"));state.colonizerShips=Math.max(0,e.getInt("ColonizerShips"));data.planets.put(key,state);
        }
        return data;
    }
    private static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}

    public static final class PlanetState {
        @Nullable private UUID owner;private boolean base;private boolean shipFactory;private int hangars,matterExtractors,powerGenerators,residential,scoutShips,colonizerShips;
        public PlanetState(){}
        @Nullable public UUID owner(){return owner;}public boolean hasOwner(){return owner!=null;}public boolean isOwner(UUID uuid){return owner!=null&&owner.equals(uuid);}public boolean hasBase(){return base;}public boolean hasShipFactory(){return shipFactory;}
        public int hangars(){return hangars;}public int matterExtractors(){return matterExtractors;}public int powerGenerators(){return powerGenerators;}public int residential(){return residential;}
        public int scoutShips(){return scoutShips;}public int colonizerShips(){return colonizerShips;}public int shipCount(){return scoutShips+colonizerShips;}
        /** Legacy Ship Hangar: +2 FLEET_SIZE. The modern colony bridge retains two baseline berths. */ public int fleetCapacity(){return 2+hangars*2;}
        /** Legacy Base contributes +2 BUILDINGS_SIZE; each Residential contributes +4. */ public int buildingCapacity(){return (base?2:0)+residential*4;}
        public int buildingCount(){return (base?1:0)+(shipFactory?1:0)+hangars+matterExtractors+powerGenerators+residential;}
        /** Legacy Matter Extractor +10 matter/-6 energy; Power Generator +8 energy/-2 matter; Residential -4 energy/-2 matter. */
        public int energyProduction(){return powerGenerators*8-matterExtractors*6-residential*4;}
        public int matterProduction(){return matterExtractors*10-powerGenerators*2-residential*2;}
        /** Legacy Residential adds 10,000 population. */ public int population(){return residential*10000;}
        /** Legacy Residential happiness: +0.5/-0.4 for power and +0.5/-0.6 for matter, based on colony totals. Returned as hundredths for menu sync. */
        public int happinessHundredths(){if(residential<=0)return 0;float per=(energyProduction()>=0?.5f:-.4f)+(matterProduction()>=0?.5f:-.6f);return Math.round(per*residential*100f);}
    }
}
