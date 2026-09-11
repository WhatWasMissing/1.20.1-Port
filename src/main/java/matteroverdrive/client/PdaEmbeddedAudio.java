package matteroverdrive.client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Plays the original offline-generated PDA voice/UI assets embedded in the jar.
 * The compressed WAV pack is stored as small base64 text chunks because the
 * repository connector is text-only. Runtime playback performs no file writes,
 * downloads or cloud TTS calls.
 */
public final class PdaEmbeddedAudio {
    private static final int PACK_PARTS = 16;
    private static final Map<String, byte[]> WAVS = new HashMap<>();
    private static boolean loaded;
    private static Clip activeVoice;

    private PdaEmbeddedAudio() {}

    public static synchronized int playVoice(String id) {
        stopVoice();
        Clip clip = open(id + ".wav", true);
        if (clip == null) return 0;
        activeVoice = clip;
        clip.start();
        return Math.max(20, (int) Math.ceil(clip.getMicrosecondLength() / 50_000.0D));
    }

    public static synchronized void stopVoice() {
        if (activeVoice == null) return;
        try {
            activeVoice.stop();
            activeVoice.close();
        } catch (Exception ignored) {
        }
        activeVoice = null;
    }

    public static boolean playUi(String id) {
        Clip clip = open(id + ".wav", false);
        if (clip == null) return false;
        clip.start();
        return true;
    }

    public static boolean has(String id) {
        ensureLoaded();
        return WAVS.containsKey(id + ".wav");
    }

    private static Clip open(String name, boolean voice) {
        try {
            ensureLoaded();
            byte[] wav = WAVS.get(name);
            if (wav == null) return null;
            AudioInputStream source = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav));
            AudioFormat base = source.getFormat();
            AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(), 16, base.getChannels(),
                    Math.max(1, base.getChannels()) * 2, base.getSampleRate(), false);
            AudioInputStream decoded = AudioSystem.isConversionSupported(pcm, base)
                    ? AudioSystem.getAudioInputStream(pcm, source) : source;
            Clip clip = AudioSystem.getClip();
            clip.open(decoded);
            decoded.close();
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), voice ? -7.0F : -10.0F)));
            }
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    try { clip.close(); } catch (Exception ignored) { }
                    synchronized (PdaEmbeddedAudio.class) {
                        if (clip == activeVoice) activeVoice = null;
                    }
                }
            });
            return clip;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static synchronized void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try {
            StringBuilder encoded = new StringBuilder(800_000);
            for (int i = 0; i < PACK_PARTS; i++) {
                encoded.append(readText(String.format(
                        "/assets/matteroverdrive/pda_audio/pda_audio_pack.part%02d.b64", i)));
            }
            if (encoded.isEmpty()) return;
            byte[] zipBytes = Base64.getMimeDecoder().decode(encoded.toString());
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".wav")) {
                        WAVS.put(entry.getName(), zip.readAllBytes());
                    }
                    zip.closeEntry();
                }
            }
        } catch (Throwable ignored) {
            WAVS.clear();
        }
    }

    private static String readText(String resource) throws Exception {
        try (InputStream stream = PdaEmbeddedAudio.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("Missing embedded PDA audio chunk: " + resource);
            return new String(stream.readAllBytes(), StandardCharsets.US_ASCII).replaceAll("\\s+", "");
        }
    }
}
