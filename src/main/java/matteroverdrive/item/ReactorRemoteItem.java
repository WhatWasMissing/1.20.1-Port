package matteroverdrive.item;

import matteroverdrive.blockentity.FusionReactorControllerBlockEntity;
import matteroverdrive.menu.FusionReactorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class ReactorRemoteItem extends Item {
    private static final String X_TAG = "ReactorX";
    private static final String Y_TAG = "ReactorY";
    private static final String Z_TAG = "ReactorZ";
    private static final String DIMENSION_TAG = "ReactorDimension";

    public ReactorRemoteItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());

        if (player != null && player.isShiftKeyDown()
                && blockEntity instanceof FusionReactorControllerBlockEntity) {
            if (!level.isClientSide) {
                ItemStack stack = context.getItemInHand();
                CompoundTag tag = stack.getOrCreateTag();
                BlockPos target = context.getClickedPos();
                tag.putInt(X_TAG, target.getX());
                tag.putInt(Y_TAG, target.getY());
                tag.putInt(Z_TAG, target.getZ());
                tag.putString(DIMENSION_TAG, level.dimension().location().toString());
                stack.setHoverName(Component.literal("Reactor Remote: "
                        + target.getX() + ", " + target.getY() + ", " + target.getZ()));
                player.displayClientMessage(Component.literal("Reactor Remote linked to Fusion Reactor Controller."), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            openLinkedController(serverPlayer, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void openLinkedController(ServerPlayer player, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (!hasTarget(tag)) {
            player.displayClientMessage(Component.literal("This Reactor Remote is not linked."), true);
            return;
        }

        if (!player.serverLevel().dimension().location().toString().equals(tag.getString(DIMENSION_TAG))) {
            player.displayClientMessage(Component.literal("The linked reactor is in another dimension."), true);
            return;
        }

        BlockPos target = getTarget(tag);
        ServerLevel level = player.serverLevel();
        level.getChunkAt(target);
        BlockEntity blockEntity = level.getBlockEntity(target);
        if (!(blockEntity instanceof FusionReactorControllerBlockEntity controller)) {
            player.displayClientMessage(Component.literal("The linked Fusion Reactor Controller no longer exists."), true);
            return;
        }

        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return controller.getDisplayName();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return new FusionReactorMenu(containerId, inventory, controller, true);
            }
        }, target);
    }

    private static boolean hasTarget(@Nullable CompoundTag tag) {
        return tag != null && tag.contains(X_TAG) && tag.contains(Y_TAG)
                && tag.contains(Z_TAG) && tag.contains(DIMENSION_TAG);
    }

    private static BlockPos getTarget(CompoundTag tag) {
        return new BlockPos(tag.getInt(X_TAG), tag.getInt(Y_TAG), tag.getInt(Z_TAG));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        CompoundTag tag = stack.getTag();
        if (!hasTarget(tag)) {
            tooltip.add(Component.literal("Shift-right-click a Fusion Reactor Controller to link."));
        } else {
            tooltip.add(Component.literal("Linked reactor: " + tag.getInt(X_TAG) + ", "
                    + tag.getInt(Y_TAG) + ", " + tag.getInt(Z_TAG)));
            tooltip.add(Component.literal("Dimension: " + tag.getString(DIMENSION_TAG)));
            tooltip.add(Component.literal("Right-click to open its GUI remotely."));
        }
    }
}
