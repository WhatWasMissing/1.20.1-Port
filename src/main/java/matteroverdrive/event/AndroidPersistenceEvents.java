package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Preserves Android runtime state that intentionally lives outside the two
 * main Android NBT roots. The progression/loadout roots already copy themselves
 * during PlayerEvent.Clone; these absolute cooldown timestamps previously did
 * not, allowing death/respawn to reset subclass cooldowns.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AndroidPersistenceEvents {
    private static final String CLASS_COOLDOWN = "MatterOverdriveAndroidClassAbilityUntil";
    private static final String TECH_COOLDOWN = "MatterOverdriveAndroidTechAbilityUntil";
    private static final String ULTIMATE_COOLDOWN = "MatterOverdriveAndroidUltimateUntil";
    private static final String REACTIVE_EXOSHELL = "MatterOverdriveReactiveExoshellUntil";

    private AndroidPersistenceEvents() {}

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag source = event.getOriginal().getPersistentData();
        CompoundTag target = event.getEntity().getPersistentData();
        copyLong(source, target, CLASS_COOLDOWN);
        copyLong(source, target, TECH_COOLDOWN);
        copyLong(source, target, ULTIMATE_COOLDOWN);
        copyLong(source, target, REACTIVE_EXOSHELL);
    }

    private static void copyLong(CompoundTag source, CompoundTag target, String key) {
        if (source.contains(key)) {
            target.putLong(key, Math.max(0L, source.getLong(key)));
        }
    }
}
