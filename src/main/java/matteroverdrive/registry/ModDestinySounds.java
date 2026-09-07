package matteroverdrive.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModDestinySounds {
    public static final String NAMESPACE = "matteroverdrive_destiny";
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, NAMESPACE);
    private static final Map<String, RegistryObject<SoundEvent>> SOUNDS = new LinkedHashMap<>();

    static {
        register("destiny_aceofspades");
        register("destiny_chaosdogma");
        register("destiny_eyasluna");
        register("destiny_hawkmoon");
        register("destiny_khvostov7g02");
        register("destiny_marshala1");
        register("destiny_midamultitool");
        register("destiny_montecarlo");
        register("destiny_proximacentauriii");
        register("destiny_sleepersimulant_fire");
        register("destiny_surosregime");
        register("destiny_thelastword");
        register("destiny_thorn");
        register("destiny_traxcallum1");
    }

    private static void register(String id) {
        SOUNDS.put(id, SOUND_EVENTS.register(id,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(NAMESPACE, id))));
    }

    public static SoundEvent get(String id) {
        RegistryObject<SoundEvent> sound = SOUNDS.get(id);
        if (sound == null) throw new IllegalArgumentException("Unknown native Destiny sound: " + id);
        return sound.get();
    }

    private ModDestinySounds() {}
}
