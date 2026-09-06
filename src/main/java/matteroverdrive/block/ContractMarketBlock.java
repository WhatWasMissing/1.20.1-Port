package matteroverdrive.block;

import matteroverdrive.blockentity.ContractMarketBlockEntity;
import matteroverdrive.item.ContractItem;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class ContractMarketBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ContractMarketBlock(Properties properties) {
        super(properties.noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ContractMarketBlockEntity(pos, state); }
    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) { builder.add(FACING); }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.CONTRACT_MARKET.get()) return null;
        return (tickLevel, tickPos, tickState, entity) -> ContractMarketBlockEntity.serverTick(tickLevel, tickPos, tickState, (ContractMarketBlockEntity) entity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide && held.getItem() instanceof ContractItem && ContractItem.complete(held)) {
            for (ItemStack reward : ContractItem.rewardItems(held)) {
                if (!player.getInventory().add(reward.copy())) player.drop(reward.copy(), false);
            }
            int xp = ContractItem.xp(held);
            if (xp > 0) player.giveExperiencePoints(xp);
            String title = ContractItem.title(held);
            held.shrink(1);
            player.displayClientMessage(Component.literal("Contract redeemed: " + title + (xp > 0 ? " (" + xp + " XP)" : "")), true);
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new net.minecraft.resources.ResourceLocation("matteroverdrive", "gui.quest_complete"));
            if (sound != null && player instanceof ServerPlayer serverPlayer) serverPlayer.playNotifySound(sound, SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide && player instanceof ServerPlayer server
                && level.getBlockEntity(pos) instanceof ContractMarketBlockEntity market) NetworkHooks.openScreen(server, market, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
