package matteroverdrive.network;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidClassAbilities;
import matteroverdrive.android.AndroidData;
import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.android.AndroidUltimates;
import matteroverdrive.dialogue.DialogueCatalog;
import matteroverdrive.dialogue.DialogueSessionManager;
import matteroverdrive.world.StructureLoreSavedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

public final class ModNetwork {
    private static final String PROTOCOL = "17";
    private static int nextId;
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, "network"))
            .networkProtocolVersion(() -> PROTOCOL).clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals).simpleChannel();

    private ModNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(nextId++, AndroidStatePacket.class, AndroidStatePacket::encode, AndroidStatePacket::decode, AndroidStatePacket::handle);
        CHANNEL.registerMessage(nextId++, AndroidAbilityPacket.class, AndroidAbilityPacket::encode, AndroidAbilityPacket::decode, AndroidAbilityPacket::handle);
        CHANNEL.registerMessage(nextId++, AndroidPerkSelectPacket.class, AndroidPerkSelectPacket::encode, AndroidPerkSelectPacket::decode, AndroidPerkSelectPacket::handle);
        CHANNEL.registerMessage(nextId++, AndroidLoadoutSelectPacket.class, AndroidLoadoutSelectPacket::encode, AndroidLoadoutSelectPacket::decode, AndroidLoadoutSelectPacket::handle);
        CHANNEL.registerMessage(nextId++, DataPadOpenPacket.class, DataPadOpenPacket::encode, DataPadOpenPacket::decode, DataPadOpenPacket::handle);
        CHANNEL.registerMessage(nextId++, DocumentationOpenPacket.class, DocumentationOpenPacket::encode, DocumentationOpenPacket::decode, DocumentationOpenPacket::handle);
        CHANNEL.registerMessage(nextId++, OmniToolFirePacket.class, OmniToolFirePacket::encode, OmniToolFirePacket::decode, OmniToolFirePacket::handle);
        CHANNEL.registerMessage(nextId++, WeaponTriggerPacket.class, WeaponTriggerPacket::encode, WeaponTriggerPacket::decode, WeaponTriggerPacket::handle);
        CHANNEL.registerMessage(nextId++, ContractAbandonPacket.class, ContractAbandonPacket::encode, ContractAbandonPacket::decode, ContractAbandonPacket::handle);
        CHANNEL.registerMessage(nextId++, QuestTrackerSyncPacket.class, QuestTrackerSyncPacket::encode, QuestTrackerSyncPacket::decode, QuestTrackerSyncPacket::handle);
        CHANNEL.registerMessage(nextId++, NpcDialoguePacket.class, NpcDialoguePacket::encode, NpcDialoguePacket::decode, NpcDialoguePacket::handle);
        CHANNEL.registerMessage(nextId++, DialogueChoicePacket.class, DialogueChoicePacket::encode, DialogueChoicePacket::decode, DialogueChoicePacket::handle);
        CHANNEL.registerMessage(nextId++, PdaVoicePacket.class, PdaVoicePacket::encode, PdaVoicePacket::decode, PdaVoicePacket::handle);
        CHANNEL.registerMessage(nextId++, FacilityDiscoveryPacket.class, FacilityDiscoveryPacket::encode, FacilityDiscoveryPacket::decode, FacilityDiscoveryPacket::handle);
        CHANNEL.registerMessage(nextId++, EnvironmentalHazardPacket.class, EnvironmentalHazardPacket::encode, EnvironmentalHazardPacket::decode, EnvironmentalHazardPacket::handle);
        CHANNEL.registerMessage(nextId++, WelcomeBriefingPacket.class, WelcomeBriefingPacket::encode, WelcomeBriefingPacket::decode, WelcomeBriefingPacket::handle);
        CHANNEL.registerMessage(nextId++, DroneCommandPacket.class, DroneCommandPacket::encode, DroneCommandPacket::decode, DroneCommandPacket::handle);
        CHANNEL.registerMessage(nextId++, DroneStatusPacket.class, DroneStatusPacket::encode, DroneStatusPacket::decode, DroneStatusPacket::handle);
    }

    public static void openDataPad(ServerPlayer p, List<String> h) {
        int loreMask = StructureLoreSavedData.get(p.serverLevel().getServer().overworld()).mask(p.getUUID());
        openDataPad(p, h, loreMask);
    }

    public static void openDataPad(ServerPlayer p, List<String> h, int loreMask) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new DataPadOpenPacket(h, loreMask));
    }

    public static void openDocumentation(ServerPlayer p, int d) { CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new DocumentationOpenPacket(d)); }
    public static void openDialogue(ServerPlayer p, String speaker, String title, List<String> lines) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new NpcDialoguePacket(speaker, title, lines));
    }
    public static void openBranchingDialogue(ServerPlayer p, String dialogueId, String speaker) {
        DialogueSessionManager.open(p, dialogueId, speaker);
    }
    public static void sendDialogueView(ServerPlayer p, String dialogueId, DialogueCatalog.DialogueView view) {
        List<NpcDialoguePacket.ChoiceOption> options = view.choices().stream()
                .map(choice -> new NpcDialoguePacket.ChoiceOption(choice.id(), choice.label())).toList();
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new NpcDialoguePacket(
                dialogueId, view.nodeId(), view.speaker(), view.heading(), view.lines(), options));
    }
    public static void chooseDialogue(String dialogueId, String nodeId, String choiceId) {
        CHANNEL.sendToServer(new DialogueChoicePacket(dialogueId, nodeId, choiceId));
    }
    public static void sendPdaVoice(ServerPlayer p, String lineId) { CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new PdaVoicePacket(lineId)); }
    public static void sendFacilityDiscovery(ServerPlayer p, String siteId, String facility, String recordTitle, int archiveIndex, String classification) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new FacilityDiscoveryPacket(siteId, facility, recordTitle, archiveIndex, classification));
    }
    public static void sendEnvironmentalHazard(ServerPlayer p, String id, String title, String detail, int severity) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new EnvironmentalHazardPacket(id, title, detail, severity));
    }
    public static void openWelcomeBriefing(ServerPlayer p) { CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new WelcomeBriefingPacket()); }
    public static void fireOmniTool() { CHANNEL.sendToServer(new OmniToolFirePacket()); }
    public static void weaponTrigger(boolean pressed, boolean aiming) { CHANNEL.sendToServer(new WeaponTriggerPacket(pressed, aiming)); }
    public static void requestContractAbandon(int slot) { CHANNEL.sendToServer(new ContractAbandonPacket(slot)); }
    public static void requestDroneStatus() { CHANNEL.sendToServer(DroneCommandPacket.requestStatus()); }

    public static void syncQuestTracker(ServerPlayer p) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), QuestTrackerSyncPacket.from(p));
    }

    public static void sendDroneStatus(ServerPlayer p) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), DroneStatusPacket.from(p));
    }

    public static void syncAndroidState(ServerPlayer p) {
        AndroidData.Ability a = AndroidData.getSelectedAbility(p);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new AndroidStatePacket(
                AndroidData.isAndroid(p), AndroidData.getEnergy(p), AndroidData.getEnergyCapacity(p), AndroidData.getParts(p), a.ordinal(),
                AndroidData.getRemainingCooldown(p, a, p.level().getGameTime()), AndroidData.getActiveAbilityFlags(p),
                AndroidData.getExperience(p), AndroidData.getLevel(p), AndroidData.getSelectedPerks(p),
                AndroidLoadout.getAspectMask(p), AndroidLoadout.getFragmentMask(p),
                AndroidLoadout.getArtifact(p).ordinal(), AndroidLoadout.getDronePerkMask(p),
                AndroidLoadout.getSpecialization(p).ordinal(), AndroidUltimates.getRemainingCooldown(p),
                AndroidClassAbilities.getClassCooldown(p), AndroidClassAbilities.getTechCooldown(p)));
    }
}
