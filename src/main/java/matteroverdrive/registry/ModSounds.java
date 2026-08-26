package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MatterOverdrive.MOD_ID);

    private static final Map<String, RegistryObject<SoundEvent>> SOUNDS_BY_ID = new LinkedHashMap<>();

    private static final List<String> SOUND_IDS = List.of(
        "analyzer",
        "android.cloak_off",
        "android.cloak_on",
        "android.night_vision",
        "android.power_down",
        "android.shield_hit",
        "android.shield_loop",
        "android.shield_power_down",
        "android.shield_power_up",
        "android.shockwave",
        "android.teleport",
        "anomaly_consume",
        "blocks.crate_close",
        "blocks.crate_open",
        "blocks.pylon",
        "decomposer",
        "electric_machine",
        "failed_animal_die",
        "failed_animal_idle_chicken",
        "failed_animal_idle_cow",
        "failed_animal_idle_pig",
        "failed_animal_idle_sheep",
        "force_field",
        "fx.electric_arc",
        "gui.biotic_stat_unlock",
        "gui.button_expand",
        "gui.button_loud",
        "gui.button_soft",
        "gui.glitch",
        "gui.quest_complete",
        "gui.quest_started",
        "machine",
        "mobs.rogue_android_death",
        "mobs.rogue_android_say",
        "music.transformation",
        "replicate_success",
        "scanner_beep",
        "scanner_fail",
        "scanner_scanning",
        "scanner_success",
        "transporter",
        "weapons.bolt_hit",
        "weapons.explosive_shot",
        "weapons.laser_fire",
        "weapons.laser_ricochet",
        "weapons.omni_tool_hum",
        "weapons.overheat",
        "weapons.overheat_alarm",
        "weapons.phaser_beam",
        "weapons.phaser_rifle_shot",
        "weapons.phaser_switch_mode",
        "weapons.plasma_shotgun_charging",
        "weapons.plasma_shotgun_shot",
        "weapons.reload",
        "weapons.sizzle",
        "weapons.sniper_rifle_fire",
        "windy"
    );

    static {
        SOUND_IDS.forEach(id -> SOUNDS_BY_ID.put(id,
                SOUND_EVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(
                        new ResourceLocation(MatterOverdrive.MOD_ID, id)))));
    }

    private ModSounds() {
    }

    public static Map<String, RegistryObject<SoundEvent>> all() {
        return Collections.unmodifiableMap(SOUNDS_BY_ID);
    }

    public static RegistryObject<SoundEvent> get(String id) {
        RegistryObject<SoundEvent> sound = SOUNDS_BY_ID.get(id);
        if (sound == null) {
            throw new IllegalArgumentException("Unknown Matter Overdrive sound id: " + id);
        }
        return sound;
    }
}
