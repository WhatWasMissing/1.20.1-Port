package matteroverdrive.item;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.event.StructureLoreEvents;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.world.AmbientLoreCatalog;
import matteroverdrive.world.AmbientLoreSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Physical facility log that permanently authenticates into the player's PDA archive. */
public final class RecoveredLoreFragmentItem extends Item {
    public static final String LORE_ID_TAG = "MatterOverdriveLoreId";

    public RecoveredLoreFragmentItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static String loreId(ItemStack stack) {
        return stack == null || stack.isEmpty() || !stack.hasTag() ? "" : stack.getOrCreateTag().getString(LORE_ID_TAG);
    }

    @Override
    public Component getName(ItemStack stack) {
        AmbientLoreCatalog.Entry entry = AmbientLoreCatalog.byId(loreId(stack));
        return entry == null ? Component.literal("Recovered Lore Fragment")
                : Component.literal(entry.title());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            AmbientLoreCatalog.Entry entry = AmbientLoreCatalog.byId(loreId(stack));
            if (entry == null) {
                serverPlayer.sendSystemMessage(Component.literal("The recovered record is unreadable or missing its archive identifier.")
                        .withStyle(ChatFormatting.DARK_RED));
                return InteractionResultHolder.consume(stack);
            }

            AmbientLoreSavedData data = AmbientLoreSavedData.get(serverPlayer.serverLevel());
            StructureLoreEvents.discoverFromAmbientFragment(serverPlayer, entry.siteId());
            boolean first = data.discover(serverPlayer.getUUID(), entry.id());
            serverPlayer.sendSystemMessage(Component.literal("[ " + entry.classification() + " ] " + entry.title())
                    .withStyle(ChatFormatting.AQUA));
            serverPlayer.sendSystemMessage(Component.literal(entry.source()).withStyle(ChatFormatting.DARK_AQUA));
            serverPlayer.sendSystemMessage(Component.literal(entry.excerpt()).withStyle(ChatFormatting.GRAY));
            serverPlayer.sendSystemMessage(Component.literal("PDA ANALYSIS: " + entry.analysis())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));

            if (first) {
                serverPlayer.giveExperiencePoints(15);
                int recovered = data.count(serverPlayer.getUUID());
                serverPlayer.sendSystemMessage(Component.literal("Archive authenticated. +15 XP • Optional lore "
                        + recovered + "/" + AmbientLoreCatalog.count())
                        .withStyle(ChatFormatting.GREEN));
                ModNetwork.sendPdaVoice(serverPlayer, entry.voiceLine());
                if (recovered >= 16) grant(serverPlayer, "field_archivist");
                if (recovered >= 32) grant(serverPlayer, "field_historian");
                if (recovered >= AmbientLoreCatalog.count()) grant(serverPlayer, "every_scrap_matters");
            } else {
                serverPlayer.sendSystemMessage(Component.literal("Duplicate archive copy. Existing PDA record retained.")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void grant(ServerPlayer player, String path) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(
                ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "campaign/" + path));
        if (advancement == null || player.getAdvancements().getOrStartProgress(advancement).isDone()) return;
        for (String criterion : advancement.getCriteria().keySet()) player.getAdvancements().award(advancement, criterion);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        AmbientLoreCatalog.Entry entry = AmbientLoreCatalog.byId(loreId(stack));
        if (entry == null) {
            tooltip.add(Component.literal("Unauthenticated physical record").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Right-click to inspect").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.literal(entry.classification()).withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal(entry.source()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Right-click to authenticate into the PDA archive").withStyle(ChatFormatting.DARK_AQUA));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
