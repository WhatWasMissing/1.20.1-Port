package matteroverdrive.item;

import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.matter.MatterValueRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PortableDecomposerItem extends Item {
    public static final int ENERGY_CAPACITY = 128000;
    public static final int ENERGY_RECEIVE = 256;
    public static final int MATTER_CAPACITY = 512;
    public static final double MATTER_RATIO = 0.1D;

    private static final String ENERGY_TAG = "PortableDecomposerEnergy";
    private static final String MATTER_TAG = "PortableDecomposerMatter";
    private static final String REMAINDER_TAG = "PortableDecomposerRemainder";
    private static final String FILTERS_TAG = "PortableDecomposerFilters";

    public PortableDecomposerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new Provider(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack decomposer = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack filter = player.getItemInHand(otherHand);

        if (player.isShiftKeyDown() && !filter.isEmpty() && filter.getItem() != this) {
            if (!level.isClientSide) {
                boolean added = toggleFilter(decomposer, filter);
                player.sendSystemMessage(Component.literal((added ? "Added " : "Removed ")
                        + filter.getHoverName().getString() + (added ? " to" : " from")
                        + " Portable Decomposer filter.")
                        .withStyle(added ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
                sync(player);
            }
            return InteractionResultHolder.sidedSuccess(decomposer, level.isClientSide);
        }

        if (!level.isClientSide) {
            player.sendSystemMessage(Component.literal("Portable Decomposer: "
                    + getEnergy(decomposer) + "/" + ENERGY_CAPACITY + " FE, "
                    + getMatter(decomposer) + "/" + MATTER_CAPACITY + " kM, "
                    + getFilters(decomposer).size() + " filtered item(s).")
                    .withStyle(ChatFormatting.AQUA));
            if (filter.isEmpty()) {
                player.sendSystemMessage(Component.literal(
                        "Sneak-right-click with a matter-valued item in the other hand to toggle its pickup filter.")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        return InteractionResultHolder.sidedSuccess(decomposer, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity targetEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (targetEntity == null) {
            return InteractionResult.PASS;
        }

        LazyOptional<IMatterStorage> targetOptional =
                targetEntity.getCapability(ModCapabilities.MATTER, context.getClickedFace());
        if (!targetOptional.isPresent()) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack decomposer = context.getItemInHand();
        int available = getMatter(decomposer);
        if (available <= 0) {
            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(Component.literal("Portable Decomposer contains no matter.")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.CONSUME;
        }

        IMatterStorage target = targetOptional.orElseThrow(IllegalStateException::new);
        if (!target.canReceive()) {
            return InteractionResult.CONSUME;
        }

        int accepted = target.receiveMatter(available, true);
        int moved = accepted <= 0 ? 0 : target.receiveMatter(accepted, false);
        if (moved > 0) {
            setMatter(decomposer, available - moved);
            targetEntity.setChanged();
            context.getLevel().sendBlockUpdated(targetEntity.getBlockPos(),
                    targetEntity.getBlockState(), targetEntity.getBlockState(), 3);
            if (context.getPlayer() != null) {
                sync(context.getPlayer());
                context.getPlayer().sendSystemMessage(Component.literal(
                        "Transferred " + moved + " kM; " + getMatter(decomposer) + " kM remains.")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
        return InteractionResult.CONSUME;
    }

    public int decomposePickup(ItemStack decomposer, ItemStack pickup) {
        if (pickup.isEmpty() || !isFiltered(decomposer, pickup)) {
            return 0;
        }

        int matterValue = MatterValueRegistry.getMatter(pickup);
        int energy = getEnergy(decomposer);
        int freeMatter = MATTER_CAPACITY - getMatter(decomposer);
        if (matterValue <= 0 || energy < matterValue || freeMatter <= 0) {
            return 0;
        }

        double remainder = Math.max(0.0D, Math.min(0.999999D,
                decomposer.getOrCreateTag().getDouble(REMAINDER_TAG)));
        int consumed = 0;
        int produced = 0;
        int remainingEnergy = energy;
        int remainingSpace = freeMatter;

        while (consumed < pickup.getCount() && remainingEnergy >= matterValue) {
            double rawYield = remainder + matterValue * MATTER_RATIO;
            int wholeYield = (int) Math.floor(rawYield);
            if (wholeYield > remainingSpace) {
                break;
            }
            remainder = rawYield - wholeYield;
            produced += wholeYield;
            remainingSpace -= wholeYield;
            remainingEnergy -= matterValue;
            consumed++;
        }

        if (consumed <= 0) {
            return 0;
        }

        pickup.shrink(consumed);
        setEnergy(decomposer, remainingEnergy);
        setMatter(decomposer, getMatter(decomposer) + produced);
        decomposer.getOrCreateTag().putDouble(REMAINDER_TAG, remainder);
        return consumed;
    }

    private static boolean toggleFilter(ItemStack decomposer, ItemStack candidate) {
        ListTag filters = decomposer.getOrCreateTag().getList(FILTERS_TAG, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < filters.size(); i++) {
            ItemStack stored = ItemStack.of(filters.getCompound(i));
            if (ItemStack.isSameItemSameTags(stored, candidate)) {
                filters.remove(i);
                decomposer.getOrCreateTag().put(FILTERS_TAG, filters);
                return false;
            }
        }

        ItemStack single = candidate.copy();
        single.setCount(1);
        CompoundTag saved = new CompoundTag();
        single.save(saved);
        filters.add(saved);
        decomposer.getOrCreateTag().put(FILTERS_TAG, filters);
        return true;
    }

    private static boolean isFiltered(ItemStack decomposer, ItemStack candidate) {
        for (ItemStack filter : getFilters(decomposer)) {
            if (ItemStack.isSameItemSameTags(filter, candidate)) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> getFilters(ItemStack decomposer) {
        List<ItemStack> result = new ArrayList<>();
        CompoundTag root = decomposer.getTag();
        if (root == null) {
            return result;
        }
        ListTag filters = root.getList(FILTERS_TAG, CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < filters.size(); i++) {
            ItemStack stored = ItemStack.of(filters.getCompound(i));
            if (!stored.isEmpty()) {
                result.add(stored);
            }
        }
        return result;
    }

    public static int getEnergy(ItemStack stack) {
        return Math.max(0, Math.min(ENERGY_CAPACITY,
                stack.getOrCreateTag().getInt(ENERGY_TAG)));
    }

    private static void setEnergy(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt(ENERGY_TAG,
                Math.max(0, Math.min(ENERGY_CAPACITY, amount)));
    }

    public static int getMatter(ItemStack stack) {
        return Math.max(0, Math.min(MATTER_CAPACITY,
                stack.getOrCreateTag().getInt(MATTER_TAG)));
    }

    private static void setMatter(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt(MATTER_TAG,
                Math.max(0, Math.min(MATTER_CAPACITY, amount)));
    }

    private static void sync(Player player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getEnergy(stack) / ENERGY_CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x33CCFF;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(getEnergy(stack) + " / " + ENERGY_CAPACITY + " FE")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal(getMatter(stack) + " / " + MATTER_CAPACITY + " kM")
                .withStyle(ChatFormatting.AQUA));
        List<ItemStack> filters = getFilters(stack);
        tooltip.add(Component.literal("Pickup filters: " + filters.size())
                .withStyle(ChatFormatting.GRAY));
        for (int i = 0; i < filters.size() && i < 5; i++) {
            tooltip.add(Component.literal(" - ").append(filters.get(i).getHoverName())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (filters.size() > 5) {
            tooltip.add(Component.literal(" - and " + (filters.size() - 5) + " more")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private static final class Provider implements ICapabilityProvider {
        private final LazyOptional<IEnergyStorage> energy;
        private final LazyOptional<IMatterStorage> matter;

        private Provider(ItemStack stack) {
            energy = LazyOptional.of(() -> new StackEnergyStorage(stack));
            matter = LazyOptional.of(() -> new StackMatterStorage(stack));
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeCapabilities.ENERGY) {
                return energy.cast();
            }
            if (cap == ModCapabilities.MATTER) {
                return matter.cast();
            }
            return LazyOptional.empty();
        }
    }

    private static final class StackEnergyStorage implements IEnergyStorage {
        private final ItemStack stack;

        private StackEnergyStorage(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int accepted = Math.min(Math.max(0, maxReceive),
                    Math.min(ENERGY_RECEIVE, ENERGY_CAPACITY - getEnergyStored()));
            if (!simulate && accepted > 0) {
                setEnergy(stack, getEnergyStored() + accepted);
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override public int getEnergyStored() { return getEnergy(stack); }
        @Override public int getMaxEnergyStored() { return ENERGY_CAPACITY; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }

    private static final class StackMatterStorage implements IMatterStorage {
        private final ItemStack stack;

        private StackMatterStorage(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int receiveMatter(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractMatter(int maxExtract, boolean simulate) {
            int extracted = Math.min(Math.max(0, maxExtract), getMatterStored());
            if (!simulate && extracted > 0) {
                setMatter(stack, getMatterStored() - extracted);
            }
            return extracted;
        }

        @Override public int getMatterStored() { return getMatter(stack); }
        @Override public int getMatterCapacity() { return MATTER_CAPACITY; }
        @Override public boolean canReceive() { return false; }
        @Override public boolean canExtract() { return true; }
    }
}
