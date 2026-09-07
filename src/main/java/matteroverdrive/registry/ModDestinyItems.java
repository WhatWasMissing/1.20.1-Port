package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.NativeDestinyWeaponItem;
import matteroverdrive.item.weapon.NativeDestinyWeaponProfile;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModDestinyItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MatterOverdrive.MOD_ID);
    private static final Map<String, RegistryObject<Item>> WEAPONS_MUTABLE = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> WEAPONS = java.util.Collections.unmodifiableMap(WEAPONS_MUTABLE);

    static {
        for (NativeDestinyWeaponProfile profile : NativeDestinyWeaponProfile.values()) {
            WEAPONS_MUTABLE.put(profile.id(), ITEMS.register(profile.id(),
                    () -> new NativeDestinyWeaponItem(new Item.Properties().stacksTo(1), profile)));
        }
    }

    private ModDestinyItems() {}
}
