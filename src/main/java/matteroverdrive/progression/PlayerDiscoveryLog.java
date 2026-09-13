package matteroverdrive.progression;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/** Persistent, structure-independent PDA discoveries produced by the player's own experiments. */
public final class PlayerDiscoveryLog {
    private static final String LOG_TAG = "MatterOverdrivePdaDiscoveries";
    private static final String KEYS_TAG = "MatterOverdrivePdaDiscoveryKeys";
    private static final int CAPACITY = 32;

    private PlayerDiscoveryLog() {}

    public static boolean record(ServerPlayer player, String key, String text) {
        if (player == null || key == null || key.isBlank() || text == null || text.isBlank()) return false;
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        ListTag keys = persisted.getList(KEYS_TAG, Tag.TAG_STRING);
        for (int i = 0; i < keys.size(); i++) if (key.equals(keys.getString(i))) return false;

        ListTag nextKeys = new ListTag();
        nextKeys.add(StringTag.valueOf(key));
        for (int i = 0; i < keys.size() && nextKeys.size() < CAPACITY; i++) nextKeys.add(StringTag.valueOf(keys.getString(i)));

        ListTag previous = persisted.getList(LOG_TAG, Tag.TAG_STRING);
        ListTag next = new ListTag();
        next.add(StringTag.valueOf(text));
        for (int i = 0; i < previous.size() && next.size() < CAPACITY; i++) next.add(StringTag.valueOf(previous.getString(i)));

        persisted.put(KEYS_TAG, nextKeys);
        persisted.put(LOG_TAG, next);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        return true;
    }

    public static List<String> lines(ServerPlayer player) {
        List<String> result = new ArrayList<>();
        if (player == null) return result;
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        ListTag list = persisted.getList(LOG_TAG, Tag.TAG_STRING);
        for (int i = 0; i < list.size() && i < CAPACITY; i++) result.add(list.getString(i));
        return result;
    }
}
