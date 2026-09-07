package matteroverdrive.network;

import matteroverdrive.android.AndroidData;
import matteroverdrive.client.QuestTrackerClientState;
import matteroverdrive.entity.MadScientistEntity;
import matteroverdrive.event.CocktailQuestEvents;
import matteroverdrive.item.ContractItem;
import matteroverdrive.quest.ContractStageSupport;
import matteroverdrive.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Server-authoritative quest snapshot used by the lightweight HUD tracker. */
public record QuestTrackerSyncPacket(List<QuestTrackerClientState.Entry> entries) {
    private static final int MAX_ENTRIES = 12;

    public static QuestTrackerSyncPacket from(ServerPlayer player) {
        List<QuestTrackerClientState.Entry> result = new ArrayList<>();

        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.getBoolean(MadScientistEntity.QUEST_ACTIVE)
                && !persisted.getBoolean(MadScientistEntity.QUEST_DONE)) {
            int found = 0;
            StringBuilder status = new StringBuilder();
            for (AndroidData.Part part : AndroidData.Part.values()) {
                boolean has = player.getInventory().countItem(ModItems.get(part.itemId).get()) > 0;
                if (has) found++;
                if (status.length() > 0) status.append("  ");
                status.append(part.name()).append(has ? " ✓" : " ✗");
            }
            result.add(new QuestTrackerClientState.Entry(
                    "Puny Humans",
                    "Recover one of each Rogue Android body part, then return to a Mad Scientist.",
                    found + "/" + AndroidData.Part.values().length + " parts · " + status,
                    "MAD SCIENTIST"));
        }

        if (persisted.getBoolean(CocktailQuestEvents.ACTIVE)
                && !persisted.getBoolean(CocktailQuestEvents.DONE)) {
            int kills = Math.min(CocktailQuestEvents.REQUIRED_CREEPERS, persisted.getInt(CocktailQuestEvents.CREEPER_KILLS));
            int gunpowder = Math.min(CocktailQuestEvents.REQUIRED_GUNPOWDER, persisted.getInt(CocktailQuestEvents.GUNPOWDER));
            int mushrooms = Math.min(CocktailQuestEvents.REQUIRED_MUSHROOMS, persisted.getInt(CocktailQuestEvents.MUSHROOMS));
            result.add(new QuestTrackerClientState.Entry(
                    "Cocktail of Ascension",
                    "Complete the Mad Scientist's unstable field experiment.",
                    "Shovel Creepers " + kills + "/5 · Gunpowder " + gunpowder + "/5 · Nether mushrooms " + mushrooms + "/5",
                    "MAD SCIENTIST"));
        }

        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof ContractItem) || ContractItem.complete(stack)) continue;
            String stage = ContractStageSupport.stageLabel(stack);
            result.add(new QuestTrackerClientState.Entry(
                    ContractItem.title(stack),
                    ContractItem.objectiveText(stack),
                    ContractItem.progress(stack) + " / " + ContractItem.goal(stack),
                    stage.isBlank() ? "CONTRACT" : stage.toUpperCase()));
            if (result.size() >= MAX_ENTRIES) break;
        }

        return new QuestTrackerSyncPacket(List.copyOf(result));
    }

    public static void encode(QuestTrackerSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(Math.min(MAX_ENTRIES, packet.entries.size()));
        for (int i = 0; i < packet.entries.size() && i < MAX_ENTRIES; i++) {
            QuestTrackerClientState.Entry entry = packet.entries.get(i);
            buffer.writeUtf(entry.title(), 128);
            buffer.writeUtf(entry.objective(), 256);
            buffer.writeUtf(entry.progress(), 256);
            buffer.writeUtf(entry.stage(), 64);
        }
    }

    public static QuestTrackerSyncPacket decode(FriendlyByteBuf buffer) {
        int count = Math.min(MAX_ENTRIES, buffer.readVarInt());
        List<QuestTrackerClientState.Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new QuestTrackerClientState.Entry(
                    buffer.readUtf(128), buffer.readUtf(256), buffer.readUtf(256), buffer.readUtf(64)));
        }
        return new QuestTrackerSyncPacket(entries);
    }

    public static void handle(QuestTrackerSyncPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> QuestTrackerClientState.set(packet.entries)));
        context.setPacketHandled(true);
    }
}
