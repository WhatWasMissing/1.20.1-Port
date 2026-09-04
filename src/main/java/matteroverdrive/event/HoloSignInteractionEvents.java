package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.HoloSignBlockEntity;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles Holo Sign programming before a crouching player's held BlockItem can place itself.
 * Runs after security checks, so claimed signs still honour the ownership/access layer.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HoloSignInteractionEvents {
    private HoloSignInteractionEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (!player.isShiftKeyDown()
                || level.getBlockState(event.getPos()).getBlock() != ModBlocks.get("holo_sign").get()
                || !(level.getBlockEntity(event.getPos()) instanceof HoloSignBlockEntity sign)) {
            return;
        }

        // Cancel on both sides so a renamed BlockItem cannot be placed next to the sign by client prediction.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (level.isClientSide) {
            return;
        }

        ItemStack held = event.getItemStack();
        if (held.isEmpty()) {
            sign.setText("");
            player.displayClientMessage(Component.literal("Holo Sign cleared.")
                    .withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        if (held.hasCustomHoverName()) {
            sign.setText(held.getHoverName().getString());
            player.displayClientMessage(Component.literal("Holo Sign programmed: ")
                    .append(Component.literal(sign.getText()).withStyle(ChatFormatting.AQUA)), true);
            return;
        }

        player.displayClientMessage(Component.literal(
                        "Rename any item in an anvil, then sneak-use that renamed item on the Holo Sign. Sneak-use with an empty hand to clear it.")
                .withStyle(ChatFormatting.GRAY), true);
    }
}
