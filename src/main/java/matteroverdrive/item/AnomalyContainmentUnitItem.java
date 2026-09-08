package matteroverdrive.item;

import matteroverdrive.blockentity.GravitationalAnomalyBlockEntity;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.List;

/** Captures a gravitational anomaly and redeploys it while preserving its accumulated mass. */
public class AnomalyContainmentUnitItem extends Item {
    private static final String STORED = "ContainedAnomaly";
    private static final String MASS = "Mass";

    public AnomalyContainmentUnitItem(Properties properties) { super(properties.stacksTo(1)); }

    public static boolean isLoaded(ItemStack stack) { return stack.hasTag() && stack.getTag().contains(STORED); }
    public static long storedMass(ItemStack stack) { return isLoaded(stack) ? Math.max(0L, stack.getTag().getCompound(STORED).getLong(MASS)) : 0L; }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        BlockEntity blockEntity = level.getBlockEntity(clicked);

        if (blockEntity instanceof GravitationalAnomalyBlockEntity anomaly) {
            if (isLoaded(stack)) {
                if (!level.isClientSide && context.getPlayer() != null) context.getPlayer().displayClientMessage(Component.literal("Containment unit already holds an anomaly.").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide) {
                CompoundTag stored = new CompoundTag();
                stored.putLong(MASS, anomaly.getMass());
                stored.putBoolean("MassInitialized", true);
                stack.getOrCreateTag().put(STORED, stored);
                level.removeBlock(clicked, false);
                if (context.getPlayer() != null) context.getPlayer().displayClientMessage(Component.literal("Anomaly contained: mass " + anomaly.getMass()).withStyle(ChatFormatting.AQUA), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!isLoaded(stack)) return InteractionResult.PASS;
        BlockPos placePos = clicked.relative(context.getClickedFace());
        if (!level.getBlockState(placePos).isAir()) {
            if (!level.isClientSide && context.getPlayer() != null) context.getPlayer().displayClientMessage(Component.literal("Need an empty adjacent block to redeploy the anomaly.").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            long mass = storedMass(stack);
            level.setBlock(placePos, ModBlocks.get("gravitational_anomaly").get().defaultBlockState(), 3);
            BlockEntity placed = level.getBlockEntity(placePos);
            if (placed instanceof GravitationalAnomalyBlockEntity anomaly) {
                CompoundTag restored = new CompoundTag();
                restored.putLong(MASS, mass);
                restored.putBoolean("MassInitialized", true);
                anomaly.load(restored);
                anomaly.setChanged();
            }
            stack.getOrCreateTag().remove(STORED);
            if (context.getPlayer() != null) context.getPlayer().displayClientMessage(Component.literal("Anomaly redeployed with preserved mass " + mass).withStyle(ChatFormatting.GREEN), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (isLoaded(stack)) {
            tooltip.add(Component.literal("CONTAINED ANOMALY").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.literal("Mass: " + storedMass(stack)).withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("Use on a block face to redeploy it.").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Empty containment field").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Use directly on a gravitational anomaly to capture it.").withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}