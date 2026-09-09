package matteroverdrive.block;

import matteroverdrive.blockentity.AndroidInductionRelayBlockEntity;
import matteroverdrive.blockentity.GridCapacitorBlockEntity;
import matteroverdrive.blockentity.HolographicStatusPanelBlockEntity;
import matteroverdrive.blockentity.MatterExcavatorBlockEntity;
import matteroverdrive.blockentity.MatterStorageMatrixBlockEntity;
import matteroverdrive.blockentity.QuantumPowerRelayBlockEntity;
import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Shared shell for the experimental tech-overhaul infrastructure blocks. */
public class TechMachineBlock extends BaseEntityBlock {
    public enum Type { GRID_CAPACITOR, ANDROID_INDUCTION_RELAY, QUANTUM_POWER_RELAY, MATTER_STORAGE_MATRIX, MATTER_EXCAVATOR, STATUS_PANEL }
    private final Type type;

    public TechMachineBlock(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (type) {
            case GRID_CAPACITOR -> new GridCapacitorBlockEntity(pos, state);
            case ANDROID_INDUCTION_RELAY -> new AndroidInductionRelayBlockEntity(pos, state);
            case QUANTUM_POWER_RELAY -> new QuantumPowerRelayBlockEntity(pos, state);
            case MATTER_STORAGE_MATRIX -> new MatterStorageMatrixBlockEntity(pos, state);
            case MATTER_EXCAVATOR -> new MatterExcavatorBlockEntity(pos, state);
            case STATUS_PANEL -> new HolographicStatusPanelBlockEntity(pos, state);
        };
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;
        return switch (type) {
            case GRID_CAPACITOR -> createTickerHelper(blockEntityType, ModExtraBlockEntities.GRID_CAPACITOR.get(), GridCapacitorBlockEntity::serverTick);
            case ANDROID_INDUCTION_RELAY -> createTickerHelper(blockEntityType, ModExtraBlockEntities.ANDROID_INDUCTION_RELAY.get(), AndroidInductionRelayBlockEntity::serverTick);
            case QUANTUM_POWER_RELAY -> createTickerHelper(blockEntityType, ModExtraBlockEntities.QUANTUM_POWER_RELAY.get(), QuantumPowerRelayBlockEntity::serverTick);
            case MATTER_STORAGE_MATRIX -> null;
            case MATTER_EXCAVATOR -> createTickerHelper(blockEntityType, ModExtraBlockEntities.MATTER_EXCAVATOR.get(), MatterExcavatorBlockEntity::serverTick);
            case STATUS_PANEL -> createTickerHelper(blockEntityType, ModExtraBlockEntities.HOLOGRAPHIC_STATUS_PANEL.get(), HolographicStatusPanelBlockEntity::serverTick);
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer server)) return InteractionResult.PASS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GridCapacitorBlockEntity node) return node.onUse(server, hand);
        if (be instanceof AndroidInductionRelayBlockEntity node) return node.onUse(server, hand);
        if (be instanceof QuantumPowerRelayBlockEntity node) return node.onUse(server, hand);
        if (be instanceof MatterStorageMatrixBlockEntity node) return node.onUse(server, hand);
        if (be instanceof MatterExcavatorBlockEntity node) return node.onUse(server, hand, hit);
        if (be instanceof HolographicStatusPanelBlockEntity node) return node.onUse(server, hand);
        return InteractionResult.PASS;
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GridCapacitorBlockEntity node) return node.comparatorLevel();
        if (be instanceof AndroidInductionRelayBlockEntity node) return node.comparatorLevel();
        if (be instanceof QuantumPowerRelayBlockEntity node) return node.comparatorLevel();
        if (be instanceof MatterStorageMatrixBlockEntity node) return node.comparatorLevel();
        if (be instanceof MatterExcavatorBlockEntity node) return node.comparatorLevel();
        if (be instanceof HolographicStatusPanelBlockEntity node) return node.comparatorLevel();
        return 0;
    }
}