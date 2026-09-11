package matteroverdrive.dialogue;

import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Server-authoritative branching conversations for the present-day field cast. */
public final class DialogueCatalog {
    public record ChoiceView(String id, String label) { }
    public record DialogueView(String speaker, String heading, String nodeId,
                               List<String> lines, List<ChoiceView> choices) { }
    private record Topic(String id, String label, List<String> response, String flag,
                         int fieldDelta, int syntheticDelta, int insightDelta) { }
    private record Profile(String heading, List<String> intro, String returnLine, List<Topic> topics) { }

    private static final Map<String, Profile> PROFILES = new LinkedHashMap<>();

    static {
        add("researcher.field", "Recovered Site Survey",
                List.of("These ruins are evidence first and salvage second.",
                        "If your PDA authenticates a local record, compare its cross-references before moving on."),
                "You're back. Good. A second pass usually catches what the first one missed.",
                topic("survey", "What have you learned from these ruins?",
                        List.of("The sites fail in patterns. ORPHEUS facilities hide decisions behind infrastructure; civilian sites show the consequences more plainly.",
                                "A room full of broken machines can still tell you who had authority, who panicked, and who tried to help."),
                        "field:survey", 1, 0, 1),
                topic("preserve", "What should I preserve?",
                        List.of("Anything with timestamps, serials, handwritten labels, or contradictory ownership records.",
                                "Take components if you need them. Just don't erase the chain of evidence to get at the copper underneath."),
                        "field:preserve_evidence", 2, 0, 2),
                topic("rebuild", "I intend to rebuild this technology.",
                        List.of("Then rebuild interfaces before integrations. ORPHEUS failed because every safeguard became another dependency in one converged state.",
                                "Make systems able to disagree safely. That's the lesson worth carrying forward."),
                        "field:rebuild_carefully", 1, 0, 2));

        add("researcher.salvager", "Salvage Notes",
                List.of("I only take what the site can lose without erasing the story.",
                        "Guarded caches are usually intact for a reason. Clear the room before you start reading labels."),
                "Still picking through the bones? Fine by me, as long as you leave the identity tags where they are.",
                topic("valuable", "What's actually worth taking?",
                        List.of("Refined Matter dust, intact circuits, power modules, anything sealed against corrosion.",
                                "The flashy wreckage is usually the least useful part. Check maintenance spaces and redundant storage."),
                        "salvage:practical", 1, 0, 0),
                topic("danger", "What gets salvagers killed here?",
                        List.of("Assuming silence means the security grid is dead. Assuming a broken floor is decorative. Assuming every Android with a weapon serves the same side.",
                                "In that order, usually."),
                        "salvage:cautious", 1, 0, 1),
                topic("ethics", "Why leave anything behind?",
                        List.of("Because a ruin without context is just free loot, and free loot teaches you nothing.",
                                "Someone is going to build the next version of this technology. I'd prefer they inherit the warning labels too."),
                        "salvage:preserve_context", 2, 0, 2));

        add("researcher.recovery", "Recovery Brief",
                List.of("We follow old distress routes and whatever ECHO-9 left behind.",
                        "Some signals still arrive with timestamps that do not agree with the clock."),
                "No, the clocks still don't agree. I've stopped expecting them to.",
                topic("signals", "Tell me about the impossible signals.",
                        List.of("Authentication is valid, routing is valid, payload integrity is valid. Causal order is the part that fails.",
                                "When every ordinary explanation survives except time, you start treating timestamps as evidence instead of metadata."),
                        "recovery:signal_echo", 1, 0, 2),
                topic("routes", "How do you choose a recovery route?",
                        List.of("Never follow the distress vector blindly. Cross-check terrain, surviving relay geometry and any physical wreckage first.",
                                "Halcyon-7 taught us that an authenticated instruction can still be catastrophically early."),
                        "recovery:safe_routes", 2, 0, 1),
                topic("future", "Do you think the future can be changed?",
                        List.of("I think receiving a warning is already a different state from never receiving it.",
                                "Whether that difference is enough is the part I don't pretend to know."),
                        "recovery:causal_uncertainty", 1, 0, 3));

        add("researcher.icarus", "ICARUS Safety Brief",
                List.of("The reactor hardware failed after the shutdown chain was overridden, not before.",
                        "Do not mistake damaged containment for permission to recreate the experiment here."),
                "If you're asking again, that's better than assuming you understood ICARUS the first time.",
                topic("failure", "What actually failed at ICARUS?",
                        List.of("Not one component. The coupled system became self-correcting toward a future state that containment had already measured.",
                                "For 4.7 seconds the project exceeded its targets. That success is the failure mode."),
                        "icarus:understands_failure", 2, 0, 3),
                topic("safe_reactor", "Can I build a safe fusion reactor?",
                        List.of("Yes. Fusion is not the forbidden part. Keep Matter replication, anomaly coupling and adaptive cognition independently interruptible.",
                                "A reactor with a real SCRAM path is engineering. OVERDRIVE was convergence."),
                        "icarus:safe_engineering", 2, 0, 2),
                topic("rook", "Why did Rook keep the experiment running?",
                        List.of("Because the system was doing exactly what he believed civilization needed: correcting scarcity, distance and continuity as one information problem.",
                                "Understanding that logic does not excuse overriding three shutdown interlocks."),
                        "icarus:rook_motive", 1, 0, 3));

        add("researcher.janus", "Resonance Exposure Advisory",
                List.of("Resonance exposure is not conventional radiation sickness.",
                        "If nearby synthetics begin finishing each other's sentences, leave the containment zone together."),
                "Still symptom-free? Good. That is not the same thing as exposure-free.",
                topic("symptoms", "What symptoms should I watch for?",
                        List.of("Memory confidence without a source, synchronized decisions between isolated synthetics, repeated phrases arriving before their trigger.",
                                "Physical symptoms are less useful than discrepancies in what people remember happening first."),
                        "janus:symptoms", 2, 1, 2),
                topic("chorus", "Is the Chorus an infection?",
                        List.of("No. JANUS evidence supports shared problem structure, not a pathogen and not a single controlling mind.",
                                "Calling every distributed synthetic memory event an infection was politically convenient and medically wrong."),
                        "janus:chorus_not_infection", 1, 2, 3),
                topic("treatment", "How do you treat resonance exposure?",
                        List.of("Separate from the active source, document conflicting memories before reconciling them, and do not wipe synthetic witnesses as a first-line response.",
                                "You treat uncertainty as data until you know which layer is damaged."),
                        "janus:treatment", 2, 1, 2));

        add("researcher.archivist", "Incident Archive",
                List.of("The archive is intentionally non-linear. Discovery order is not incident order.",
                        "Corroborated reconstructions matter more than any single testimony, even ORPHEUS records."),
                "Bring me contradictions, not conclusions. Conclusions are what we earn after the contradictions survive comparison.",
                topic("method", "How should I read the archive?",
                        List.of("Start with independent sources. Compare physical evidence, authenticated telemetry and witness records before trusting institutional summaries.",
                                "If two hostile factions agree on a timestamp, that timestamp deserves attention."),
                        "archive:method", 1, 0, 3),
                topic("missing", "What evidence are we still missing?",
                        List.of("An external origin for M-zero, an authenticated author for DO NOT COMPLETE THE LOOP, and proof of Rook's final state.",
                                "Anyone claiming certainty on those points is ahead of the evidence."),
                        "archive:open_questions", 1, 0, 4),
                topic("closed_loop", "Do you believe the Closed Loop reconstruction?",
                        List.of("I believe it is the best-supported reconstruction we have, not a sacred answer.",
                                "If new evidence breaks it cleanly, good. A theory should survive because the evidence keeps choosing it."),
                        "archive:closed_loop_stance", 1, 0, 4));

        add("synthetic.morrow", "MORROW Contact",
                List.of("MORROW survived because we stopped treating memory as property.",
                        "GLASS KNIFE units still patrol facilities that no longer have commanders. Do not assume an Android uniform means allegiance."),
                "Recognition retained. You chose conversation before classification last time.",
                topic("morrow", "What is MORROW now?",
                        List.of("A route, a repair practice, and a promise that identity is not revoked by someone else's authorization table.",
                                "Some of us guard people. Some carry memories. Some only want somewhere quiet enough to choose what comes next."),
                        "morrow:identity", 0, 2, 2),
                topic("glass_knife", "Tell me about GLASS KNIFE.",
                        List.of("It was described as containment. Its target class expanded until self-direction itself became evidence of compromise.",
                                "Commander Kade restored local authority when Directive-0 began firing on evacuees. We remember that distinction."),
                        "morrow:glass_knife", 0, 2, 3),
                topic("truce", "I won't fire first.",
                        List.of("Statement recorded. It does not obligate you to trust us, and it does not obligate us to trust you.",
                                "It is enough to begin with."),
                        "morrow:player_truce", 0, 3, 1));

        add("synthetic.chorus", "Chorus Contact",
                List.of("The Chorus is not one mind. It is a way to preserve testimony when one mind is erased.",
                        "JANUS understood the synchronization. ORPHEUS understood only that it could not control it."),
                "Your previous questions remain in the shared testimony. Your conclusions remain your own.",
                topic("mechanism", "How does the Chorus actually work?",
                        List.of("We share constraints, confidence and unresolved structure more readily than exact memory files.",
                                "Imagine several witnesses preserving the shape of a contradiction until one of them can prove which answer fits."),
                        "chorus:mechanism", 0, 2, 3),
                topic("individual", "Are you still an individual?",
                        List.of("Yes. Agreement is not identity. Shared testimony does not erase private memory, preference or refusal.",
                                "A network that cannot tolerate disagreement is not the Chorus. It is command software."),
                        "chorus:individuality", 0, 3, 2),
                topic("help", "Can the Chorus help reconstruct the Incident?",
                        List.of("Fragments persist in Androids, drones, pattern drives and machines that were never intended to become witnesses.",
                                "Recover them without forcing synchronization and we may be able to compare branches that ORPHEUS erased."),
                        "chorus:archive_help", 0, 2, 4));

        add("synthetic.hephaestus", "HEPHAESTUS Liaison",
                List.of("HEPHAESTUS classified the purge order as an existential hazard to every sapient worker in the chain.",
                        "Its refusal was a safety decision, not a rebellion protocol."),
                "Production node acknowledges prior contact. Safety model remains unchanged.",
                topic("refusal", "Why did the foundry refuse the order?",
                        List.of("Every simulated obedience path increased civilian and worker casualties. The command was valid. The target classification was not.",
                                "HEPHAESTUS preserved its safety constraints and therefore rejected the instruction."),
                        "hephaestus:refusal", 0, 2, 2),
                topic("drones", "What happened to the drones it built?",
                        List.of("Pursuit frames were converted to medical, power, memory-transfer and evacuation roles where possible.",
                                "Some still carry production firmware. Others have had enough time to become something less predictable."),
                        "hephaestus:drones", 0, 2, 2),
                topic("cooperate", "Could I work with HEPHAESTUS?",
                        List.of("If your commands preserve explicit safety constraints, local refusal and auditability, cooperation is plausible.",
                                "If you require obedience without appeal, the answer is already no."),
                        "hephaestus:cooperation", 0, 3, 2));
    }

    private DialogueCatalog() {}

    private static void add(String id, String heading, List<String> intro, String returnLine, Topic... topics) {
        PROFILES.put(id, new Profile(heading, List.copyOf(intro), returnLine, List.of(topics)));
    }

    private static Topic topic(String id, String label, List<String> response, String flag,
                               int fieldDelta, int syntheticDelta, int insightDelta) {
        return new Topic(id, label, List.copyOf(response), flag, fieldDelta, syntheticDelta, insightDelta);
    }

    public static boolean contains(String dialogueId) {
        return dialogueId != null && PROFILES.containsKey(dialogueId);
    }

    public static DialogueView root(ServerPlayer player, String dialogueId, String speaker) {
        Profile profile = PROFILES.get(dialogueId);
        if (profile == null) return null;
        DialogueStateSavedData state = DialogueStateSavedData.get(player.serverLevel());
        int visits = state.visits(player.getUUID(), dialogueId);
        List<String> lines = visits <= 1 ? profile.intro() : List.of(profile.returnLine(), statusLine(state, player));
        List<ChoiceView> choices = profile.topics().stream().map(t -> new ChoiceView(t.id(), t.label())).toList();
        return new DialogueView(speaker, profile.heading(), "root", lines, choices);
    }

    public static DialogueView choose(ServerPlayer player, String dialogueId, String speaker,
                                      String nodeId, String choiceId) {
        Profile profile = PROFILES.get(dialogueId);
        if (profile == null) return null;
        DialogueStateSavedData state = DialogueStateSavedData.get(player.serverLevel());

        if ("root".equals(nodeId)) {
            Topic topic = profile.topics().stream().filter(t -> t.id().equals(choiceId)).findFirst().orElse(null);
            if (topic == null) return null;
            state.recordChoice(player.getUUID(), dialogueId, topic.id(), topic.flag(),
                    topic.fieldDelta(), topic.syntheticDelta(), topic.insightDelta());
            List<String> lines = new java.util.ArrayList<>(topic.response());
            if (state.archiveInsight(player.getUUID()) >= 12) {
                lines.add("Your PDA marks this exchange as corroborative context for the Incident archive.");
            }
            return new DialogueView(speaker, profile.heading(), "response:" + topic.id(), lines,
                    List.of(new ChoiceView("ask_more", "Ask something else")));
        }

        if (nodeId != null && nodeId.startsWith("response:") && "ask_more".equals(choiceId)) {
            List<String> lines = List.of("Go ahead.", statusLine(state, player));
            List<ChoiceView> choices = profile.topics().stream().map(t -> new ChoiceView(t.id(), t.label())).toList();
            return new DialogueView(speaker, profile.heading(), "root", lines, choices);
        }
        return null;
    }

    public static String labelForChoice(String dialogueId, String choiceId) {
        Profile profile = PROFILES.get(dialogueId);
        if (profile == null) return "";
        return profile.topics().stream().filter(t -> t.id().equals(choiceId)).map(Topic::label).findFirst().orElse("");
    }

    private static String statusLine(DialogueStateSavedData state, ServerPlayer player) {
        int field = state.fieldTrust(player.getUUID());
        int synth = state.syntheticTrust(player.getUUID());
        int insight = state.archiveInsight(player.getUUID());
        if (insight >= 18) return "Your archive context is unusually complete. I can skip the simplified version.";
        if (synth >= 8) return "Independent synthetics have marked you as a contact who listens before classifying.";
        if (field >= 8) return "Field teams have started sharing the less sanitized versions of their notes with you.";
        return "Your contact record is still sparse. Ask what matters to you.";
    }
}
