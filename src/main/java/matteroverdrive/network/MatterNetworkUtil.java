package matteroverdrive.network;

import matteroverdrive.blockentity.MatterAnalyzerBlockEntity;
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

    private MatterNetworkUtil() {
    }

    /**
     * Finds block entities on the same data-network graph as {@code origin}.
     *
     * Alpha.4 only traversed Network Pipe blocks. That made a machine behave as
     * an endpoint which could accidentally split what was visually one network.
     * The 1.20.1 port now treats the currently implemented data-network clients
     * and transport blocks as nodes in the same graph. This keeps direct machine
     * adjacency working while also allowing branches through Storage/Monitor and
     * the placeholder Router/Switch blocks.
     */
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
                boolean traversable = isNetworkTransport(state) || isNetworkClient(blockEntity);
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

    /**
     * Transfers matter either directly to a neighboring capability or through a
     * connected Matter Pipe graph. The route index selects the first endpoint to
     * try, so callers can rotate successful transfers without keeping global pipe
     * state. Each target block is represented only once even if several pipe faces
     * touch it.
     */
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
        List<BlockEntity> found = new ArrayList<>();
        for (MatterEndpoint endpoint : findMatterEndpoints(level, origin)) {
            BlockEntity blockEntity = level.getBlockEntity(endpoint.pos());
            if (blockEntity != null) {
                found.add(blockEntity);
            }
        }
        return found;
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

    private static boolean isNetworkTransport(BlockState state) {
        return state.is(ModBlocks.get("network_pipe").get())
                || state.is(ModBlocks.get("network_router").get())
                || state.is(ModBlocks.get("network_switch").get());
    }

    private static boolean isNetworkClient(BlockEntity blockEntity) {
        return blockEntity instanceof MatterAnalyzerBlockEntity
                || blockEntity instanceof PatternStorageBlockEntity
                || blockEntity instanceof PatternMonitorBlockEntity
                || blockEntity instanceof ReplicatorBlockEntity;
    }

    private static boolean isMatterPipe(BlockState state) {
        return state.is(ModBlocks.get("matter_pipe").get())
                || state.is(ModBlocks.get("heavy_matter_pipe").get());
    }

    private record MatterEndpoint(BlockPos pos, Direction side) {
    }
}
