package matteroverdrive.network;

import matteroverdrive.blockentity.MatterAnalyzerBlockEntity;
import matteroverdrive.blockentity.PatternMonitorBlockEntity;
import matteroverdrive.blockentity.PatternStorageBlockEntity;
import matteroverdrive.blockentity.ReplicatorBlockEntity;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
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

    public static List<BlockEntity> findMatterTargets(Level level, BlockPos origin) {
        Map<BlockPos, BlockEntity> found = new LinkedHashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visitedPipes = new HashSet<>();

        for (Direction direction : Direction.values()) {
            BlockPos next = origin.relative(direction);
            if (level.hasChunkAt(next) && isMatterPipe(level.getBlockState(next))) {
                queue.add(next.immutable());
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
                BlockState state = level.getBlockState(next);
                if (isMatterPipe(state)) {
                    if (!visitedPipes.contains(next)) {
                        queue.add(next.immutable());
                    }
                } else {
                    BlockEntity blockEntity = level.getBlockEntity(next);
                    if (blockEntity != null) {
                        found.put(next.immutable(), blockEntity);
                    }
                }
            }
        }

        return new ArrayList<>(found.values());
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
}
