package matteroverdrive.item;

import matteroverdrive.android.AndroidData;
import matteroverdrive.android.AndroidLoadout;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Legendary structure relic that installs one of the existing Android passive protocols. */
public final class ArtifactItem extends Item {
    public static final String RELIC_ID = "RelicId";

    public ArtifactItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Nullable
    public static AndroidLoadout.Artifact relic(ItemStack stack) {
        if (!stack.hasTag()) return null;
        String id = stack.getTag().getString(RELIC_ID);
        if (id.isBlank()) return null;
        try {
            AndroidLoadout.Artifact artifact = AndroidLoadout.Artifact.valueOf(id);
            return artifact == AndroidLoadout.Artifact.NONE ? null : artifact;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        AndroidLoadout.Artifact relic = relic(stack);
        return Component.literal(relic == null ? "Unknown Artifact" : "Legendary Relic: " + relic.displayName);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return relic(stack) != null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        AndroidLoadout.Artifact relic = relic(stack);
        if (relic == null) {
            if (!level.isClientSide) player.displayClientMessage(
                    Component.literal("This artifact has no stable relic signature.").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!AndroidData.isAndroid(player)) {
            if (!level.isClientSide) player.displayClientMessage(
                    Component.literal("Legendary relics require Android conversion before installation.")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (AndroidLoadout.hasArtifact(player, relic)) {
            player.displayClientMessage(Component.literal(relic.displayName + " is already installed.")
                    .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.success(stack);
        }
        AndroidLoadout.selectArtifact(player, relic);
        stack.shrink(1);
        player.displayClientMessage(Component.literal("Installed legendary relic: " + relic.displayName)
                .withStyle(ChatFormatting.GOLD), true);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        AndroidLoadout.Artifact relic = relic(stack);
        tooltip.add(Component.literal("LEGENDARY RELIC").withStyle(ChatFormatting.GOLD));
        if (relic == null) {
            tooltip.add(Component.literal("Unstable relic signature").withStyle(ChatFormatting.RED));
            return;
        }
        tooltip.add(Component.literal(relic.description).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click as an Android to install this passive protocol.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
