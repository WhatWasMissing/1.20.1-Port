package matteroverdrive.client;

/** Client endpoint for short contextual PDA callouts. */
public final class ClientPdaVoiceOpener {
    private ClientPdaVoiceOpener() {}

    public static void play(String lineId) {
        ClientPdaNotificationManager.enqueue(lineId);
    }
}
