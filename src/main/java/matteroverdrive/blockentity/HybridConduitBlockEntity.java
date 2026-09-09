package matteroverdrive.blockentity;

import matteroverdrive.registry.ModExtraBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/** Higher-tier conduit that participates in the FE cable graph and Matter pipe graph simultaneously. */
public class HybridConduitBlockEntity extends EnergyPipeBlockEntity {
    public HybridConduitBlockEntity(BlockPos pos, BlockState state) { super(ModExtraBlockEntities.HYBRID_CONDUIT.get(), pos, state); }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.hybrid_conduit"); }
}