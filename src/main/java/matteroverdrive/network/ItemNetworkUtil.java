package matteroverdrive.network;

import matteroverdrive.blockentity.NetworkRouterBlockEntity;
import matteroverdrive.blockentity.NetworkSwitchBlockEntity;
import matteroverdrive.blockentity.PylonBlockEntity;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ItemNetworkUtil {
    private static final int MAX_NODES = 1024;
    private static final Comparator<BlockPos> POSITION_ORDER = Comparator
            .comparingInt((BlockPos pos) -> pos.getX())
            .thenComparingInt(pos -> pos.getY())
            .thenComparingInt(pos -> pos.getZ());

    private ItemNetworkUtil() {}

    public static Scan scan(Level level, BlockPos origin) {
        Map<BlockPos, Endpoint> endpointMap = new LinkedHashMap<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> routerPositions = new HashSet<>();
        pending.add(origin.immutable());
        int pylons = 0;
        while (!pending.isEmpty() && visited.size() < MAX_NODES) {
            BlockPos current = pending.removeFirst();
            if (!visited.add(current) || !level.hasChunkAt(current)) continue;
            BlockEntity currentEntity = level.getBlockEntity(current);
            if (currentEntity instanceof NetworkRouterBlockEntity) {
                routerPositions.add(current.immutable());
            }
            if (currentEntity instanceof PylonBlockEntity pylon) {
                pylons++;
                for (BlockPos linked : PylonBlockEntity.linked(level, current, pylon.getChannel())) {
                    if (!visited.contains(linked)) pending.addLast(linked);
                }
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!level.hasChunkAt(next) || visited.contains(next)) continue;
                BlockState state = level.getBlockState(next);
                BlockEntity entity = level.getBlockEntity(next);
                if (isTransport(state, entity)) {
                    pending.addLast(next.immutable());
                } else if (entity != null && entity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).isPresent()) {
                    endpointMap.putIfAbsent(next.immutable(), new Endpoint(next.immutable(), direction.getOpposite()));
                }
            }
        }
        List<Endpoint> endpoints = new ArrayList<>(endpointMap.values());
        endpoints.sort(Comparator.comparing((Endpoint endpoint) -> endpoint.pos(), POSITION_ORDER));
        List<BlockPos> routers = new ArrayList<>(routerPositions);
        routers.sort(POSITION_ORDER);
        return new Scan(endpoints, routers, visited.size(), pylons);
    }

    public static MoveResult moveOneStack(Level level, List<Endpoint> endpoints, ItemStack filter,
                                          int maximum, long cursor, Set<BlockPos> blockedSources) {
        if (maximum <= 0 || endpoints.size() < 2) return MoveResult.NONE;
        int start = (int) Math.floorMod(cursor, endpoints.size());
        for (int sourceOffset = 0; sourceOffset < endpoints.size(); sourceOffset++) {
            Endpoint source = endpoints.get((start + sourceOffset) % endpoints.size());
            if (blockedSources.contains(source.pos())) continue;
            IItemHandler sourceHandler = handler(level, source);
            if (sourceHandler == null) continue;
            for (int slot = 0; slot < sourceHandler.getSlots(); slot++) {
                ItemStack candidate = sourceHandler.extractItem(slot, maximum, true);
                if (candidate.isEmpty() || (!filter.isEmpty() && !ItemStack.isSameItemSameTags(candidate, filter))) continue;
                for (int destinationOffset = 1; destinationOffset < endpoints.size(); destinationOffset++) {
                    Endpoint destination = endpoints.get((start + sourceOffset + destinationOffset) % endpoints.size());
                    if (destination.pos().equals(source.pos())) continue;
                    IItemHandler destinationHandler = handler(level, destination);
                    if (destinationHandler == null) continue;
                    ItemStack remaining = candidate.copy();
                    for (int destinationSlot = 0; destinationSlot < destinationHandler.getSlots() && !remaining.isEmpty(); destinationSlot++) {
                        remaining = destinationHandler.insertItem(destinationSlot, remaining, true);
                    }
                    int accepted = candidate.getCount() - remaining.getCount();
                    if (accepted <= 0) continue;
                    ItemStack extracted = sourceHandler.extractItem(slot, accepted, false);
                    ItemStack toInsert = extracted;
                    for (int destinationSlot = 0; destinationSlot < destinationHandler.getSlots() && !toInsert.isEmpty(); destinationSlot++) {
                        toInsert = destinationHandler.insertItem(destinationSlot, toInsert, false);
                    }
                    if (!toInsert.isEmpty()) sourceHandler.insertItem(slot, toInsert, false);
                    int moved = extracted.getCount() - toInsert.getCount();
                    return moved <= 0 ? MoveResult.NONE : new MoveResult(moved, source.pos(), destination.pos());
                }
            }
        }
        return MoveResult.NONE;
    }

    public static boolean endpointHasItems(Level level, Endpoint endpoint) {
        IItemHandler handler = handler(level, endpoint);
        if (handler == null) return false;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) return true;
        }
        return false;
    }

    private static IItemHandler handler(Level level, Endpoint endpoint) {
        BlockEntity entity = level.getBlockEntity(endpoint.pos());
        return entity == null ? null : entity.getCapability(ForgeCapabilities.ITEM_HANDLER, endpoint.side()).orElse(null);
    }

    private static boolean isTransport(BlockState state, BlockEntity entity) {
        if (state.is(ModBlocks.get("network_pipe").get()) || state.is(ModBlocks.get("network_router").get())
                || state.is(ModBlocks.get("pylon").get())) return true;
        return state.is(ModBlocks.get("network_switch").get())
                && entity instanceof NetworkSwitchBlockEntity networkSwitch && networkSwitch.isEnabled();
    }

    public record Endpoint(BlockPos pos, Direction side) {}
    public record Scan(List<Endpoint> endpoints, List<BlockPos> routers, int nodes, int pylons) {}
    public record MoveResult(int count, BlockPos source, BlockPos destination) {
        public static final MoveResult NONE = new MoveResult(0, null, null);
    }
}
