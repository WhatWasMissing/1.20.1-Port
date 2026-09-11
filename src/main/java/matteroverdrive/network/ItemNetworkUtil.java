package matteroverdrive.network;

import matteroverdrive.blockentity.NetworkRouterBlockEntity;
import matteroverdrive.blockentity.NetworkSwitchBlockEntity;
import matteroverdrive.blockentity.PylonBlockEntity;
import matteroverdrive.item.NetworkFlashDriveItem;
import matteroverdrive.machine.MachineSideConfigurationData;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
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
    public static final int MAX_NODES = 1024;
    private static final Comparator<BlockPos> POSITION_ORDER = Comparator.comparingInt((BlockPos pos) -> pos.getX()).thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getZ);
    private ItemNetworkUtil() {}

    public static Scan scan(Level level, BlockPos origin) {
        int channel = 0;
        BlockEntity originEntity = level.getBlockEntity(origin);
        if (originEntity instanceof NetworkRouterBlockEntity router) channel = router.getChannel();
        else if (originEntity instanceof NetworkSwitchBlockEntity networkSwitch) channel = networkSwitch.getChannel();
        return scan(level, origin, channel);
    }

    public static Scan scan(Level level, BlockPos origin, int channel) {
        channel &= 15;
        Map<BlockPos, Endpoint> endpointMap = new LinkedHashMap<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> routerPositions = new HashSet<>();
        Set<Long> pylonEdges = new HashSet<>();
        pending.add(origin.immutable());
        int pylons = 0, disabledSwitches = 0;
        while (!pending.isEmpty() && visited.size() < MAX_NODES) {
            BlockPos current = pending.removeFirst();
            if (!visited.add(current) || !level.hasChunkAt(current)) continue;
            BlockEntity currentEntity = level.getBlockEntity(current);
            if (currentEntity instanceof NetworkRouterBlockEntity router && router.getChannel() == channel) routerPositions.add(current.immutable());
            if (currentEntity instanceof PylonBlockEntity pylon) {
                pylons++;
                for (BlockPos linked : PylonBlockEntity.linked(level, current, pylon.getChannel())) {
                    pylonEdges.add(edgeKey(current, linked));
                    if (!visited.contains(linked)) pending.addLast(linked);
                }
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!level.hasChunkAt(next) || visited.contains(next)) continue;
                BlockState state = level.getBlockState(next);
                BlockEntity entity = level.getBlockEntity(next);
                if (state.is(ModBlocks.get("network_switch").get()) && entity instanceof NetworkSwitchBlockEntity networkSwitch) {
                    if (!networkSwitch.isEnabled()) { disabledSwitches++; continue; }
                    if (networkSwitch.getChannel() != channel) continue;
                }
                if (state.is(ModBlocks.get("network_router").get()) && entity instanceof NetworkRouterBlockEntity router && router.getChannel() != channel) continue;
                if (isTransport(state, entity, channel)) pending.addLast(next.immutable());
                else if (entity != null && entity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).isPresent())
                    endpointMap.putIfAbsent(next.immutable(), new Endpoint(next.immutable(), direction.getOpposite()));
            }
        }
        List<Endpoint> endpoints = new ArrayList<>(endpointMap.values()); endpoints.sort(Comparator.comparing(Endpoint::pos, POSITION_ORDER));
        List<BlockPos> routers = new ArrayList<>(routerPositions); routers.sort(POSITION_ORDER);
        boolean truncated = !pending.isEmpty() && visited.size() >= MAX_NODES;
        return new Scan(endpoints, routers, visited.size(), pylons, disabledSwitches, pylonEdges.size(), truncated);
    }

    private static long edgeKey(BlockPos a, BlockPos b) { long first=a.asLong(),second=b.asLong(),lo=Math.min(first,second),hi=Math.max(first,second); return lo*31L+hi; }

    public static MoveResult moveOneStack(Level level, List<Endpoint> endpoints, ItemStack filter, int maximum, long cursor, Set<BlockPos> blockedSources) {
        if (maximum <= 0) return MoveResult.failed(MoveStatus.NO_BUDGET);
        if (endpoints.size() < 2) return MoveResult.failed(MoveStatus.NO_ENDPOINTS);
        boolean destinationFilter = NetworkFlashDriveItem.isNetworkFlashDrive(filter);
        Set<BlockPos> allowedDestinations = destinationFilter ? NetworkFlashDriveItem.getConnections(filter) : null;
        boolean sourceItems=false,filterCandidate=false,allowedDestination=false,destinationSpace=false;
        int start=(int)Math.floorMod(cursor,endpoints.size());
        for(int sourceOffset=0;sourceOffset<endpoints.size();sourceOffset++){
            Endpoint source=endpoints.get((start+sourceOffset)%endpoints.size());
            if(blockedSources.contains(source.pos()) || !MachineSideConfigurationData.allowsOutput(level, source.pos(), source.side(), MachineSideConfigurationData.Resource.ITEMS)) continue;
            IItemHandler sourceHandler=handler(level,source); if(sourceHandler==null) continue;
            for(int slot=0;slot<sourceHandler.getSlots();slot++){
                ItemStack candidate=sourceHandler.extractItem(slot,maximum,true); if(candidate.isEmpty()) continue; sourceItems=true;
                if(!destinationFilter&&!filter.isEmpty()&&!ItemStack.isSameItemSameTags(candidate,filter)) continue; filterCandidate=true;
                for(int destinationOffset=1;destinationOffset<endpoints.size();destinationOffset++){
                    Endpoint destination=endpoints.get((start+sourceOffset+destinationOffset)%endpoints.size());
                    if(destination.pos().equals(source.pos())) continue;
                    if(allowedDestinations!=null&&!allowedDestinations.contains(destination.pos())) continue;
                    if(!MachineSideConfigurationData.allowsInput(level, destination.pos(), destination.side(), MachineSideConfigurationData.Resource.ITEMS)) continue;
                    allowedDestination=true;
                    IItemHandler destinationHandler=handler(level,destination); if(destinationHandler==null) continue;
                    ItemStack remaining=candidate.copy();
                    for(int destinationSlot=0;destinationSlot<destinationHandler.getSlots()&&!remaining.isEmpty();destinationSlot++) remaining=destinationHandler.insertItem(destinationSlot,remaining,true);
                    int accepted=candidate.getCount()-remaining.getCount(); if(accepted<=0) continue; destinationSpace=true;
                    ItemStack extracted=sourceHandler.extractItem(slot,accepted,false),toInsert=extracted;
                    for(int destinationSlot=0;destinationSlot<destinationHandler.getSlots()&&!toInsert.isEmpty();destinationSlot++) toInsert=destinationHandler.insertItem(destinationSlot,toInsert,false);
                    int moved = extracted.getCount() - toInsert.getCount();
                    if(!toInsert.isEmpty()) restoreRemainder(level, source, sourceHandler, slot, toInsert);
                    if(moved>0) return new MoveResult(MoveStatus.MOVED,moved,source.pos(),destination.pos());
                }
            }
        }
        if(!sourceItems)return MoveResult.failed(MoveStatus.NO_SOURCE_ITEMS); if(!filterCandidate)return MoveResult.failed(MoveStatus.FILTER_MISS);
        if(!allowedDestination)return MoveResult.failed(MoveStatus.NO_ALLOWED_DESTINATION); if(!destinationSpace)return MoveResult.failed(MoveStatus.DESTINATION_FULL);
        return MoveResult.failed(MoveStatus.NO_ROUTE);
    }

    /** Pulls a specific ingredient from a connected item network into a machine inventory. */
    public static int pullMatchingItem(Level level, BlockPos origin, IItemHandler destination, int destinationSlot, ItemStack wanted, int maximum, long cursor) {
        return pullMatchingItem(level, origin, destination, destinationSlot, wanted, maximum, cursor, 0);
    }

    public static int pullMatchingItem(Level level, BlockPos origin, IItemHandler destination, int destinationSlot, ItemStack wanted, int maximum, long cursor, int channel) {
        if (maximum <= 0 || wanted == null || wanted.isEmpty()) return 0;
        Scan scan = scan(level, origin, channel);
        List<Endpoint> endpoints = scan.endpoints();
        if (endpoints.isEmpty()) return 0;
        int start = (int) Math.floorMod(cursor, (long) endpoints.size());
        for (int offset = 0; offset < endpoints.size(); offset++) {
            Endpoint endpoint = endpoints.get((start + offset) % endpoints.size());
            if (endpoint.pos().equals(origin) || !MachineSideConfigurationData.allowsOutput(level, endpoint.pos(), endpoint.side(), MachineSideConfigurationData.Resource.ITEMS)) continue;
            IItemHandler source = handler(level, endpoint);
            if (source == null) continue;
            for (int slot = 0; slot < source.getSlots(); slot++) {
                ItemStack available = source.getStackInSlot(slot);
                if (available.isEmpty() || !ItemStack.isSameItemSameTags(available, wanted)) continue;
                int amount = Math.min(maximum, available.getCount());
                ItemStack simulated = source.extractItem(slot, amount, true);
                if (simulated.isEmpty()) continue;
                ItemStack remainder = destination.insertItem(destinationSlot, simulated, true);
                int accepted = simulated.getCount() - remainder.getCount();
                if (accepted <= 0) continue;
                ItemStack extracted = source.extractItem(slot, accepted, false);
                ItemStack leftover = destination.insertItem(destinationSlot, extracted, false);
                if (!leftover.isEmpty()) restoreRemainder(level, endpoint, source, slot, leftover);
                return extracted.getCount() - leftover.getCount();
            }
        }
        return 0;
    }

    /** Pulls one arbitrary stack from a bounded channel graph into a configured inventory target. */
    public static int pullAnyItemToDestination(Level level, BlockPos origin, BlockPos destinationPos,
                                                IItemHandler destination, int maximum, long cursor, int channel) {
        if (maximum <= 0 || destination == null) return 0;
        Scan scan = scan(level, origin, channel);
        if (scan.endpoints().isEmpty() || !MachineSideConfigurationData.allowsInput(level, destinationPos,
                Direction.UP, MachineSideConfigurationData.Resource.ITEMS)) return 0;
        int start = (int) Math.floorMod(cursor, (long) scan.endpoints().size());
        for (int offset = 0; offset < scan.endpoints().size(); offset++) {
            Endpoint sourceEndpoint = scan.endpoints().get((start + offset) % scan.endpoints().size());
            if (sourceEndpoint.pos().equals(destinationPos)
                    || !MachineSideConfigurationData.allowsOutput(level, sourceEndpoint.pos(), sourceEndpoint.side(), MachineSideConfigurationData.Resource.ITEMS)) continue;
            IItemHandler source = handler(level, sourceEndpoint);
            if (source == null) continue;
            for (int slot = 0; slot < source.getSlots(); slot++) {
                ItemStack candidate = source.extractItem(slot, maximum, true);
                if (candidate.isEmpty()) continue;
                ItemStack remainder = destination.insertItem(0, candidate, true);
                int accepted = candidate.getCount() - remainder.getCount();
                if (accepted <= 0) {
                    for (int destinationSlot = 1; destinationSlot < destination.getSlots() && accepted <= 0; destinationSlot++) {
                        remainder = destination.insertItem(destinationSlot, candidate, true);
                        accepted = candidate.getCount() - remainder.getCount();
                    }
                }
                if (accepted <= 0) continue;
                ItemStack extracted = source.extractItem(slot, accepted, false);
                ItemStack leftover = extracted.copy();
                for (int destinationSlot = 0; destinationSlot < destination.getSlots() && !leftover.isEmpty(); destinationSlot++)
                    leftover = destination.insertItem(destinationSlot, leftover, false);
                int moved = extracted.getCount() - leftover.getCount();
                if (!leftover.isEmpty()) restoreRemainder(level, sourceEndpoint, source, slot, leftover);
                if (moved > 0) return moved;
            }
        }
        return 0;
    }

    private static void restoreRemainder(Level level, Endpoint source, IItemHandler sourceHandler, int originalSlot, ItemStack remainder) {
        ItemStack remaining = sourceHandler.insertItem(originalSlot, remainder, false);
        for (int slot = 0; slot < sourceHandler.getSlots() && !remaining.isEmpty(); slot++) {
            if (slot == originalSlot) continue;
            remaining = sourceHandler.insertItem(slot, remaining, false);
        }
        if (!remaining.isEmpty()) {
            Containers.dropItemStack(level,
                    source.pos().getX() + 0.5D,
                    source.pos().getY() + 0.5D,
                    source.pos().getZ() + 0.5D,
                    remaining);
        }
    }

    public static boolean endpointHasItems(Level level, Endpoint endpoint) {
        if (!MachineSideConfigurationData.allowsOutput(level, endpoint.pos(), endpoint.side(), MachineSideConfigurationData.Resource.ITEMS)) return false;
        IItemHandler handler=handler(level,endpoint); if(handler==null)return false;
        for(int slot=0;slot<handler.getSlots();slot++) if(!handler.getStackInSlot(slot).isEmpty()) return true; return false;
    }
    private static IItemHandler handler(Level level, Endpoint endpoint){BlockEntity entity=level.getBlockEntity(endpoint.pos());return entity==null?null:entity.getCapability(ForgeCapabilities.ITEM_HANDLER,endpoint.side()).orElse(null);}
    private static boolean isTransport(BlockState state, BlockEntity entity, int channel){
        if(state.is(ModBlocks.get("network_pipe").get())||state.is(ModBlocks.get("pylon").get()))return true;
        if(state.is(ModBlocks.get("network_router").get()))return entity instanceof NetworkRouterBlockEntity router&&router.getChannel()==channel;
        return state.is(ModBlocks.get("network_switch").get())&&entity instanceof NetworkSwitchBlockEntity networkSwitch&&networkSwitch.isEnabled()&&networkSwitch.getChannel()==channel;
    }

    public enum MoveStatus{IDLE,MOVED,NO_ENDPOINTS,NO_BUDGET,NO_SOURCE_ITEMS,FILTER_MISS,NO_ALLOWED_DESTINATION,DESTINATION_FULL,NO_ROUTE,NO_ENERGY,SECONDARY_ROUTER,GRAPH_LIMIT;
        public static MoveStatus byOrdinal(int value){MoveStatus[] values=values();return value>=0&&value<values.length?values[value]:IDLE;}}
    public record Endpoint(BlockPos pos,Direction side){}
    public record Scan(List<Endpoint>endpoints,List<BlockPos>routers,int nodes,int pylons,int disabledSwitches,int pylonLinks,boolean truncated){}
    public record MoveResult(MoveStatus status,int count,BlockPos source,BlockPos destination){public static MoveResult failed(MoveStatus status){return new MoveResult(status,0,null,null);}}
}
