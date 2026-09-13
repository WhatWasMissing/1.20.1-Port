package matteroverdrive.client;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persistent client preference for spoken PDA callouts and read-aloud narration. */
public final class PdaVoiceSettings {
    private static final String CONFIG_DIRECTORY = "config";
    private static final String CONFIG_FILE = "matteroverdrive_pda_voice.dat";

    private static boolean loaded;
    private static boolean enabled = true;

    private PdaVoiceSettings() { }

    public static synchronized boolean isEnabled() {
        load();
        return enabled;
    }

    public static boolean toggle() {
        boolean next;
        synchronized (PdaVoiceSettings.class) {
            load();
            enabled = !enabled;
            next = enabled;
            save();
        }
        if (!next) {
            // Disabling voice must also silence a line that is already speaking.
            PdaNarrationController.stop();
        }
        return next;
    }

    public static String buttonLabel() {
        return "VOICE: " + (isEnabled() ? "ON" : "OFF");
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        Path file = configFile();
        try {
            if (!Files.isRegularFile(file)) return;
            for (String line : Files.readAllLines(file)) {
                if (line.startsWith("enabled=")) {
                    enabled = Boolean.parseBoolean(line.substring("enabled=".length()).trim());
                    break;
                }
            }
        } catch (IOException ignored) {
            // A missing/unreadable preference must preserve the safe enabled default.
        }
    }

    private static void save() {
        try {
            Path file = configFile();
            Files.createDirectories(file.getParent());
            Files.writeString(file, "# Matter Overdrive PDA voice preference\nenabled=" + enabled + "\n");
        } catch (IOException ignored) {
            // Voice remains usable for this session even if the preference cannot persist.
        }
    }

    private static Path configFile() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve(CONFIG_DIRECTORY)
                .resolve(CONFIG_FILE);
    }
}
