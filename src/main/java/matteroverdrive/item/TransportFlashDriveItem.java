package matteroverdrive.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class TransportFlashDriveItem extends Item {
    public TransportFlashDriveItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            BlockPos target = context.getClickedPos();
            ItemStack stack = context.getItemInHand();
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("TransportX", target.getX());
            tag.putInt("TransportY", target.getY());
            tag.putInt("TransportZ", target.getZ());
            tag.putString("TransportDimension", level.dimension().location().toString());
            stack.setHoverName(Component.literal("Transport Target: " + target.getX() + ", "
                    + target.getY() + ", " + target.getZ()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean hasTarget(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("TransportX") && tag.contains("TransportY")
                && tag.contains("TransportZ")
                && level.dimension().location().toString().equals(tag.getString("TransportDimension"));
    }

    public static BlockPos getTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? BlockPos.ZERO : new BlockPos(
                tag.getInt("TransportX"), tag.getInt("TransportY"), tag.getInt("TransportZ"));
    }

    @Override
    public void appendHoverText(
            ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("TransportX")) {
            tooltip.add(Component.literal("Right-click a block to bind a target."));
        } else {
            tooltip.add(Component.literal("Target: " + tag.getInt("TransportX") + ", "
                    + tag.getInt("TransportY") + ", " + tag.getInt("TransportZ")));
            tooltip.add(Component.literal("Dimension: " + tag.getString("TransportDimension")));
        }
    }
}
