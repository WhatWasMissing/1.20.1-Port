package matteroverdrive.network;

import matteroverdrive.blockentity.FusionReactorIOBlockEntity;
import matteroverdrive.blockentity.MatterAnalyzerBlockEntity;
import matteroverdrive.blockentity.NetworkSwitchBlockEntity;
import matteroverdrive.blockentity.PatternMonitorBlockEntity;
import matteroverdrive.blockentity.PatternStorageBlockEntity;
import matteroverdrive.blockentity.ReplicatorBlockEntity;
import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.machine.MachineSideConfigurationData;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MatterNetworkUtil {
    private static final int MAX_NETWORK_NODES = 2048;
    private static final long LEGACY_MATTER_ROUTE_PERIOD = 32L;
    private MatterNetworkUtil() {}

    public static <T extends BlockEntity> List<T> findConnected(Level level, BlockPos origin, Class<T> type) {
        Map<BlockPos,T> found=new LinkedHashMap<>(); ArrayDeque<BlockPos> queue=new ArrayDeque<>(); Set<BlockPos> visited=new HashSet<>(); queue.add(origin.immutable());
        while(!queue.isEmpty()&&visited.size()<MAX_NETWORK_NODES){
            BlockPos current=queue.removeFirst(); if(!visited.add(current))continue;
            for(Direction direction:Direction.values()){
                BlockPos next=current.relative(direction); if(next.equals(origin)||!level.hasChunkAt(next))continue;
                BlockState state=level.getBlockState(next); BlockEntity be=level.getBlockEntity(next);
                if(!(isNetworkTransport(state,be)||isNetworkClient(be)))continue;
                if(type.isInstance(be))found.put(next.immutable(),type.cast(be)); if(!visited.contains(next))queue.add(next.immutable());
            }
        }
        return new ArrayList<>(found.values());
    }

    public static int pullMatter(Level level, BlockPos origin, IMatterStorage destination,int maxAmount,long routeIndex){
        if(maxAmount<=0||!destination.canReceive())return 0; List<MatterEndpoint> sources=findMatterSources(level,origin); if(sources.isEmpty())return 0;
        int start=(int)Math.floorMod(routeIndex,(long)sources.size()); for(int offset=0;offset<sources.size();offset++){int moved=transferFromEndpoint(level,sources.get((start+offset)%sources.size()),destination,maxAmount);if(moved>0)return moved;} return 0;
    }
    public static int transferMatter(Level level,BlockPos origin,int maxAmount,long routeIndex){
        if(maxAmount<=0)return 0; List<MatterEndpoint>endpoints=findMatterEndpoints(level,origin);if(endpoints.isEmpty())return 0;
        int start=(int)Math.floorMod(routeIndex,(long)endpoints.size());for(int offset=0;offset<endpoints.size();offset++){int accepted=transferToEndpoint(level,endpoints.get((start+offset)%endpoints.size()),maxAmount);if(accepted>0)return accepted;}return 0;
    }
    public static int transferMatter(Level level,BlockPos origin,int maxAmount){return transferMatter(level,origin,maxAmount,0L);}

    public static List<BlockEntity> findMatterTargets(Level level,BlockPos origin){
        List<MatterEndpoint>endpoints=findMatterEndpoints(level,origin);List<BlockEntity>found=new ArrayList<>();if(endpoints.isEmpty())return found;
        long routeEpoch=Math.floorDiv(level.getGameTime(),LEGACY_MATTER_ROUTE_PERIOD);int start=(int)Math.floorMod(routeEpoch+origin.asLong(),(long)endpoints.size());
        for(int offset=0;offset<endpoints.size();offset++){BlockEntity be=level.getBlockEntity(endpoints.get((start+offset)%endpoints.size()).pos());if(be!=null)found.add(be);}return found;
    }

    private static List<MatterEndpoint> findMatterSources(Level level,BlockPos origin){
        ArrayDeque<BlockPos>queue=new ArrayDeque<>();Set<BlockPos>visited=new HashSet<>();Set<MatterEndpoint>candidates=new HashSet<>();
        for(Direction direction:Direction.values()){BlockPos next=origin.relative(direction);if(!level.hasChunkAt(next))continue;if(isMatterPipe(level.getBlockState(next)))queue.add(next.immutable());else addMatterSource(level,next,direction.getOpposite(),origin,candidates);}
        while(!queue.isEmpty()&&visited.size()<MAX_NETWORK_NODES){BlockPos pipe=queue.removeFirst();if(!visited.add(pipe))continue;for(Direction direction:Direction.values()){BlockPos next=pipe.relative(direction);if(next.equals(origin)||!level.hasChunkAt(next))continue;if(isMatterPipe(level.getBlockState(next))){if(!visited.contains(next))queue.add(next.immutable());}else addMatterSource(level,next,direction.getOpposite(),origin,candidates);}}
        return resolve(level,candidates,true);
    }
    private static void addMatterSource(Level level,BlockPos position,Direction side,BlockPos origin,Set<MatterEndpoint>candidates){if(position.equals(origin))return;BlockEntity be=level.getBlockEntity(position);if(be==null||be instanceof FusionReactorIOBlockEntity)return;candidates.add(new MatterEndpoint(position.immutable(),side));}
    private static boolean canExtractMatter(Level level,MatterEndpoint endpoint){
        if(!MachineSideConfigurationData.allowsOutput(level,endpoint.pos(),endpoint.side(),MachineSideConfigurationData.Resource.MATTER))return false;
        BlockEntity be=level.getBlockEntity(endpoint.pos());return be!=null&&be.getCapability(ModCapabilities.MATTER,endpoint.side()).map(IMatterStorage::canExtract).orElse(false);
    }
    private static int transferFromEndpoint(Level level,MatterEndpoint endpoint,IMatterStorage destination,int maxAmount){
        if(!MachineSideConfigurationData.allowsOutput(level,endpoint.pos(),endpoint.side(),MachineSideConfigurationData.Resource.MATTER))return 0;
        BlockEntity be=level.getBlockEntity(endpoint.pos());if(be==null)return 0;
        return be.getCapability(ModCapabilities.MATTER,endpoint.side()).map(source->{if(!source.canExtract())return 0;int available=source.extractMatter(maxAmount,true);if(available<=0)return 0;int accepted=destination.receiveMatter(available,true);if(accepted<=0)return 0;int extracted=source.extractMatter(accepted,false);if(extracted<=0)return 0;int received=destination.receiveMatter(extracted,false);if(received<extracted)source.receiveMatter(extracted-received,false);return received;}).orElse(0);
    }

    private static List<MatterEndpoint> findMatterEndpoints(Level level,BlockPos origin){
        ArrayDeque<BlockPos>queue=new ArrayDeque<>();Set<BlockPos>visited=new HashSet<>();Set<MatterEndpoint>candidates=new HashSet<>();
        for(Direction direction:Direction.values()){BlockPos next=origin.relative(direction);if(!level.hasChunkAt(next))continue;if(isMatterPipe(level.getBlockState(next)))queue.add(next.immutable());else if(level.getBlockEntity(next)!=null)candidates.add(new MatterEndpoint(next.immutable(),direction.getOpposite()));}
        while(!queue.isEmpty()&&visited.size()<MAX_NETWORK_NODES){BlockPos pipe=queue.removeFirst();if(!visited.add(pipe))continue;for(Direction direction:Direction.values()){BlockPos next=pipe.relative(direction);if(next.equals(origin)||!level.hasChunkAt(next))continue;if(isMatterPipe(level.getBlockState(next))){if(!visited.contains(next))queue.add(next.immutable());}else if(level.getBlockEntity(next)!=null)candidates.add(new MatterEndpoint(next.immutable(),direction.getOpposite()));}}
        return resolve(level,candidates,false);
    }
    private static List<MatterEndpoint> resolve(Level level,Set<MatterEndpoint>candidates,boolean extracting){
        List<MatterEndpoint>sorted=new ArrayList<>(candidates);sorted.sort(Comparator.comparingInt((MatterEndpoint e)->e.pos().getX()).thenComparingInt(e->e.pos().getY()).thenComparingInt(e->e.pos().getZ()).thenComparingInt(e->e.side().ordinal()));
        Map<BlockPos,MatterEndpoint>resolved=new LinkedHashMap<>();for(MatterEndpoint candidate:sorted){if(resolved.containsKey(candidate.pos()))continue;if(extracting?canExtractMatter(level,candidate):canReceiveMatter(level,candidate))resolved.put(candidate.pos(),candidate);}return new ArrayList<>(resolved.values());
    }
    private static boolean canReceiveMatter(Level level,MatterEndpoint endpoint){
        if(!MachineSideConfigurationData.allowsInput(level,endpoint.pos(),endpoint.side(),MachineSideConfigurationData.Resource.MATTER))return false;
        BlockEntity be=level.getBlockEntity(endpoint.pos());return be!=null&&be.getCapability(ModCapabilities.MATTER,endpoint.side()).map(IMatterStorage::canReceive).orElse(false);
    }
    private static int transferToEndpoint(Level level,MatterEndpoint endpoint,int maxAmount){
        if(!MachineSideConfigurationData.allowsInput(level,endpoint.pos(),endpoint.side(),MachineSideConfigurationData.Resource.MATTER))return 0;
        BlockEntity be=level.getBlockEntity(endpoint.pos());if(be==null)return 0;
        return be.getCapability(ModCapabilities.MATTER,endpoint.side()).map(storage->{if(!storage.canReceive())return 0;int accepted=storage.receiveMatter(maxAmount,true);return accepted<=0?0:storage.receiveMatter(Math.min(maxAmount,accepted),false);}).orElse(0);
    }

    private static boolean isNetworkTransport(BlockState state,BlockEntity be){if(state.is(ModBlocks.get("network_pipe").get())||state.is(ModBlocks.get("network_router").get()))return true;return state.is(ModBlocks.get("network_switch").get())&&be instanceof NetworkSwitchBlockEntity networkSwitch&&networkSwitch.isEnabled();}
    private static boolean isNetworkClient(BlockEntity be){return be instanceof MatterAnalyzerBlockEntity||be instanceof PatternStorageBlockEntity||be instanceof PatternMonitorBlockEntity||be instanceof ReplicatorBlockEntity;}
    private static boolean isMatterPipe(BlockState state){return state.is(ModBlocks.get("matter_pipe").get());}
    private record MatterEndpoint(BlockPos pos,Direction side){}
}
