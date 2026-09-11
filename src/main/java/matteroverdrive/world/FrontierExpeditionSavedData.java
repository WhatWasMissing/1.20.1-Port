package matteroverdrive.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Durable per-player exploration state for the four Frontier Expedition sites. */
public final class FrontierExpeditionSavedData extends SavedData {
    private static final String ID = "matteroverdrive_frontier_expeditions";
    private static final int MAX_DISCOVERIES = 8192;
    private static final int MAX_PLAYERS = 2048;
    private static final int MAX_KEY_LENGTH = 256;

    private final Set<String> discoveries = new HashSet<>();
    private final Map<UUID, Integer> siteMasks = new HashMap<>();

    public static FrontierExpeditionSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FrontierExpeditionSavedData::load,
                FrontierExpeditionSavedData::new, ID);
    }

    public DiscoveryResult discover(ServerLevel level, UUID player, String site, int siteBit, long anchor) {
        String key = level.dimension().location() + ":" + player + ":" + site + ":" + anchor;
        if (key.length() > MAX_KEY_LENGTH || discoveries.size() >= MAX_DISCOVERIES) return DiscoveryResult.REJECTED;
        if (!discoveries.add(key)) return DiscoveryResult.ALREADY_LOGGED;

        int oldMask = siteMasks.getOrDefault(player, 0);
        int newMask = oldMask | siteBit;
        siteMasks.put(player, newMask);
        setDirty();
        if (Integer.bitCount(newMask) >= 4 && Integer.bitCount(oldMask) < 4) return DiscoveryResult.EXPEDITION_COMPLETE;
        if ((oldMask & siteBit) == 0) return DiscoveryResult.NEW_SITE_TYPE;
        return DiscoveryResult.NEW_LOCATION;
    }

    public int uniqueSiteCount(UUID player) {
        return Integer.bitCount(siteMasks.getOrDefault(player, 0) & 0x0F);
    }

    public int siteMask(UUID player) {
        return siteMasks.getOrDefault(player, 0) & 0x0F;
    }

    public static FrontierExpeditionSavedData load(CompoundTag tag) {
        FrontierExpeditionSavedData data = new FrontierExpeditionSavedData();
        ListTag discoveries = tag.getList("Discoveries", net.minecraft.nbt.Tag.TAG_STRING);
        for (int i = 0; i < discoveries.size() && i < MAX_DISCOVERIES; i++) {
            String value = discoveries.getString(i);
            if (value.length() <= MAX_KEY_LENGTH) data.discoveries.add(value);
        }
        ListTag masks = tag.getList("SiteMasks", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < masks.size() && i < MAX_PLAYERS; i++) {
            CompoundTag entry = masks.getCompound(i);
            if (entry.hasUUID("Player")) data.siteMasks.put(entry.getUUID("Player"), entry.getInt("Mask") & 0x0F);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag discoveryList = new ListTag();
        discoveries.stream().sorted().forEach(value -> discoveryList.add(StringTag.valueOf(value)));
        tag.put("Discoveries", discoveryList);

        ListTag masks = new ListTag();
        siteMasks.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            CompoundTag value = new CompoundTag();
            value.putUUID("Player", entry.getKey());
            value.putInt("Mask", entry.getValue() & 0x0F);
            masks.add(value);
        });
        tag.put("SiteMasks", masks);
        return tag;
    }

    public enum DiscoveryResult {
        REJECTED,
        ALREADY_LOGGED,
        NEW_LOCATION,
        NEW_SITE_TYPE,
        EXPEDITION_COMPLETE
    }
}
