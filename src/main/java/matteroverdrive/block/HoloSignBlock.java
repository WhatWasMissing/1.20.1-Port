package matteroverdrive.block;

import matteroverdrive.blockentity.HoloSignBlockEntity;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Modern Holo Sign foundation based on the legacy BlockHoloSign/TileEntityHoloSign system.
 * The legacy tile persisted a Text value and rendered that value holographically.
 */
public class HoloSignBlock extends BaseEntityBlock {
    public HoloSignBlock(Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HoloSignBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof HoloSignBlockEntity sign)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (held.isEmpty()) {
                sign.setText("");
                player.displayClientMessage(Component.literal("Holo Sign cleared.")
                        .withStyle(ChatFormatting.YELLOW), true);
                return InteractionResult.CONSUME;
            }
            if (held.hasCustomHoverName()) {
                sign.setText(held.getHoverName().getString());
                player.displayClientMessage(Component.literal("Holo Sign text updated.")
                        .withStyle(ChatFormatting.AQUA), true);
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(Component.literal("Rename an item, then sneak-use it on the Holo Sign to set its text. Sneak-use with an empty hand to clear it.")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }

        String text = sign.getText();
        player.displayClientMessage(text.isBlank()
                        ? Component.literal("Holo Sign is blank. Rename an item and sneak-use it here to program the sign.")
                        : Component.literal("Holo Sign: ").append(Component.literal(text).withStyle(ChatFormatting.AQUA)),
                true);
        return InteractionResult.CONSUME;
    }
}
