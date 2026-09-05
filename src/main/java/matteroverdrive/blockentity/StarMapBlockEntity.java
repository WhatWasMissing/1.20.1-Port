package matteroverdrive.blockentity;

import matteroverdrive.item.ContractItem;
import matteroverdrive.menu.StarMapMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModItems;
import matteroverdrive.starmap.StarMapCatalog;
import matteroverdrive.starmap.StarMapGalaxyData;
import matteroverdrive.starmap.StarMapShipTravelData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class StarMapBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENCOUNTER_NONE = 0, ENCOUNTER_ASTEROIDS = 1, ENCOUNTER_SLINGSHOT = 2,
            ENCOUNTER_ANDROID_INTERCEPT = 3, ENCOUNTER_SIGNAL_ECHO = 4, ENCOUNTER_HOSTILE_FLEET = 5;
    public static final int ECON_NONE = 0, ECON_SCOUT = 1, ECON_COLONIZER = 2, ECON_FACTORY = 3,
            ECON_HANGAR = 4, ECON_COLONIZE = 5, ECON_EXTRACTOR = 6, ECON_GENERATOR = 7, ECON_RESIDENTIAL = 8;
    private static final int SCOUT_BUILD_TICKS = 3600, COLONIZER_BUILD_TICKS = 5000, FACTORY_BUILD_TICKS = 8000,
            HANGAR_BUILD_TICKS = 4800, EXTRACTOR_BUILD_TICKS = 14400, GENERATOR_BUILD_TICKS = 14400,
            RESIDENTIAL_BUILD_TICKS = 6000, FLEET_MAX_HULL = 100, FLEET_MAX_SHIELD = 60,
            FLEET_BASE_FIREPOWER = 20, FLEET_ATTACK_COOLDOWN = 20;

    private int currentQuadrant, currentStar, currentPlanet, destinationQuadrant, destinationStar, destinationPlanet;
    private boolean traveling;
    private long travelStartTime, travelEndTime;
    private int travelDuration, encounterCode, lastEncounterCode;
    private long encounterTriggerTime;
    private boolean encounterResolved = true;
    @Nullable private UUID fleetOwner;
    private int fleetHull = FLEET_MAX_HULL, fleetShield = FLEET_MAX_SHIELD, fleetFirepower = FLEET_BASE_FIREPOWER, fleetVictories;
    private boolean fleetCombat;
    private int enemyHull, enemyMaxHull, enemyFirepower;
    private long fleetCombatStarted, lastFleetAttack;
    private int legacyScoutShips, legacyColonizerShips;
    private boolean planetFleetMigrated;
    private int legacyBuildAction, legacyBuildQuadrant, legacyBuildStar, legacyBuildPlanet;
    private long legacyBuildEndTime;
    private boolean planetBuildMigrated;

    public StarMapBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.STAR_MAP.get(), pos, state); }

    public static void tick(Level level, BlockPos pos, BlockState state, StarMapBlockEntity map) {
        if (level.isClientSide) return;
        long now = level.getGameTime();
        map.migrateLegacyFleet();
        map.migrateLegacyBuild();
        map.resolveBuildCompletions(now);
        map.resolveShipArrivals(now);
        map.resolveEncounterIfDue(level, now);
        map.finishTravelIfDue(now);
    }

    private StarMapGalaxyData.PlanetState currentState() {
        return level instanceof ServerLevel sl ? StarMapGalaxyData.get(sl).planet(currentQuadrant, currentStar, currentPlanet) : new StarMapGalaxyData.PlanetState();
    }

    public ContainerData dataFor(Player viewer) {
        return new ContainerData() {
            @Override public int get(int i) {
                int active = 0, done = 0;
                for (ItemStack stack : viewer.getInventory().items) if (stack.getItem() instanceof ContractItem) { active++; if (ContractItem.complete(stack)) done++; }
                var ps = currentState(); long now = level == null ? 0 : level.getGameTime();
                int scoutTransit = transitCount(StarMapShipTravelData.SHIP_SCOUT);
                int colonizerTransit = transitCount(StarMapShipTravelData.SHIP_COLONIZER);
                return switch (i) {
                    case 0 -> active; case 1 -> done; case 2 -> currentQuadrant; case 3 -> currentStar; case 4 -> currentPlanet;
                    case 5 -> destinationQuadrant; case 6 -> destinationStar; case 7 -> destinationPlanet; case 8 -> remainingTicks();
                    case 9 -> travelDuration; case 10 -> traveling ? 1 : 0; case 11 -> encounterResolved ? lastEncounterCode : encounterCode;
                    case 12 -> encounterResolved ? 0 : encounterRemainingTicks(); case 13 -> encounterResolved ? 1 : 0;
                    case 14 -> fleetHull; case 15 -> fleetShield; case 16 -> fleetFirepower; case 17 -> fleetCombat ? 1 : 0;
                    case 18 -> enemyHull; case 19 -> enemyMaxHull; case 20 -> fleetVictories; case 21 -> fleetAttackCooldown();
                    case 22 -> ps.scoutShips(); case 23 -> ps.colonizerShips(); case 24 -> ps.firstBuildAction(); case 25 -> ps.firstBuildRemaining(now);
                    case 26 -> !ps.hasOwner() ? 0 : ps.isOwner(viewer.getUUID()) ? 1 : 2; case 27 -> ps.hasBase() ? 1 : 0;
                    case 28 -> ps.hasShipFactory() ? 1 : 0; case 29 -> ps.hangars(); case 30 -> ps.fleetCapacity();
                    case 31 -> ps.matterExtractors(); case 32 -> ps.powerGenerators(); case 33 -> ps.residential();
                    case 34 -> ps.energyProduction(); case 35 -> ps.matterProduction(); case 36 -> ps.population();
                    case 37 -> ps.happinessHundredths(); case 38 -> ps.buildingCapacity(); case 39 -> ps.buildingCount();
                    case 40 -> scoutTransit; case 41 -> colonizerTransit; case 42 -> ps.activeBuilds(); default -> 0;
                };
            }
            @Override public void set(int i, int v) {}
            @Override public int getCount() { return 43; }
        };
    }

    private int transitCount(int type) {
        if (!(level instanceof ServerLevel sl) || fleetOwner == null) return 0;
        return StarMapShipTravelData.get(sl).countInTransit(fleetOwner, type);
    }

    private boolean bind(ServerPlayer player) {
        if (!(level instanceof ServerLevel sl) || fleetOwner != null && !fleetOwner.equals(player.getUUID())) return false;
        if (fleetOwner == null) {
            fleetOwner = player.getUUID();
            StarMapGalaxyData.get(sl).ensureHomeworld(currentQuadrant, currentStar, currentPlanet, fleetOwner);
            setChanged();
        }
        migrateLegacyFleet(); migrateLegacyBuild();
        return true;
    }

    private void migrateLegacyFleet() {
        if (planetFleetMigrated || fleetOwner == null || !(level instanceof ServerLevel sl)) return;
        StarMapGalaxyData.get(sl).migrateLegacyShips(currentQuadrant, currentStar, currentPlanet, fleetOwner, legacyScoutShips, legacyColonizerShips);
        legacyScoutShips = 0; legacyColonizerShips = 0; planetFleetMigrated = true; setChanged();
    }

    private void migrateLegacyBuild() {
        if (planetBuildMigrated || fleetOwner == null || !(level instanceof ServerLevel sl)) return;
        if (legacyBuildAction != ECON_NONE) StarMapGalaxyData.get(sl).migrateLegacyBuild(legacyBuildQuadrant, legacyBuildStar, legacyBuildPlanet, fleetOwner, legacyBuildAction, legacyBuildEndTime);
        legacyBuildAction = ECON_NONE; legacyBuildEndTime = 0; planetBuildMigrated = true; setChanged();
    }

    public boolean requestShipDispatch(ServerPlayer player, int shipType, int q, int s, int p) {
        if (!(level instanceof ServerLevel sl) || !StarMapCatalog.validPosition(q, s, p)) return false;
        if (!bind(player)) { msg(player, "Star Map fleet is bound to another commander.", ChatFormatting.RED); return false; }
        if (traveling || fleetCombat) { msg(player, "Independent ships can only depart while the console fleet is stationary.", ChatFormatting.YELLOW); return false; }
        if (q == currentQuadrant && s == currentStar && p == currentPlanet) { msg(player, "Dispatch destination must be another planet.", ChatFormatting.YELLOW); return false; }
        StarMapGalaxyData data = StarMapGalaxyData.get(sl);
        StarMapGalaxyData.PlanetState source = data.planet(currentQuadrant, currentStar, currentPlanet);
        if (!source.isOwner(player.getUUID())) { msg(player, "Ships can only depart from one of your colonies.", ChatFormatting.RED); return false; }
        int available = shipType == StarMapShipTravelData.SHIP_SCOUT ? source.scoutShips() : shipType == StarMapShipTravelData.SHIP_COLONIZER ? source.colonizerShips() : 0;
        if (available <= 0) { msg(player, shipType == StarMapShipTravelData.SHIP_SCOUT ? "No Scout is stationed at this colony." : "No Colonizer is stationed at this colony.", ChatFormatting.RED); return false; }
        if (!StarMapShipTravelData.get(sl).dispatch(player.getUUID(), worldPosition.asLong(), shipType, currentQuadrant, currentStar, currentPlanet, q, s, p, level.getGameTime())) return false;
        if (!data.removeShip(currentQuadrant, currentStar, currentPlanet, player.getUUID(), shipType)) return false;
        consumePhysicalShipIfPresent(player, shipType == StarMapShipTravelData.SHIP_SCOUT ? "scout_ship" : "colonizer_ship");
        int ticks = StarMapCatalog.travelTicks(currentQuadrant, currentStar, currentPlanet, q, s, p);
        msg(player, (shipType == StarMapShipTravelData.SHIP_SCOUT ? "Scout" : "Colonizer") + " departed " + StarMapCatalog.planet(currentQuadrant,currentStar,currentPlanet).name() + " for " + StarMapCatalog.planet(q,s,p).name() + " (" + Math.max(1, (ticks + 19) / 20) + "s).", ChatFormatting.AQUA);
        return true;
    }

    private void resolveShipArrivals(long now) {
        if (!(level instanceof ServerLevel sl) || fleetOwner == null) return;
        StarMapGalaxyData data = StarMapGalaxyData.get(sl);
        for (var arrival : StarMapShipTravelData.get(sl).collectArrivals(fleetOwner, now)) {
            if (arrival.shipType() == StarMapShipTravelData.SHIP_SCOUT) {
                if (!data.addArrivingShip(arrival.q(), arrival.s(), arrival.p(), fleetOwner, StarMapShipTravelData.SHIP_SCOUT)) {
                    data.addArrivingShip(arrival.fromQ(), arrival.fromS(), arrival.fromP(), fleetOwner, StarMapShipTravelData.SHIP_SCOUT);
                    announce(level, "Scout could not berth at " + StarMapCatalog.planet(arrival.q(), arrival.s(), arrival.p()).name() + "; it returned to its origin colony.", ChatFormatting.YELLOW);
                } else announce(level, "Scout arrived and is now stationed at " + StarMapCatalog.planet(arrival.q(), arrival.s(), arrival.p()).name() + ".", ChatFormatting.GREEN);
            } else {
                boolean colonized = data.colonize(arrival.q(), arrival.s(), arrival.p(), fleetOwner);
                if (colonized) announce(level, "Colonizer arrived at " + StarMapCatalog.planet(arrival.q(), arrival.s(), arrival.p()).name() + ": Base established and ownership secured.", ChatFormatting.GREEN);
                else if (data.addArrivingShip(arrival.q(), arrival.s(), arrival.p(), fleetOwner, StarMapShipTravelData.SHIP_COLONIZER)) announce(level, "Colonizer arrived at an existing friendly colony and is now stationed there.", ChatFormatting.YELLOW);
                else { data.addArrivingShip(arrival.fromQ(), arrival.fromS(), arrival.fromP(), fleetOwner, StarMapShipTravelData.SHIP_COLONIZER); announce(level, "Colonizer could not claim or berth at the destination; it returned to its origin colony.", ChatFormatting.YELLOW); }
            }
        }
    }

    public boolean requestTravel(ServerPlayer player, int q, int s, int p) {
        if (level == null || level.isClientSide || !StarMapCatalog.validPosition(q, s, p)) return false;
        finishTravelIfDue(level.getGameTime());
        if (!bind(player)) return false;
        if (fleetCombat || traveling || q == currentQuadrant && s == currentStar && p == currentPlanet) return false;
        int duration = StarMapCatalog.travelTicks(currentQuadrant, currentStar, currentPlanet, q, s, p);
        if (duration <= 0) return false;
        destinationQuadrant = q; destinationStar = s; destinationPlanet = p; traveling = true; travelDuration = duration;
        travelStartTime = level.getGameTime(); travelEndTime = travelStartTime + duration; fleetShield = FLEET_MAX_SHIELD;
        scheduleEncounter(duration); setChanged();
        msg(player, "Star Map journey started: " + StarMapCatalog.planet(q, s, p).name() + " (" + Math.max(1, duration / 20) + "s)", ChatFormatting.AQUA);
        return true;
    }

    public boolean requestEconomyAction(ServerPlayer player, int action) {
        if (!(level instanceof ServerLevel sl) || action < ECON_SCOUT || action > ECON_RESIDENTIAL) return false;
        if (!bind(player) || traveling || fleetCombat) return false;
        StarMapGalaxyData data = StarMapGalaxyData.get(sl); var ps = data.planet(currentQuadrant, currentStar, currentPlanet);
        if (action == ECON_COLONIZE) {
            if (ps.hasOwner() || ps.colonizerShips() <= 0) return false;
            if (!data.colonize(currentQuadrant, currentStar, currentPlanet, player.getUUID())) return false;
            if (!data.removeShip(currentQuadrant, currentStar, currentPlanet, player.getUUID(), StarMapShipTravelData.SHIP_COLONIZER)) return false;
            consumePhysicalShipIfPresent(player, "colonizer_ship"); return true;
        }
        if (!ps.isOwner(player.getUUID()) || !ps.hasBase() || ps.freeBuildSlots() <= 0) return false;
        int duration;
        switch (action) {
            case ECON_SCOUT -> { if (!ps.hasShipFactory() || ps.shipCount()+ps.queuedCount(ECON_SCOUT)+ps.queuedCount(ECON_COLONIZER) >= ps.fleetCapacity()) return false; duration = SCOUT_BUILD_TICKS; }
            case ECON_COLONIZER -> { if (!ps.hasShipFactory() || ps.shipCount()+ps.queuedCount(ECON_SCOUT)+ps.queuedCount(ECON_COLONIZER) >= ps.fleetCapacity()) return false; duration = COLONIZER_BUILD_TICKS; }
            case ECON_FACTORY -> { if (ps.hasShipFactory() || ps.queuedCount(ECON_FACTORY)>0) return false; duration = FACTORY_BUILD_TICKS; }
            case ECON_HANGAR -> { if (ps.hangars()+ps.queuedCount(ECON_HANGAR) >= 8) return false; duration = HANGAR_BUILD_TICKS; }
            case ECON_EXTRACTOR -> { if (ps.matterExtractors()+ps.queuedCount(ECON_EXTRACTOR) >= 16) return false; duration = EXTRACTOR_BUILD_TICKS; }
            case ECON_GENERATOR -> { if (ps.powerGenerators()+ps.queuedCount(ECON_GENERATOR) >= 16) return false; duration = GENERATOR_BUILD_TICKS; }
            case ECON_RESIDENTIAL -> { if (ps.residential()+ps.queuedCount(ECON_RESIDENTIAL) >= 16) return false; duration = RESIDENTIAL_BUILD_TICKS; }
            default -> { return false; }
        }
        boolean started=data.startBuild(currentQuadrant,currentStar,currentPlanet,player.getUUID(),action,level.getGameTime()+duration);
        if(started)msg(player,"Planet construction queued: "+economyName(action)+" (slot "+ps.activeBuilds()+"/"+StarMapGalaxyData.MAX_BUILD_SLOTS+")",ChatFormatting.AQUA);
        return started;
    }

    private void resolveBuildCompletions(long now) {
        if (!(level instanceof ServerLevel sl) || fleetOwner == null) return;
        StarMapGalaxyData data=StarMapGalaxyData.get(sl);
        for(var completion:data.collectCompletedBuilds(fleetOwner,now)){
            boolean ok=switch(completion.action()){
                case ECON_SCOUT -> data.addShip(completion.q(),completion.s(),completion.p(),fleetOwner,StarMapShipTravelData.SHIP_SCOUT);
                case ECON_COLONIZER -> data.addShip(completion.q(),completion.s(),completion.p(),fleetOwner,StarMapShipTravelData.SHIP_COLONIZER);
                case ECON_FACTORY -> data.addShipFactory(completion.q(),completion.s(),completion.p(),fleetOwner);
                case ECON_HANGAR -> data.addHangar(completion.q(),completion.s(),completion.p(),fleetOwner);
                case ECON_EXTRACTOR -> data.addMatterExtractor(completion.q(),completion.s(),completion.p(),fleetOwner);
                case ECON_GENERATOR -> data.addPowerGenerator(completion.q(),completion.s(),completion.p(),fleetOwner);
                case ECON_RESIDENTIAL -> data.addResidential(completion.q(),completion.s(),completion.p(),fleetOwner);
                default -> false;
            };
            if(ok){
                if((completion.action()==ECON_SCOUT||completion.action()==ECON_COLONIZER)&&completion.q()==currentQuadrant&&completion.s()==currentStar&&completion.p()==currentPlanet)spawnShipItem(sl,completion.action()==ECON_SCOUT?"scout_ship":"colonizer_ship",completion.q(),completion.s(),completion.p());
                announce(level,"Star Map construction complete at "+StarMapCatalog.planet(completion.q(),completion.s(),completion.p()).name()+": "+economyName(completion.action())+".",ChatFormatting.GREEN);
            }
        }
    }

    private void spawnShipItem(ServerLevel sl, String id, int q, int s, int p) {
        ItemStack stack = new ItemStack(ModItems.get(id).get()); CompoundTag tag = stack.getOrCreateTag();
        if (fleetOwner != null) tag.putUUID("Owner", fleetOwner); tag.putString("ShipType", id.equals("scout_ship") ? "SCOUT" : "COLONIZER");
        tag.putInt("Quadrant", q); tag.putInt("Star", s); tag.putInt("Planet", p);
        ItemEntity entity = new ItemEntity(sl, worldPosition.getX() + .5, worldPosition.getY() + 1.2, worldPosition.getZ() + .5, stack); entity.setDefaultPickUpDelay(); sl.addFreshEntity(entity);
    }

    private void consumePhysicalShipIfPresent(ServerPlayer player, String id) { for (int i = 0; i < player.getInventory().getContainerSize(); i++) { ItemStack stack = player.getInventory().getItem(i); if (stack.is(ModItems.get(id).get())) { stack.shrink(1); return; } } }
    private static String economyName(int action) { return switch (action) { case ECON_SCOUT -> "Scout Ship"; case ECON_COLONIZER -> "Colonizer Ship"; case ECON_FACTORY -> "Ship Factory"; case ECON_HANGAR -> "Ship Hangar"; case ECON_COLONIZE -> "Colonize"; case ECON_EXTRACTOR -> "Matter Extractor"; case ECON_GENERATOR -> "Power Generator"; case ECON_RESIDENTIAL -> "Residential"; default -> "Idle"; }; }

    public boolean requestFleetAttack(ServerPlayer player) {
        if (level == null || !fleetCombat || !traveling || fleetOwner == null || !fleetOwner.equals(player.getUUID())) return false;
        long now = level.getGameTime(); if (now - lastFleetAttack < FLEET_ATTACK_COOLDOWN) return false; lastFleetAttack = now;
        enemyHull = Math.max(0, enemyHull - fleetFirepower);
        if (enemyHull <= 0) { fleetVictories++; fleetCombat = false; travelEndTime += Math.max(0, now - fleetCombatStarted); fleetCombatStarted = 0; setChanged(); return true; }
        int damage = enemyFirepower, shield = Math.min(fleetShield, damage); fleetShield -= shield; damage -= shield;
        if (damage > 0) fleetHull = Math.max(0, fleetHull - damage);
        if (fleetHull <= 0) { fleetCombat = false; traveling = false; destinationQuadrant = currentQuadrant; destinationStar = currentStar; destinationPlanet = currentPlanet; fleetHull = 35; fleetShield = 0; }
        setChanged(); return true;
    }

    private void scheduleEncounter(int duration) { encounterCode = ENCOUNTER_NONE; encounterResolved = true; lastEncounterCode = ENCOUNTER_NONE; if (duration < 80) return; encounterCode = Math.floorMod(currentQuadrant*97 + currentStar*53 + currentPlanet*31 + destinationQuadrant*211 + destinationStar*127 + destinationPlanet*71 + duration, 5) + 1; encounterTriggerTime = travelStartTime + Math.max(30, duration / 2L); encounterResolved = false; }
    private void resolveEncounterIfDue(Level level, long now) { if (!traveling || encounterResolved || encounterCode == 0 || now < encounterTriggerTime) return; switch (encounterCode) { case ENCOUNTER_ASTEROIDS -> travelEndTime += 100; case ENCOUNTER_SLINGSHOT -> travelEndTime = Math.max(now + 20, travelEndTime - Math.min(80, Math.max(20, travelDuration / 6L))); case ENCOUNTER_ANDROID_INTERCEPT -> { travelEndTime += 120; spawnBoardingParty(level); } case ENCOUNTER_SIGNAL_ECHO -> travelEndTime += 40; case ENCOUNTER_HOSTILE_FLEET -> beginFleetCombat(level, now); } lastEncounterCode = encounterCode; encounterResolved = true; encounterTriggerTime = 0; travelDuration = (int)Math.max(1, travelEndTime - travelStartTime); setChanged(); }
    private void beginFleetCombat(Level level, long now) { int threat = Math.floorMod(destinationQuadrant*7 + destinationStar*11 + destinationPlanet*13, 4); enemyMaxHull = 60 + threat*20; enemyHull = enemyMaxHull; enemyFirepower = 8 + threat*3; fleetCombat = true; fleetCombatStarted = now; lastFleetAttack = now - FLEET_ATTACK_COOLDOWN; announce(level, "Star Map encounter: hostile fleet contact.", ChatFormatting.RED); }
    private void spawnBoardingParty(Level level) { if (!(level instanceof ServerLevel sl)) return; for (int i=0;i<3;i++) { Mob android = (i==2 ? ModEntities.RANGED_ROGUE_ANDROID.get() : ModEntities.ROGUE_ANDROID.get()).create(sl); if (android != null) { android.moveTo(worldPosition.getX()+.5+(i-1)*2, worldPosition.getY()+1, worldPosition.getZ()+2.5, 0, 0); sl.addFreshEntity(android); } } }
    private void announce(Level level, String text, ChatFormatting color) { if (level instanceof ServerLevel sl) for (ServerPlayer p : sl.players()) if (p.distanceToSqr(worldPosition.getX()+.5, worldPosition.getY()+.5, worldPosition.getZ()+.5) <= 1024) p.displayClientMessage(Component.literal(text).withStyle(color), false); }
    private static void msg(ServerPlayer p, String text, ChatFormatting color) { p.displayClientMessage(Component.literal(text).withStyle(color), true); }
    private int remainingTicks() { return !traveling || level == null ? 0 : (int)Math.max(0, travelEndTime - level.getGameTime()); }
    private int encounterRemainingTicks() { return encounterResolved || level == null ? 0 : (int)Math.max(0, encounterTriggerTime - level.getGameTime()); }
    private int fleetAttackCooldown() { return !fleetCombat || level == null ? 0 : (int)Math.max(0, FLEET_ATTACK_COOLDOWN - (level.getGameTime() - lastFleetAttack)); }
    private void finishTravelIfDue(long now) { if (!traveling || fleetCombat || now < travelEndTime) return; currentQuadrant = destinationQuadrant; currentStar = destinationStar; currentPlanet = destinationPlanet; traveling = false; fleetHull = Math.min(FLEET_MAX_HULL, fleetHull + 10); fleetShield = FLEET_MAX_SHIELD; setChanged(); }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("CurrentQuadrant",currentQuadrant);tag.putInt("CurrentStar",currentStar);tag.putInt("CurrentPlanet",currentPlanet);tag.putInt("DestinationQuadrant",destinationQuadrant);tag.putInt("DestinationStar",destinationStar);tag.putInt("DestinationPlanet",destinationPlanet);
        tag.putBoolean("Traveling",traveling);tag.putLong("TravelStart",travelStartTime);tag.putLong("TravelEnd",travelEndTime);tag.putInt("TravelDuration",travelDuration);tag.putInt("EncounterCode",encounterCode);tag.putInt("LastEncounterCode",lastEncounterCode);tag.putLong("EncounterTrigger",encounterTriggerTime);tag.putBoolean("EncounterResolved",encounterResolved);
        if(fleetOwner!=null)tag.putUUID("FleetOwner",fleetOwner);tag.putInt("FleetHull",fleetHull);tag.putInt("FleetShield",fleetShield);tag.putInt("FleetFirepower",fleetFirepower);tag.putInt("FleetVictories",fleetVictories);tag.putBoolean("FleetCombat",fleetCombat);tag.putInt("EnemyHull",enemyHull);tag.putInt("EnemyMaxHull",enemyMaxHull);tag.putInt("EnemyFirepower",enemyFirepower);tag.putLong("FleetCombatStarted",fleetCombatStarted);tag.putLong("LastFleetAttack",lastFleetAttack);
        tag.putBoolean("PlanetFleetMigrated",planetFleetMigrated);tag.putBoolean("PlanetBuildMigrated",planetBuildMigrated);tag.putInt("BuildAction",legacyBuildAction);tag.putLong("BuildEnd",legacyBuildEndTime);tag.putInt("BuildQuadrant",legacyBuildQuadrant);tag.putInt("BuildStar",legacyBuildStar);tag.putInt("BuildPlanet",legacyBuildPlanet);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        currentQuadrant=tag.getInt("CurrentQuadrant");currentStar=tag.getInt("CurrentStar");currentPlanet=tag.getInt("CurrentPlanet");destinationQuadrant=tag.getInt("DestinationQuadrant");destinationStar=tag.getInt("DestinationStar");destinationPlanet=tag.getInt("DestinationPlanet");traveling=tag.getBoolean("Traveling");travelStartTime=tag.getLong("TravelStart");travelEndTime=tag.getLong("TravelEnd");travelDuration=tag.getInt("TravelDuration");encounterCode=tag.getInt("EncounterCode");lastEncounterCode=tag.getInt("LastEncounterCode");encounterTriggerTime=tag.getLong("EncounterTrigger");encounterResolved=!tag.contains("EncounterResolved")||tag.getBoolean("EncounterResolved");fleetOwner=tag.hasUUID("FleetOwner")?tag.getUUID("FleetOwner"):null;fleetHull=tag.contains("FleetHull")?tag.getInt("FleetHull"):FLEET_MAX_HULL;fleetShield=tag.contains("FleetShield")?tag.getInt("FleetShield"):FLEET_MAX_SHIELD;fleetFirepower=tag.contains("FleetFirepower")?tag.getInt("FleetFirepower"):FLEET_BASE_FIREPOWER;fleetVictories=tag.getInt("FleetVictories");fleetCombat=tag.getBoolean("FleetCombat");enemyHull=tag.getInt("EnemyHull");enemyMaxHull=tag.getInt("EnemyMaxHull");enemyFirepower=tag.getInt("EnemyFirepower");fleetCombatStarted=tag.getLong("FleetCombatStarted");lastFleetAttack=tag.getLong("LastFleetAttack");
        planetFleetMigrated=tag.getBoolean("PlanetFleetMigrated");legacyScoutShips=planetFleetMigrated?0:Math.max(0,tag.getInt("ScoutShips"));legacyColonizerShips=planetFleetMigrated?0:Math.max(0,tag.getInt("ColonizerShips"));planetBuildMigrated=tag.getBoolean("PlanetBuildMigrated");legacyBuildAction=planetBuildMigrated?ECON_NONE:tag.getInt("BuildAction");legacyBuildEndTime=tag.getLong("BuildEnd");legacyBuildQuadrant=tag.getInt("BuildQuadrant");legacyBuildStar=tag.getInt("BuildStar");legacyBuildPlanet=tag.getInt("BuildPlanet");if(legacyBuildAction<ECON_NONE||legacyBuildAction>ECON_RESIDENTIAL)legacyBuildAction=ECON_NONE;
    }

    @Override public Component getDisplayName(){return Component.translatable("block.matteroverdrive.star_map");}
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player){return new StarMapMenu(id, inventory, this);}
}
