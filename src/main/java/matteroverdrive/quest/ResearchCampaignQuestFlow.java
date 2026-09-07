package matteroverdrive.quest;

import matteroverdrive.item.ContractItem;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Drives the technology campaign after the recovered legacy scientist programme. */
public final class ResearchCampaignQuestFlow {
    public static final String INDEX = "MatterOverdriveResearchCampaignIndex";
    public static final String DONE = "MatterOverdriveResearchCampaignDone";
    private ResearchCampaignQuestFlow() {}

    public static boolean handle(ServerPlayer player) {
        CompoundTag data = data(player);
        if (data.getBoolean(DONE)) {
            showEndgame(player);
            return true;
        }
        int index = normalize(player, data);
        ItemStack active = find(player, index);
        if (!active.isEmpty()) {
            if (!ContractItem.complete(active)) {
                showProgress(player, active, index);
                return true;
            }
            redeem(player, active, index, data);
            return true;
        }
        if (index >= ResearchCampaignContracts.QUEST_IDS.length) {
            completeCampaign(player, data);
            return true;
        }
        issue(player, index, data);
        return true;
    }

    public static String status(ServerPlayer player) {
        CompoundTag d = data(player);
        if (d.getBoolean(DONE)) return "Anomaly Engineering complete - independent endgame research active";
        int i = Math.min(Math.max(0, d.getInt(INDEX)), ResearchCampaignContracts.QUEST_IDS.length - 1);
        ItemStack active = find(player, i);
        String stage = ResearchCampaignContracts.stageForIndex(i).title;
        if (active.isEmpty()) return stage + " - next: " + title(ResearchCampaignContracts.QUEST_IDS[i]);
        return stage + " - " + ContractItem.title(active) + " (" + ContractItem.progress(active) + "/" + ContractItem.goal(active) + ")";
    }

    private static void issue(ServerPlayer player, int index, CompoundTag data) {
        String id = ResearchCampaignContracts.QUEST_IDS[index];
        ItemStack quest = ResearchCampaignContracts.create(id, player.getRandom());
        if (quest.isEmpty()) return;
        ResearchProgression.Stage stage = ResearchCampaignContracts.stageForIndex(index);
        ResearchProgression.unlock(player, stage);
        give(player, quest);
        save(player, data);
        player.level().playSound(null, player.blockPosition(), ModSounds.get("gui.quest_started").get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        ModNetwork.openDialogue(player, scientist(stage), ContractItem.title(quest), List.of(
                stageIntro(stage),
                briefing(id),
                objective(quest),
                "Research stage: " + stage.title,
                "Complete the field objective and return with the contract."));
    }

    private static void showProgress(ServerPlayer player, ItemStack active, int index) {
        ResearchProgression.Stage stage = ResearchCampaignContracts.stageForIndex(index);
        ModNetwork.openDialogue(player, scientist(stage), ContractItem.title(active), List.of(
                progressLine(ContractItem.contractId(active)),
                objective(active),
                "Progress: " + ContractItem.progress(active) + " / " + ContractItem.goal(active),
                "Research stage: " + stage.title));
    }

    private static void redeem(ServerPlayer player, ItemStack completed, int index, CompoundTag data) {
        String id = ContractItem.contractId(completed);
        for (ItemStack reward : ContractItem.rewardItems(completed)) give(player, reward.copy());
        if (ContractItem.xp(completed) > 0) player.giveExperiencePoints(ContractItem.xp(completed));
        remove(player, completed);
        cleanup(player, index);
        int next = Math.max(index + 1, data.getInt(INDEX));
        data.putInt(INDEX, next);
        ResearchProgression.Stage oldStage = ResearchCampaignContracts.stageForIndex(index);
        ResearchProgression.Stage nextStage = next < ResearchCampaignContracts.QUEST_IDS.length ? ResearchCampaignContracts.stageForIndex(next) : ResearchProgression.Stage.ANOMALY_ENGINEERING;
        if (nextStage.ordinal() > oldStage.ordinal()) ResearchProgression.unlock(player, nextStage);
        if (next >= ResearchCampaignContracts.QUEST_IDS.length) data.putBoolean(DONE, true);
        save(player, data);
        player.level().playSound(null, player.blockPosition(), ModSounds.get("gui.quest_complete").get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        if (next >= ResearchCampaignContracts.QUEST_IDS.length) {
            completeCampaign(player, data);
            return;
        }
        String nextId = ResearchCampaignContracts.QUEST_IDS[next];
        ModNetwork.openDialogue(player, scientist(oldStage), ContractItem.title(completed) + " Complete", List.of(
                completion(id),
                nextStage.ordinal() > oldStage.ordinal() ? "NEW RESEARCH STAGE: " + nextStage.title : "Research continues: " + oldStage.title,
                "Reward package delivered.",
                "Next assignment: " + title(nextId) + "."));
    }

    private static void completeCampaign(ServerPlayer player, CompoundTag data) {
        data.putBoolean(DONE, true);
        data.putInt(INDEX, ResearchCampaignContracts.QUEST_IDS.length);
        save(player, data);
        ResearchProgression.unlock(player, ResearchProgression.Stage.ANOMALY_ENGINEERING);
        ModNetwork.openDialogue(player, "Dr. Voss - Research Directorate", "Anomaly Engineering Complete", List.of(
                "You began as a research subject and ended as the person I would call when spacetime starts making noises.",
                "Matter Technology, Automation, Advanced Power, Fusion Research and Anomaly Engineering are now complete.",
                "No further clearance exists. Any larger anomaly you create is officially your responsibility.",
                "Independent endgame research unlocked."));
    }

    private static void showEndgame(ServerPlayer player) {
        ModNetwork.openDialogue(player, "Dr. Voss - Research Directorate", "Independent Endgame Research", List.of(
                "The formal campaign is complete.",
                "Build larger matter networks, automate your laboratory, refine reactor efficiency and experiment with anomaly mass safely.",
                "Android progression remains independent, so continue refining your chassis if you wish."));
    }

    private static int normalize(ServerPlayer player, CompoundTag data) {
        int stored = Math.max(0, data.getInt(INDEX));
        int furthest = -1;
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof ContractItem)) continue;
            int i = indexOf(ContractItem.contractId(stack));
            if (i >= 0) furthest = Math.max(furthest, i);
        }
        int normalized = Math.min(Math.max(stored, furthest), ResearchCampaignContracts.QUEST_IDS.length);
        if (normalized != stored) { data.putInt(INDEX, normalized); save(player, data); }
        return normalized;
    }

    private static ItemStack find(ServerPlayer player, int wanted) {
        ItemStack fallback = ItemStack.EMPTY; int furthest = -1;
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof ContractItem)) continue;
            int i = indexOf(ContractItem.contractId(stack));
            if (i < 0) continue;
            if (i == wanted) return stack;
            if (i > furthest) { fallback = stack; furthest = i; }
        }
        return fallback;
    }

    private static int indexOf(String id) { for (int i=0;i<ResearchCampaignContracts.QUEST_IDS.length;i++) if (ResearchCampaignContracts.QUEST_IDS[i].equals(id)) return i; return -1; }
    private static void remove(ServerPlayer p, ItemStack target) { for (int i=0;i<p.getInventory().getContainerSize();i++) if (p.getInventory().getItem(i)==target) { p.getInventory().setItem(i,ItemStack.EMPTY); return; } }
    private static void cleanup(ServerPlayer p, int through) { for (int i=0;i<p.getInventory().getContainerSize();i++) { ItemStack s=p.getInventory().getItem(i); if (s.getItem() instanceof ContractItem) { int q=indexOf(ContractItem.contractId(s)); if(q>=0&&q<=through)p.getInventory().setItem(i,ItemStack.EMPTY); } } }
    private static CompoundTag data(ServerPlayer p){return p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);} private static void save(ServerPlayer p,CompoundTag d){p.getPersistentData().put(Player.PERSISTED_NBT_TAG,d);p.getInventory().setChanged();} private static void give(ServerPlayer p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}
    private static String objective(ItemStack s){String stage=ContractStageSupport.stageLabel(s);return stage.isBlank()?ContractItem.objectiveText(s):stage+": "+ContractItem.objectiveText(s);} private static String title(String id){ItemStack q=ResearchCampaignContracts.create(id,net.minecraft.util.RandomSource.create(0));return q.isEmpty()?id:ContractItem.title(q);}
    private static String scientist(ResearchProgression.Stage s){return switch(s){case MATTER_TECHNOLOGY->"Dr. Sato - Matter Systems";case AUTOMATION_DRONES->"Dr. Kessler - Automation";case ADVANCED_POWER->"Dr. Hale - Energy Systems";case FUSION_RESEARCH->"Dr. Hale - Fusion Research";case ANOMALY_ENGINEERING->"Dr. Voss - Anomaly Directorate";default->"Dr. Voss - Systems Research";};}
    private static String stageIntro(ResearchProgression.Stage s){return switch(s){case MATTER_TECHNOLOGY->"We stop treating matter as loot and start treating it as infrastructure.";case AUTOMATION_DRONES->"A laboratory that needs you to carry every item by hand is not a laboratory. It is a punishment.";case ADVANCED_POWER->"Automation is useless if the grid collapses whenever three machines start at once.";case FUSION_RESEARCH->"Solar power taught you stability. Fusion will teach you respect for containment.";case ANOMALY_ENGINEERING->"The anomaly is no longer a side effect. It is now the subject.";default->"Research continues.";};}
    private static String briefing(String id){return switch(id){case"matter_scanner"->"Build the scanner. If you cannot measure matter, you cannot control it.";case"matter_decomposer"->"Establish a decomposer so ordinary resources can become stored matter.";case"matter_storage"->"Build redundant matter containers. Running dry mid-replication is embarrassing.";case"pattern_archival"->"Archive patterns outside your own memory.";case"replication_test"->"Install a Replicator and close the matter loop.";case"network_backbone"->"Build a router, switch and a short network run.";case"matter_logistics"->"Move matter through pipes and monitor patterns centrally.";case"transport_trial"->"Use a Transporter successfully. Local logistics, not space tourism.";case"solar_baseline"->"Establish three solar panels as a predictable generation baseline.";case"charge_reserve"->"Add a charging station so portable storage participates in the grid.";case"heavy_distribution"->"Build a heavy distribution run suitable for industrial load.";case"containment_field"->"Before a reactor exists, prove you can build its stabilizers.";case"reactor_core"->"Construct the controller and its first coil set.";case"reactor_io"->"Add a Reactor IO so matter and energy have controlled entry and exit points.";case"fusion_load"->"Connect the reactor to heavy distribution and inspect the controller under load.";case"event_horizon"->"Cross an event horizon and return alive. Data from outside is not enough.";case"spacetime_control"->"Build the accelerator and an equalizer. Manipulation requires a way back.";case"anomaly_mastery"->"Rebuild containment, inspect the reactor and enter the anomaly as one controlled experiment.";default->"Complete the assigned research task.";};}
    private static String progressLine(String id){return "Outstanding task: "+title(id)+". The research programme does not advance on optimism.";}
    private static String completion(String id){return switch(id){case"replication_test"->"Matter loop proven. You are ready to automate it.";case"transport_trial"->"Logistics programme complete. Now power the infrastructure properly.";case"heavy_distribution"->"Grid baseline accepted. Fusion clearance granted.";case"fusion_load"->"Fusion systems accepted. Anomaly research is now authorised.";case"anomaly_mastery"->"Containment, reactor control and event-horizon exposure all verified.";default->"Research objective verified.";};}
}
