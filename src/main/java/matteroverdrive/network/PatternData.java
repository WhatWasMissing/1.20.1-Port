package matteroverdrive.network;

import net.minecraft.world.item.ItemStack;

public record PatternData(ItemStack stack, int matter, int progress) {
    public PatternData {
        ItemStack copy = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!copy.isEmpty()) {
            copy.setCount(1);
        }
        stack = copy;
        matter = Math.max(0, matter);
        progress = Math.max(0, Math.min(100, progress));
    }

    public PatternData copy() {
        return new PatternData(stack.copy(), matter, progress);
    }

    public boolean matches(ItemStack candidate) {
        if (stack.isEmpty() || candidate == null || candidate.isEmpty()) {
            return false;
        }
        ItemStack one = candidate.copy();
        one.setCount(1);
        return ItemStack.isSameItemSameTags(stack, one);
    }
}
