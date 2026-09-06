package matteroverdrive.quest;

import matteroverdrive.item.ContractItem;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Source-backed legacy story quests that are not part of normal Contract Market generation. */
public final class LegacyStoryContracts {
    public static final String NEXT_CONTRACT = "NextContract";
    public static final String LEGACY_SCAN_PAD = "LegacyQuestScanner";
    public static final String[] QUEST_IDS = {
            "crash_landing", "we_must_know", "gmo", "trade_route", "stem_bolts", "to_the_power_of"
    };

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
                List.of("matteroverdrive:security_protocol_empty"), 1, 60, false, List.of());
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
                List.of(),
                List.of(
                        new ContractStageSupport.StageSpec("scan", List.of("minecraft:carrots"), carrots),
                        new ContractStageSupport.StageSpec("scan", List.of("minecraft:potatoes"), potatoes)
                ));
    }

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

    private static ItemStack stemBolts() {
        return ContractStageSupport.createStaged("stem_bolts", "Self-Sealing Stem Bolts", 0,
                List.of(),
                List.of(new ContractStageSupport.StageSpec("item_interact",
                        List.of("matteroverdrive:isolinear_circuit_mk1"), 1, "Trade Route Agreement Copy")));
    }

    private static ItemStack toThePowerOf() {
        return ContractItem.create("to_the_power_of", "To the Power Of", "craft",
                List.of("matteroverdrive:solar_panel"), 1, 120, false,
                List.of(
                        new ContractItem.RewardSpec("matteroverdrive:tritanium_ingot", 5),
                        new ContractItem.RewardSpec("matteroverdrive:tritanium_plate", 4)
                ));
    }

    /** Legacy item rewards whose identity included custom NBT/display data rather than only an item id. */
    public static List<ItemStack> specialRewards(ItemStack completed) {
        if (completed == null || completed.isEmpty()) return List.of();
        return switch (ContractItem.contractId(completed)) {
            case "crash_landing" -> List.of(namedBlockItem("decorative.coils", "Communication Relay"));
            case "gmo" -> {
                ItemStack spine = new ItemStack(ModItems.get("tritanium_spine").get());
                spine.setHoverName(Component.literal("Hardened Tritanium Spine"));
                spine.getOrCreateTag().putBoolean("MatterOverdriveLegacyHardenedSpine", true);
                yield List.of(spine);
            }
            case "trade_route" -> {
                ItemStack copy = new ItemStack(ModItems.get("isolinear_circuit_mk1").get());
                copy.setHoverName(Component.literal("Trade Route Agreement Copy"));
                yield List.of(copy);
            }
            default -> List.of();
        };
    }

    /** Legacy entity rewards. Trade Route spawned one Failed Pig at the stored quest position with Y -2. */
    public static void applyWorldRewards(ServerPlayer player, ItemStack completed) {
        if (player == null || completed == null || completed.isEmpty()) return;
        if (!"trade_route".equals(ContractItem.contractId(completed)) || !ContractStageSupport.hasQuestPosition(completed)) return;
        BlockPos origin = ContractStageSupport.questPosition(completed);
        if (origin == null) return;
        var pig = ModEntities.FAILED_PIG.get().create(player.serverLevel());
        if (pig == null) return;
        BlockPos spawn = origin.below(2);
        pig.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, player.getYRot(), 0.0F);
        pig.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(spawn), MobSpawnType.EVENT, null, null);
        player.serverLevel().addFreshEntity(pig);
    }

    /** Exact helper item placed beside the G.M.O. contract in the Mad Scientist House. */
    public static ItemStack createMadScientistDataPad() {
        ItemStack pad = new ItemStack(ModItems.get("data_pad").get());
        pad.setHoverName(Component.literal("Mad Scientist's Data Pad"));
        pad.getOrCreateTag().putBoolean(LEGACY_SCAN_PAD, true);
        return pad;
    }

    public static ItemStack chainedFrom(ItemStack completed, RandomSource random) {
        if (completed == null || completed.isEmpty() || !completed.hasTag()) return ItemStack.EMPTY;
        String next = completed.getTag().getString(NEXT_CONTRACT);
        if (next.isBlank()) return ItemStack.EMPTY;
        ItemStack chained = create(next, random);
        ContractStageSupport.copyQuestPosition(completed, chained);
        return chained;
    }

    private static ItemStack namedBlockItem(String id, String name) {
        ItemStack stack = new ItemStack(ModItems.get(id).get());
        stack.setHoverName(Component.literal(name));
        return stack;
    }

    private static int between(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
