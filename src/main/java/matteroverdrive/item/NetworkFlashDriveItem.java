package matteroverdrive.item;

import matteroverdrive.MatterOverdrive;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Modern port of the 1.7.10 NetworkFlashDrive CONNECTIONS destination filter. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NetworkFlashDriveItem extends Item {
    public static final String CONNECTIONS_TAG = "CONNECTIONS";

    public NetworkFlashDriveItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return toggle(context.getLevel(), context.getClickedPos(), context.getItemInHand(), context.getPlayer());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!isNetworkFlashDrive(stack)) return;
        InteractionResult result = toggle(event.getLevel(), event.getPos(), stack, event.getEntity());
        if (result.consumesAction()) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (isNetworkFlashDrive(event.getItemStack())) {
            appendDetails(event.getItemStack(), event.getToolTip(), null);
        }
    }

    private static InteractionResult toggle(Level level, BlockPos pos, ItemStack stack,
                                            @Nullable net.minecraft.world.entity.player.Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!isNetworkEndpoint(blockEntity)) {
            if (!level.isClientSide && player != null) {
                player.displayClientMessage(Component.literal("That block is not an item-network endpoint.")
                        .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            ListTag connections = stack.getOrCreateTag().getList(CONNECTIONS_TAG, Tag.TAG_COMPOUND);
            int existing = find(connections, pos);
            boolean added = existing < 0;
            if (added) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("X", pos.getX());
                entry.putInt("Y", pos.getY());
                entry.putInt("Z", pos.getZ());
                connections.add(entry);
            } else {
                connections.remove(existing);
            }
            stack.getOrCreateTag().put(CONNECTIONS_TAG, connections);
            if (player != null) {
                player.displayClientMessage(Component.literal(
                        (added ? "Added network destination " : "Removed network destination ")
                                + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                        .withStyle(added ? ChatFormatting.AQUA : ChatFormatting.YELLOW), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean isNetworkFlashDrive(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && MatterOverdrive.MOD_ID.equals(id.getNamespace())
                && "network_flash_drive".equals(id.getPath());
    }

    private static boolean isNetworkEndpoint(@Nullable BlockEntity blockEntity) {
        if (blockEntity == null) return false;
        for (Direction direction : Direction.values()) {
            if (blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).isPresent()) return true;
        }
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent();
    }

    private static int find(ListTag list, BlockPos pos) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.getInt("X") == pos.getX() && entry.getInt("Y") == pos.getY()
                    && entry.getInt("Z") == pos.getZ()) return i;
        }
        return -1;
    }

    public static Set<BlockPos> getConnections(ItemStack stack) {
        Set<BlockPos> result = new LinkedHashSet<>();
        CompoundTag tag = stack.getTag();
        if (tag == null) return result;
        ListTag connections = tag.getList(CONNECTIONS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < connections.size(); i++) {
            CompoundTag entry = connections.getCompound(i);
            result.add(new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z")));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flags) {
        appendDetails(stack, tooltip, level);
    }

    private static void appendDetails(ItemStack stack, List<Component> tooltip, @Nullable Level level) {
        Set<BlockPos> connections = getConnections(stack);
        tooltip.add(Component.literal("Network destinations: " + connections.size())
                .withStyle(ChatFormatting.AQUA));
        if (connections.isEmpty()) {
            tooltip.add(Component.literal("Right-click an inventory block to add it.")
                    .withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal("An empty drive permits no destinations when installed.")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        int shown = 0;
        for (BlockPos pos : connections) {
            if (shown++ >= 6) {
                tooltip.add(Component.literal("...and " + (connections.size() - 6) + " more")
                        .withStyle(ChatFormatting.DARK_GRAY));
                break;
            }
            String name = "Unknown";
            if (level != null && level.hasChunkAt(pos) && !level.isEmptyBlock(pos)) {
                name = level.getBlockState(pos).getBlock().getName().getString();
            }
            tooltip.add(Component.literal("[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "] " + name)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
