package matteroverdrive.block;

import matteroverdrive.blockentity.EnvironmentalRegulatorBlockEntity;
import matteroverdrive.registry.OverhaulContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import javax.annotation.Nullable;

public class EnvironmentalRegulatorBlock extends BaseEntityBlock {
    public EnvironmentalRegulatorBlock(Properties properties){super(properties);}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new EnvironmentalRegulatorBlockEntity(pos,state);}
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){if(level.isClientSide||type!=OverhaulContent.ENVIRONMENTAL_REGULATOR_BE.get())return null;return(l,p,s,be)->EnvironmentalRegulatorBlockEntity.serverTick(l,p,s,(EnvironmentalRegulatorBlockEntity)be);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(level.isClientSide)return InteractionResult.SUCCESS;if(level.getBlockEntity(pos) instanceof EnvironmentalRegulatorBlockEntity r)player.displayClientMessage(Component.literal("Environmental Regulator // "+(r.isActive()?"FIELD ACTIVE":"STANDBY")+" // "+r.getEnergyStored()+" FE // Radius "+EnvironmentalRegulatorBlockEntity.RADIUS),true);return InteractionResult.CONSUME;}
}
