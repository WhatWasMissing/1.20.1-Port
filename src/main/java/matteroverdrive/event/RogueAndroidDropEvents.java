package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidData;
import matteroverdrive.entity.RogueAndroidEntity;
import matteroverdrive.registry.ModItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Drops a salvageable Android body part from the restored dedicated Rogue Android entity. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RogueAndroidDropEvents {
    private RogueAndroidDropEvents() {}

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof RogueAndroidEntity android)) {
            return;
        }
        AndroidData.Part[] parts = AndroidData.Part.values();
        AndroidData.Part part = parts[android.getRandom().nextInt(parts.length)];
        event.getDrops().add(new ItemEntity(android.level(), android.getX(), android.getY(), android.getZ(),
                new ItemStack(ModItems.get(part.itemId).get())));
    }
}
