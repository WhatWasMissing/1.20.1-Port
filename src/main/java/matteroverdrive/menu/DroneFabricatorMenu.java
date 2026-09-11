package matteroverdrive.menu;

import matteroverdrive.blockentity.DroneFabricatorBlockEntity;
import matteroverdrive.item.MachineUpgradeItem;
import matteroverdrive.machine.MachineRedstoneMode;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

/** Inventory and button bridge for the server-authoritative Drone Fabricator. */
public final class DroneFabricatorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = DroneFabricatorBlockEntity.SLOT_COUNT + DroneFabricatorBlockEntity.UPGRADE_SLOT_COUNT, PLAYER_START = MACHINE_SLOTS, PLAYER_END = PLAYER_START + 27, HOTBAR_END = PLAYER_END + 9;
    private final DroneFabricatorBlockEntity fabricator;
    private final ContainerData data;
    public DroneFabricatorMenu(int id, Inventory playerInventory, FriendlyByteBuf buffer) { this(id, playerInventory, find(playerInventory, buffer.readBlockPos()), new SimpleContainerData(DroneFabricatorBlockEntity.DATA_COUNT)); }
    public DroneFabricatorMenu(int id, Inventory playerInventory, DroneFabricatorBlockEntity fabricator) { this(id, playerInventory, fabricator, fabricator.getContainerData()); }
    private DroneFabricatorMenu(int id, Inventory playerInventory, DroneFabricatorBlockEntity fabricator, ContainerData data) {
        super(ModMenus.DRONE_FABRICATOR.get(), id); this.fabricator = fabricator; this.data = data;
        addSlot(new SlotItemHandler(fabricator.getInventory(), DroneFabricatorBlockEntity.SLOT_PLASMA_CORE, 27, 42));
        addSlot(new SlotItemHandler(fabricator.getInventory(), DroneFabricatorBlockEntity.SLOT_CIRCUITS, 54, 42));
        addSlot(new SlotItemHandler(fabricator.getInventory(), DroneFabricatorBlockEntity.SLOT_PLATES, 81, 42));
        addSlot(new SlotItemHandler(fabricator.getInventory(), DroneFabricatorBlockEntity.SLOT_OUTPUT, 135, 42) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
        for (int slot = 0; slot < DroneFabricatorBlockEntity.UPGRADE_SLOT_COUNT; slot++) addSlot(new SlotItemHandler(fabricator.getUpgrades(), slot, 108 + slot * 18, 58));
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 112 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(playerInventory, column, 8 + column * 18, 170));
        addDataSlots(data);
    }
    private static DroneFabricatorBlockEntity find(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DroneFabricatorBlockEntity fabricator) return fabricator;
        throw new IllegalStateException("Drone Fabricator missing at " + pos);
    }
    @Override public boolean stillValid(Player player) { return stillValid(ContainerLevelAccess.create(fabricator.getLevel(), fabricator.getBlockPos()), player, ModBlocks.get("drone_fabricator").get()); }
    @Override public boolean clickMenuButton(Player player, int id) {
        if (id == 1 && player instanceof net.minecraft.server.level.ServerPlayer server) {
            if (!fabricator.cycleSelectedRole(server)) {
                player.displayClientMessage(Component.literal("No unlocked advanced drone template is available."), true);
                return false;
            }
            player.displayClientMessage(Component.literal("Fabricating: " + roleName(fabricator.selectedRole())), true);
            return true;
        }
        if (id == 2) { player.displayClientMessage(Component.literal("Redstone mode: " + MachineRedstoneMode.label(fabricator.cycleRedstoneMode())), true); return true; }
        if (id == 3) { player.displayClientMessage(Component.literal("Item network channel: " + fabricator.cycleNetworkChannel()), true); return true; }
        return false;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem(), copy = source.copy(); boolean moved;
        if (index < MACHINE_SLOTS) moved = moveItemStackTo(source, PLAYER_START, HOTBAR_END, true);
        else {
            moved = false;
            if (source.getItem() instanceof MachineUpgradeItem) moved = moveItemStackTo(source, DroneFabricatorBlockEntity.SLOT_COUNT, MACHINE_SLOTS, false);
            if (!moved) moved = moveItemStackTo(source, 0, DroneFabricatorBlockEntity.SLOT_OUTPUT, false) || (index < PLAYER_END ? moveItemStackTo(source, PLAYER_END, HOTBAR_END, false) : moveItemStackTo(source, PLAYER_START, PLAYER_END, false));
        }
        if (!moved) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged(); slot.onTake(player, source); return copy;
    }
    public int energy() { return combine(data.get(0), data.get(1)); }
    public int capacity() { return combine(data.get(2), data.get(3)); }
    public int progress() { return data.get(4); }
    public int maxProgress() { return data.get(5); }
    public boolean running() { return data.get(6) != 0; }
    public int role() { return data.get(7); }
    public int completedCores() { return data.get(8); }
    public String redstoneModeLabel() { return MachineRedstoneMode.label(data.get(9)); }
    public int plasmaCount() { return data.get(10); }
    public int circuitCount() { return data.get(11); }
    public int plateCount() { return data.get(12); }
    public int networkChannel() { return data.get(13) & 15; }
    public boolean recipeReady() { return plasmaCount() >= 1 && circuitCount() >= 2 && plateCount() >= 4; }
    public int fabricationTicks() { return data.get(5); }
    public static String roleName(int role) { return switch (role) { case 1 -> "REPAIR"; case 2 -> "LOGISTICS"; case 3 -> "SURVEY"; case 4 -> "REACTOR"; default -> "COMBAT"; }; }
    private static int combine(int low, int high) { return (low & 0xffff) | ((high & 0xffff) << 16); }
}
