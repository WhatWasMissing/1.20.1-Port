package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * Recreates the 1.12.2 SecurityProtocol behaviour over the modern machine block entities.
 *
 * Legacy behaviour confirmed from MatterOverdrive-1.12.2-0.7.1.0-universal:
 * empty -> claim -> access -> remove -> claim while sneaking; claim/remove are consumed
 * when applied to a machine; an access protocol carrying the machine owner's UUID grants access.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SecurityProtocolEvents {
    private static final String PROTOCOL_OWNER = "Owner";
    private static final String MACHINE_OWNER = "MatterOverdriveOwner";
    private static final String PREFIX = "security_protocol_";

    private SecurityProtocolEvents() {}

    private enum Mode {
        EMPTY("empty"),
        CLAIM("claim"),
        ACCESS("access"),
        REMOVE("remove");

        private final String id;

        Mode(String id) {
            this.id = id;
        }

        String itemId() {
            return PREFIX + id;
        }

        Mode nextBoundMode() {
            return switch (this) {
                case EMPTY, REMOVE -> CLAIM;
                case CLAIM -> ACCESS;
                case ACCESS -> REMOVE;
            };
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        Mode mode = protocolMode(stack);
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (mode == null || !player.isShiftKeyDown()) {
            return;
        }

        if (level.isClientSide) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        UUID owner = protocolOwner(stack);
        Mode next;
        if (owner == null) {
            owner = player.getUUID();
            next = Mode.CLAIM;
        } else {
            if (!owner.equals(player.getUUID()) && !player.getAbilities().instabuild) {
                player.displayClientMessage(Component.literal("This security protocol belongs to another user.")
                        .withStyle(ChatFormatting.RED), true);
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }
            next = mode.nextBoundMode();
        }

        ItemStack replacement = new ItemStack(ModItems.get(next.itemId()).get(), stack.getCount());
        if (stack.hasTag()) {
            replacement.setTag(stack.getTag().copy());
        }
        replacement.getOrCreateTag().putUUID(PROTOCOL_OWNER, owner);
        player.setItemInHand(event.getHand(), replacement);
        player.displayClientMessage(Component.literal("Security protocol: " + next.id)
                .withStyle(ChatFormatting.AQUA), true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockEntity blockEntity = securedBlockEntity(level, pos);
        if (blockEntity == null) {
            return;
        }

        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        Mode mode = protocolMode(held);
        UUID machineOwner = machineOwner(blockEntity);

        if (mode == Mode.CLAIM) {
            UUID tokenOwner = protocolOwner(held);
            if (tokenOwner == null) {
                fail(event, player, "Bind the claim protocol first (sneak + use it in the air).");
                return;
            }
            if (machineOwner != null) {
                fail(event, player, "This machine is already claimed.");
                return;
            }
            setMachineOwner(blockEntity, tokenOwner);
            held.shrink(1);
            player.displayClientMessage(Component.literal("Machine claimed for " + tokenOwner)
                    .withStyle(ChatFormatting.GREEN), true);
            succeed(event);
            return;
        }

        if (mode == Mode.REMOVE) {
            UUID tokenOwner = protocolOwner(held);
            if (machineOwner == null) {
                fail(event, player, "This machine is not claimed.");
                return;
            }
            if (tokenOwner == null || !machineOwner.equals(tokenOwner)) {
                fail(event, player, "This remove protocol does not match the machine owner.");
                return;
            }
            clearMachineOwner(blockEntity);
            held.shrink(1);
            player.displayClientMessage(Component.literal("Machine security removed.")
                    .withStyle(ChatFormatting.YELLOW), true);
            succeed(event);
            return;
        }

        if (machineOwner != null && !canAccess(player, machineOwner)) {
            fail(event, player, "Access denied: this Matter Overdrive machine is secured.");
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) {
            return;
        }
        BlockEntity blockEntity = securedBlockEntity(level, event.getPos());
        if (blockEntity == null) {
            return;
        }
        UUID owner = machineOwner(blockEntity);
        Player player = event.getPlayer();
        if (owner != null && !canAccess(player, owner)) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal("Access denied: you cannot dismantle this secured machine.")
                    .withStyle(ChatFormatting.RED), true);
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Mode mode = protocolMode(stack);
        if (mode == null) {
            return;
        }
        event.getToolTip().add(Component.literal("Mode: " + mode.id).withStyle(ChatFormatting.GRAY));
        UUID owner = protocolOwner(stack);
        if (owner == null) {
            event.getToolTip().add(Component.literal("Unbound - sneak + use in the air to bind")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            event.getToolTip().add(Component.literal("Owner: " + owner).withStyle(ChatFormatting.YELLOW));
            if (mode == Mode.ACCESS) {
                event.getToolTip().add(Component.literal("Carry this to access machines claimed by this owner")
                        .withStyle(ChatFormatting.AQUA));
            }
        }
    }

    private static BlockEntity securedBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock());
        return key != null && MatterOverdrive.MOD_ID.equals(key.getNamespace()) ? blockEntity : null;
    }

    private static boolean canAccess(Player player, UUID owner) {
        if (player.getAbilities().instabuild || owner.equals(player.getUUID())) {
            return true;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (protocolMode(stack) == Mode.ACCESS && owner.equals(protocolOwner(stack))) {
                return true;
            }
        }
        return false;
    }

    private static Mode protocolMode(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null || !MatterOverdrive.MOD_ID.equals(key.getNamespace()) || !key.getPath().startsWith(PREFIX)) {
            return null;
        }
        String suffix = key.getPath().substring(PREFIX.length());
        for (Mode mode : Mode.values()) {
            if (mode.id.equals(suffix)) {
                return mode;
            }
        }
        return null;
    }

    private static UUID protocolOwner(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID(PROTOCOL_OWNER) ? tag.getUUID(PROTOCOL_OWNER) : null;
    }

    private static UUID machineOwner(BlockEntity blockEntity) {
        CompoundTag tag = blockEntity.getPersistentData();
        return tag.hasUUID(MACHINE_OWNER) ? tag.getUUID(MACHINE_OWNER) : null;
    }

    private static void setMachineOwner(BlockEntity blockEntity, UUID owner) {
        blockEntity.getPersistentData().putUUID(MACHINE_OWNER, owner);
        blockEntity.setChanged();
    }

    private static void clearMachineOwner(BlockEntity blockEntity) {
        blockEntity.getPersistentData().remove(MACHINE_OWNER);
        blockEntity.setChanged();
    }

    private static void succeed(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static void fail(PlayerInteractEvent.RightClickBlock event, Player player, String message) {
        player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.RED), true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }
}
