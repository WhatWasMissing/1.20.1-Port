package matteroverdrive.world;

/**
 * Legacy crash-debris generation is temporarily disabled.
 *
 * The previous implementation subscribed to ChunkEvent.Load and then queried the
 * ServerLevel heightmap / placed blocks from inside the chunk-load callback. That
 * can re-enter chunk loading while the integrated server is still finalising the
 * spawn region, which is unsafe and can stall world entry at 100%.
 *
 * The debris feature will be reintroduced through the normal worldgen pipeline
 * once the 0.6 world-load regression is fully isolated.
 */
public final class LegacyWorldEvents {
    private LegacyWorldEvents() {}
}
