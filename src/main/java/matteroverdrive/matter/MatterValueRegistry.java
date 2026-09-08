package matteroverdrive.matter;

import matteroverdrive.item.MatterDustItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MatterValueRegistry {
    public enum ValueSource {
        DYNAMIC,
        EXPLICIT,
        TAG_BASE,
        RECIPE,
        FALLBACK,
        NONE
    }

    public record MatterValue(int value, ValueSource source) {
        public boolean hasMatter() {
            return value > 0;
        }
    }

    private static final Map<String, Integer> VALUES = new HashMap<>();
    private static final Map<String, MatterValue> RECIPE_CACHE = new HashMap<>();
    private static final Map<String, List<Recipe<?>>> RECIPE_INDEX = new HashMap<>();
    private static int cachedRecipeCount = -1;
    private static int cachedRecipeFingerprint;
    private static final int MAX_RECIPE_DEPTH = 24;

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
        return getMatterValue(null, stack).value();
    }

    public static int getMatter(@Nullable Level level, ItemStack stack) {
        return getMatterValue(level, stack).value();
    }

    public static MatterValue getMatterValue(@Nullable Level level, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new MatterValue(0, ValueSource.NONE);
        }
        if (stack.getItem() instanceof MatterDustItem) {
            return new MatterValue(MatterDustItem.getMatter(stack), ValueSource.DYNAMIC);
        }

        MatterValue base = getBaseValue(stack);
        if (base.hasMatter()) {
            return base;
        }
        if (level == null) {
            return fallbackValue(stack);
        }

        refreshRecipeCacheIfNeeded(level);
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        MatterValue cached = RECIPE_CACHE.get(id);
        if (cached != null) {
            return cached;
        }

        MatterValue resolved = resolveMatter(level, stack, new HashSet<>(), 0);
        RECIPE_CACHE.put(id, resolved);
        return resolved;
    }

    /**
     * Lightweight value lookup for UI/tooltips. This deliberately never walks
     * the recipe graph because Minecraft rebuilds its creative/search index by
     * generating tooltips for a very large number of item stacks on the render
     * thread. Recursive recipe valuation from that callback can freeze the
     * client while joining a world.
     *
     * <p>Explicit/tag/dynamic values are returned immediately. A recipe-derived
     * value is used when it has already been resolved by normal gameplay or a
     * diagnostic command; otherwise the cheap fallback is shown until then.</p>
     */
    public static MatterValue getMatterValueForTooltip(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new MatterValue(0, ValueSource.NONE);
        }
        if (stack.getItem() instanceof MatterDustItem) {
            return new MatterValue(MatterDustItem.getMatter(stack), ValueSource.DYNAMIC);
        }

        MatterValue base = getBaseValue(stack);
        if (base.hasMatter()) {
            return base;
        }

        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        MatterValue cached = RECIPE_CACHE.get(id);
        return cached != null ? cached : fallbackValue(stack);
    }

    public static void clearRecipeCache() {
        RECIPE_CACHE.clear();
        RECIPE_INDEX.clear();
        cachedRecipeCount = -1;
        cachedRecipeFingerprint = 0;
    }

    private static void refreshRecipeCacheIfNeeded(Level level) {
        int recipeCount = level.getRecipeManager().getRecipes().size();
        int fingerprint = 1;
        for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            fingerprint = 31 * fingerprint + recipe.getId().hashCode();
        }
        if (recipeCount != cachedRecipeCount || fingerprint != cachedRecipeFingerprint) {
            RECIPE_CACHE.clear();
            RECIPE_INDEX.clear();
            for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
                if (recipe.isSpecial()) {
                    continue;
                }
                ItemStack result = recipe.getResultItem(level.registryAccess());
                if (result.isEmpty()) {
                    continue;
                }
                String resultId = BuiltInRegistries.ITEM.getKey(result.getItem()).toString();
                RECIPE_INDEX.computeIfAbsent(resultId, ignored -> new ArrayList<>()).add(recipe);
            }
            cachedRecipeCount = recipeCount;
            cachedRecipeFingerprint = fingerprint;
        }
    }

    private static MatterValue resolveMatter(Level level, ItemStack stack, Set<String> resolving, int depth) {
        if (stack == null || stack.isEmpty()) {
            return new MatterValue(0, ValueSource.NONE);
        }
        if (stack.getItem() instanceof MatterDustItem) {
            return new MatterValue(MatterDustItem.getMatter(stack), ValueSource.DYNAMIC);
        }

        MatterValue base = getBaseValue(stack);
        if (base.hasMatter()) {
            return base;
        }
        if (depth >= MAX_RECIPE_DEPTH) {
            return fallbackValue(stack);
        }

        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (!resolving.add(id)) {
            return new MatterValue(0, ValueSource.NONE);
        }

        int bestRecipeValue = Integer.MAX_VALUE;
        try {
            List<Recipe<?>> candidates = RECIPE_INDEX.get(id);
            if (candidates != null) {
                for (Recipe<?> recipe : candidates) {
                    ItemStack result = recipe.getResultItem(level.registryAccess());
                    if (result.isEmpty() || !ItemStack.isSameItem(result, stack)) {
                        continue;
                    }

                    long subtotal = 0L;
                    boolean complete = true;
                    for (Ingredient ingredient : recipe.getIngredients()) {
                        if (ingredient.isEmpty()) {
                            continue;
                        }

                        int cheapest = Integer.MAX_VALUE;
                        for (ItemStack option : ingredient.getItems()) {
                            MatterValue optionValue = resolveMatter(level, option, resolving, depth + 1);
                            if (optionValue.hasMatter()) {
                                cheapest = Math.min(cheapest, optionValue.value());
                            }
                        }

                        if (cheapest == Integer.MAX_VALUE) {
                            complete = false;
                            break;
                        }
                        subtotal += cheapest;
                        if (subtotal >= Integer.MAX_VALUE) {
                            subtotal = Integer.MAX_VALUE;
                            break;
                        }
                    }

                    if (!complete || subtotal <= 0) {
                        continue;
                    }

                    int outputCount = Math.max(1, result.getCount());
                    int perItem = (int) Math.max(1L, Math.min(Integer.MAX_VALUE,
                            (subtotal + outputCount - 1L) / outputCount));
                    bestRecipeValue = Math.min(bestRecipeValue, perItem);
                }
            }
        } finally {
            resolving.remove(id);
        }

        if (bestRecipeValue != Integer.MAX_VALUE) {
            return new MatterValue(bestRecipeValue, ValueSource.RECIPE);
        }
        return fallbackValue(stack);
    }

    private static MatterValue getBaseValue(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new MatterValue(0, ValueSource.NONE);
        }
        if (stack.getItem() instanceof MatterDustItem) {
            return new MatterValue(MatterDustItem.getMatter(stack), ValueSource.DYNAMIC);
        }

        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        Integer direct = VALUES.get(id);
        if (direct != null) {
            return new MatterValue(direct, ValueSource.EXPLICIT);
        }

        if (stack.is(ItemTags.LOGS)) {
            return new MatterValue(16, ValueSource.TAG_BASE);
        }
        if (stack.is(ItemTags.PLANKS)) {
            return new MatterValue(4, ValueSource.TAG_BASE);
        }
        if (stack.is(ItemTags.LEAVES)) {
            return new MatterValue(1, ValueSource.TAG_BASE);
        }
        if (stack.is(ItemTags.WOOL)) {
            return new MatterValue(2, ValueSource.TAG_BASE);
        }
        if (stack.is(ItemTags.SAPLINGS)) {
            return new MatterValue(2, ValueSource.TAG_BASE);
        }

        return new MatterValue(0, ValueSource.NONE);
    }

    private static MatterValue fallbackValue(ItemStack stack) {
        if (stack == null || stack.isEmpty() || BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals("air")) {
            return new MatterValue(0, ValueSource.NONE);
        }

        int value;
        if (stack.isDamageableItem()) {
            value = 8 + Math.min(248, Math.max(1, stack.getMaxDamage()) / 32);
        } else {
            int maxStack = Math.max(1, stack.getMaxStackSize());
            value = maxStack <= 1 ? 16 : maxStack <= 16 ? 4 : 1;
        }

        Rarity rarity = stack.getRarity();
        if (rarity == Rarity.UNCOMMON) {
            value *= 2;
        } else if (rarity == Rarity.RARE) {
            value *= 4;
        } else if (rarity == Rarity.EPIC) {
            value *= 8;
        }

        return new MatterValue(Math.max(1, value), ValueSource.FALLBACK);
    }

    public static boolean containsMatter(ItemStack stack) {
        return getMatter(stack) > 0;
    }

    public static boolean containsMatter(@Nullable Level level, ItemStack stack) {
        return getMatter(level, stack) > 0;
    }
}
