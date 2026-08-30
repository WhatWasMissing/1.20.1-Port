package matteroverdrive.matter;

import matteroverdrive.item.MatterDustItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class MatterValueRegistry {
    private static final Map<String, Integer> VALUES = new HashMap<>();

    static {
        register("minecraft:dirt", 1);
        register("minecraft:grass_block", 1);
        register("minecraft:cobblestone", 1);
        register("minecraft:stone", 1);
        register("minecraft:netherrack", 1);
        register("minecraft:sand", 2);
        register("minecraft:red_sand", 2);
        register("minecraft:gravel", 2);
        register("minecraft:sandstone", 4);
        register("minecraft:clay", 4);
        register("minecraft:cactus", 4);
        register("minecraft:end_stone", 6);
        register("minecraft:soul_sand", 4);
        register("minecraft:snow_block", 2);
        register("minecraft:pumpkin", 2);
        register("minecraft:obsidian", 16);
        register("minecraft:mycelium", 5);
        register("minecraft:ice", 3);
        register("minecraft:packed_ice", 4);
        register("minecraft:glass", 3);
        register("minecraft:glass_pane", 1);
        register("minecraft:sponge", 8);
        register("minecraft:vine", 1);
        register("minecraft:mossy_cobblestone", 2);

        register("minecraft:apple", 1);
        register("minecraft:arrow", 1);
        register("minecraft:baked_potato", 1);
        register("minecraft:beef", 2);
        register("minecraft:blaze_rod", 4);
        register("minecraft:bone", 2);
        register("minecraft:carrot", 1);
        register("minecraft:clay_ball", 1);
        register("minecraft:coal", 8);
        register("minecraft:charcoal", 5);
        register("minecraft:egg", 1);
        register("minecraft:ender_pearl", 8);
        register("minecraft:feather", 1);
        register("minecraft:fermented_spider_eye", 1);
        register("minecraft:flint", 1);
        register("minecraft:ghast_tear", 8);
        register("minecraft:gold_nugget", 4);
        register("minecraft:gunpowder", 2);
        register("minecraft:melon_slice", 1);
        register("minecraft:wheat", 1);
        register("minecraft:wheat_seeds", 1);
        register("minecraft:sugar", 1);
        register("minecraft:string", 1);
        register("minecraft:stick", 1);
        register("minecraft:redstone", 4);
        register("minecraft:glowstone_dust", 2);
        register("minecraft:spider_eye", 1);
        register("minecraft:saddle", 18);
        register("minecraft:sugar_cane", 1);
        register("minecraft:potato", 1);
        register("minecraft:leather", 3);
        register("minecraft:porkchop", 2);
        register("minecraft:cooked_porkchop", 4);
        register("minecraft:paper", 1);
        register("minecraft:nether_wart", 3);
        register("minecraft:nether_star", 1024);
        register("minecraft:experience_bottle", 32);
        register("minecraft:slime_ball", 2);
        register("minecraft:chicken", 2);
        register("minecraft:rabbit", 2);
        register("minecraft:mutton", 2);
        register("minecraft:cooked_chicken", 3);
        register("minecraft:rotten_flesh", 1);
        register("minecraft:name_tag", 32);
        register("minecraft:glass_bottle", 3);

        register("minecraft:diamond", 256);
        register("minecraft:quartz", 3);
        register("minecraft:lapis_lazuli", 4);
        register("minecraft:emerald", 256);
        register("minecraft:brick", 2);
        register("minecraft:iron_ingot", 32);
        register("minecraft:gold_ingot", 42);

        register("matteroverdrive:dilithium_crystal", 512);
        register("matteroverdrive:debug_matter_block", 1_000_000);
        register("matteroverdrive:tritanium_ingot", 128);
        register("matteroverdrive:tritanium_dust", 128);
        register("matteroverdrive:emergency_ration", 3);
        register("matteroverdrive:earl_gray_tea", 2);
        register("matteroverdrive:romulan_ale", 2);
        register("matteroverdrive:android_pill_red", 64);
        register("matteroverdrive:android_pill_blue", 32);
    }

    private MatterValueRegistry() {
    }

    private static void register(String id, int value) {
        VALUES.put(id, value);
    }

    public static int getMatter(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        if (stack.getItem() instanceof MatterDustItem) {
            return MatterDustItem.getMatter(stack);
        }

        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        Integer direct = VALUES.get(id);
        if (direct != null) {
            return direct;
        }

        if (stack.is(ItemTags.LOGS)) {
            return 16;
        }
        if (stack.is(ItemTags.PLANKS)) {
            return 4;
        }
        if (stack.is(ItemTags.LEAVES)) {
            return 1;
        }
        if (stack.is(ItemTags.WOOL)) {
            return 2;
        }
        if (stack.is(ItemTags.SAPLINGS)) {
            return 2;
        }

        return 0;
    }

    public static boolean containsMatter(ItemStack stack) {
        return getMatter(stack) > 0;
    }
}
