package matteroverdrive.item.weapon;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public enum NativeDestinyWeaponProfile {
    ACE_OF_SPADES("destiny_aceofspades", "Ace of Spades", 11.0F, 72, 140, 10, 59, 1000, 0.15F, false, "destiny_aceofspades"),
    CHAOS_DOGMA("destiny_chaosdogma", "CHAOS DOGMA~", 7.0F, 88, 210, 16, 49, 650, 0.15F, false, "destiny_chaosdogma"),
    EYASLUNA("destiny_eyasluna", "Eyasluna", 8.0F, 72, 140, 11, 55, 750, 0.15F, false, "destiny_eyasluna"),
    HAWKMOON("destiny_hawkmoon", "Hawkmoon", 11.0F, 72, 140, 13, 55, 1000, 0.15F, false, "destiny_hawkmoon"),
    KHVOSTOV_7G02("destiny_khvostov7g02", "Khvostov 7G-02", 5.0F, 72, 600, 32, 49, 550, 0.65F, true, "destiny_khvostov7g02"),
    MARSHAL_A1("destiny_marshala1", "Marshal-A1", 5.0F, 72, 600, 26, 49, 550, 0.65F, true, "destiny_marshala1"),
    MIDA_MULTI_TOOL("destiny_midamultitool", "MIDA Multi-Tool", 8.0F, 88, 220, 16, 49, 750, 0.15F, false, "destiny_midamultitool"),
    MONTE_CARLO("destiny_montecarlo", "Monte Carlo", 6.0F, 72, 600, 36, 43, 650, 0.65F, true, "destiny_montecarlo"),
    PROXIMA_CENTAURI_II("destiny_proximacentauriii", "Proxima Centauri II", 7.0F, 88, 200, 14, 54, 650, 0.15F, false, "destiny_proximacentauriii"),
    SLEEPER_SIMULANT("destiny_sleepersimulant", "Sleeper Simulant", 30.0F, 112, 120, 3, 51, 4000, 0.02F, false, "destiny_sleepersimulant_fire"),
    SUROS_REGIME("destiny_surosregime", "SUROS Regime", 5.0F, 72, 600, 33, 49, 550, 0.65F, true, "destiny_surosregime"),
    THE_LAST_WORD("destiny_thelastword", "The Last Word", 11.0F, 72, 225, 8, 48, 1000, 0.15F, false, "destiny_thelastword"),
    THORN("destiny_thorn", "Thorn", 11.0F, 72, 140, 9, 48, 1000, 0.15F, false, "destiny_thorn"),
    TRAX_CALLUM_1("destiny_traxcallum1", "Trax Callum 1", 6.0F, 88, 200, 13, 54, 550, 0.15F, false, "destiny_traxcallum1");

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

    NativeDestinyWeaponProfile(String id, String displayName, float damage, int range, int rpm, int magazine,
                               int reloadTicks, int energyPerShot, float spreadDegrees, boolean automatic,
                               String fireSound) {
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
    public int cooldownTicks() { return Math.max(1, Math.round(1200.0F / Math.max(1, rpm))); }

    public static NativeDestinyWeaponProfile byId(String id) {
        return BY_ID.get(id);
    }
}
