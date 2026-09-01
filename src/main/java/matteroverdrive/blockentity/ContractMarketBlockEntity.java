package matteroverdrive.blockentity;

import matteroverdrive.item.ContractItem;
import matteroverdrive.menu.ContractMarketMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class ContractMarketBlockEntity extends BlockEntity implements MenuProvider {
    private static final long REFRESH_DELAY = 1_200L;
    private boolean refreshing;
    private long refreshAt;
    private final ItemStackHandler offers = new ItemStackHandler(3) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
        @Override protected void onContentsChanged(int slot) {
            setChanged();
            if (!refreshing && level != null && !level.isClientSide && empty()) {
                refreshAt = level.getGameTime() + REFRESH_DELAY;
            }
        }
    };

    public ContractMarketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONTRACT_MARKET.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ContractMarketBlockEntity market) {
        if (market.empty() && level.getGameTime() >= market.refreshAt) market.refresh();
    }

    private boolean empty() {
        for (int i = 0; i < offers.getSlots(); i++) if (!offers.getStackInSlot(i).isEmpty()) return false;
        return true;
    }

    public void refresh() {
        refreshing = true;
        try {
            offers.setStackInSlot(0, ContractItem.collect("minecraft:iron_ingot", 16, "matteroverdrive:tritanium_dust", 8));
            offers.setStackInSlot(1, ContractItem.hunt("minecraft:zombie", 6, "matteroverdrive:dilithium_crystal", 1));
            offers.setStackInSlot(2, ContractItem.collect("minecraft:redstone", 32, "matteroverdrive:upgrade_base", 1));
        } finally {
            refreshing = false;
        }
        refreshAt = level == null ? 0 : level.getGameTime() + REFRESH_DELAY;
        setChanged();
    }

    public ItemStackHandler getOffers() { return offers; }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.contract_market"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        if (empty() && level != null && level.getGameTime() >= refreshAt) refresh();
        return new ContractMarketMenu(id, inv, this);
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Offers", offers.serializeNBT());
        tag.putLong("RefreshAt", refreshAt);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        refreshing = true;
        try {
            offers.deserializeNBT(tag.getCompound("Offers"));
        } finally {
            refreshing = false;
        }
        refreshAt = tag.getLong("RefreshAt");
    }
}
