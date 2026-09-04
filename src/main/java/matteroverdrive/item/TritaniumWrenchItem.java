package matteroverdrive.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import matteroverdrive.MatterOverdrive;

/**
 * Tritanium utility wrench. The original 1.7 wrench rotated blocks normally
 * and dismantled supported machines while sneaking. The modern dismantle path
 * intentionally uses ServerPlayerGameMode.destroyBlock so Forge break/security
 * events still run instead of bypassing machine ownership.
 */
public class TritaniumWrenchItem extends Item {
    public TritaniumWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);

        if (player != null && player.isShiftKeyDown() && isMatterOverdriveBlock(state)) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                boolean removed = serverPlayer.gameMode.destroyBlock(pos);
                if (removed) {
                    context.getItemInHand().hurtAndBreak(1, player,
                            p -> p.broadcastBreakEvent(context.getHand()));
                }
                return removed ? InteractionResult.SUCCESS : InteractionResult.FAIL;
            }
            return InteractionResult.SUCCESS;
        }

        BlockState rotated = state.rotate(level, pos, Rotation.CLOCKWISE_90);
        if (rotated == state) return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.setBlock(pos, rotated, 3);
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player,
                        p -> p.broadcastBreakEvent(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean isMatterOverdriveBlock(BlockState state) {
        var key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return key != null && MatterOverdrive.MOD_ID.equals(key.getNamespace());
    }
}
