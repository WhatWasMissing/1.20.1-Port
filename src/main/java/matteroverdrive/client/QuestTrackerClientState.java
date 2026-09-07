package matteroverdrive.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Client-side snapshot of server-authoritative quest progress for the HUD tracker. */
public final class QuestTrackerClientState {
    private static List<Entry> entries = List.of();

    private QuestTrackerClientState() {}

    public static void set(List<Entry> newEntries) {
        entries = Collections.unmodifiableList(new ArrayList<>(newEntries));
    }

    public static List<Entry> entries() { return entries; }

    public record Entry(String title, String objective, String progress, String stage) {}
}
