package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** Sound events for the Matter Overdrive exotic weapons that remain in scope. */
public final class ModExoticSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MatterOverdrive.MOD_ID);
    private static final Map<String, RegistryObject<SoundEvent>> SOUNDS = new LinkedHashMap<>();

    static {
        register("vex_mythoclast_fire");
        register("vex_mythoclast_linear_fire");
    }

    private static void register(String id) {
        SOUNDS.put(id, SOUND_EVENTS.register(id,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, id))));
    }

    public static SoundEvent get(String id) {
        RegistryObject<SoundEvent> sound = SOUNDS.get(id);
        if (sound == null) throw new IllegalArgumentException("Unknown exotic sound: " + id);
        return sound.get();
    }

    private ModExoticSounds() {}
}
