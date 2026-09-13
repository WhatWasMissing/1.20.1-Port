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
    TRAX_CALLUM_1("destiny_traxcallum1", "Trax Callum 1", 6.0F, 88, 200, 13, 54, 550, 0.15F, false, "destiny_traxcallum1", null, null, "destiny_traxmallus1_reload");

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
