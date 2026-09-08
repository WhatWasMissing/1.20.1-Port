package matteroverdrive.item;

import matteroverdrive.android.AndroidChassisData;
import matteroverdrive.android.AndroidData;
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

public class AndroidChassisModuleItem extends Item {
    private final AndroidChassisData.Module module;

    public AndroidChassisModuleItem(Properties properties, AndroidChassisData.Module module) {
        super(properties.stacksTo(1));
        this.module = module;
    }

    public AndroidChassisData.Module module() { return module; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!AndroidData.isAndroid(player)) {
            if (!level.isClientSide) player.displayClientMessage(Component.literal("Android chassis modules require conversion first.").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(held);
        }
        if (level.isClientSide) return InteractionResultHolder.success(held);

        AndroidChassisData.Module current = AndroidChassisData.get(player, module.slot);
        if (current == module) {
            player.displayClientMessage(Component.literal(module.displayName + " is already installed.").withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.success(held);
        }

        AndroidChassisData.Module old = AndroidChassisData.install(player, module);
        held.shrink(1);
        if (old != null) {
            ItemStack returned = AndroidChassisData.stack(old);
            if (!player.getInventory().add(returned)) player.drop(returned, false);
        }
        player.displayClientMessage(Component.literal("Installed " + module.displayName + " in " + module.slot.name().toLowerCase() + " slot.").withStyle(ChatFormatting.AQUA), true);
        return InteractionResultHolder.success(held);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Android Chassis: " + module.slot.name()).withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal(description(module)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Right-click to install; an existing module in this slot is returned.").withStyle(ChatFormatting.DARK_GRAY));
    }

    private static String description(AndroidChassisData.Module module) {
        return switch (module) {
            case CAPACITOR_CORE -> "High-efficiency reserve core that slowly recovers FE while powered.";
            case OVERCLOCK_CORE -> "Aggressive core tuning: stronger combat output at a continuous FE cost.";
            case LIGHTWEIGHT_FRAME -> "Faster movement and lower mass, but slightly less protection.";
            case REINFORCED_FRAME -> "Tritanium reinforcement substantially reduces incoming damage.";
            case AGILITY_MUSCLES -> "Fast-response synthetic myomers improve movement speed.";
            case SIEGE_MUSCLES -> "Heavy myomers improve attack output at a small mobility cost.";
            case HUNTER_OPTICS -> "Continuously identifies nearby hostile targets while energy is available.";
            case PRECISION_OPTICS -> "Target-processing package increases outgoing combat damage.";
            case STEALTH_SHELL -> "Crouching can suppress your visible signature at a small FE cost.";
            case REACTIVE_SHELL -> "Emergency armour hardens automatically below half health.";
        };
    }
}