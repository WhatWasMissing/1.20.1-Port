package matteroverdrive.quest;

import matteroverdrive.item.ContractItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Recovered story-quest definitions that can be handed out by restored dialog/NPC paths later.
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
            default -> ItemStack.EMPTY;
        };
    }

    /**
     * Legacy objective: craft the base Security Protocol, 60 XP.
     * Reward: a decorative coil named Communication Relay, then We Must Know with copied position data.
     * The modern split security system maps the base protocol to security_protocol_empty.
     */
    private static ItemStack crashLanding() {
        ItemStack contract = ContractItem.create("crash_landing", "Crash Landing", "craft",
                List.of("matteroverdrive:security_protocol_empty"), 1, 60, false,
                List.of(new ContractItem.RewardSpec("matteroverdrive:decorative.coils", 1)));
        contract.getOrCreateTag().putString(NEXT_CONTRACT, "we_must_know");
        return contract;
    }

    /**
     * Legacy objective: place the Communication Relay at the quest position, 120 XP, 8 emeralds.
     * Position-locking remains dormant until the original crash-site acquisition path can supply a position.
     */
    private static ItemStack weMustKnow() {
        return ContractItem.create("we_must_know", "We Must Know", "place",
                List.of("matteroverdrive:decorative.coils"), 1, 120, false,
                List.of(new ContractItem.RewardSpec("minecraft:emerald", 8)));
    }

    /**
     * Legacy G.M.O. is an auto-completing sequential multi-quest: scan carrots then potatoes.
     * Each stage randomizes 12-24 scans and contributes 10 XP per successful scan.
     */
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

    public static ItemStack chainedFrom(ItemStack completed, RandomSource random) {
        if (completed == null || completed.isEmpty() || !completed.hasTag()) return ItemStack.EMPTY;
        String next = completed.getTag().getString(NEXT_CONTRACT);
        return next.isBlank() ? ItemStack.EMPTY : create(next, random);
    }

    private static int between(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
