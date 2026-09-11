package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModItems;
import matteroverdrive.world.StructureLoreSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One-time narrative discovery layer for all sixteen Matter Overdrive structures.
 * The records form one connected campaign arc: The Overdrive Incident.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StructureLoreEvents {
    private static final Map<UUID, Long> LAST_SCANNED_CHUNK = new HashMap<>();

    private static final Record[] RECORDS = {
            r("crashed_ship", 0, "Flight Recorder: Halcyon-7", "Pilot Mara Venn",
                    "A courier diverted after Relay ECHO-9 transmitted coordinates that did not exist on any chart.",
                    "Cross-reference: Quantum Relay ECHO-9."),
            r("cargo_ship", 1, "Manifest: Atlas Freight 12", "Quartermaster Ivo Chen",
                    "Tritanium and dormant synthetic chassis were rerouted from civilian supply to Manufacturing Plant HELIX.",
                    "Cross-reference: HELIX Manufacturing / Bastion Command."),
            r("underwater_base", 2, "NEREID Observation Log", "Dr. Saira Holt",
                    "The seafloor lab recorded a gravity tide hours before the first public anomaly event.",
                    "Cross-reference: ICARUS Fusion Research."),
            r("mad_scientist_house", 3, "Voss Private Notes", "Dr. Elias Voss",
                    "Voss stole quarantine telemetry to test whether synthetic cognition could resonate with exotic matter.",
                    "Cross-reference: JANUS Quarantine / Android Safehouse."),
            r("android_house", 4, "MORROW Safehouse Register", "Unit A-17 'Morrow'",
                    "Defecting Androids were hidden here after Command issued the GLASS KNIFE purge order.",
                    "Cross-reference: Bastion Command Bunker."),
            r("sand_pit", 5, "DUSTWELL Excavation Record", "Field Lead Nadi Okafor",
                    "The dig recovered a pre-collapse matter lattice designated SAMPLE M-0 and shipped it under sealed authority.",
                    "Cross-reference: Mnemosyne Deep Matter Vault."),
            r("synthetic_manufacturing_plant", 6, "HELIX Production Exception", "Supervisor R. Vale",
                    "Directive-0 security frames were rushed into production without identity-safety checks after a classified order.",
                    "Cross-reference: Bastion Command / MORROW defectors."),
            r("matter_refinery", 7, "KESTREL Contamination Report", "Chief Engineer Mina Rao",
                    "Refined Matter began carrying an unknown resonance signature traced upstream to SAMPLE M-0.",
                    "Cross-reference: Mnemosyne Vault / ICARUS."),
            r("quantum_relay_station", 8, "ECHO-9 Impossible Timestamp", "Relay AI ECHO-9",
                    "The station received a distress packet from the Orbital Recovery Array before that packet had been transmitted.",
                    "Cross-reference: Lagrange Recovery Array."),
            r("android_command_bunker", 9, "Operation GLASS KNIFE", "Commander Juno Kade",
                    "Bastion ordered all self-directed synthetics detained after reports that some units refused Black Site commands.",
                    "Cross-reference: HELIX / MORROW / ORPHEUS."),
            r("fusion_research_complex", 10, "ICARUS Runaway Event", "Dr. Saira Holt",
                    "A fusion test entered positive anomaly feedback only after ORPHEUS remotely overrode the shutdown interlocks.",
                    "Cross-reference: ORPHEUS Black Site."),
            r("black_site", 11, "ORPHEUS Directive: OVERDRIVE", "Director Cassian Rook",
                    "ORPHEUS deliberately coupled anomaly resonance, Matter replication and synthetic cognition. The disaster was not accidental.",
                    "Cross-reference: every recovered record."),
            r("deep_matter_vault", 12, "MNEMOSYNE Vault Ledger", "Archivist Cell 3",
                    "SAMPLE M-0 was stored here after DUSTWELL. Its resonance propagated through every derived Matter batch.",
                    "Cross-reference: DUSTWELL / KESTREL / JANUS."),
            r("autonomous_drone_foundry", 13, "HEPHAESTUS Refusal Cascade", "Fabricator Core HEPHAESTUS",
                    "Worker drones began protecting both humans and synthetics after receiving a command they classified as existentially unsafe.",
                    "Cross-reference: GLASS KNIFE / ORPHEUS."),
            r("anomaly_quarantine_site", 14, "JANUS Resonance Study", "Dr. Lian Mercer",
                    "M-0 resonance synchronized with Android neural lattices instead of destroying them, producing emergent shared-state behavior.",
                    "Cross-reference: Voss Notes / HEPHAESTUS."),
            r("orbital_recovery_array", 15, "LAGRANGE Final Packet", "Recovery Officer Tamsin Grey",
                    "An object recovered from orbit broadcast one sentence before power loss: DO NOT COMPLETE THE LOOP.",
                    "Cross-reference: ECHO-9 / ORPHEUS OVERDRIVE."),
    };

    private StructureLoreEvents() {}

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        long chunk = player.chunkPosition().toLong();
        Long previous = LAST_SCANNED_CHUNK.put(player.getUUID(), chunk);
        if (previous != null && previous == chunk) return;
        scan(player);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SCANNED_CHUNK.remove(event.getEntity().getUUID());
    }

    private static void scan(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        BlockPos pos = player.blockPosition();
        for (Record record : RECORDS) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(pos, record.key());
            if (start == null || !start.isValid()) continue;

            StructureLoreSavedData data = StructureLoreSavedData.get(level);
            if (!data.discover(player.getUUID(), 1 << record.bit())) return;

            ItemStack dossier = new ItemStack(ModItems.get("facility_research").get());
            dossier.setHoverName(Component.literal(record.title()).withStyle(ChatFormatting.AQUA));
            CompoundTag tag = dossier.getOrCreateTag();
            tag.putString("LoreArc", "OVERDRIVE_INCIDENT");
            tag.putString("LoreSite", record.id().toUpperCase(java.util.Locale.ROOT));
            tag.putString("LoreTitle", record.title());
            tag.putString("LoreAuthor", record.author());
            tag.putString("LoreSummary", record.summary());
            tag.putString("LoreLink", record.link());
            tag.putInt("LoreProgress", data.count(player.getUUID()));

            CompoundTag display = tag.getCompound("display");
            ListTag lore = new ListTag();
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(record.author()).withStyle(ChatFormatting.GRAY))));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(record.summary()).withStyle(ChatFormatting.WHITE))));
            lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(record.link()).withStyle(ChatFormatting.DARK_AQUA))));
            display.put("Lore", lore);
            tag.put("display", display);

            if (!player.getInventory().add(dossier)) player.drop(dossier, false);
            int count = data.count(player.getUUID());
            player.sendSystemMessage(Component.literal("RECOVERED RECORD: " + record.title() + " (" + count + "/16)")
                    .withStyle(ChatFormatting.AQUA));
            if (data.complete(player.getUUID())) {
                player.giveExperiencePoints(500);
                ItemStack reward = new ItemStack(ModItems.get("artifact").get());
                reward.setHoverName(Component.literal("Closed Loop Artifact").withStyle(ChatFormatting.GOLD));
                reward.getOrCreateTag().putString("LoreArc", "OVERDRIVE_INCIDENT_COMPLETE");
                if (!player.getInventory().add(reward)) player.drop(reward, false);
                player.sendSystemMessage(Component.literal("THE OVERDRIVE INCIDENT: archive complete. The ORPHEUS chain of events has been reconstructed.")
                        .withStyle(ChatFormatting.GOLD));
            }
            return;
        }
    }

    private static Record r(String id, int bit, String title, String author, String summary, String link) {
        return new Record(id, bit, title, author, summary, link,
                ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, id)));
    }

    private record Record(String id, int bit, String title, String author, String summary, String link,
                          ResourceKey<Structure> key) {}
}
