package matteroverdrive.dialogue;

import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

/**
 * Read-only faction reputation derived from the existing authoritative dialogue state.
 * No second reputation ledger is maintained: choices already authenticated by the
 * server remain the source of truth.
 */
public final class FactionReputation {
    public enum Faction {
        RECOVERY_NETWORK("Recovery Network"),
        MORROW("MORROW"),
        CHORUS("Chorus"),
        HEPHAESTUS("HEPHAESTUS"),
        ARCHIVE_COUNCIL("Archive Council");

        private final String title;
        Faction(String title) { this.title = title; }
        public String title() { return title; }
    }

    public enum Tier {
        DISTRUSTED("Distrusted", -20),
        UNVERIFIED("Unverified", -4),
        COOPERATIVE("Cooperative", 4),
        TRUSTED("Trusted", 8),
        ALLIED("Allied", 14);

        private final String title;
        private final int threshold;
        Tier(String title, int threshold) {
            this.title = title;
            this.threshold = threshold;
        }
        public String title() { return title; }
        public int threshold() { return threshold; }
        public boolean atLeast(Tier other) { return ordinal() >= other.ordinal(); }
    }

    private FactionReputation() {}

    public static int score(ServerPlayer player, Faction faction) {
        DialogueStateSavedData state = DialogueStateSavedData.get(player.serverLevel());
        var id = player.getUUID();
        int field = state.fieldTrust(id);
        int synthetic = state.syntheticTrust(id);
        int insight = state.archiveInsight(id);

        int value = switch (faction) {
            case RECOVERY_NETWORK -> field + Math.min(4, insight / 8)
                    + flag(state, id, "field:preserve_evidence")
                    + flag(state, id, "recovery:safe_routes")
                    + flag(state, id, "icarus:safe_engineering");
            case MORROW -> synthetic
                    + 2 * flag(state, id, "morrow:player_truce")
                    + flag(state, id, "morrow:identity")
                    + flag(state, id, "morrow:glass_knife");
            case CHORUS -> synthetic + Math.min(6, insight / 8)
                    + flag(state, id, "chorus:individuality")
                    + 2 * flag(state, id, "chorus:archive_help")
                    + flag(state, id, "janus:chorus_not_infection");
            case HEPHAESTUS -> synthetic
                    + 2 * flag(state, id, "hephaestus:cooperation")
                    + flag(state, id, "hephaestus:refusal")
                    + flag(state, id, "hephaestus:drones");
            case ARCHIVE_COUNCIL -> Math.min(20, insight / 4 + Math.max(0, field / 2))
                    + flag(state, id, "archive:method")
                    + flag(state, id, "archive:open_questions")
                    + flag(state, id, "archive:closed_loop_stance");
        };
        return Math.max(-20, Math.min(20, value));
    }

    public static Tier tier(ServerPlayer player, Faction faction) {
        int score = score(player, faction);
        if (score >= Tier.ALLIED.threshold()) return Tier.ALLIED;
        if (score >= Tier.TRUSTED.threshold()) return Tier.TRUSTED;
        if (score >= Tier.COOPERATIVE.threshold()) return Tier.COOPERATIVE;
        if (score >= Tier.UNVERIFIED.threshold()) return Tier.UNVERIFIED;
        return Tier.DISTRUSTED;
    }

    public static String status(ServerPlayer player, Faction faction) {
        return faction.title() + ": " + tier(player, faction).title() + " (" + score(player, faction) + ")";
    }

    public static Faction forDialogue(String dialogueId) {
        String id = dialogueId == null ? "" : dialogueId.toLowerCase(Locale.ROOT);
        if (id.equals("synthetic.morrow")) return Faction.MORROW;
        if (id.equals("synthetic.chorus")) return Faction.CHORUS;
        if (id.equals("synthetic.hephaestus")) return Faction.HEPHAESTUS;
        if (id.equals("researcher.archivist")) return Faction.ARCHIVE_COUNCIL;
        return Faction.RECOVERY_NETWORK;
    }

    private static int flag(DialogueStateSavedData state, java.util.UUID player, String flag) {
        return state.hasFlag(player, flag) ? 1 : 0;
    }
}
