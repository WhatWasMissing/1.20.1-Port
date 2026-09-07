package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.VexMythoclastItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModExoticItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MatterOverdrive.MOD_ID);
    public static final RegistryObject<Item> VEX_MYTHOCLAST = ITEMS.register("vex_mythoclast",
            () -> new VexMythoclastItem(new Item.Properties().stacksTo(1)));

    private ModExoticItems() {}
}
