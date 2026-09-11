package matteroverdrive.client;

import matteroverdrive.pda.PdaVoiceLineCatalog;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * Offline audio backend for the PDA presentation layer.
 *
 * Voice callouts use speech engines already present on the operating system:
 * Windows System.Speech via PowerShell, macOS `say`, or Linux `espeak`/`spd-say`.
 * Nothing is uploaded and no cloud account is required. If none are available,
 * the notification manager transparently falls back to Minecraft Narrator.
 *
 * Short UI cues are synthesized directly in Java, so startup, archive, hazard,
 * facility and comm sounds are always original and require no binary assets.
 */
public final class PdaEmbeddedAudio {
    private static Process activeVoice;

    private PdaEmbeddedAudio() {}

    public static synchronized int playVoice(String id) {
        String text = PdaVoiceLineCatalog.line(id);
        if (text.isBlank()) return 0;
        stopVoice();
        Process process = startOfflineTts(text);
        if (process == null) return 0;
        activeVoice = process;

        Thread waiter = new Thread(() -> {
            try { process.waitFor(); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            synchronized (PdaEmbeddedAudio.class) {
                if (activeVoice == process) activeVoice = null;
            }
        }, "matteroverdrive-pda-voice");
        waiter.setDaemon(true);
        waiter.start();
        return estimateDurationTicks(text);
    }

    public static synchronized void stopVoice() {
        Process process = activeVoice;
        activeVoice = null;
        if (process == null) return;
        try {
            process.destroy();
            if (process.isAlive()) process.destroyForcibly();
        } catch (Throwable ignored) {
        }
    }

    public static synchronized boolean isVoicePlaying() {
        return activeVoice != null && activeVoice.isAlive();
    }

    public static boolean playUi(String id) {
        double[][] pattern = switch (id == null ? "" : id) {
            case "ui_startup" -> new double[][]{{620, 70}, {880, 80}, {1240, 120}};
            case "ui_record" -> new double[][]{{880, 55}, {1320, 60}, {1760, 90}};
            case "ui_facility" -> new double[][]{{440, 65}, {660, 75}, {990, 120}};
            case "ui_hazard" -> new double[][]{{360, 95}, {0, 55}, {360, 95}};
            case "ui_comm" -> new double[][]{{1180, 45}, {760, 70}};
            default -> null;
        };
        if (pattern == null) return false;
        try {
            final int sampleRate = 16_000;
            byte[] pcm = synthesize(pattern, sampleRate);
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            Clip clip = AudioSystem.getClip();
            clip.open(format, pcm, 0, pcm.length);
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    try { clip.close(); } catch (Throwable ignored) { }
                }
            });
            clip.start();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean has(String id) {
        if (PdaVoiceLineCatalog.contains(id)) return true;
        return switch (id == null ? "" : id) {
            case "ui_startup", "ui_record", "ui_facility", "ui_hazard", "ui_comm" -> true;
            default -> false;
        };
    }

    private static Process startOfflineTts(String text) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            String script = "Add-Type -AssemblyName System.Speech; "
                    + "$s=New-Object System.Speech.Synthesis.SpeechSynthesizer; "
                    + "$v=$s.GetInstalledVoices() | Where-Object {$_.Enabled -and $_.VoiceInfo.Culture.Name -like 'en-*' -and $_.VoiceInfo.Gender -eq 'Female'} | Select-Object -First 1; "
                    + "if(-not $v){$v=$s.GetInstalledVoices() | Where-Object {$_.Enabled -and $_.VoiceInfo.Culture.Name -like 'en-*'} | Select-Object -First 1}; "
                    + "if($v){$s.SelectVoice($v.VoiceInfo.Name)}; $s.Rate=-1; $s.Volume=78; $s.Speak($env:MO_PDA_TEXT); $s.Dispose();";
            ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive",
                    "-WindowStyle", "Hidden", "-ExecutionPolicy", "Bypass", "-Command", script);
            builder.environment().put("MO_PDA_TEXT", text);
            return start(builder);
        }
        if (os.contains("mac")) {
            return start(new ProcessBuilder("say", "-r", "170", text));
        }
        Process linux = start(new ProcessBuilder("espeak", "-v", "en+f3", "-s", "154", "-p", "40", "-a", "125", text));
        if (linux != null) return linux;
        return start(new ProcessBuilder("spd-say", "-r", "-10", "-p", "-15", text));
    }

    private static Process start(ProcessBuilder builder) {
        try {
            builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);
            return builder.start();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int estimateDurationTicks(String text) {
        String trimmed = text.trim();
        int words = trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
        return Math.max(55, Math.min(360, 24 + words * 8));
    }

    private static byte[] synthesize(double[][] pattern, int sampleRate) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (double[] step : pattern) {
            double frequency = step[0];
            int samples = Math.max(1, (int) Math.round(sampleRate * step[1] / 1000.0D));
            for (int i = 0; i < samples; i++) {
                double edge = Math.min(1.0D, Math.min(i / 80.0D, (samples - 1 - i) / 80.0D));
                double wave = frequency <= 0.0D ? 0.0D
                        : Math.sin(2.0D * Math.PI * frequency * i / sampleRate)
                        + 0.16D * Math.sin(4.0D * Math.PI * frequency * i / sampleRate);
                short sample = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE,
                        wave * edge * 0.16D * Short.MAX_VALUE));
                out.write(sample & 0xFF);
                out.write((sample >>> 8) & 0xFF);
            }
        }
        return out.toByteArray();
    }
}
