package matteroverdrive.item;

import matteroverdrive.blockentity.PatternStorageBlockEntity;
import matteroverdrive.matter.MatterValueRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class MatterScannerItem extends Item {
    public static final int SCAN_TIME = 60;
    public static final int PROGRESS_PER_BLOCK = 10;
    public static final int LINK_ENERGY_COST = 128;

    private static final String LINKED_TAG = "ScannerLinked";
    private static final String LINK_POS_TAG = "ScannerLinkPos";
    private static final String LINK_DIMENSION_TAG = "ScannerLinkDimension";
    private static final String TARGET_POS_TAG = "ScannerTargetPos";
    private static final String TARGET_DIMENSION_TAG = "ScannerTargetDimension";
    private static final String TARGET_BLOCK_TAG = "ScannerTargetBlock";
    private static final String LAST_SCAN_TAG = "ScannerLastScan";
    private static final String LAST_PROGRESS_TAG = "ScannerLastProgress";

    public MatterScannerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack scanner = context.getItemInHand();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (player.isShiftKeyDown() && blockEntity instanceof PatternStorageBlockEntity storage) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (storage.getEnergyStorage().getEnergyStored() < LINK_ENERGY_COST) {
                player.sendSystemMessage(Component.literal(
                        "Pattern Storage needs at least " + LINK_ENERGY_COST + " FE to link.")
                        .withStyle(ChatFormatting.RED));
                return InteractionResult.CONSUME;
            }
            storage.getEnergyStorage().extractEnergy(LINK_ENERGY_COST, false);
            CompoundTag tag = scanner.getOrCreateTag();
            tag.putBoolean(LINKED_TAG, true);
            tag.putLong(LINK_POS_TAG, pos.asLong());
            tag.putString(LINK_DIMENSION_TAG, level.dimension().location().toString());
            player.sendSystemMessage(Component.literal("Matter Scanner linked to Pattern Storage at "
                    + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                    .withStyle(ChatFormatting.GREEN));
            return InteractionResult.CONSUME;
        }

        BlockState state = level.getBlockState(pos);
        ItemStack scanned = new ItemStack(state.getBlock().asItem());
        int matter = MatterValueRegistry.getMatter(scanned);
        if (scanned.isEmpty() || matter <= 0) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("That block has no scannable matter pattern.")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        if (!isLinked(scanner)) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal(
                        "Sneak-use the Scanner on a powered Pattern Storage first.")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            CompoundTag tag = scanner.getOrCreateTag();
            tag.putLong(TARGET_POS_TAG, pos.asLong());
            tag.putString(TARGET_DIMENSION_TAG, level.dimension().location().toString());
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            tag.putString(TARGET_BLOCK_TAG, blockId.toString());
        }
        player.startUsingItem(context.getHand());
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return SCAN_TIME;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack scanner, Level level, LivingEntity living) {
        if (level.isClientSide || !(living instanceof Player player)) {
            return scanner;
        }

        CompoundTag tag = scanner.getTag();
        if (tag == null || !tag.contains(TARGET_POS_TAG)) {
            return scanner;
        }

        String currentDimension = level.dimension().location().toString();
        if (!currentDimension.equals(tag.getString(TARGET_DIMENSION_TAG))) {
            fail(player, "Scan cancelled: target dimension changed.");
            clearTarget(tag);
            return scanner;
        }

        BlockPos targetPos = BlockPos.of(tag.getLong(TARGET_POS_TAG));
        if (!level.hasChunkAt(targetPos) || player.distanceToSqr(
                targetPos.getX() + 0.5D, targetPos.getY() + 0.5D, targetPos.getZ() + 0.5D) > 36.0D) {
            fail(player, "Scan cancelled: target is unavailable or out of range.");
            clearTarget(tag);
            return scanner;
        }

        BlockState targetState = level.getBlockState(targetPos);
        ResourceLocation currentBlock = BuiltInRegistries.BLOCK.getKey(targetState.getBlock());
        if (!currentBlock.toString().equals(tag.getString(TARGET_BLOCK_TAG))) {
            fail(player, "Scan cancelled: target changed.");
            clearTarget(tag);
            return scanner;
        }

        ItemStack scanned = new ItemStack(targetState.getBlock().asItem());
        int matter = MatterValueRegistry.getMatter(scanned);
        PatternStorageBlockEntity storage = linkedStorage(level, scanner);
        if (scanned.isEmpty() || matter <= 0 || storage == null) {
            fail(player, "Scan failed: linked Pattern Storage is offline or unavailable.");
            clearTarget(tag);
            return scanner;
        }
        if (!storage.canAcceptAnalysis(scanned)) {
            fail(player, "Scan failed: pattern is complete or no drive space is available.");
            clearTarget(tag);
            return scanner;
        }
        if (!level.mayInteract(player, targetPos)
                || !player.mayUseItemAt(targetPos, net.minecraft.core.Direction.UP, scanner)
                || !level.destroyBlock(targetPos, false, player)) {
            fail(player, "Scan failed: the target could not be consumed.");
            clearTarget(tag);
            return scanner;
        }
        if (!storage.addAnalysis(scanned, matter, PROGRESS_PER_BLOCK)) {
            fail(player, "Scan failed while recording the pattern.");
            clearTarget(tag);
            return scanner;
        }

        int progress = storage.getProgressFor(scanned);
        tag.putString(LAST_SCAN_TAG, scanned.getHoverName().getString());
        tag.putInt(LAST_PROGRESS_TAG, progress);
        clearTarget(tag);
        player.sendSystemMessage(Component.literal("Scanned ")
                .append(scanned.getHoverName())
                .append(Component.literal(" — pattern progress " + progress + "%"))
                .withStyle(ChatFormatting.AQUA));
        return scanner;
    }

    private static PatternStorageBlockEntity linkedStorage(Level level, ItemStack scanner) {
        CompoundTag tag = scanner.getTag();
        if (tag == null || !tag.getBoolean(LINKED_TAG)
                || !level.dimension().location().toString().equals(tag.getString(LINK_DIMENSION_TAG))) {
            return null;
        }
        BlockPos pos = BlockPos.of(tag.getLong(LINK_POS_TAG));
        if (!level.hasChunkAt(pos)) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof PatternStorageBlockEntity storage && storage.isNetworkActive()
                ? storage : null;
    }

    private static boolean isLinked(ItemStack scanner) {
        return scanner.hasTag() && scanner.getTag().getBoolean(LINKED_TAG);
    }

    private static void clearTarget(CompoundTag tag) {
        tag.remove(TARGET_POS_TAG);
        tag.remove(TARGET_DIMENSION_TAG);
        tag.remove(TARGET_BLOCK_TAG);
    }

    private static void fail(Player player, String message) {
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.getBoolean(LINKED_TAG)) {
            tooltip.add(Component.literal("Offline — sneak-use on powered Pattern Storage")
                    .withStyle(ChatFormatting.RED));
        } else {
            BlockPos pos = BlockPos.of(tag.getLong(LINK_POS_TAG));
            tooltip.add(Component.literal("Linked: " + tag.getString(LINK_DIMENSION_TAG)
                    + " [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                    .withStyle(ChatFormatting.GREEN));
            if (tag.contains(LAST_SCAN_TAG)) {
                tooltip.add(Component.literal("Last: " + tag.getString(LAST_SCAN_TAG)
                        + " [" + tag.getInt(LAST_PROGRESS_TAG) + "%]")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        tooltip.add(Component.literal("Hold use on a block for 3 seconds; successful scans consume it.")
                .withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
