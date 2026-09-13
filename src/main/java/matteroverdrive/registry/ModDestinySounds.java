package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.NativeDestinyWeaponProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** Sound events bundled for the native Destiny-style Matter Overdrive weapons. */
public final class ModDestinySounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MatterOverdrive.MOD_ID);
    private static final Map<String, RegistryObject<SoundEvent>> SOUNDS = new LinkedHashMap<>();

    static {
        register("destiny_khvostov_charge");
        register("destiny_khvostov_draw");
        register("destiny_khvostov_first");
        register("destiny_khvostov_last");
        register("destiny_khvostov_reload");
        register("destiny_khvostov_unload");
        register("destiny_khvostov7g02");
        register("destiny_marshala1");
        register("destiny_marshal_unload");
        register("destiny_marshal_reload");
        register("destiny_marshal_charge");
        register("destiny_eyasluna");
        register("destiny_hawkmoon");
        register("destiny_hawkmoon_close");
        register("destiny_hawkmoon_draw");
        register("destiny_hawkmoon_eject");
        register("destiny_hawkmoon_insert");
        register("destiny_hawkmoon_open");
        register("destiny_hawkmoon_rest");
        register("destiny_hawkmoon_up");
        register("destiny_surosregime");
        register("destiny_surosregime_charge");
        register("destiny_surosregime_load");
        register("destiny_montecarlo");
        register("destiny_montecarlo_unload");
        register("destiny_montecarlo_reload");
        register("destiny_montecarlo_charge");
        register("destiny_traxmallus1_charge");
        register("destiny_traxmallus1_reload");
        register("destiny_traxmallus1_unload");
        register("destiny_traxcallum1");
        register("destiny_proximacentauriii");
        register("destiny_midamultitool");
        register("destiny_midamultitool_unload");
        register("destiny_midamultitool_charge");
        register("destiny_chaosdogma");
        register("destiny_cd_charge");
        register("destiny_cd_unload");
        register("destiny_cd_reload");
        register("destiny_thelastword");
        register("destiny_aceofspades");
        register("destiny_aos_spin");
        register("destiny_aos_open");
        register("destiny_aos_insert");
        register("destiny_aos_close");
        register("destiny_thorn");
        register("destiny_thorn_close");
        register("destiny_thorn_open");
        register("destiny_thorn_reload");
        register("destiny_thorn_unload");
        register("destiny_sleepersimulant_draw");
        register("destiny_sleepersimulant_charge");
        register("destiny_sleepersimulant_fire");
        register("destiny_sleepersimulant_hit");
        register("destiny_sleepersimulant_reload");
        register("destiny_sleepersimulant_unload");

        // Vex Mythoclast owns dedicated events even when its bundled fallback samples are
        // shared with the native Destiny set. This stops the exotic from falling through
        // to Matter Overdrive's generic phaser/sniper SoundEvents and gives us stable IDs
        // for drop-in Mythoclast recordings in a resource pack or future asset pass.
        register("destiny_vex_mythoclast_fire");
        register("destiny_vex_mythoclast_linear_fire");

        // Exact first-/third-person and reload/draw recordings from the supplied
        // Destiny GunPack. The primary events above remain stable for the older
        // profiles; these IDs keep the source-specific 3P routing explicit.
        register("destiny_aceofspades_3p");
        register("destiny_aceofspades_reload");
        register("destiny_hawkmoon_3p");
        register("destiny_hawkmoon_reload");
        register("destiny_khvostov7g02_3p");
        register("destiny_midamultitool_3p");
        register("destiny_midamultitool_draw");
        register("destiny_midamultitool_reload");
        register("destiny_montecarlo_3p");
        register("destiny_sleepersimulant_3p");
        register("destiny_surosregime_3p");
        register("destiny_surosregime_reload");
        register("destiny_thelastword_3p");
        register("destiny_thelastword_draw");
        register("destiny_thelastword_reload");
        register("destiny_thorn_3p");
        register("destiny_thorn_draw");

        // Every native Destiny profile owns its core fire/perspective/draw/reload IDs.
        // Deriving these registrations from the profile keeps an imported weapon from
        // becoming playable with a missing SoundEvent registration.
        for (NativeDestinyWeaponProfile profile : NativeDestinyWeaponProfile.values()) {
            registerIfAbsent(profile.fireSound());
            registerIfAbsent(profile.thirdPersonFireSound());
            registerIfAbsent(profile.drawSound());
            registerIfAbsent(profile.reloadSound());
        }
    }

    private static void register(String id) {
        SOUNDS.put(id, SOUND_EVENTS.register(id,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MatterOverdrive.MOD_ID, id))));
    }

    private static void registerIfAbsent(String id) {
        if (id != null && !SOUNDS.containsKey(id)) register(id);
    }

    public static SoundEvent get(String id) {
        RegistryObject<SoundEvent> sound = SOUNDS.get(id);
        if (sound == null) throw new IllegalArgumentException("Unknown native Destiny sound: " + id);
        return sound.get();
    }

    public static int size() { return SOUNDS.size(); }
    private ModDestinySounds() {}
}
