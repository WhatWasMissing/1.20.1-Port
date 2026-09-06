package matteroverdrive.blockentity;

import matteroverdrive.item.ContractItem;
import matteroverdrive.menu.ContractMarketMenu;
import matteroverdrive.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

/** Legacy Contract Market cadence with a modern, self-contained contract representation. */
public class ContractMarketBlockEntity extends BlockEntity implements MenuProvider {
    public static final int OFFER_SLOTS = 18;
    public static final int BASE_GENERATION_DELAY = 36_000;
    public static final int OCCUPIED_SLOT_DELAY = 6_000;
    private static final int LEGACY_TOTAL_WEIGHT = 620;

    private long nextGenerationTime;
    private boolean loading;

    private final ItemStackHandler offers = new ItemStackHandler(OFFER_SLOTS) {
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
        @Override protected void onContentsChanged(int slot) { if (!loading) setChanged(); }
    };

    public ContractMarketBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.CONTRACT_MARKET.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ContractMarketBlockEntity market) {
        if (market.nextGenerationTime <= 0L) market.nextGenerationTime = level.getGameTime();
        if (level.getGameTime() >= market.nextGenerationTime) market.generateContract();
    }

    private void generateContract() {
        if (level == null || level.isClientSide) return;
        int emptySlot = firstEmptySlot();
        if (emptySlot >= 0) {
            ItemStack generated = ItemStack.EMPTY;
            for (int attempt = 0; attempt < 16; attempt++) {
                ItemStack candidate = createWeightedOffer(level.random);
                if (occupiedSlots() >= 7 || !containsContractId(ContractItem.contractId(candidate))) {
                    generated = candidate;
                    break;
                }
            }
            if (generated.isEmpty()) generated = createWeightedOffer(level.random);
            offers.setStackInSlot(emptySlot, generated);
        }
        scheduleNextGeneration(level.getGameTime());
        setChanged();
    }

    /** Exact shipped 1.12 market weights: 100/100/100/80/100/80/60. */
    private ItemStack createWeightedOffer(RandomSource random) {
        int roll = random.nextInt(LEGACY_TOTAL_WEIGHT);
        if ((roll -= 100) < 0) return killAndroids(random);
        if ((roll -= 100) < 0) return sacrifice(random);
        if ((roll -= 100) < 0) return agriculture(random);
        if ((roll -= 80) < 0) return weaponsOfWar(random);
        if ((roll -= 100) < 0) return oneTrueLove();
        if ((roll -= 80) < 0) return isItReallyMe();
        return beastBelly();
    }

    private ItemStack killAndroids(RandomSource random) {
        String[] parts = {"rogue_android_part_head", "rogue_android_part_chest", "rogue_android_part_arms", "rogue_android_part_legs"};
        return ContractItem.create("kill_androids", "They will kill us all!", "hunt",
                List.of("matteroverdrive:rogue_android", "matteroverdrive:ranged_rogue_android"),
                between(random, 12, 28), 40, false,
                List.of(new ContractItem.RewardSpec("matteroverdrive:" + parts[random.nextInt(parts.length)], 1)));
    }

    private ItemStack sacrifice(RandomSource random) {
        return ContractItem.create("sacrifice", "Sacrifice", "hunt",
                List.of("minecraft:pig", "minecraft:cow", "minecraft:sheep", "minecraft:chicken"),
                between(random, 8, 15), 10, true,
                List.of(new ContractItem.RewardSpec("minecraft:saddle", 1), new ContractItem.RewardSpec("minecraft:name_tag", 1)));
    }

    private ItemStack agriculture(RandomSource random) {
        String[] crops = {"minecraft:wheat", "minecraft:carrot", "minecraft:potato"};
        return ContractItem.create("department_of_agriculture", "Department of Agriculture", "collect",
                List.of(crops[random.nextInt(crops.length)]), between(random, 31, 63), 3, false,
                List.of(new ContractItem.RewardSpec("minecraft:emerald", 4), new ContractItem.RewardSpec("minecraft:diamond_hoe", 1)));
    }

    private ItemStack weaponsOfWar(RandomSource random) {
        return ContractItem.create("weapons_of_war", "Weapons of war", "craft",
                List.of("minecraft:anvil"), between(random, 1, 3), 60, false,
                List.of(new ContractItem.RewardSpec("matteroverdrive:tritanium_sword", 1), new ContractItem.RewardSpec("matteroverdrive:tritanium_chestplate", 1)));
    }

    private ItemStack oneTrueLove() {
        return ContractItem.create("one_true_love", "One true love", "mine",
                List.of("minecraft:diamond_ore", "minecraft:deepslate_diamond_ore"), 1, 180, false,
                List.of(new ContractItem.RewardSpec("minecraft:emerald", 6)));
    }

    private ItemStack isItReallyMe() {
        return ContractItem.create("is_it_really_me", "Is it really me?", "transport",
                List.of(), 1, 120, false,
                List.of(new ContractItem.RewardSpec("matteroverdrive:upgrade_range", 2)));
    }

    private ItemStack beastBelly() {
        return ContractItem.create("beast_belly", "The belly of the beast", "anomaly",
                List.of(), 1, 210, false,
                List.of(new ContractItem.RewardSpec("matteroverdrive:gravitational_stabilizer", 2)));
    }

    private static int between(RandomSource random, int min, int max) { return min + random.nextInt(max - min + 1); }

    private boolean containsContractId(String id) {
        if (id == null || id.isBlank()) return false;
        for (int slot = 0; slot < offers.getSlots(); slot++) {
            ItemStack stack = offers.getStackInSlot(slot);
            if (!stack.isEmpty() && id.equals(ContractItem.contractId(stack))) return true;
        }
        return false;
    }

    private void scheduleNextGeneration(long now) { nextGenerationTime = now + BASE_GENERATION_DELAY + (long) occupiedSlots() * OCCUPIED_SLOT_DELAY; }
    private int firstEmptySlot() { for (int slot = 0; slot < offers.getSlots(); slot++) if (offers.getStackInSlot(slot).isEmpty()) return slot; return -1; }

    public int occupiedSlots() {
        int occupied = 0;
        for (int slot = 0; slot < offers.getSlots(); slot++) if (!offers.getStackInSlot(slot).isEmpty()) occupied++;
        return occupied;
    }

    public int ticksUntilNextGeneration() {
        if (level == null || nextGenerationTime <= 0L) return 0;
        long remaining = Math.max(0L, nextGenerationTime - level.getGameTime());
        return (int) Math.min(Integer.MAX_VALUE, remaining);
    }

    public ItemStackHandler getOffers() { return offers; }
    @Override public Component getDisplayName() { return Component.translatable("block.matteroverdrive.contract_market"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new ContractMarketMenu(id, inv, this); }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Offers", offers.serializeNBT());
        tag.putLong("NextGeneration", nextGenerationTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loading = true;
        try {
            for (int slot = 0; slot < offers.getSlots(); slot++) offers.setStackInSlot(slot, ItemStack.EMPTY);
            ItemStackHandler loaded = new ItemStackHandler();
            loaded.deserializeNBT(tag.getCompound("Offers"));
            int copySlots = Math.min(OFFER_SLOTS, loaded.getSlots());
            for (int slot = 0; slot < copySlots; slot++) offers.setStackInSlot(slot, loaded.getStackInSlot(slot));
        } finally { loading = false; }
        nextGenerationTime = tag.contains("NextGeneration") ? tag.getLong("NextGeneration") : tag.getLong("RefreshAt");
    }
}
