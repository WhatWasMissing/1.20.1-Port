package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.EnergyWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, value = Dist.CLIENT)
public final class WeaponClientEffects {
    private WeaponClientEffects() {}
    private static EnergyWeaponItem heldWeapon(){var p=Minecraft.getInstance().player;if(p==null)return null;ItemStack s=p.getMainHandItem();return s.getItem() instanceof EnergyWeaponItem w?w:null;}
    @SubscribeEvent public static void onFov(ViewportEvent.ComputeFov e){var p=Minecraft.getInstance().player;EnergyWeaponItem w=heldWeapon();if(p==null||w==null||!p.isUsingItem())return;if(w.getWeaponType()==EnergyWeaponItem.WeaponType.ION_SNIPER)e.setFOV(e.getFOV()*.35D);}
    @SubscribeEvent public static void onCamera(ViewportEvent.ComputeCameraAngles e){var p=Minecraft.getInstance().player;EnergyWeaponItem w=heldWeapon();if(p==null||w==null||!p.isUsingItem())return;if(w.getWeaponType()==EnergyWeaponItem.WeaponType.PHASER_RIFLE||w.getWeaponType()==EnergyWeaponItem.WeaponType.PHASER){float pulse=(float)Math.sin(p.tickCount*1.7D)*.22F;e.setPitch(e.getPitch()-Math.abs(pulse));}}
}
