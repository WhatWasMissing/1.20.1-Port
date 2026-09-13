package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.entity.FieldScientistEntity;
import matteroverdrive.entity.MadScientistEntity;
import matteroverdrive.entity.SystemsEngineerEntity;
import matteroverdrive.registry.ModEntities;
import matteroverdrive.registry.OverhaulContent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Structure-free village population hook for Matter Overdrive characters. */
@Mod.EventBusSubscriber(modid=MatterOverdrive.MOD_ID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class VillageNpcSpawnEvents {
    private VillageNpcSpawnEvents(){}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer player)||player.tickCount%400!=0)return;ServerLevel level=player.serverLevel();if(!level.isVillage(player.blockPosition()))return;AABB area=player.getBoundingBox().inflate(48.0D);if(level.getEntitiesOfClass(FieldScientistEntity.class,area).isEmpty()&&level.random.nextFloat()<0.20F)spawnNear(level,player,OverhaulContent.FIELD_SCIENTIST.get());if(level.getEntitiesOfClass(SystemsEngineerEntity.class,area).isEmpty()&&level.random.nextFloat()<0.16F)spawnNear(level,player,OverhaulContent.SYSTEMS_ENGINEER.get());if(level.getEntitiesOfClass(MadScientistEntity.class,area).isEmpty()&&level.random.nextFloat()<0.04F)spawnNear(level,player,ModEntities.MAD_SCIENTIST.get());}
    private static void spawnNear(ServerLevel level,ServerPlayer player,EntityType<? extends Mob> type){for(int attempt=0;attempt<8;attempt++){int x=player.getBlockX()+level.random.nextInt(25)-12,z=player.getBlockZ()+level.random.nextInt(25)-12,y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);BlockPos pos=new BlockPos(x,y,z);if(!level.isVillage(pos))continue;Mob mob=type.create(level);if(mob==null)continue;mob.moveTo(x+0.5D,y,z+0.5D,level.random.nextFloat()*360.0F,0.0F);mob.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),MobSpawnType.EVENT,null,null);if(!level.noCollision(mob)){mob.discard();continue;}mob.setPersistenceRequired();level.addFreshEntity(mob);return;}}
}
