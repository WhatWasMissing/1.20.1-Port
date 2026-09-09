package matteroverdrive.network;

import matteroverdrive.blockentity.GravitationalAnomalyBlockEntity;
import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Shared scan used by the Facility Controller, status panels and diagnostic tools. */
public final class FacilityNetworkTelemetry {
    private FacilityNetworkTelemetry() {}

    public static Snapshot scan(Level level, BlockPos origin) {
        List<BlockEntity> nodes = MatterNetworkUtil.findConnected(level, origin, BlockEntity.class);
        long energyStored = 0L, energyCapacity = 0L, matterStored = 0L, matterCapacity = 0L;
        int energyEndpoints = 0, matterEndpoints = 0, anomalies = 0, lowEnergy = 0, lowMatter = 0, fullMatter = 0;
        Map<String, Integer> types = new TreeMap<>();
        List<String> alarms = new ArrayList<>();

        for (BlockEntity node : nodes) {
            types.merge(pretty(node.getClass().getSimpleName()), 1, Integer::sum);
            var energy = node.getCapability(ForgeCapabilities.ENERGY).resolve();
            if (energy.isPresent()) {
                int stored = Math.max(0, energy.get().getEnergyStored());
                int cap = Math.max(0, energy.get().getMaxEnergyStored());
                energyStored += stored;
                energyCapacity += cap;
                energyEndpoints++;
                if (cap > 0 && stored * 10L < cap) lowEnergy++;
            }
            var matter = node.getCapability(ModCapabilities.MATTER).resolve();
            if (matter.isPresent()) {
                IMatterStorage storage = matter.get();
                int stored = Math.max(0, storage.getMatterStored());
                int cap = Math.max(0, storage.getMatterCapacity());
                matterStored += stored;
                matterCapacity += cap;
                matterEndpoints++;
                if (cap > 0 && stored * 10L < cap) lowMatter++;
                if (cap > 0 && stored * 100L >= cap * 95L) fullMatter++;
            }
            if (node instanceof GravitationalAnomalyBlockEntity) anomalies++;
        }

        if (nodes.isEmpty()) alarms.add("NO NETWORK CLIENTS");
        if (energyEndpoints > 0 && energyCapacity > 0 && energyStored * 20L < energyCapacity) alarms.add("GRID ENERGY CRITICAL");
        else if (lowEnergy > 0) alarms.add(lowEnergy + " LOW-ENERGY ENDPOINT(S)");
        if (fullMatter > 0) alarms.add(fullMatter + " MATTER ENDPOINT(S) NEAR FULL");
        if (anomalies > 0) alarms.add(anomalies + " ANOMALY NODE(S) DETECTED");
        boolean critical = nodes.isEmpty() || (energyCapacity > 0 && energyStored * 20L < energyCapacity);
        int severity = critical ? 2 : alarms.isEmpty() ? 0 : 1;
        return new Snapshot(nodes.size(), energyEndpoints, matterEndpoints, energyStored, energyCapacity,
                matterStored, matterCapacity, anomalies, lowEnergy, lowMatter, fullMatter, severity,
                List.copyOf(alarms), Map.copyOf(types));
    }

    private static String pretty(String name) {
        return name.replace("BlockEntity", "").replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    public record Snapshot(int nodes, int energyEndpoints, int matterEndpoints,
                           long energyStored, long energyCapacity, long matterStored, long matterCapacity,
                           int anomalies, int lowEnergyEndpoints, int lowMatterEndpoints, int fullMatterEndpoints,
                           int severity, List<String> alarms, Map<String, Integer> deviceTypes) {
        public int energyPercent() { return energyCapacity <= 0 ? 0 : (int)Math.min(100L, energyStored * 100L / energyCapacity); }
        public int matterPercent() { return matterCapacity <= 0 ? 0 : (int)Math.min(100L, matterStored * 100L / matterCapacity); }
        public int comparatorLevel() { return severity == 2 ? 15 : severity == 1 ? 8 : nodes > 0 ? 1 : 0; }
    }
}