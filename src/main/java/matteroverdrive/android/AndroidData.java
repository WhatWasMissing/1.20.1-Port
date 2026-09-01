package matteroverdrive.android;

import matteroverdrive.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;

/** Persistent Android state owned by a player rather than a block. */
public final class AndroidData {
    public static final int ENERGY_CAPACITY = 100_000;
    private static final String ROOT = "MatterOverdriveAndroid";
    private static final String ACTIVE = "Active";
    private static final String ENERGY = "Energy";
    private static final String PARTS = "Parts";

    public enum Part {
        HEAD(1, "rogue_android_part_head"),
        CHEST(2, "rogue_android_part_chest"),
        ARMS(4, "rogue_android_part_arms"),
        LEGS(8, "rogue_android_part_legs");

        public final int bit;
        public final String itemId;
        Part(int bit, String itemId) { this.bit = bit; this.itemId = itemId; }
    }

    private AndroidData() {}

    private static CompoundTag data(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) persistent.put(ROOT, new CompoundTag());
        return persistent.getCompound(ROOT);
    }

    private static void save(Player player, CompoundTag data) {
        player.getPersistentData().put(ROOT, data);
    }

    public static boolean isAndroid(Player player) { return data(player).getBoolean(ACTIVE); }
    public static int getEnergy(Player player) { return Mth.clamp(data(player).getInt(ENERGY), 0, ENERGY_CAPACITY); }
    public static int getParts(Player player) { return data(player).getInt(PARTS) & 15; }
    public static boolean hasPart(Player player, Part part) { return (getParts(player) & part.bit) != 0; }

    public static void activate(Player player) {
        CompoundTag data = data(player);
        data.putBoolean(ACTIVE, true);
        data.putInt(ENERGY, Math.max(25_000, getEnergy(player)));
        save(player, data);
    }

    public static int receiveEnergy(Player player, int amount) {
        if (!isAndroid(player)) return 0;
        int accepted = Math.min(Math.max(0, amount), ENERGY_CAPACITY - getEnergy(player));
        if (accepted > 0) setEnergy(player, getEnergy(player) + accepted);
        return accepted;
    }

    public static int consumeEnergy(Player player, int amount) {
        int used = Math.min(Math.max(0, amount), getEnergy(player));
        if (used > 0) setEnergy(player, getEnergy(player) - used);
        return used;
    }

    public static void setEnergy(Player player, int amount) {
        CompoundTag data = data(player);
        data.putInt(ENERGY, Mth.clamp(amount, 0, ENERGY_CAPACITY));
        save(player, data);
    }

    public static boolean installPart(Player player, Part part) {
        if (!isAndroid(player) || hasPart(player, part)) return false;
        CompoundTag data = data(player);
        data.putInt(PARTS, getParts(player) | part.bit);
        save(player, data);
        return true;
    }

    public static int deactivate(Player player) {
        CompoundTag data = data(player);
        int installed = getParts(player);
        data.putBoolean(ACTIVE, false);
        data.putInt(ENERGY, 0);
        data.putInt(PARTS, 0);
        save(player, data);
        return installed;
    }

    public static ItemStack partStack(Part part) { return new ItemStack(ModItems.get(part.itemId).get()); }

    public static void copyTo(Player original, Player clone) {
        CompoundTag root = original.getPersistentData().getCompound(ROOT);
        clone.getPersistentData().put(ROOT, root.copy());
    }
}