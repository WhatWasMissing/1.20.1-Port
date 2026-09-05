package matteroverdrive.starmap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Server-global persistent ownership, buildings, production, stationed fleets and construction state for deterministic Star Map planets. */
public final class StarMapGalaxyData extends SavedData {
    public static final int MAX_BUILD_SLOTS = 4;
    private static final String DATA_NAME = "matteroverdrive_starmap_galaxy";
    private final Map<String, PlanetState> planets = new HashMap<>();

    public static StarMapGalaxyData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(StarMapGalaxyData::load, StarMapGalaxyData::new, DATA_NAME);
    }

    public PlanetState planet(int quadrant, int star, int planet) {
        PlanetState state = planets.computeIfAbsent(key(quadrant, star, planet), ignored -> new PlanetState());
        state.ensureLegacyBaseCapacities(quadrant, star, planet);
        return state;
    }

    public boolean ensureHomeworld(int quadrant, int star, int planet, UUID owner) {
        PlanetState state = planet(quadrant, star, planet);
        if (state.owner != null && !state.owner.equals(owner)) return false;
        boolean fresh = state.owner == null;
        boolean changed = fresh || !state.base || !state.shipFactory || !state.homeworld || state.baseBuildingSpaces != 8 || state.baseFleetSpaces != 10;
        state.owner = owner;
        state.base = true;
        state.homeworld = true;
        state.baseBuildingSpaces = 8;
        state.baseFleetSpaces = 10;
        state.shipFactory = true;
        if (fresh && state.scoutShips == 0) state.scoutShips = 1;
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

    public boolean addShip(int q,int s,int p,UUID owner,int shipType){PlanetState state=planet(q,s,p);if(!state.isOwner(owner)||!state.base||state.shipCount()>=state.fleetCapacity())return false;if(shipType==StarMapShipTravelData.SHIP_SCOUT)state.scoutShips++;else if(shipType==StarMapShipTravelData.SHIP_COLONIZER)state.colonizerShips++;else return false;setDirty();return true;}
    public boolean addArrivingShip(int q,int s,int p,UUID owner,int shipType){PlanetState state=planet(q,s,p);if(!state.isOwner(owner)||state.shipCount()>=state.fleetCapacity())return false;if(shipType==StarMapShipTravelData.SHIP_SCOUT)state.scoutShips++;else if(shipType==StarMapShipTravelData.SHIP_COLONIZER)state.colonizerShips++;else return false;setDirty();return true;}
    public boolean removeShip(int q,int s,int p,UUID owner,int shipType){PlanetState state=planet(q,s,p);if(!state.isOwner(owner))return false;if(shipType==StarMapShipTravelData.SHIP_SCOUT){if(state.scoutShips<=0)return false;state.scoutShips--;}else if(shipType==StarMapShipTravelData.SHIP_COLONIZER){if(state.colonizerShips<=0)return false;state.colonizerShips--;}else return false;setDirty();return true;}

    public boolean startBuild(int q,int s,int p,UUID owner,int action,long endTime){
        PlanetState state=planet(q,s,p);
        if(!state.isOwner(owner)||!state.base||state.builds.size()>=MAX_BUILD_SLOTS)return false;
        if(isBuildingAction(action) && state.buildingCount()+state.queuedBuildingCount()>=state.buildingCapacity())return false;
        state.builds.add(new BuildProject(action,Math.max(1,endTime)));setDirty();return true;
    }
    public boolean migrateLegacyBuild(int q,int s,int p,UUID owner,int action,long endTime){if(action<=0)return false;PlanetState state=planet(q,s,p);if(!state.isOwner(owner)||state.builds.size()>=MAX_BUILD_SLOTS)return false;state.builds.add(new BuildProject(action,Math.max(1,endTime)));setDirty();return true;}
    public List<BuildCompletion> collectCompletedBuilds(UUID owner,long now){List<BuildCompletion> out=new ArrayList<>();boolean changed=false;for(var entry:planets.entrySet()){PlanetState state=entry.getValue();if(!state.isOwner(owner)||state.builds.isEmpty())continue;int[] pos=parseKey(entry.getKey());state.ensureLegacyBaseCapacities(pos[0],pos[1],pos[2]);Iterator<BuildProject> it=state.builds.iterator();while(it.hasNext()){BuildProject build=it.next();if(now<build.endTime)continue;out.add(new BuildCompletion(pos[0],pos[1],pos[2],build.action));it.remove();changed=true;}}if(changed)setDirty();return out;}

    public void migrateLegacyShips(int q,int s,int p,UUID owner,int scouts,int colonizers){if(scouts<=0&&colonizers<=0)return;PlanetState state=planet(q,s,p);if(!state.isOwner(owner))return;state.scoutShips=Math.max(0,state.scoutShips+scouts);state.colonizerShips=Math.max(0,state.colonizerShips+colonizers);setDirty();}

    private static boolean isBuildingAction(int action){return action==3||action==4||action==6||action==7||action==8;}
    private static String key(int q,int s,int p){return q+":"+s+":"+p;}
    private static int[] parseKey(String key){String[] parts=key.split(":",3);return new int[]{Integer.parseInt(parts[0]),Integer.parseInt(parts[1]),Integer.parseInt(parts[2])};}

    @Override public CompoundTag save(CompoundTag tag){
        ListTag list=new ListTag();
        for(var entry:planets.entrySet()){
            PlanetState state=entry.getValue();CompoundTag e=new CompoundTag();e.putString("Key",entry.getKey());if(state.owner!=null)e.putUUID("Owner",state.owner);e.putBoolean("Base",state.base);e.putBoolean("ShipFactory",state.shipFactory);e.putBoolean("Homeworld",state.homeworld);e.putInt("BaseBuildingSpaces",state.baseBuildingSpaces);e.putInt("BaseFleetSpaces",state.baseFleetSpaces);e.putInt("Hangars",state.hangars);e.putInt("MatterExtractors",state.matterExtractors);e.putInt("PowerGenerators",state.powerGenerators);e.putInt("Residential",state.residential);e.putInt("ScoutShips",state.scoutShips);e.putInt("ColonizerShips",state.colonizerShips);ListTag builds=new ListTag();for(BuildProject build:state.builds){CompoundTag b=new CompoundTag();b.putInt("Action",build.action);b.putLong("End",build.endTime);builds.add(b);}e.put("Builds",builds);list.add(e);
        }
        tag.put("Planets",list);return tag;
    }

    private static StarMapGalaxyData load(CompoundTag tag){
        StarMapGalaxyData data=new StarMapGalaxyData();ListTag list=tag.getList("Planets",10);
        for(int i=0;i<list.size();i++){
            CompoundTag e=list.getCompound(i);String key=e.getString("Key");if(key.isEmpty())continue;PlanetState state=new PlanetState();state.owner=e.hasUUID("Owner")?e.getUUID("Owner"):null;state.base=e.getBoolean("Base");state.shipFactory=e.getBoolean("ShipFactory");state.homeworld=e.getBoolean("Homeworld");state.baseBuildingSpaces=e.contains("BaseBuildingSpaces")?Math.max(0,e.getInt("BaseBuildingSpaces")):-1;state.baseFleetSpaces=e.contains("BaseFleetSpaces")?Math.max(0,e.getInt("BaseFleetSpaces")):-1;if(!e.contains("Homeworld")&&"0:0:0".equals(key)&&state.owner!=null&&state.base&&state.shipFactory)state.homeworld=true;state.hangars=clamp(e.getInt("Hangars"),0,8);state.matterExtractors=clamp(e.getInt("MatterExtractors"),0,16);state.powerGenerators=clamp(e.getInt("PowerGenerators"),0,16);state.residential=clamp(e.getInt("Residential"),0,16);state.scoutShips=Math.max(0,e.getInt("ScoutShips"));state.colonizerShips=Math.max(0,e.getInt("ColonizerShips"));ListTag builds=e.getList("Builds",10);for(int j=0;j<builds.size()&&state.builds.size()<MAX_BUILD_SLOTS;j++){CompoundTag b=builds.getCompound(j);int action=b.getInt("Action");if(action>0)state.builds.add(new BuildProject(action,Math.max(1,b.getLong("End"))));}int[] pos=parseKey(key);state.ensureLegacyBaseCapacities(pos[0],pos[1],pos[2]);data.planets.put(key,state);
        }
        return data;
    }
    private static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}

    public static final class PlanetState {
        @Nullable private UUID owner;private boolean base,shipFactory,homeworld;private int baseBuildingSpaces=-1,baseFleetSpaces=-1;private int hangars,matterExtractors,powerGenerators,residential,scoutShips,colonizerShips;private final List<BuildProject> builds=new ArrayList<>();
        public PlanetState(){}
        private void ensureLegacyBaseCapacities(int q,int s,int p){if(homeworld){baseBuildingSpaces=8;baseFleetSpaces=10;return;}if(baseBuildingSpaces>=0&&baseFleetSpaces>=0)return;StarMapCatalog.Planet planet=StarMapCatalog.planet(q,s,p);String type=planet==null?"Terrestrial":planet.type();if("Gas Giant".equals(type)){baseBuildingSpaces=2;baseFleetSpaces=8;}else if("Dwarf".equals(type)){baseBuildingSpaces=4;baseFleetSpaces=4;}else{baseBuildingSpaces=6;baseFleetSpaces=6;}}
        @Nullable public UUID owner(){return owner;}public boolean hasOwner(){return owner!=null;}public boolean isOwner(UUID uuid){return owner!=null&&owner.equals(uuid);}public boolean hasBase(){return base;}public boolean hasShipFactory(){return shipFactory;}public boolean isHomeworld(){return homeworld;}
        public int hangars(){return hangars;}public int matterExtractors(){return matterExtractors;}public int powerGenerators(){return powerGenerators;}public int residential(){return residential;}public int scoutShips(){return scoutShips;}public int colonizerShips(){return colonizerShips;}public int shipCount(){return scoutShips+colonizerShips;}
        public int activeBuilds(){return builds.size();}public int freeBuildSlots(){return MAX_BUILD_SLOTS-builds.size();}public int firstBuildAction(){return builds.isEmpty()?0:builds.get(0).action;}public int firstBuildRemaining(long now){return builds.isEmpty()?0:(int)Math.max(0,builds.get(0).endTime-now);}public int queuedCount(int action){int n=0;for(BuildProject b:builds)if(b.action==action)n++;return n;}public int queuedBuildingCount(){int n=0;for(BuildProject b:builds)if(isBuildingAction(b.action))n++;return n;}
        public int baseBuildingSpaces(){return Math.max(0,baseBuildingSpaces);}public int baseFleetSpaces(){return Math.max(0,baseFleetSpaces);}
        public int fleetCapacity(){return Math.max(0,baseFleetSpaces)+hangars*2;}
        public int buildingCapacity(){return Math.max(0,baseBuildingSpaces)+(base?2:0)+residential*4;}
        public int buildingCount(){return (base?1:0)+(shipFactory?1:0)+hangars+matterExtractors+powerGenerators+residential;}
        public int energyProduction(){return powerGenerators*8-matterExtractors*6-residential*4;}public int matterProduction(){return matterExtractors*10-powerGenerators*2-residential*2;}public int population(){return residential*10000;}public int happinessHundredths(){if(residential<=0)return 0;float per=(energyProduction()>=0?.5f:-.4f)+(matterProduction()>=0?.5f:-.6f);return Math.round(per*residential*100f);}
    }

    private record BuildProject(int action,long endTime){}
    public record BuildCompletion(int q,int s,int p,int action){}
}
