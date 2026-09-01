package matteroverdrive.block;

import matteroverdrive.android.AndroidData;
import matteroverdrive.blockentity.AndroidStationBlockEntity;
import matteroverdrive.item.AndroidPartItem;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class AndroidStationBlock extends BaseEntityBlock {
    public AndroidStationBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AndroidStationBlockEntity(pos, state); }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.ANDROID_STATION.get()) return null;
        return (tickLevel, tickPos, tickState, entity) ->
                AndroidStationBlockEntity.serverTick(tickLevel, tickPos, tickState, (AndroidStationBlockEntity) entity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide && held.getItem() instanceof AndroidPartItem part) {
            if (!AndroidData.isAndroid(player)) {
                player.displayClientMessage(Component.literal("Activate Android conversion with a blue Android Pill first."), true);
            } else if (AndroidData.installPart(player, part.getPart())) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                player.displayClientMessage(Component.literal(part.getPart().name() + " bionic part installed."), true);
            } else {
                player.displayClientMessage(Component.literal("That bionic part is already installed."), true);
            }
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AndroidStationBlockEntity station
                && player instanceof ServerPlayer serverPlayer) {
            station.link(serverPlayer);
            NetworkHooks.openScreen(serverPlayer, station, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}