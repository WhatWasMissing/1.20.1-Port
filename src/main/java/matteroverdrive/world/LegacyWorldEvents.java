package matteroverdrive.world;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID)
public final class LegacyWorldEvents {
    private LegacyWorldEvents() {}
    @SubscribeEvent public static void onChunkLoad(ChunkEvent.Load event){if(!(event.getLevel() instanceof ServerLevel level)||level.dimension()!=ServerLevel.OVERWORLD)return;ChunkPos chunk=event.getChunk().getPos();if(!LegacyWorldEventData.get(level).markChecked(chunk.toLong()))return;RandomSource random=RandomSource.create(level.getSeed()^chunk.toLong()*0x9E3779B97F4A7C15L);if(random.nextInt(1800)!=0)return;int x=chunk.getMinBlockX()+4+random.nextInt(8),z=chunk.getMinBlockZ()+4+random.nextInt(8),y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);generateCrashDebris(level,new BlockPos(x,y,z),random);}
    private static void generateCrashDebris(ServerLevel level,BlockPos o,RandomSource r){var plate=ModBlocks.get("tritanium_plate").get().defaultBlockState();var crate=ModBlocks.get("tritanium_crate").get().defaultBlockState();var holo=ModBlocks.get("holo_matrix").get().defaultBlockState();for(int dx=-3;dx<=3;dx++)for(int dz=-2;dz<=2;dz++)if(r.nextFloat()<.48F)level.setBlock(o.offset(dx,0,dz),plate,3);level.setBlock(o.above(),holo,3);level.setBlock(o.offset(2,1,1),crate,3);level.setBlock(o.offset(-2,1,-1),crate,3);level.setBlock(o.offset(0,1,2),Blocks.IRON_BARS.defaultBlockState(),3);}
}
