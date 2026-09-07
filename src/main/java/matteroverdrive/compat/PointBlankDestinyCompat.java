package matteroverdrive.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Zero-dependency bridge for Vic's Point Blank Destiny content pack.
 *
 * The content pack registers its items in the pointblank namespace. Matter Overdrive
 * only looks them up after registries exist, so this class is safe when Point Blank
 * or the Destiny pack is not installed.
 */
public final class PointBlankDestinyCompat {
    public static final String NAMESPACE = "pointblank";

    public record Weapon(String id, String displayName, int matterValue) {
        public ResourceLocation location() {
            return new ResourceLocation(NAMESPACE, id);
        }
    }

    private static final List<Weapon> WEAPONS = List.of(
            new Weapon("destiny_khvostov7g02", "Khvostov 7G-02", 384),
            new Weapon("destiny_marshala1", "Marshal-A1", 512),
            new Weapon("destiny_traxcallum1", "Trax Callum 1", 512),
            new Weapon("destiny_proximacentauriii", "Proxima Centauri II", 768),
            new Weapon("destiny_hawkmoon", "Hawkmoon", 2048),
            new Weapon("destiny_eyasluna", "Eyasluna", 768),
            new Weapon("destiny_thelastword", "The Last Word", 2048),
            new Weapon("destiny_aceofspades", "Ace Of Spades", 2048),
            new Weapon("destiny_thorn", "Thorn", 2304),
            new Weapon("destiny_surosregime", "SUROS Regime", 2048),
            new Weapon("destiny_montecarlo", "Monte Carlo", 2048),
            new Weapon("destiny_midamultitool", "MIDA Multi-Tool", 2048),
            new Weapon("destiny_chaosdogma", "CHAOS DOGMA~", 768),
            new Weapon("destiny_sleepersimulant", "Sleeper Simulant", 4096)
    );

    private PointBlankDestinyCompat() {}

    public static List<Weapon> weapons() {
        return Collections.unmodifiableList(WEAPONS);
    }

    public static List<Item> availableWeapons() {
        List<Item> available = new ArrayList<>();
        for (Weapon weapon : WEAPONS) {
            Item item = ForgeRegistries.ITEMS.getValue(weapon.location());
            if (item != null && item != Items.AIR) available.add(item);
        }
        return available;
    }

    public static boolean isInstalled() {
        return !availableWeapons().isEmpty();
    }

    public static boolean isDestinyWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !NAMESPACE.equals(id.getNamespace())) return false;
        for (Weapon weapon : WEAPONS) {
            if (weapon.id.equals(id.getPath())) return true;
        }
        return false;
    }

    public static Weapon definition(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !NAMESPACE.equals(id.getNamespace())) return null;
        for (Weapon weapon : WEAPONS) {
            if (weapon.id.equals(id.getPath())) return weapon;
        }
        return null;
    }

    public static int matterValue(ResourceLocation id) {
        if (id == null || !NAMESPACE.equals(id.getNamespace())) return 0;
        for (Weapon weapon : WEAPONS) {
            if (weapon.id.equals(id.getPath())) return weapon.matterValue;
        }
        return 0;
    }
}
