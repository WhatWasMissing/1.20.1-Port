package matteroverdrive.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Decorative terminal with a useful local-grid diagnostic interaction. */
public class WallTerminalBlock extends Block {
    public WallTerminalBlock(Properties properties){super(properties);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(!level.isClientSide){int machines=0,powered=0;for(BlockPos p:BlockPos.betweenClosed(pos.offset(-4,-2,-4),pos.offset(4,2,4))){var be=level.getBlockEntity(p);if(be==null)continue;var cap=be.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY).orElse(null);if(cap!=null){machines++;if(cap.getEnergyStored()>0)powered++;}}player.sendSystemMessage(Component.literal("LOCAL GRID // "+powered+" powered energy nodes / "+machines+" detected within 4 blocks"));}return InteractionResult.sidedSuccess(level.isClientSide);}
}
