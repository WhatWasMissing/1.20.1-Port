package matteroverdrive.menu;

import matteroverdrive.blockentity.ContractMarketBlockEntity;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class ContractMarketMenu extends AbstractContainerMenu {
    public static final int MARKET_SLOT_COUNT = ContractMarketBlockEntity.OFFER_SLOTS;
    private static final int PLAYER_SLOT_COUNT = 36;

    private final ContractMarketBlockEntity market;
    private final DataSlot refreshTicks;
    private final DataSlot occupiedSlots;

    public ContractMarketMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, find(inv, buf.readBlockPos()));
    }

    public ContractMarketMenu(int id, Inventory inv, ContractMarketBlockEntity market) {
        super(ModMenus.CONTRACT_MARKET.get(), id);
        this.market = market;

        // 18 real remove-only contract slots, arranged as the legacy-capacity 6x3 grid.
        for (int slot = 0; slot < MARKET_SLOT_COUNT; slot++) {
            int col = slot % 6;
            int row = slot / 6;
            addSlot(new SlotItemHandler(market.getOffers(), slot, 18 + col * 18, 42 + row * 18));
        }

        // Center the vanilla player inventory beneath the wider market operator panel.
        int playerX = 47;
        int playerY = 132;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, playerX + col * 18, playerY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, playerX + col * 18, playerY + 58));
        }

        refreshTicks = addDataSlot(new DataSlot() {
            private int clientValue;
            @Override public int get() {
                return inv.player.level().isClientSide ? clientValue : market.ticksUntilNextGeneration();
            }
            @Override public void set(int value) { clientValue = value; }
        });
        occupiedSlots = addDataSlot(new DataSlot() {
            private int clientValue;
            @Override public int get() {
                return inv.player.level().isClientSide ? clientValue : market.occupiedSlots();
            }
            @Override public void set(int value) { clientValue = value; }
        });
    }

    private static ContractMarketBlockEntity find(Inventory inv, BlockPos pos) {
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        return be instanceof ContractMarketBlockEntity market
                ? market
                : new ContractMarketBlockEntity(pos, ModBlocks.get("contract_market").get().defaultBlockState());
    }

    public int getRefreshTicks() {
        return Math.max(0, refreshTicks.get());
    }

    public int getOccupiedSlots() {
        return Math.max(0, occupiedSlots.get());
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), market.getBlockPos()), player,
                ModBlocks.get("contract_market").get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = getSlot(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        int playerStart = MARKET_SLOT_COUNT;
        int playerEnd = playerStart + PLAYER_SLOT_COUNT;

        if (index < MARKET_SLOT_COUNT) {
            // Market is remove-only: contracts may be shift-taken into player inventory.
            if (!moveItemStackTo(source, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Never shift-insert arbitrary player items/contracts back into the market.
            return ItemStack.EMPTY;
        }

        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, source);
        return original;
    }
}
