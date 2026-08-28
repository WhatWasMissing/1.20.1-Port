package matteroverdrive.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

public class WeaponBatteryItem extends Item {
    private static final String ENERGY_TAG = "MatterOverdriveEnergy";

    private final int capacity;
    private final int input;
    private final int output;

    public WeaponBatteryItem(Properties properties, int capacity, int input, int output) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
        this.input = input;
        this.output = output;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new Provider(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getEnergy(stack) / Math.max(1.0F, capacity));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x33CCFF;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(getEnergy(stack) + " / " + capacity + " FE").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Weapon battery module").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private int getEnergy(ItemStack stack) {
        return Math.min(capacity, Math.max(0, stack.getOrCreateTag().getInt(ENERGY_TAG)));
    }

    private final class Provider implements ICapabilityProvider {
        private final LazyOptional<IEnergyStorage> energy;

        private Provider(ItemStack stack) {
            energy = LazyOptional.of(() -> new StackEnergyStorage(stack));
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            return cap == ForgeCapabilities.ENERGY ? energy.cast() : LazyOptional.empty();
        }
    }

    private final class StackEnergyStorage implements IEnergyStorage {
        private final ItemStack stack;

        private StackEnergyStorage(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int accepted = Math.min(Math.max(0, maxReceive), Math.min(input, capacity - getEnergyStored()));
            if (!simulate && accepted > 0) {
                stack.getOrCreateTag().putInt(ENERGY_TAG, getEnergyStored() + accepted);
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = Math.min(Math.max(0, maxExtract), Math.min(output, getEnergyStored()));
            if (!simulate && extracted > 0) {
                stack.getOrCreateTag().putInt(ENERGY_TAG, getEnergyStored() - extracted);
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return getEnergy(stack);
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return output > 0;
        }

        @Override
        public boolean canReceive() {
            return input > 0;
        }
    }
}
