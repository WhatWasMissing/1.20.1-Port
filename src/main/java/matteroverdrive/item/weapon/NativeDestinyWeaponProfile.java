package matteroverdrive.item.weapon;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public enum NativeDestinyWeaponProfile {
    ACE_OF_SPADES("destiny_aceofspades", "Ace of Spades", 11.0F, 72, 140, 10, 59, 1000, 0.15F, false, "destiny_aceofspades", "destiny_aceofspades_3p", null, "destiny_aceofspades_reload"),
    CHAOS_DOGMA("destiny_chaosdogma", "CHAOS DOGMA~", 7.0F, 88, 210, 16, 49, 650, 0.15F, false, "destiny_chaosdogma", null, null, "destiny_cd_reload"),
    EYASLUNA("destiny_eyasluna", "Eyasluna", 8.0F, 72, 140, 11, 55, 750, 0.15F, false, "destiny_eyasluna", null, null, null),
    HAWKMOON("destiny_hawkmoon", "Hawkmoon", 11.0F, 72, 140, 13, 55, 1000, 0.15F, false, "destiny_hawkmoon", "destiny_hawkmoon_3p", "destiny_hawkmoon_draw", "destiny_hawkmoon_reload"),
    KHVOSTOV_7G02("destiny_khvostov7g02", "Khvostov 7G-02", 5.0F, 72, 600, 32, 49, 550, 0.65F, true, "destiny_khvostov7g02", "destiny_khvostov7g02_3p", "destiny_khvostov_draw", "destiny_khvostov_reload"),
    MARSHAL_A1("destiny_marshala1", "Marshal-A1", 5.0F, 72, 600, 26, 49, 550, 0.65F, true, "destiny_marshala1", null, null, "destiny_marshal_reload"),
    MIDA_MULTI_TOOL("destiny_midamultitool", "MIDA Multi-Tool", 8.0F, 88, 220, 16, 49, 750, 0.15F, false, "destiny_midamultitool", "destiny_midamultitool_3p", "destiny_midamultitool_draw", "destiny_midamultitool_reload"),
    MONTE_CARLO("destiny_montecarlo", "Monte Carlo", 6.0F, 72, 600, 36, 43, 650, 0.65F, true, "destiny_montecarlo", "destiny_montecarlo_3p", null, "destiny_montecarlo_reload"),
    PROXIMA_CENTAURI_II("destiny_proximacentauriii", "Proxima Centauri II", 7.0F, 88, 200, 14, 54, 650, 0.15F, false, "destiny_proximacentauriii", null, null, null),
    SLEEPER_SIMULANT("destiny_sleepersimulant", "Sleeper Simulant", 30.0F, 112, 120, 3, 51, 4000, 0.02F, false, "destiny_sleepersimulant_fire", "destiny_sleepersimulant_3p", "destiny_sleepersimulant_draw", "destiny_sleepersimulant_reload"),
    SUROS_REGIME("destiny_surosregime", "SUROS Regime", 5.0F, 72, 600, 33, 49, 550, 0.65F, true, "destiny_surosregime", "destiny_surosregime_3p", null, "destiny_surosregime_reload"),
    THE_LAST_WORD("destiny_thelastword", "The Last Word", 11.0F, 72, 225, 8, 48, 1000, 0.15F, false, "destiny_thelastword", "destiny_thelastword_3p", "destiny_thelastword_draw", "destiny_thelastword_reload"),
    THORN("destiny_thorn", "Thorn", 11.0F, 72, 140, 9, 48, 1000, 0.15F, false, "destiny_thorn", "destiny_thorn_3p", "destiny_thorn_draw", "destiny_thorn_reload"),
    TRAX_CALLUM_1("destiny_traxcallum1", "Trax Callum 1", 6.0F, 88, 200, 13, 54, 550, 0.15F, false, "destiny_traxcallum1", null, null, "destiny_traxmallus1_reload"),

    // Additional complete GunPack conversions. These remain native Matter Overdrive
    // energy weapons: the source geometry/audio is preserved, while FE cost, capacitor
    // size, module compatibility and server-side hitscan behavior use the common port.
    FOURTH_HORSEMAN("destiny_4thhorseman", "The Fourth Horseman", 17.0F, 52, 240, 5, 66, 1800, 4.5F, false, "destiny_4thhorseman", "destiny_4thhorseman_3p", null, null),
    BAD_JUJU("destiny_badjuju", "Bad Juju", 8.0F, 88, 450, 24, 58, 800, 0.75F, true, "destiny_badjuju", "destiny_badjuju_3p", "destiny_badjuju_draw", "destiny_badjuju_reload"),
    BXR_55_BATTLER("destiny_bxr55battler", "BXR-55 Battler", 9.0F, 92, 450, 36, 62, 900, 0.65F, true, "destiny_bxr55battler", "destiny_bxr55battler_3p", null, "destiny_bxr55battler_reload"),
    CHAPERONE("destiny_chaperone", "Chaperone", 24.0F, 80, 70, 6, 58, 1700, 0.05F, false, "destiny_chaperone", "destiny_chaperone_3p", "destiny_chaperone_draw", null),
    CLOUDSTRIKE("destiny_cloudstrike", "Cloudstrike", 32.0F, 120, 90, 6, 65, 2200, 0.05F, false, "destiny_cloudstrike", "destiny_cloudstrike_3p", "destiny_cloudstrike_draw", "destiny_cloudstrike_reload"),
    DEAD_MANS_TALE("destiny_deadman", "Dead Man's Tale", 16.0F, 100, 150, 14, 55, 1300, 0.20F, false, "destiny_deadman", "destiny_deadman_3p", "destiny_deadman_draw", "destiny_deadman_reload"),
    FABIAN_STRATEGY("destiny_fabianstrategy", "Fabian Strategy", 5.5F, 76, 600, 30, 48, 600, 0.80F, true, "destiny_fabianstrategy", "destiny_fabianstrategy_3p", "destiny_fabianstrategy_draw", "destiny_fabianstrategy_reload"),
    FATEBRINGER("destiny_fatebringer", "Fatebringer", 10.0F, 84, 140, 12, 52, 950, 0.15F, false, "destiny_fatebringer", "destiny_fatebringer_3p", null, "destiny_fatebringer_reload"),
    FIRST_CURSE("destiny_firstcurse", "The First Curse", 12.0F, 96, 100, 8, 58, 1100, 0.08F, false, "destiny_firstcurse", "destiny_firstcurse_3p", "destiny_firstcurse_draw", "destiny_firstcurse_reload"),
    GJALLARHORN("destiny_gjallarhorn", "Gjallarhorn", 36.0F, 112, 30, 2, 75, 6500, 0.40F, false, "destiny_gjallarhorn", "destiny_gjallarhorn_3p", "destiny_gjallarhorn_draw", "destiny_gjallarhorn_reload"),
    GRAVITON_LANCE("destiny_gravitonlance", "Graviton Lance", 12.0F, 120, 300, 30, 60, 1100, 0.35F, false, "destiny_gravitonlance", "destiny_gravitonlance_3p", "destiny_gravitonlance_draw", "destiny_gravitonlance_reload"),
    HAMMERHEAD("destiny_hammerhead", "Hammerhead", 7.0F, 100, 450, 45, 72, 950, 0.80F, true, "destiny_hammerhead", "destiny_hammerhead_3p", "destiny_hammerhead_draw", "destiny_hammerhead_reload"),
    HARD_LIGHT("destiny_hardlight", "Hard Light", 6.0F, 100, 600, 30, 48, 700, 0.55F, true, "destiny_hardlight", "destiny_hardlight_3p", "destiny_hardlight_draw", "destiny_hardlight_reload"),
    ICE_BREAKER("destiny_icebreaker", "Ice Breaker", 32.0F, 120, 60, 6, 75, 2500, 0.04F, false, "destiny_icebreaker", "destiny_icebreaker_3p", null, null),
    JADE_RABBIT("destiny_jaderabbit", "The Jade Rabbit", 15.0F, 112, 150, 15, 60, 1500, 0.08F, false, "destiny_jaderabbit", "destiny_jaderabbit_3p", null, "destiny_jaderabbit_reload"),
    LEGEND_OF_ACRIUS("destiny_legendofacrius", "Legend of Acrius", 28.0F, 70, 45, 2, 80, 4000, 0.50F, false, "destiny_legendofacrius", "destiny_legendofacrius_3p", "destiny_legendofacrius_draw", "destiny_legendofacrius_reload"),
    LORD_OF_WOLVES("destiny_lordofwolves", "Lord of Wolves", 7.5F, 56, 300, 30, 65, 1600, 1.50F, true, "destiny_lordofwolves", "destiny_lordofwolves_3p", "destiny_lordofwolves_draw", "destiny_lordofwolves_reload"),
    MIDNIGHT_COUP("destiny_midnightcoup", "Midnight Coup", 10.0F, 84, 140, 12, 52, 950, 0.18F, false, "destiny_midnightcoup", "destiny_midnightcoup_3p", null, "destiny_midnightcoup_reload"),
    NECROCHASM("destiny_necrochasm", "Necrochasm", 6.0F, 82, 900, 50, 68, 600, 0.90F, true, "destiny_necrochasm", "destiny_necrochasm_3p", "destiny_necrochasm_draw", "destiny_necrochasm_reload"),
    NO_LAND_BEYOND("destiny_nolandbeyond", "No Land Beyond", 35.0F, 128, 50, 1, 75, 3500, 0.0F, false, "destiny_nolandbeyond", "destiny_nolandbeyond_3p", null, "destiny_nolandbeyond_reload"),
    ORIGIN_STORY("destiny_originstory", "Origin Story", 6.0F, 88, 600, 31, 52, 700, 0.70F, true, "destiny_originstory", "destiny_originstory_3p", "destiny_originstory_draw", "destiny_originstory_reload"),
    OUTBREAK_PRIME("destiny_outbreakprime", "Outbreak Prime", 8.0F, 100, 450, 33, 62, 900, 0.70F, true, "destiny_outbreakprime", "destiny_outbreakprime_3p", "destiny_outbreakprime_draw", "destiny_outbreakprime_reload"),
    RED_DEATH("destiny_reddeath", "Red Death", 9.5F, 100, 400, 30, 58, 800, 0.70F, true, "destiny_reddeath", "destiny_reddeath_3p", "destiny_reddeath_draw", "destiny_reddeath_reload"),
    STOLEN_WILL("destiny_stolenwill", "Stolen Will", 18.0F, 56, 90, 6, 60, 1800, 3.50F, false, "destiny_stolenwill", "destiny_stolenwill_3p", null, null),
    THUNDERLORD("destiny_thunderlord", "Thunderlord", 9.0F, 90, 450, 60, 78, 1500, 1.10F, true, "destiny_thunderlord", "destiny_thunderlord_3p", "destiny_thunderlord_draw", "destiny_thunderlord_reload"),
    TLALOC("destiny_tlaloc", "Tlaloc", 14.0F, 96, 180, 18, 56, 1200, 0.10F, false, "destiny_tlaloc", "destiny_tlaloc_3p", "destiny_tlaloc_draw", "destiny_tlaloc_reload"),
    TOMMYS_MATCHBOOK("destiny_tommysmatchbook", "Tommy's Matchbook", 6.0F, 80, 720, 30, 65, 850, 0.80F, true, "destiny_tommysmatchbook", "destiny_tommysmatchbook_3p", "destiny_tommysmatchbook_draw", "destiny_tommysmatchbook_reload"),
    TOUCH_OF_MALICE("destiny_touchofmalice", "Touch of Malice", 9.0F, 96, 260, 30, 70, 1000, 0.70F, true, "destiny_touchofmalice", "destiny_touchofmalice_3p", "destiny_touchofmalice_draw", "destiny_touchofmalice_reload"),
    TRESPASSER("destiny_trespasser", "Trespasser", 8.0F, 60, 300, 18, 55, 900, 1.50F, true, "destiny_trespasser", "destiny_trespasser_3p", "destiny_trespasser_draw", "destiny_trespasser_reload"),
    TRUTH("destiny_truth", "Truth", 34.0F, 112, 30, 2, 75, 5000, 0.40F, false, "destiny_truth", "destiny_truth_3p", "destiny_truth_draw", "destiny_truth_reload"),
    UNIVERSAL_REMOTE("destiny_universalremote", "Universal Remote", 18.0F, 56, 80, 8, 70, 1900, 4.0F, false, "destiny_universalremote", "destiny_universalremote_3p", null, null),
    WHISPER("destiny_whisper", "Whisper of the Worm", 36.0F, 128, 45, 3, 82, 4800, 0.01F, false, "destiny_whisper", "destiny_whisper_3p", "destiny_whisper_draw", "destiny_whisper_reload"),
    WITHERHOARD("destiny_witherhoard", "Witherhoard", 22.0F, 80, 40, 6, 82, 3000, 0.50F, false, "destiny_witherhoard", "destiny_witherhoard_3p", "destiny_witherhoard_draw", "destiny_witherhoard_reload"),
    ZEN_METEOR("destiny_zenmeteor", "Zen Meteor", 38.0F, 120, 60, 4, 80, 3800, 0.03F, false, "destiny_zenmeteor", "destiny_zenmeteor_3p", null, "destiny_zenmeteor_reload"),
    ZHALO("destiny_zhalo", "Zhalo Supercell", 6.5F, 88, 600, 36, 58, 800, 0.65F, true, "destiny_zhalo", "destiny_zhalo_3p", null, null);

    private static final Map<String, NativeDestinyWeaponProfile> BY_ID = new LinkedHashMap<>();

    static {
        Arrays.stream(values()).forEach(profile -> BY_ID.put(profile.id, profile));
    }

    private final String id;
    private final String displayName;
    private final float damage;
    private final int range;
    private final int rpm;
    private final int magazine;
    private final int reloadTicks;
    private final int energyPerShot;
    private final float spreadDegrees;
    private final boolean automatic;
    private final String fireSound;
    private final String thirdPersonFireSound;
    private final String drawSound;
    private final String reloadSound;

    NativeDestinyWeaponProfile(String id, String displayName, float damage, int range, int rpm, int magazine,
                               int reloadTicks, int energyPerShot, float spreadDegrees, boolean automatic,
                               String fireSound, String thirdPersonFireSound, String drawSound, String reloadSound) {
        this.id = id;
        this.displayName = displayName;
        this.damage = damage;
        this.range = range;
        this.rpm = rpm;
        this.magazine = magazine;
        this.reloadTicks = reloadTicks;
        this.energyPerShot = energyPerShot;
        this.spreadDegrees = spreadDegrees;
        this.automatic = automatic;
        this.fireSound = fireSound;
        this.thirdPersonFireSound = thirdPersonFireSound;
        this.drawSound = drawSound;
        this.reloadSound = reloadSound;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public float damage() { return damage; }
    public int range() { return range; }
    public int rpm() { return rpm; }
    public int magazine() { return magazine; }
    public int reloadTicks() { return reloadTicks; }
    public int energyPerShot() { return energyPerShot; }
    public float spreadDegrees() { return spreadDegrees; }
    public boolean automatic() { return automatic; }
    public String fireSound() { return fireSound; }
    public String thirdPersonFireSound() { return thirdPersonFireSound; }
    public String drawSound() { return drawSound; }
    public String reloadSound() { return reloadSound; }
    public int cooldownTicks() { return Math.max(1, Math.round(1200.0F / Math.max(1, rpm))); }

    public static NativeDestinyWeaponProfile byId(String id) {
        return BY_ID.get(id);
    }
}
