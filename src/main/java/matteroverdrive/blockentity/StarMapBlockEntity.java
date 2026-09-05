package matteroverdrive.blockentity;

import matteroverdrive.item.ContractItem;
import matteroverdrive.menu.StarMapMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.starmap.StarMapCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class StarMapBlockEntity extends BlockEntity implements MenuProvider {
    public static final int ENCOUNTER_NONE = 0;
    public static final int ENCOUNTER_ASTEROIDS = 1;
    public static final int ENCOUNTER_SLINGSHOT = 2;
    public static final int ENCOUNTER_ANDROID_INTERCEPT = 3;
    public static final int ENCOUNTER_SIGNAL_ECHO = 4;

    private int currentQuadrant;
    private int currentStar;
    private int currentPlanet;
    private int destinationQuadrant;
    private int destinationStar;
    private int destinationPlanet;
    private boolean traveling;
    private long travelStartTime;
    private long travelEndTime;
    private int travelDuration;
    private int encounterCode;
    private int lastEncounterCode;
    private long encounterTriggerTime;
    private boolean encounterResolved = true;

    public StarMapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STAR_MAP.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StarMapBlockEntity map) {
        if (level.isClientSide) return;
        long gameTime = level.getGameTime();
        map.resolveEncounterIfDue(level, gameTime);
        map.finishTravelIfDue(gameTime);
    }

    public ContainerData dataFor(Player viewer) {
        return new ContainerData() {
            @Override public int get(int index) {
                int active = 0;
                int done = 0;
                for (var stack : viewer.getInventory().items) {
                    if (stack.getItem() instanceof ContractItem) {
                        active++;
                        if (ContractItem.complete(stack)) done++;
                    }
                }
                return switch (index) {
                    case 0 -> active;
                    case 1 -> done;
                    case 2 -> currentQuadrant;
                    case 3 -> currentStar;
                    case 4 -> currentPlanet;
                    case 5 -> destinationQuadrant;
                    case 6 -> destinationStar;
                    case 7 -> destinationPlanet;
                    case 8 -> remainingTicks();
                    case 9 -> travelDuration;
                    case 10 -> traveling ? 1 : 0;
                    case 11 -> encounterResolved ? lastEncounterCode : encounterCode;
                    case 12 -> encounterResolved ? 0 : encounterRemainingTicks();
                    case 13 -> encounterResolved ? 1 : 0;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 14; }
        };
    }

    public boolean requestTravel(ServerPlayer player, int quadrant, int star, int planet) {
        if (level == null || level.isClientSide || !StarMapCatalog.validPosition(quadrant, star, planet)) return false;
        finishTravelIfDue(level.getGameTime());
        if (traveling) {
            player.displayClientMessage(Component.literal("Star Map: a journey is already in progress.")
                    .withStyle(ChatFormatting.YELLOW), true);
            return false;
        }
        if (quadrant == currentQuadrant && star == currentStar && planet == currentPlanet) {
            player.displayClientMessage(Component.literal("Star Map: already at that destination.")
                    .withStyle(ChatFormatting.YELLOW), true);
            return false;
        }
        int duration = StarMapCatalog.travelTicks(currentQuadrant, currentStar, currentPlanet,
                quadrant, star, planet);
        if (duration <= 0) return false;
        destinationQuadrant = quadrant;
        destinationStar = star;
        destinationPlanet = planet;
        traveling = true;
        travelDuration = duration;
        travelStartTime = level.getGameTime();
        travelEndTime = travelStartTime + duration;
        scheduleEncounter(duration);
        setChanged();
        StarMapCatalog.Planet target = StarMapCatalog.planet(quadrant, star, planet);
        player.displayClientMessage(Component.literal("Star Map journey started: " + target.name()
                        + " (" + Math.max(1, duration / 20) + "s)")
                .withStyle(ChatFormatting.AQUA), true);
        return true;
    }

    private void scheduleEncounter(int duration) {
        encounterCode = ENCOUNTER_NONE;
        encounterTriggerTime = 0L;
        encounterResolved = true;
        lastEncounterCode = ENCOUNTER_NONE;
        if (duration < 80) return;
        int routeHash = Math.floorMod(currentQuadrant * 97 + currentStar * 53 + currentPlanet * 31
                + destinationQuadrant * 211 + destinationStar * 127 + destinationPlanet * 71 + duration, 4);
        encounterCode = routeHash + 1;
        encounterTriggerTime = travelStartTime + Math.max(30L, duration / 2L);
        encounterResolved = false;
    }

    private void resolveEncounterIfDue(Level level, long gameTime) {
        if (!traveling || encounterResolved || encounterCode == ENCOUNTER_NONE || gameTime < encounterTriggerTime) return;
        switch (encounterCode) {
            case ENCOUNTER_ASTEROIDS -> {
                travelEndTime += 100L;
                announce(level, "Star Map encounter: asteroid field. Route slowed by 5 seconds.", ChatFormatting.YELLOW);
            }
            case ENCOUNTER_SLINGSHOT -> {
                long reduction = Math.min(80L, Math.max(20L, travelDuration / 6L));
                travelEndTime = Math.max(gameTime + 20L, travelEndTime - reduction);
                announce(level, "Star Map encounter: gravitational slingshot. ETA reduced.", ChatFormatting.GREEN);
            }
            case ENCOUNTER_ANDROID_INTERCEPT -> {
                travelEndTime += 120L;
                spawnBoardingParty(level);
                announce(level, "Star Map encounter: Rogue Android intercept. Boarding party detected!", ChatFormatting.RED);
            }
            case ENCOUNTER_SIGNAL_ECHO -> {
                travelEndTime += 40L;
                announce(level, "Star Map encounter: anomalous signal echo. Navigation recalibration required.", ChatFormatting.AQUA);
            }
            default -> { }
        }
        lastEncounterCode = encounterCode;
        encounterResolved = true;
        encounterTriggerTime = 0L;
        travelDuration = (int) Math.max(1L, Math.min(Integer.MAX_VALUE, travelEndTime - travelStartTime));
        setChanged();
    }

    private void spawnBoardingParty(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        for (int i = 0; i < 3; i++) {
            Mob android = (i == 2 ? ModEntities.RANGED_ROGUE_ANDROID.get() : ModEntities.ROGUE_ANDROID.get()).create(serverLevel);
            if (android == null) continue;
            double ox = i == 0 ? 2.5D : i == 1 ? -2.5D : 0.5D;
            double oz = i == 2 ? 2.5D : -1.5D;
            android.moveTo(worldPosition.getX() + 0.5D + ox, worldPosition.getY() + 1.0D,
                    worldPosition.getZ() + 0.5D + oz, serverLevel.random.nextFloat() * 360F, 0F);
            serverLevel.addFreshEntity(android);
        }
    }

    private void announce(Level level, String message, ChatFormatting color) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        double cx = worldPosition.getX() + 0.5D;
        double cy = worldPosition.getY() + 0.5D;
        double cz = worldPosition.getZ() + 0.5D;
        Component text = Component.literal(message).withStyle(color);
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(cx, cy, cz) <= 1024D) player.displayClientMessage(text, false);
        }
    }

    private int remainingTicks() {
        if (!traveling || level == null) return 0;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, travelEndTime - level.getGameTime()));
    }

    private int encounterRemainingTicks() {
        if (encounterResolved || level == null) return 0;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, encounterTriggerTime - level.getGameTime()));
    }

    private void finishTravelIfDue(long gameTime) {
        if (!traveling || gameTime < travelEndTime) return;
        currentQuadrant = destinationQuadrant;
        currentStar = destinationStar;
        currentPlanet = destinationPlanet;
        traveling = false;
        travelStartTime = 0L;
        travelEndTime = 0L;
        encounterTriggerTime = 0L;
        encounterResolved = true;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("CurrentQuadrant", currentQuadrant);
        tag.putInt("CurrentStar", currentStar);
        tag.putInt("CurrentPlanet", currentPlanet);
        tag.putInt("DestinationQuadrant", destinationQuadrant);
        tag.putInt("DestinationStar", destinationStar);
        tag.putInt("DestinationPlanet", destinationPlanet);
        tag.putBoolean("Traveling", traveling);
        tag.putLong("TravelStart", travelStartTime);
        tag.putLong("TravelEnd", travelEndTime);
        tag.putInt("TravelDuration", travelDuration);
        tag.putInt("EncounterCode", encounterCode);
        tag.putInt("LastEncounterCode", lastEncounterCode);
        tag.putLong("EncounterTrigger", encounterTriggerTime);
        tag.putBoolean("EncounterResolved", encounterResolved);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        currentQuadrant = tag.getInt("CurrentQuadrant");
        currentStar = tag.getInt("CurrentStar");
        currentPlanet = tag.getInt("CurrentPlanet");
        destinationQuadrant = tag.getInt("DestinationQuadrant");
        destinationStar = tag.getInt("DestinationStar");
        destinationPlanet = tag.getInt("DestinationPlanet");
        traveling = tag.getBoolean("Traveling");
        travelStartTime = tag.getLong("TravelStart");
        travelEndTime = tag.getLong("TravelEnd");
        travelDuration = tag.getInt("TravelDuration");
        encounterCode = tag.getInt("EncounterCode");
        lastEncounterCode = tag.getInt("LastEncounterCode");
        encounterTriggerTime = tag.getLong("EncounterTrigger");
        encounterResolved = !tag.contains("EncounterResolved") || tag.getBoolean("EncounterResolved");
        if (!StarMapCatalog.validPosition(currentQuadrant, currentStar, currentPlanet)) {
            currentQuadrant = currentStar = currentPlanet = 0;
        }
        if (!StarMapCatalog.validPosition(destinationQuadrant, destinationStar, destinationPlanet)) {
            destinationQuadrant = currentQuadrant;
            destinationStar = currentStar;
            destinationPlanet = currentPlanet;
            traveling = false;
        }
        if (encounterCode < ENCOUNTER_NONE || encounterCode > ENCOUNTER_SIGNAL_ECHO) {
            encounterCode = ENCOUNTER_NONE;
            encounterResolved = true;
            encounterTriggerTime = 0L;
        }
    }

    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.star_map"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new StarMapMenu(id, inventory, this);
    }
}
