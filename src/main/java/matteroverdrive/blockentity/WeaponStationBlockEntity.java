package matteroverdrive.blockentity;

import matteroverdrive.registry.ModBlockEntities;
import matteroverdrive.menu.WeaponStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class WeaponStationBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(7);
    public WeaponStationBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.WEAPON_STATION.get(), pos, state); }
    public ItemStackHandler getInventory() { return inventory; }
    @Override public Component getDisplayName() { return Component.literal("Weapon Station"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory player, Player owner) { return new WeaponStationMenu(id, player, this); }
}
