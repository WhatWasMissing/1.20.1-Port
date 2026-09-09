package matteroverdrive.blockentity;

import matteroverdrive.network.FacilityNetworkTelemetry;
import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Compact facility display. The renderer reads the synchronized cached telemetry. */
public class HolographicStatusPanelBlockEntity extends BlockEntity {
    public enum Mode { OVERVIEW, ENERGY, MATTER, ALERTS }
    private Mode mode = Mode.OVERVIEW;
    private int nodes, energyPercent, matterPercent, severity, alarmCount;

    public HolographicStatusPanelBlockEntity(BlockPos pos, BlockState state) { super(ModExtraBlockEntities.HOLOGRAPHIC_STATUS_PANEL.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HolographicStatusPanelBlockEntity panel) {
        if (level.getGameTime() % 20L != 0L) return;
        FacilityNetworkTelemetry.Snapshot snapshot = FacilityNetworkTelemetry.scan(level, pos);
        int oldNodes = panel.nodes, oldEnergy = panel.energyPercent, oldMatter = panel.matterPercent, oldSeverity = panel.severity, oldAlarms = panel.alarmCount;
        panel.nodes = snapshot.nodes();
        panel.energyPercent = snapshot.energyPercent();
        panel.matterPercent = snapshot.matterPercent();
        panel.severity = snapshot.severity();
        panel.alarmCount = snapshot.alarms().size();
        if (oldNodes != panel.nodes || oldEnergy != panel.energyPercent || oldMatter != panel.matterPercent || oldSeverity != panel.severity || oldAlarms != panel.alarmCount) {
            panel.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public InteractionResult onUse(ServerPlayer player, InteractionHand hand) {
        if (player.isCrouching()) {
            mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            player.sendSystemMessage(Component.literal("Status panel mode -> " + mode.name()).withStyle(ChatFormatting.AQUA));
            return InteractionResult.CONSUME;
        }
        FacilityNetworkTelemetry.Snapshot snapshot = FacilityNetworkTelemetry.scan(player.level(), worldPosition);
        player.sendSystemMessage(Component.literal(displayLine()).withStyle(severity == 2 ? ChatFormatting.RED : severity == 1 ? ChatFormatting.YELLOW : ChatFormatting.AQUA));
        for (String alarm : snapshot.alarms()) player.sendSystemMessage(Component.literal(" - " + alarm).withStyle(ChatFormatting.YELLOW));
        return InteractionResult.CONSUME;
    }

    public String displayLine() {
        return switch (mode) {
            case OVERVIEW -> "MO FACILITY | " + nodes + " nodes | FE " + energyPercent + "% | M " + matterPercent + "%";
            case ENERGY -> "POWER GRID  " + energyPercent + "%";
            case MATTER -> "MATTER GRID  " + matterPercent + "%";
            case ALERTS -> severity == 0 ? "SYSTEMS NOMINAL" : (severity == 2 ? "CRITICAL" : "WARNING") + "  x" + alarmCount;
        };
    }

    public int severity() { return severity; }
    public Mode mode() { return mode; }
    public int comparatorLevel() { return severity == 2 ? 15 : severity == 1 ? 8 : nodes > 0 ? 1 : 0; }

    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); writeState(tag); }
    @Override public void load(CompoundTag tag) { super.load(tag); readState(tag); }
    @Override public CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); writeState(tag); return tag; }
    @Nullable @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) { CompoundTag tag = pkt.getTag(); if (tag != null) readState(tag); }

    private void writeState(CompoundTag tag) {
        tag.putInt("Mode", mode.ordinal()); tag.putInt("Nodes", nodes); tag.putInt("EnergyPercent", energyPercent); tag.putInt("MatterPercent", matterPercent); tag.putInt("Severity", severity); tag.putInt("AlarmCount", alarmCount);
    }
    private void readState(CompoundTag tag) {
        int m = tag.getInt("Mode"); mode = m >= 0 && m < Mode.values().length ? Mode.values()[m] : Mode.OVERVIEW;
        nodes = Math.max(0, tag.getInt("Nodes")); energyPercent = Math.max(0, Math.min(100, tag.getInt("EnergyPercent"))); matterPercent = Math.max(0, Math.min(100, tag.getInt("MatterPercent"))); severity = Math.max(0, Math.min(2, tag.getInt("Severity"))); alarmCount = Math.max(0, tag.getInt("AlarmCount"));
    }
}