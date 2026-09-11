package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidData;
import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.entity.RogueAndroidEntity;
import matteroverdrive.entity.EncounterFaction;
import matteroverdrive.item.RecoveredArtifactItem;
import matteroverdrive.quest.ResearchProgression;
import matteroverdrive.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Drops a salvageable Android body part from the restored dedicated Rogue Android entity. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RogueAndroidDropEvents {
    private static final String ENCOUNTER_RESEARCH = "MatterOverdriveEncounterResearch";
    private RogueAndroidDropEvents() {}

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof RogueAndroidEntity android)) {
            return;
        }
        AndroidData.Part[] parts = AndroidData.Part.values();
        AndroidData.Part part = parts[android.getRandom().nextInt(parts.length)];
        event.getDrops().add(new ItemEntity(android.level(), android.getX(), android.getY(), android.getZ(),
                new ItemStack(ModItems.get(part.itemId).get())));
        if (android.getEncounterFaction() == EncounterFaction.BLACK_SITE
                && android.getRandom().nextFloat() < 0.35F) {
            AndroidLoadout.Artifact[] protocols = AndroidLoadout.Artifact.values();
            int index = 1 + Math.floorMod(android.getUUID().hashCode(), protocols.length - 1);
            event.getDrops().add(new ItemEntity(android.level(), android.getX(), android.getY(), android.getZ(),
                    RecoveredArtifactItem.recovered(protocols[index])));
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof RogueAndroidEntity android)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        String faction = android.getEncounterFaction().id();
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag encounters = persisted.getCompound(ENCOUNTER_RESEARCH);
        if (encounters.getBoolean(faction)) return;
        encounters.putBoolean(faction, true);
        persisted.put(ENCOUNTER_RESEARCH, encounters);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        player.giveExperiencePoints(25);
        ResearchProgression.Stage evidence = android.getEncounterFaction() == EncounterFaction.BLACK_SITE
                ? ResearchProgression.Stage.ANOMALY_ENGINEERING
                : ResearchProgression.Stage.AUTOMATION_DRONES;
        boolean advanced = ResearchProgression.unlockEvidence(player, evidence);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "Encounter logged: " + faction + " (+25 XP" + (advanced ? "; research clearance advanced" : "") + ")"), false);
    }
}
