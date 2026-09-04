package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.entity.DroneEntity;
import matteroverdrive.entity.MutantScientistEntity;
import matteroverdrive.entity.RogueAndroidEntity;
import matteroverdrive.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LegacyEntityEvents {
    private LegacyEntityEvents() {}
    @SubscribeEvent public static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ROGUE_ANDROID.get(), RogueAndroidEntity.createAttributes().build());
        event.put(ModEntities.RANGED_ROGUE_ANDROID.get(), RogueAndroidEntity.createAttributes().build());
        event.put(ModEntities.DRONE.get(), DroneEntity.createAttributes().build());
        event.put(ModEntities.MUTANT_SCIENTIST.get(), MutantScientistEntity.createAttributes().build());
        event.put(ModEntities.MAD_SCIENTIST.get(), Villager.createAttributes().build());
        event.put(ModEntities.FAILED_COW.get(), Cow.createAttributes().build());
        event.put(ModEntities.FAILED_PIG.get(), Pig.createAttributes().build());
        event.put(ModEntities.FAILED_SHEEP.get(), Sheep.createAttributes().build());
        event.put(ModEntities.FAILED_CHICKEN.get(), Chicken.createAttributes().build());
    }
    @SubscribeEvent public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.ROGUE_ANDROID.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random) && random.nextFloat() < RogueAndroidEntity.NATURAL_SPAWN_CHANCE,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ModEntities.RANGED_ROGUE_ANDROID.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random) && random.nextFloat() < 0.035F,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
