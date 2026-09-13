package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.OverhaulContent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=MatterOverdrive.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class OverhaulEntityEvents {
    private OverhaulEntityEvents(){}
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(OverhaulContent.FIELD_SCIENTIST.get(),Villager.createAttributes().add(Attributes.MAX_HEALTH,32.0D).add(Attributes.MOVEMENT_SPEED,0.5D).build());e.put(OverhaulContent.SYSTEMS_ENGINEER.get(),Villager.createAttributes().add(Attributes.MAX_HEALTH,32.0D).add(Attributes.MOVEMENT_SPEED,0.5D).build());e.put(OverhaulContent.ASSIMILATOR.get(),Zombie.createAttributes().add(Attributes.MAX_HEALTH,30.0D).add(Attributes.ATTACK_DAMAGE,6.0D).add(Attributes.ARMOR,4.0D).add(Attributes.MOVEMENT_SPEED,0.25D).build());e.put(OverhaulContent.PHASE_STALKER.get(),Zombie.createAttributes().add(Attributes.MAX_HEALTH,22.0D).add(Attributes.ATTACK_DAMAGE,5.0D).add(Attributes.MOVEMENT_SPEED,0.31D).build());}
}
