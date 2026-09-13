package matteroverdrive.block;

import matteroverdrive.blockentity.QuantumFluxReactorBlockEntity;
import matteroverdrive.registry.ModItems;
import matteroverdrive.registry.OverhaulContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

public class QuantumFluxReactorBlock extends BaseEntityBlock {
    public QuantumFluxReactorBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new QuantumFluxReactorBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != OverhaulContent.QUANTUM_FLUX_REACTOR_BE.get()) return null;
        return (l,p,s,be) -> QuantumFluxReactorBlockEntity.serverTick(l,p,s,(QuantumFluxReactorBlockEntity)be);
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof QuantumFluxReactorBlockEntity reactor)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ModItems.get("dilithium_crystal").get()) || held.is(ModItems.get("trilithium_crystal").get())) {
            boolean trilithium = held.is(ModItems.get("trilithium_crystal").get());
            if (reactor.addFuel(trilithium ? 2 : 1) && !player.getAbilities().instabuild) held.shrink(1);
            player.displayClientMessage(Component.literal(trilithium ? "Quantum Flux Reactor: trilithium charge accepted" : "Quantum Flux Reactor: dilithium charge accepted"), true);
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.literal("Quantum Flux Reactor // " + reactor.getEnergyStored() + "/" + reactor.getCapacity() + " FE // Heat " + reactor.getHeatPercent() + "% // Fuel " + reactor.getFuelSeconds() + "s"), true);
        return InteractionResult.CONSUME;
    }
}
