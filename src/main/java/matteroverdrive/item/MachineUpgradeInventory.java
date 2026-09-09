package matteroverdrive.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class MachineUpgradeInventory extends ItemStackHandler {
    private final Predicate<MachineUpgradeItem.Upgrade> supported;
    private final Runnable changeListener;

    public MachineUpgradeInventory(int slots, Predicate<MachineUpgradeItem.Upgrade> supported, Runnable changeListener) {
        super(slots);
        this.supported = supported;
        this.changeListener = changeListener;
    }

    @Override public boolean isItemValid(int slot, ItemStack stack) { return stack.getItem() instanceof MachineUpgradeItem item && supported.test(item.getUpgrade()); }
    @Override public int getSlotLimit(int slot) { return 1; }
    @Override protected void onContentsChanged(int slot) { if (changeListener != null) changeListener.run(); }

    public double getMultiplier(ToDoubleFunction<MachineUpgradeItem.Upgrade> effect) {
        double multiplier = 1.0D;
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack.getItem() instanceof MachineUpgradeItem item && supported.test(item.getUpgrade())) multiplier *= effect.applyAsDouble(item.getUpgrade());
        }
        return multiplier;
    }

    public int count(MachineUpgradeItem.Upgrade wanted) {
        int count = 0;
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack.getItem() instanceof MachineUpgradeItem item && item.getUpgrade() == wanted && supported.test(wanted)) count++;
        }
        return count;
    }
}