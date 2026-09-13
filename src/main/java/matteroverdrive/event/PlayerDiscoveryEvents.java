package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.progression.PlayerDiscoveryLog;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Turns normal crafting, mining and combat into PDA discoveries instead of structure scavenger hunts. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerDiscoveryEvents {
    private PlayerDiscoveryEvents() {}

    @SubscribeEvent
    public static void crafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getCrafting();
        if (stack.isEmpty()) return;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!MatterOverdrive.MOD_ID.equals(id.getNamespace())) return;
        StructureLoreEvents.discoverFromTechnology(player, id.getPath());
        if (PlayerDiscoveryLog.record(player, "craft:" + id,
                "FABRICATION // " + stack.getHoverName().getString() + " assembled. PDA archived the construction result.")) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("PDA // New fabrication log: " + stack.getHoverName().getString()), true);
        }
    }

    @SubscribeEvent
    public static void mined(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock());
        if (!MatterOverdrive.MOD_ID.equals(id.getNamespace())) return;
        StructureLoreEvents.discoverFromBlock(player, id.getPath());
        PlayerDiscoveryLog.record(player, "break:" + id,
                "FIELD TEST // " + event.getState().getBlock().getName().getString() + " dismantled and its material response recorded.");
    }

    @SubscribeEvent
    public static void killed(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType());
        if (!MatterOverdrive.MOD_ID.equals(id.getNamespace())) return;
        StructureLoreEvents.discoverFromEntity(player, id.getPath());
        if (PlayerDiscoveryLog.record(player, "encounter:" + id,
                "ENCOUNTER // First confirmed neutralisation of " + event.getEntity().getDisplayName().getString() + ". Combat telemetry retained.")) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("PDA // New encounter record"), true);
        }
    }
}
