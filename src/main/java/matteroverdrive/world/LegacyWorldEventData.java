package matteroverdrive.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashSet; import java.util.Set;
public class LegacyWorldEventData extends SavedData {
    private static final String ID="matteroverdrive_legacy_world_events"; private final Set<Long> checkedChunks=new HashSet<>();
    public static LegacyWorldEventData get(ServerLevel level){return level.getDataStorage().computeIfAbsent(LegacyWorldEventData::load,LegacyWorldEventData::new,ID);}
    public boolean markChecked(long chunk){if(!checkedChunks.add(chunk))return false;setDirty();return true;}
    public static LegacyWorldEventData load(CompoundTag tag){LegacyWorldEventData d=new LegacyWorldEventData();for(long v:tag.getLongArray("CheckedChunks"))d.checkedChunks.add(v);return d;}
    @Override public CompoundTag save(CompoundTag tag){tag.putLongArray("CheckedChunks",checkedChunks.stream().mapToLong(Long::longValue).toArray());return tag;}
}
