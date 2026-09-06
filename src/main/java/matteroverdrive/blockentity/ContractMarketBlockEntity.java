package matteroverdrive.blockentity;

import matteroverdrive.item.ContractItem;
import matteroverdrive.menu.ContractMarketMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

/**
 * Contract Market inventory/generation state.
 *
 * The authoritative 1.12 market used 18 remove-only contract slots and generated
 * a single weighted contract per cycle.  Its next-generation delay was
 * 36,000 ticks + 6,000 ticks for each occupied market slot.  The current port
 * keeps that market cadence while mapping the legacy weighted categories onto
 * contract types that the 1.20.1 ContractItem backend actually supports.
 */
public class ContractMarketBlockEntity extends BlockEntity implements MenuProvider {
    public static final int OFFER_SLOTS = 18;
    public static final int BASE_GENERATION_DELAY = 36_000;
    public static final int OCCUPIED_SLOT_DELAY = 6_000;

    private long nextGenerationTime;
    private boolean loading;

    private final ItemStackHandler offers = new ItemStackHandler(OFFER_SLOTS) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!loading) {
                setChanged();
            }
        }
    };

    public ContractMarketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTRACT_MARKET.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ContractMarketBlockEntity market) {
        if (market.nextGenerationTime <= 0L) {
            market.nextGenerationTime = level.getGameTime();
        }
        if (level.getGameTime() >= market.nextGenerationTime) {
            market.generateContract();
        }
    }

    private void generateContract() {
        if (level == null || level.isClientSide) {
            return;
        }

        int emptySlot = firstEmptySlot();
        if (emptySlot >= 0) {
            offers.setStackInSlot(emptySlot, createWeightedOffer(level.random));
        }

        scheduleNextGeneration(level.getGameTime());
        setChanged();
    }

    private ItemStack createWeightedOffer(RandomSource random) {
        // Legacy MatterOverdriveQuests.contractGeneration weights were 100/80/60.
        // Quest internals are intentionally mapped to real currently-supported
        // collect/hunt contracts rather than exposing non-functional legacy quest UI.
        int roll = random.nextInt(240);
        if (roll < 100) {
            return ContractItem.collect("minecraft:diamond", 2, "matteroverdrive:tritanium_dust", 8);
        }
        if (roll < 180) {
            return ContractItem.hunt("matteroverdrive:rogue_android", 3, "matteroverdrive:dilithium_crystal", 1);
        }
        return ContractItem.hunt("minecraft:enderman", 4, "matteroverdrive:upgrade_base", 1);
    }

    private void scheduleNextGeneration(long now) {
        nextGenerationTime = now + BASE_GENERATION_DELAY + (long) occupiedSlots() * OCCUPIED_SLOT_DELAY;
    }

    private int firstEmptySlot() {
        for (int slot = 0; slot < offers.getSlots(); slot++) {
            if (offers.getStackInSlot(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    public int occupiedSlots() {
        int occupied = 0;
        for (int slot = 0; slot < offers.getSlots(); slot++) {
            if (!offers.getStackInSlot(slot).isEmpty()) {
                occupied++;
            }
        }
        return occupied;
    }

    public int ticksUntilNextGeneration() {
        if (level == null || nextGenerationTime <= 0L) {
            return 0;
        }
        long remaining = Math.max(0L, nextGenerationTime - level.getGameTime());
        return (int) Math.min(Integer.MAX_VALUE, remaining);
    }

    public ItemStackHandler getOffers() {
        return offers;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.matteroverdrive.contract_market");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ContractMarketMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Offers", offers.serializeNBT());
        tag.putLong("NextGeneration", nextGenerationTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loading = true;
        try {
            for (int slot = 0; slot < offers.getSlots(); slot++) {
                offers.setStackInSlot(slot, ItemStack.EMPTY);
            }

            // Load through a temporary handler so old 3-slot saves cannot resize the
            // authoritative 18-slot market inventory during migration.
            ItemStackHandler loaded = new ItemStackHandler();
            loaded.deserializeNBT(tag.getCompound("Offers"));
            int copySlots = Math.min(OFFER_SLOTS, loaded.getSlots());
            for (int slot = 0; slot < copySlots; slot++) {
                offers.setStackInSlot(slot, loaded.getStackInSlot(slot));
            }
        } finally {
            loading = false;
        }

        if (tag.contains("NextGeneration")) {
            nextGenerationTime = tag.getLong("NextGeneration");
        } else {
            // Compatibility with the previous 1.20.1 three-offer implementation.
            nextGenerationTime = tag.getLong("RefreshAt");
        }
    }
}
