package matteroverdrive.item;

import matteroverdrive.android.AndroidData;
import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A structure-loot artifact that decodes into one of the Android passive
 * protocols. It is deliberately server-authoritative: clients only receive
 * the resulting item NBT and the normal Android state packet.
 */
public final class RecoveredArtifactItem extends Item {
    private static final String PROTOCOL_TAG = "RecoveredProtocol";

    public RecoveredArtifactItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    /** Creates a server-authored recovered protocol with a stable identity for encounter loot. */
    public static ItemStack recovered(AndroidLoadout.Artifact artifact) {
        ItemStack stack = new ItemStack(matteroverdrive.registry.ModItems.get("artifact").get());
        if (artifact != null && artifact != AndroidLoadout.Artifact.NONE) {
            stack.getOrCreateTag().putInt(PROTOCOL_TAG, artifact.ordinal());
        }
        return stack;
    }

    @Nullable
    private static AndroidLoadout.Artifact protocol(ItemStack stack) {
        if (!stack.hasTag()) return null;
        int ordinal = stack.getTag().getInt(PROTOCOL_TAG);
        AndroidLoadout.Artifact[] values = AndroidLoadout.Artifact.values();
        return ordinal > 0 && ordinal < values.length ? values[ordinal] : null;
    }

    private static AndroidLoadout.Artifact decode(Level level, ItemStack stack) {
        AndroidLoadout.Artifact known = protocol(stack);
        if (known != null) return known;
        AndroidLoadout.Artifact[] values = AndroidLoadout.Artifact.values();
        AndroidLoadout.Artifact decoded = values[1 + level.random.nextInt(values.length - 1)];
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(PROTOCOL_TAG, decoded.ordinal());
        return decoded;
    }

    @Override
    public Component getName(ItemStack stack) {
        AndroidLoadout.Artifact decoded = protocol(stack);
        return decoded == null
                ? Component.translatable("item.matteroverdrive.artifact")
                : Component.literal("Recovered " + decoded.displayName);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (!(player instanceof ServerPlayer server) || !AndroidData.isAndroid(server)) {
            player.displayClientMessage(Component.literal("Artifact decoding requires an Android neural interface.")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        AndroidLoadout.Artifact decoded = decode(level, stack);
        AndroidLoadout.selectArtifact(server, decoded);
        if (!server.getAbilities().instabuild) stack.shrink(1);
        server.displayClientMessage(Component.literal("Recovered protocol integrated: " + decoded.displayName)
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        level.playSound(null, server.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.7F, 1.15F);
        ModNetwork.syncAndroidState(server);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        AndroidLoadout.Artifact decoded = protocol(stack);
        if (decoded == null) {
            lines.add(Component.literal("Unstable recovered signal; decode with an Android neural interface.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            lines.add(Component.literal("Protocol: " + decoded.displayName).withStyle(ChatFormatting.AQUA));
            lines.add(Component.literal(decoded.description).withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.literal("Right-click to integrate this one-use passive protocol.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
