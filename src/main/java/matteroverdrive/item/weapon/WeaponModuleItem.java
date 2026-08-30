package matteroverdrive.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class WeaponModuleItem extends Item {
    public enum SlotType {
        COLOR,
        BARREL,
        SIGHTS,
        OTHER
    }

    public enum Effect {
        COLOR,
        DAMAGE,
        FIRE,
        EXPLOSION,
        HEAL,
        DOOMSDAY,
        BLOCK,
        HOLO_SIGHTS,
        SNIPER_SCOPE,
        RICOCHET
    }

    private final SlotType slotType;
    private final Effect effect;
    private final int color;

    public WeaponModuleItem(Properties properties, SlotType slotType, Effect effect) {
        this(properties, slotType, effect, 0x66CCFF);
    }

    public WeaponModuleItem(Properties properties, SlotType slotType, Effect effect, int color) {
        super(properties.stacksTo(1));
        this.slotType = slotType;
        this.effect = effect;
        this.color = color;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public Effect getEffect() {
        return effect;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Module slot: " + slotType.name().toLowerCase()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(description()).withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private String description() {
        return switch (effect) {
            case DAMAGE -> "+50% damage, -50% energy use";
            case FIRE -> "Ignites targets, -25% damage";
            case EXPLOSION -> "Explosive impacts, much faster fire cycle";
            case HEAL -> "Converts shots into healing energy";
            case DOOMSDAY -> "Large explosive impacts";
            case BLOCK -> "Breaks vulnerable blocks instead of damaging entities";
            case HOLO_SIGHTS -> "Improves aimed and hip-fire accuracy";
            case SNIPER_SCOPE -> "+50% range and improved aimed accuracy";
            case RICOCHET -> "Shots rebound once from solid blocks";
            case COLOR -> String.format("Beam colour: #%06X", color & 0xFFFFFF);
        };
    }
}
