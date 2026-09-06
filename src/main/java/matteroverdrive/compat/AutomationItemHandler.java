package matteroverdrive.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.function.IntPredicate;

/**
 * Capability-facing view of a machine inventory.
 *
 * Internal GUIs and machine logic keep using their full handler, while external
 * automation (including AE2 Storage/Import/Export buses) only receives the slots
 * and operations the machine is intended to expose.
 */
public final class AutomationItemHandler implements IItemHandler {
    private final IItemHandler delegate;
    private final IntPredicate canInsert;
    private final IntPredicate canExtract;

    public AutomationItemHandler(IItemHandler delegate, IntPredicate canInsert, IntPredicate canExtract) {
        this.delegate = delegate;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!isValidSlot(slot) || !canInsert.test(slot)) return stack;
        return delegate.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!isValidSlot(slot) || !canExtract.test(slot)) return ItemStack.EMPTY;
        return delegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return isValidSlot(slot) ? delegate.getSlotLimit(slot) : 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return isValidSlot(slot) && canInsert.test(slot) && delegate.isItemValid(slot, stack);
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < delegate.getSlots();
    }
}
