package matteroverdrive.item;

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

public class AndroidPillItem extends Item {
    public enum Type { BLUE, RED, YELLOW }
    private final Type type;

    public AndroidPillItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            boolean used = switch (type) {
                case BLUE -> activate(player);
                case YELLOW -> recharge(player);
                case RED -> deactivate(player);
            };
            if (!used) return InteractionResultHolder.fail(stack);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private boolean activate(Player player) {
        if (AndroidData.isAndroid(player)) {
            player.displayClientMessage(Component.literal("Android conversion already active."), true);
            return false;
        }
        AndroidData.activate(player);
        player.displayClientMessage(Component.literal("Android conversion complete. Install bionic parts at an Android Station."), true);
        return true;
    }

    private boolean recharge(Player player) {
        if (!AndroidData.isAndroid(player)) {
            player.displayClientMessage(Component.literal("Only an Android can use this recharge pill."), true);
            return false;
        }
        int received = AndroidData.receiveEnergy(player, 25_000);
        if (received == 0) {
            player.displayClientMessage(Component.literal("Android energy is already full."), true);
            return false;
        }
        player.displayClientMessage(Component.literal("Android energy restored: +" + received + " FE"), true);
        return true;
    }

    private boolean deactivate(Player player) {
        if (!AndroidData.isAndroid(player)) {
            player.displayClientMessage(Component.literal("Android conversion is not active."), true);
            return false;
        }
        int parts = AndroidData.deactivate(player);
        for (AndroidData.Part part : AndroidData.Part.values()) {
            if ((parts & part.bit) != 0 && !player.getInventory().add(AndroidData.partStack(part))) {
                player.drop(AndroidData.partStack(part), false);
            }
        }
        player.displayClientMessage(Component.literal("Android conversion removed; installed parts were returned."), true);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        switch (type) {
            case BLUE -> tooltip.add(Component.literal("Activates Android conversion.").withStyle(ChatFormatting.AQUA));
            case YELLOW -> tooltip.add(Component.literal("Restores 25,000 Android FE.").withStyle(ChatFormatting.YELLOW));
            case RED -> tooltip.add(Component.literal("Removes conversion and returns installed parts.").withStyle(ChatFormatting.RED));
        }
    }
}