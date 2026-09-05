package matteroverdrive.blockentity;

import matteroverdrive.item.ContractItem;
import matteroverdrive.menu.StarMapMenu;
import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.starmap.StarMapCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class StarMapBlockEntity extends BlockEntity implements MenuProvider {
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

    public StarMapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STAR_MAP.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StarMapBlockEntity map) {
        if (!level.isClientSide) map.finishTravelIfDue(level.getGameTime());
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
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 11; }
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
        setChanged();
        StarMapCatalog.Planet target = StarMapCatalog.planet(quadrant, star, planet);
        player.displayClientMessage(Component.literal("Star Map journey started: " + target.name()
                        + " (" + Math.max(1, duration / 20) + "s)")
                .withStyle(ChatFormatting.AQUA), true);
        return true;
    }

    private int remainingTicks() {
        if (!traveling || level == null) return 0;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, travelEndTime - level.getGameTime()));
    }

    private void finishTravelIfDue(long gameTime) {
        if (!traveling || gameTime < travelEndTime) return;
        currentQuadrant = destinationQuadrant;
        currentStar = destinationStar;
        currentPlanet = destinationPlanet;
        traveling = false;
        travelStartTime = 0L;
        travelEndTime = 0L;
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
        if (!StarMapCatalog.validPosition(currentQuadrant, currentStar, currentPlanet)) {
            currentQuadrant = currentStar = currentPlanet = 0;
        }
        if (!StarMapCatalog.validPosition(destinationQuadrant, destinationStar, destinationPlanet)) {
            destinationQuadrant = currentQuadrant;
            destinationStar = currentStar;
            destinationPlanet = currentPlanet;
            traveling = false;
        }
    }

    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.star_map"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new StarMapMenu(id, inventory, this);
    }
}
