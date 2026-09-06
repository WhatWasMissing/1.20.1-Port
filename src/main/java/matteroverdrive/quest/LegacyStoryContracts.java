package matteroverdrive.quest;

import matteroverdrive.item.ContractItem;
import matteroverdrive.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Recovered story-quest definitions that can be handed out by restored dialog/NPC/worldgen paths.
 * These are intentionally not inserted into Contract Market generation.
 */
public final class LegacyStoryContracts {
    public static final String NEXT_CONTRACT = "NextContract";

    private LegacyStoryContracts() {}

    public static ItemStack create(String id, RandomSource random) {
        if (id == null) return ItemStack.EMPTY;
        return switch (id) {
            case "crash_landing" -> crashLanding();
            case "we_must_know" -> weMustKnow();
            case "gmo" -> gmo(random == null ? RandomSource.create() : random);
            case "trade_route" -> tradeRoute();
            case "stem_bolts" -> stemBolts();
            case "to_the_power_of" -> toThePowerOf();
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack crashLanding() {
        ItemStack contract = ContractItem.create("crash_landing", "Crash Landing", "craft",
                List.of("matteroverdrive:security_protocol_empty"), 1, 60, false,
                List.of(new ContractItem.RewardSpec("matteroverdrive:decorative.coils", 1)));
        contract.getOrCreateTag().putString(NEXT_CONTRACT, "we_must_know");
        return contract;
    }

    private static ItemStack weMustKnow() {
        return ContractItem.create("we_must_know", "We Must Know", "place",
                List.of("matteroverdrive:decorative.coils"), 1, 120, false,
                List.of(new ContractItem.RewardSpec("minecraft:emerald", 8)));
    }

    private static ItemStack gmo(RandomSource random) {
        int carrots = between(random, 12, 24);
        int potatoes = between(random, 12, 24);
        return ContractStageSupport.createStaged("gmo", "G.M.O.", (carrots + potatoes) * 10,
                List.of(new ContractItem.RewardSpec("matteroverdrive:tritanium_spine", 1)),
                List.of(
                        new ContractStageSupport.StageSpec("scan", List.of("minecraft:carrots"), carrots),
                        new ContractStageSupport.StageSpec("scan", List.of("minecraft:potatoes"), potatoes)
                ));
    }

    /**
     * Recovered Trade Route: open captain storage, read the specifically named Trade Route Agreement,
     * then talk to a Mad Scientist. The JSON reward chains into Stem Bolts and gives a named agreement copy.
     */
    private static ItemStack tradeRoute() {
        ItemStack contract = ContractStageSupport.createStaged("trade_route", "Trade Route", 180,
                List.of(),
                List.of(
                        new ContractStageSupport.StageSpec("block_interact",
                                List.of("matteroverdrive:tritanium_crate_red"), 1),
                        new ContractStageSupport.StageSpec("item_interact_consume",
                                List.of("matteroverdrive:isolinear_circuit_mk1"), 1, "Trade Route Agreement"),
                        new ContractStageSupport.StageSpec("conversation",
                                List.of("matteroverdrive:mad_scientist"), 1)
                ));
        contract.getOrCreateTag().putString(NEXT_CONTRACT, "stem_bolts");
        return contract;
    }

    /** Recovered Self-Sealing Stem Bolts: read the named copy issued by Trade Route. */
    private static ItemStack stemBolts() {
        return ContractStageSupport.createStaged("stem_bolts", "Self-Sealing Stem Bolts", 0,
                List.of(),
                List.of(new ContractStageSupport.StageSpec("item_interact_consume",
                        List.of("matteroverdrive:isolinear_circuit_mk1"), 1, "Trade Route Agreement Copy")));
    }

    /**
     * Legacy JSON accepted BigReactors, ExtraUtilities or the Matter Overdrive Solar Panel.
     * The 1.20.1 port restores the native MO path without introducing hard dependencies on absent legacy mods.
     */
    private static ItemStack toThePowerOf() {
        return ContractItem.create("to_the_power_of", "To the Power Of", "craft",
                List.of("matteroverdrive:solar_panel"), 1, 120, false,
                List.of(
                        new ContractItem.RewardSpec("matteroverdrive:tritanium_ingot", 5),
                        new ContractItem.RewardSpec("matteroverdrive:tritanium_plate", 4)
                ));
    }

    /** Hidden/special legacy rewards whose identity includes custom display data. */
    public static List<ItemStack> specialRewards(ItemStack completed) {
        if (completed == null || completed.isEmpty()) return List.of();
        if (!"trade_route".equals(ContractItem.contractId(completed))) return List.of();
        ItemStack copy = new ItemStack(ModItems.get("isolinear_circuit_mk1").get());
        copy.setHoverName(Component.literal("Trade Route Agreement Copy"));
        return List.of(copy);
    }

    public static ItemStack chainedFrom(ItemStack completed, RandomSource random) {
        if (completed == null || completed.isEmpty() || !completed.hasTag()) return ItemStack.EMPTY;
        String next = completed.getTag().getString(NEXT_CONTRACT);
        return next.isBlank() ? ItemStack.EMPTY : create(next, random);
    }

    private static int between(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
