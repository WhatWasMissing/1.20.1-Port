package matteroverdrive.pda;

import matteroverdrive.MatterOverdrive;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Stable PDA message catalogue.
 *
 * Each entry has a durable event id for future optional speech/voice routing, a
 * translation key for resource packs, and an English fallback so PDA gameplay
 * never depends on an external voice generator or even a populated lang entry.
 */
public enum PdaMessage {
    SCREEN_TITLE("screen.title", "Matter Overdrive PDA"),
    FIELD_LOG_BUTTON("screen.field_log", "FIELD LOG"),
    CONTRACTS_BUTTON("screen.contracts", "CONTRACTS"),
    MANUAL_BUTTON("screen.manual", "MANUAL"),
    ABANDON_BUTTON("screen.abandon", "ABANDON"),
    ABANDON_CONFIRM_BUTTON("screen.abandon_confirm", "ABANDON?"),
    FIELD_LOG_HEADER("screen.field_log_header", "FIELD LOG // YOUR EXPERIMENTS"),
    CONTRACTS_HEADER("screen.contracts_header", "ACTIVE CONTRACTS"),
    EMPTY_LOG("screen.empty_log", "No entries yet. Build something and experiment."),
    OLDER_ENTRIES("screen.older_entries", "… older entries retained"),
    NO_ACTIVE_CONTRACTS("screen.no_active_contracts", "No active contracts."),
    RESEARCH_SAMPLE_RECORDED("notification.research_sample_recorded", "Research sample recorded: %s"),
    SCAN_RECORDED("notification.scan_recorded", "PDA recorded: %s"),
    TOOLTIP_RESEARCH_SCANNER("tooltip.research_scanner", "Scientist research scanner"),
    TOOLTIP_PERSONAL_SCANNER("tooltip.personal_scanner", "Personal research log and matter scanner"),
    TOOLTIP_USE("tooltip.use", "Use on blocks to analyse them; use in air to open the PDA.");

    private final ResourceLocation id;
    private final String translationKey;
    private final String fallback;

    PdaMessage(String path, String fallback) {
        this.id = new ResourceLocation(MatterOverdrive.MOD_ID, "pda/" + path.replace('.', '/'));
        this.translationKey = "matteroverdrive.pda." + path;
        this.fallback = fallback;
    }

    public ResourceLocation id() {
        return id;
    }

    public String translationKey() {
        return translationKey;
    }

    public MutableComponent component(Object... args) {
        return net.minecraft.network.chat.Component.translatableWithFallback(translationKey, fallback, args);
    }
}
