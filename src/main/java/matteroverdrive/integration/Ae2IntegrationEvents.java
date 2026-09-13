package matteroverdrive.integration;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.capability.ModCapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

/**
 * AE2 integration is intentionally capability-native. AE2 15.4.10's
 * Storage/Import/Export buses adapt Forge IItemHandler inventories directly, so
 * Matter Overdrive exposes its public machine inventories rather than inventing a
 * second ME transport API or converting AE power to Forge Energy.
 *
 * The /moae2 command is a runtime integration audit: point at an MO machine to see
 * exactly what AE2 can discover on each face.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Ae2IntegrationEvents {
    private Ae2IntegrationEvents() {}

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("moae2")
                .executes(context -> audit(context.getSource().getPlayerOrException())));
    }

    private static int audit(ServerPlayer player) {
        if (!ModList.get().isLoaded("ae2")) {
            player.sendSystemMessage(Component.literal("AE2 is not installed; Matter Overdrive remains standalone.")
                    .withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        HitResult hit = player.pick(7.0D, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit)) {
            player.sendSystemMessage(Component.literal("Look at a Matter Overdrive machine and run /moae2.")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(blockHit.getBlockPos());
        if (blockEntity == null) {
            player.sendSystemMessage(Component.literal("Target has no block entity to expose to AE2.")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }

        player.sendSystemMessage(Component.literal("AE2 capability audit: " + blockEntity.getBlockState().getBlock().getName().getString())
                .withStyle(ChatFormatting.AQUA));

        boolean anyItems = false;
        for (Direction side : Direction.values()) {
            var optional = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
            if (optional.isPresent()) {
                IItemHandler handler = optional.orElse(null);
                player.sendSystemMessage(Component.literal("  " + side.getName() + ": " + describe(handler))
                        .withStyle(ChatFormatting.GREEN));
                anyItems = true;
            }
        }
        var unsidedItems = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
        if (unsidedItems.isPresent()) {
            IItemHandler handler = unsidedItems.orElse(null);
            player.sendSystemMessage(Component.literal("  unsided: " + describe(handler))
                    .withStyle(ChatFormatting.GREEN));
            anyItems = true;
        }

        boolean energy = blockEntity.getCapability(ForgeCapabilities.ENERGY, blockHit.getDirection()).isPresent()
                || blockEntity.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
        boolean matter = blockEntity.getCapability(ModCapabilities.MATTER, blockHit.getDirection()).isPresent()
                || blockEntity.getCapability(ModCapabilities.MATTER, null).isPresent();

        player.sendSystemMessage(Component.literal("  FE capability: " + (energy ? "yes" : "no")
                        + " | Matter capability: " + (matter ? "yes" : "no"))
                .withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal(anyItems
                        ? "AE2 Storage/Import/Export buses can use the exposed item handler."
                        : "No item handler is exposed on this target; AE2 item buses cannot automate it directly.")
                .withStyle(anyItems ? ChatFormatting.AQUA : ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("ME and Matter Networks remain separate; AE power is never converted to FE.")
                .withStyle(ChatFormatting.DARK_GRAY));
        return anyItems ? 1 : 0;
    }

    /**
     * Describes the public inventory boundary without mutating it. The simulated
     * extraction count makes the audit useful for one-slot interfaces such as the
     * Charging Station battery port and Matter Storage Matrix cell bay.
     */
    private static String describe(IItemHandler handler) {
        if (handler == null) return "item handler unavailable";
        int occupied = 0;
        int extractable = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.getStackInSlot(slot).isEmpty()) occupied++;
            if (!handler.extractItem(slot, 1, true).isEmpty()) extractable++;
        }
        return "item handler, " + handler.getSlots() + " slot(s), " + occupied
                + " occupied, " + extractable + " extractable";
    }
}
