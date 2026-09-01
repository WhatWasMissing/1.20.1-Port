package matteroverdrive.network;

import matteroverdrive.blockentity.FusionReactorIOBlockEntity;
import matteroverdrive.blockentity.MatterAnalyzerBlockEntity;
import matteroverdrive.blockentity.NetworkSwitchBlockEntity;
import matteroverdrive.blockentity.PatternMonitorBlockEntity;
import matteroverdrive.blockentity.PatternStorageBlockEntity;
import matteroverdrive.blockentity.ReplicatorBlockEntity;
import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.ModCapabilities;
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

    private MatterNetworkUtil() {
    }

    public static <T extends BlockEntity> List<T> findConnected(Level level, BlockPos origin, Class<T> type) {
        Map<BlockPos, T> found = new LinkedHashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(origin.immutable());

        while (!queue.isEmpty() && visited.size() < MAX_NETWORK_NODES) {
            BlockPos current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (next.equals(origin) || !level.hasChunkAt(next)) {
                    continue;
                }

                BlockState state = level.getBlockState(next);
                BlockEntity blockEntity = level.getBlockEntity(next);
                boolean traversable = isNetworkTransport(state, blockEntity) || isNetworkClient(blockEntity);
                if (!traversable) {
                    continue;
                }

                if (type.isInstance(blockEntity)) {
                    found.put(next.immutable(), type.cast(blockEntity));
                }

                if (!visited.contains(next)) {
                    queue.add(next.immutable());
                }
            }
        }

        return new ArrayList<>(found.values());
    }

    public static int pullMatter(Level level, BlockPos origin, IMatterStorage destination,
                                 int maxAmount, long routeIndex) {
        if (maxAmount <= 0 || !destination.canReceive()) {
            return 0;
        }

        List<MatterEndpoint> sources = findMatterSources(level, origin);
        if (sources.isEmpty()) {
            return 0;
        }

        int start = (int) Math.floorMod(routeIndex, (long) sources.size());
        for (int offset = 0; offset < sources.size(); offset++) {
            MatterEndpoint source = sources.get((start + offset) % sources.size());
            int moved = transferFromEndpoint(level, source, destination, maxAmount);
            if (moved > 0) {
                return moved;
            }
        }
        return 0;
    }

    public static int transferMatter(Level level, BlockPos origin, int maxAmount, long routeIndex) {
        if (maxAmount <= 0) {
            return 0;
        }

        List<MatterEndpoint> endpoints = findMatterEndpoints(level, origin);
        if (endpoints.isEmpty()) {
            return 0;
        }

        int start = (int) Math.floorMod(routeIndex, (long) endpoints.size());
        for (int offset = 0; offset < endpoints.size(); offset++) {
            MatterEndpoint endpoint = endpoints.get((start + offset) % endpoints.size());
            int accepted = transferToEndpoint(level, endpoint, maxAmount);
            if (accepted > 0) {
                return accepted;
            }
        }
        return 0;
    }

    public static int transferMatter(Level level, BlockPos origin, int maxAmount) {
        return transferMatter(level, origin, maxAmount, 0L);
    }

    public static List<BlockEntity> findMatterTargets(Level level, BlockPos origin) {
        List<MatterEndpoint> endpoints = findMatterEndpoints(level, origin);
        List<BlockEntity> found = new ArrayList<>();
        if (endpoints.isEmpty()) {
            return found;
        }

        long routeEpoch = Math.floorDiv(level.getGameTime(), LEGACY_MATTER_ROUTE_PERIOD);
        int start = (int) Math.floorMod(routeEpoch + origin.asLong(), (long) endpoints.size());
        for (int offset = 0; offset < endpoints.size(); offset++) {
            MatterEndpoint endpoint = endpoints.get((start + offset) % endpoints.size());
            BlockEntity blockEntity = level.getBlockEntity(endpoint.pos());
            if (blockEntity != null) {
                found.add(blockEntity);
            }
        }
        return found;
    }

    private static List<MatterEndpoint> findMatterSources(Level level, BlockPos origin) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visitedPipes = new HashSet<>();
        Set<MatterEndpoint> candidates = new HashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos next = origin.relative(direction);
            if (!level.hasChunkAt(next)) {
                continue;
            }
            if (isMatterPipe(level.getBlockState(next))) {
                queue.add(next.immutable());
            } else {
                addMatterSource(level, next, direction.getOpposite(), origin, candidates);
            }
        }

        while (!queue.isEmpty() && visitedPipes.size() < MAX_NETWORK_NODES) {
            BlockPos pipe = queue.removeFirst();
            if (!visitedPipes.add(pipe)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = pipe.relative(direction);
                if (next.equals(origin) || !level.hasChunkAt(next)) {
                    continue;
                }

                if (isMatterPipe(level.getBlockState(next))) {
                    if (!visitedPipes.contains(next)) {
                        queue.add(next.immutable());
                    }
                } else {
                    addMatterSource(level, next, direction.getOpposite(), origin, candidates);
                }
            }
        }

        List<MatterEndpoint> sortedCandidates = new ArrayList<>(candidates);
        sortedCandidates.sort(Comparator
                .comparingInt((MatterEndpoint endpoint) -> endpoint.pos().getX())
                .thenComparingInt(endpoint -> endpoint.pos().getY())
                .thenComparingInt(endpoint -> endpoint.pos().getZ())
                .thenComparingInt(endpoint -> endpoint.side().ordinal()));

        Map<BlockPos, MatterEndpoint> resolved = new LinkedHashMap<>();
        for (MatterEndpoint candidate : sortedCandidates) {
            if (resolved.containsKey(candidate.pos()) || !canExtractMatter(level, candidate)) {
                continue;
            }
            resolved.put(candidate.pos(), candidate);
        }
        return new ArrayList<>(resolved.values());
    }

    private static void addMatterSource(Level level, BlockPos position, Direction side,
                                        BlockPos origin, Set<MatterEndpoint> candidates) {
        if (position.equals(origin)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(position);
        if (blockEntity == null || blockEntity instanceof FusionReactorIOBlockEntity) {
            return;
        }
        candidates.add(new MatterEndpoint(position.immutable(), side));
    }

    private static boolean canExtractMatter(Level level, MatterEndpoint endpoint) {
        BlockEntity blockEntity = level.getBlockEntity(endpoint.pos());
        if (blockEntity == null) {
            return false;
        }
        return blockEntity.getCapability(ModCapabilities.MATTER, endpoint.side())
                .map(IMatterStorage::canExtract)
                .orElse(false);
    }

    private static int transferFromEndpoint(Level level, MatterEndpoint endpoint,
                                            IMatterStorage destination, int maxAmount) {
        BlockEntity blockEntity = level.getBlockEntity(endpoint.pos());
        if (blockEntity == null) {
            return 0;
        }
        return blockEntity.getCapability(ModCapabilities.MATTER, endpoint.side())
                .map(source -> {
                    if (!source.canExtract()) {
                        return 0;
                    }
                    int available = source.extractMatter(maxAmount, true);
                    if (available <= 0) {
                        return 0;
                    }
                    int accepted = destination.receiveMatter(available, true);
                    if (accepted <= 0) {
                        return 0;
                    }

                    int extracted = source.extractMatter(accepted, false);
                    if (extracted <= 0) {
                        return 0;
                    }

                    int received = destination.receiveMatter(extracted, false);
                    if (received < extracted) {
                        source.receiveMatter(extracted - received, false);
                    }
                    return received;
                })
                .orElse(0);
    }

    private static List<MatterEndpoint> findMatterEndpoints(Level level, BlockPos origin) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visitedPipes = new HashSet<>();
        Set<MatterEndpoint> candidates = new HashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos next = origin.relative(direction);
            if (!level.hasChunkAt(next)) {
                continue;
            }
            if (isMatterPipe(level.getBlockState(next))) {
                queue.add(next.immutable());
            } else if (level.getBlockEntity(next) != null) {
                candidates.add(new MatterEndpoint(next.immutable(), direction.getOpposite()));
            }
        }

        while (!queue.isEmpty() && visitedPipes.size() < MAX_NETWORK_NODES) {
            BlockPos pipe = queue.removeFirst();
            if (!visitedPipes.add(pipe)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = pipe.relative(direction);
                if (next.equals(origin) || !level.hasChunkAt(next)) {
                    continue;
                }

                if (isMatterPipe(level.getBlockState(next))) {
                    if (!visitedPipes.contains(next)) {
                        queue.add(next.immutable());
                    }
                } else if (level.getBlockEntity(next) != null) {
                    candidates.add(new MatterEndpoint(next.immutable(), direction.getOpposite()));
                }
            }
        }

        List<MatterEndpoint> sortedCandidates = new ArrayList<>(candidates);
        sortedCandidates.sort(Comparator
                .comparingInt((MatterEndpoint endpoint) -> endpoint.pos().getX())
                .thenComparingInt(endpoint -> endpoint.pos().getY())
                .thenComparingInt(endpoint -> endpoint.pos().getZ())
                .thenComparingInt(endpoint -> endpoint.side().ordinal()));

        Map<BlockPos, MatterEndpoint> resolved = new LinkedHashMap<>();
        for (MatterEndpoint candidate : sortedCandidates) {
            if (resolved.containsKey(candidate.pos()) || !canReceiveMatter(level, candidate)) {
                continue;
            }
            resolved.put(candidate.pos(), candidate);
        }
        return new ArrayList<>(resolved.values());
    }

    private static boolean canReceiveMatter(Level level, MatterEndpoint endpoint) {
        BlockEntity blockEntity = level.getBlockEntity(endpoint.pos());
        if (blockEntity == null) {
            return false;
        }
        return blockEntity.getCapability(ModCapabilities.MATTER, endpoint.side())
                .map(IMatterStorage::canReceive)
                .orElse(false);
    }

    private static int transferToEndpoint(Level level, MatterEndpoint endpoint, int maxAmount) {
        BlockEntity blockEntity = level.getBlockEntity(endpoint.pos());
        if (blockEntity == null) {
            return 0;
        }
        return blockEntity.getCapability(ModCapabilities.MATTER, endpoint.side())
                .map(storage -> {
                    if (!storage.canReceive()) {
                        return 0;
                    }
                    int accepted = storage.receiveMatter(maxAmount, true);
                    return accepted <= 0 ? 0 : storage.receiveMatter(Math.min(maxAmount, accepted), false);
                })
                .orElse(0);
    }

    private static boolean isNetworkTransport(BlockState state, BlockEntity blockEntity) {
        if (state.is(ModBlocks.get("network_pipe").get()) || state.is(ModBlocks.get("network_router").get())) {
            return true;
        }
        return state.is(ModBlocks.get("network_switch").get())
                && blockEntity instanceof NetworkSwitchBlockEntity networkSwitch
                && networkSwitch.isEnabled();
    }

    private static boolean isNetworkClient(BlockEntity blockEntity) {
        return blockEntity instanceof MatterAnalyzerBlockEntity
                || blockEntity instanceof PatternStorageBlockEntity
                || blockEntity instanceof PatternMonitorBlockEntity
                || blockEntity instanceof ReplicatorBlockEntity;
    }

    private static boolean isMatterPipe(BlockState state) {
        return state.is(ModBlocks.get("matter_pipe").get());
    }

    private record MatterEndpoint(BlockPos pos, Direction side) {
    }
}
