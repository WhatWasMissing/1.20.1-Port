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
 *
 * GitHub's text-only connector cannot commit binary audio directly, so the WAV
 * pack is stored as base64 text and decoded in memory. No files are written and
 * no network/TTS service is required at runtime. Java Sound failures are treated
 * as optional-audio failures; the PDA caption/narrator fallbacks remain usable.
 */
public final class PdaEmbeddedAudio {
    private static final String PACK_RESOURCE = "/assets/matteroverdrive/pda_audio/pda_audio_pack.zip.b64";
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
            AudioFormat pcm = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(),
                    16,
                    base.getChannels(),
                    Math.max(1, base.getChannels()) * 2,
                    base.getSampleRate(),
                    false);
            AudioInputStream decoded = AudioSystem.isConversionSupported(pcm, base)
                    ? AudioSystem.getAudioInputStream(pcm, source)
                    : source;

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
        try (InputStream stream = PdaEmbeddedAudio.class.getResourceAsStream(PACK_RESOURCE)) {
            if (stream == null) return;
            String encoded = new String(stream.readAllBytes(), StandardCharsets.US_ASCII);
            byte[] zipBytes = Base64.getMimeDecoder().decode(encoded);
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
}
