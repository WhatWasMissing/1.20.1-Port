package matteroverdrive.worldgen;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.AndroidSpawnerBlockEntity;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.registries.ForgeRegistries;

/** Exploration-first implementations of the four Frontier Expedition locations. */
public final class FrontierExplorationStructurePiece extends StructurePiece {
    private final FrontierSiteStructure.Kind site;
    private final BlockPos origin;
    private final int layout;

    public FrontierExplorationStructurePiece(FrontierSiteStructure.Kind site, BlockPos origin, int layout) {
        super(ModStructures.FRONTIER_EXPLORATION_PIECE.get(), 0, box(origin));
        this.site=site; this.origin=origin; this.layout=Math.floorMod(layout,3);
    }
    public FrontierExplorationStructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.FRONTIER_EXPLORATION_PIECE.get(), tag);
        site=FrontierSiteStructure.Kind.valueOf(tag.getString("MOSite"));
        origin=new BlockPos(tag.getInt("MOX"),tag.getInt("MOY"),tag.getInt("MOZ"));
        layout=Math.floorMod(tag.getInt("MOLayout"),3);
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag){tag.putString("MOSite",site.name());tag.putInt("MOX",origin.getX());tag.putInt("MOY",origin.getY());tag.putInt("MOZ",origin.getZ());tag.putInt("MOLayout",layout);}

    @Override public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator cg, RandomSource random, BoundingBox clip, ChunkPos cp, BlockPos pivot){
        switch(site){case DEEP_MATTER_VAULT->vault(level,clip);case AUTONOMOUS_DRONE_FOUNDRY->foundry(level,clip);case ANOMALY_QUARANTINE_SITE->quarantine(level,clip);case ORBITAL_RECOVERY_ARRAY->recovery(level,clip);}
    }

    private void vault(WorldGenLevel l,BoundingBox c){
        BlockState wall=dark(),floor=darkFloor(),glass=glass();
        // Surface archive house -> long descent -> security -> central guarded vault; side rooms are optional.
        room(l,c,origin.offset(0,14,-22),6,5,5,metal(),floor,glass); stair(l,c,origin.offset(0,14,-16),14,16);
        room(l,c,origin.offset(0,0,-10),8,6,5,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,-5),4);
        room(l,c,origin,11,9,7,wall,floor,glass);
        room(l,c,origin.offset(-19,0,3),7,7,6,wall,floor,glass); corridorX(l,c,origin.offset(-12,0,3),5);
        room(l,c,origin.offset(19,0,3),7,7,6,wall,floor,glass); corridorX(l,c,origin.offset(12,0,3),5);
        archive(l,c,origin.offset(-19,0,3)); refineryRuin(l,c,origin.offset(19,0,3));
        story(l,c,origin.offset(0,15,-22),Blocks.LECTERN.defaultBlockState());
        cache(l,c,origin.offset(0,1,3),"story_cache",true,5,75);
        cache(l,c,origin.offset(-19,1,3),"salvage",false,0,0);
    }

    private void foundry(WorldGenLevel l,BoundingBox c){
        BlockState wall=metal(),floor=floor(),glass=glass();
        room(l,c,origin.offset(0,0,-27),7,5,5,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,-20),5);
        room(l,c,origin.offset(0,0,-13),8,6,6,dark(),floor,glass); corridorZ(l,c,origin.offset(0,0,-7),4);
        room(l,c,origin,11,9,7,wall,floor,glass);
        room(l,c,origin.offset(-20,0,3),8,7,6,dark(),floor,glass); corridorX(l,c,origin.offset(-13,0,3),5);
        room(l,c,origin.offset(20,0,3),8,7,6,dark(),floor,glass); corridorX(l,c,origin.offset(13,0,3),5);
        room(l,c,origin.offset(0,0,20),10,8,8,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,13),5);
        fabricationRuin(l,c,origin.offset(-20,0,3)); droneRacks(l,c,origin.offset(20,0,3)); hangarMarks(l,c,origin.offset(0,0,20));
        story(l,c,origin.offset(0,1,-13),Blocks.LECTERN.defaultBlockState());
        cache(l,c,origin.offset(0,1,20),"story_cache",true,6,65);
        cache(l,c,origin.offset(-20,1,3),"salvage",false,0,0);
    }

    private void quarantine(WorldGenLevel l,BoundingBox c){
        BlockState wall=black(),floor=darkFloor(),glass=glass();
        room(l,c,origin.offset(0,8,-25),7,5,5,metal(),floor,glass); stair(l,c,origin.offset(0,8,-19),8,10);
        room(l,c,origin.offset(0,0,-11),8,6,5,white(),floor,glass); corridorZ(l,c,origin.offset(0,0,-5),4);
        room(l,c,origin,10,8,6,wall,floor,glass);
        room(l,c,origin.offset(-18,0,3),7,7,6,wall,floor,glass); corridorX(l,c,origin.offset(-11,0,3),5);
        room(l,c,origin.offset(18,0,3),7,7,6,wall,floor,glass); corridorX(l,c,origin.offset(11,0,3),5);
        room(l,c,origin.offset(0,0,18),10,7,7,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,11),5);
        deconStory(l,c,origin.offset(0,0,-11)); observationStory(l,c,origin.offset(-18,0,3)); containmentStory(l,c,origin.offset(0,0,18));
        story(l,c,origin.offset(18,1,3),Blocks.LECTERN.defaultBlockState());
        cache(l,c,origin.offset(0,1,18),"story_cache",true,5,80);
        cache(l,c,origin.offset(-18,1,3),"salvage",false,0,0);
    }

    private void recovery(WorldGenLevel l,BoundingBox c){
        BlockState wall=metal(),floor=floor(),glass=glass();
        room(l,c,origin.offset(0,0,-28),7,5,6,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,-21),5);
        room(l,c,origin.offset(0,0,-14),9,6,6,white(),floor,glass); corridorZ(l,c,origin.offset(0,0,-7),4);
        room(l,c,origin,11,9,7,wall,floor,glass);
        room(l,c,origin.offset(-19,0,4),7,7,6,dark(),floor,glass); corridorX(l,c,origin.offset(-12,0,4),5);
        room(l,c,origin.offset(19,0,4),7,7,6,dark(),floor,glass); corridorX(l,c,origin.offset(12,0,4),5);
        antenna(l,c,origin.offset(0,0,18)); signalDebris(l,c,origin.offset(-19,0,4)); storageRacks(l,c,origin.offset(19,0,4));
        story(l,c,origin.offset(0,1,-14),Blocks.LECTERN.defaultBlockState());
        cache(l,c,origin.offset(0,1,5),"story_cache",true,4,70);
        cache(l,c,origin.offset(19,1,4),"salvage",false,0,0);
    }

    private void room(WorldGenLevel l,BoundingBox clip,BlockPos o,int hx,int hz,int h,BlockState wall,BlockState floor,BlockState glass){BlockState beam=beam();for(int x=-hx;x<=hx;x++)for(int z=-hz;z<=hz;z++){boolean cut=Math.abs(x)>=hx-1&&Math.abs(z)>=hz-1;if(cut)continue;set(l,clip,o.offset(x,0,z),floor);boolean edge=Math.abs(x)==hx||Math.abs(z)==hz||Math.abs(x)==hx-1&&Math.abs(z)>=hz-2||Math.abs(z)==hz-1&&Math.abs(x)>=hx-2;for(int y=1;y<=h;y++){boolean door=(Math.abs(x)<=1&&Math.abs(z)>=hz-1&&y<=3)||(Math.abs(z)<=1&&Math.abs(x)>=hx-1&&y<=3);set(l,clip,o.offset(x,y,z),!edge||door?Blocks.AIR.defaultBlockState():(y==2||y==3?glass:(y==1||y==h?beam:wall)));}set(l,clip,o.offset(x,h+1,z),edge?beam:wall);}for(int z=-hz+2;z<=hz-2;z++)set(l,clip,o.offset(0,0,z),stripe());}
    private void corridorZ(WorldGenLevel l,BoundingBox c,BlockPos o,int half){for(int z=-half;z<=half;z++)for(int x=-1;x<=1;x++){set(l,c,o.offset(x,0,z),x==0?stripe():floor());for(int y=1;y<=3;y++)set(l,c,o.offset(x,y,z),Blocks.AIR.defaultBlockState());}}
    private void corridorX(WorldGenLevel l,BoundingBox c,BlockPos o,int half){for(int x=-half;x<=half;x++)for(int z=-1;z<=1;z++){set(l,c,o.offset(x,0,z),z==0?stripe():floor());for(int y=1;y<=3;y++)set(l,c,o.offset(x,y,z),Blocks.AIR.defaultBlockState());}}
    private void stair(WorldGenLevel l,BoundingBox c,BlockPos top,int drop,int run){for(int i=0;i<=run;i++){int y=-Math.min(drop,(i*drop)/Math.max(1,run));for(int x=-1;x<=1;x++){BlockPos p=top.offset(x,y,i);set(l,c,p,floor());for(int h=1;h<=3;h++)set(l,c,p.above(h),Blocks.AIR.defaultBlockState());}}}
    private void archive(WorldGenLevel l,BoundingBox c,BlockPos o){for(int z=-4;z<=4;z+=4){set(l,c,o.offset(-4,1,z),Blocks.CHISELED_BOOKSHELF.defaultBlockState());set(l,c,o.offset(4,1,z),Blocks.CHISELED_BOOKSHELF.defaultBlockState());}}
    private void refineryRuin(WorldGenLevel l,BoundingBox c,BlockPos o){for(int x=-4;x<=4;x+=4){set(l,c,o.offset(x,1,3),Blocks.CUT_COPPER.defaultBlockState());set(l,c,o.offset(x,2,3),Blocks.IRON_BARS.defaultBlockState());}}
    private void fabricationRuin(WorldGenLevel l,BoundingBox c,BlockPos o){for(int z=-4;z<=4;z+=4){set(l,c,o.offset(-4,1,z),Blocks.ANVIL.defaultBlockState());set(l,c,o.offset(4,1,z),Blocks.SMITHING_TABLE.defaultBlockState());}}
    private void droneRacks(WorldGenLevel l,BoundingBox c,BlockPos o){for(int z=-4;z<=4;z+=4){set(l,c,o.offset(-4,1,z),Blocks.CHAIN.defaultBlockState());set(l,c,o.offset(4,1,z),Blocks.IRON_BARS.defaultBlockState());}}
    private void hangarMarks(WorldGenLevel l,BoundingBox c,BlockPos o){for(int x=-7;x<=7;x++)for(int z=-5;z<=5;z++)if(Math.abs(x)%6==0||Math.abs(z)==5)set(l,c,o.offset(x,0,z),stripe());}
    private void deconStory(WorldGenLevel l,BoundingBox c,BlockPos o){for(int x=-4;x<=4;x+=4)set(l,c,o.offset(x,1,2),Blocks.IRON_TRAPDOOR.defaultBlockState());}
    private void observationStory(WorldGenLevel l,BoundingBox c,BlockPos o){for(int x=-4;x<=4;x++)set(l,c,o.offset(x,1,3),glass());}
    private void containmentStory(WorldGenLevel l,BoundingBox c,BlockPos o){for(int x:new int[]{-5,5})for(int z:new int[]{-3,3}){set(l,c,o.offset(x,1,z),Blocks.OBSIDIAN.defaultBlockState());set(l,c,o.offset(x,2,z),Blocks.CRYING_OBSIDIAN.defaultBlockState());}}
    private void antenna(WorldGenLevel l,BoundingBox c,BlockPos o){for(int y=0;y<=22;y++){set(l,c,o.above(y),beam());if(y%4==0){set(l,c,o.offset(1,y,0),Blocks.IRON_BARS.defaultBlockState());set(l,c,o.offset(-1,y,0),Blocks.IRON_BARS.defaultBlockState());}}}
    private void signalDebris(WorldGenLevel l,BoundingBox c,BlockPos o){for(int x=-4;x<=4;x+=2)set(l,c,o.offset(x,1,2),x%4==0?Blocks.COPPER_BLOCK.defaultBlockState():Blocks.IRON_BARS.defaultBlockState());}
    private void storageRacks(WorldGenLevel l,BoundingBox c,BlockPos o){for(int z=-4;z<=4;z+=4){set(l,c,o.offset(-4,1,z),Blocks.IRON_BARS.defaultBlockState());set(l,c,o.offset(4,1,z),Blocks.BARREL.defaultBlockState());}}
    private void story(WorldGenLevel l,BoundingBox c,BlockPos p,BlockState s){set(l,c,p,s);if(l.getBlockState(p.above()).isAir())set(l,c,p.above(),Blocks.COBWEB.defaultBlockState());}

    private void cache(WorldGenLevel l,BoundingBox c,BlockPos p,String table,boolean guarded,int reserve,int ranged){Block crate=modBlock("tritanium_crate",Blocks.BARREL);set(l,c,p,crate.defaultBlockState());if(l.getBlockEntity(p) instanceof TritaniumCrateBlockEntity be)be.seedStructureLoot(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"chests/facilities/"+table),l.getSeed()^p.asLong()^site.ordinal());if(!guarded)return;for(BlockPos off:new BlockPos[]{new BlockPos(3,0,0),new BlockPos(-3,0,0),new BlockPos(0,0,3),new BlockPos(0,0,-3)}){BlockPos q=p.offset(off);if(!c.isInside(q)||!l.getBlockState(q).isAir()||!l.getBlockState(q.above()).isAir()||l.getBlockState(q.below()).isAir())continue;Block sp=modBlock("android_spawner",Blocks.IRON_BLOCK);set(l,c,q,sp.defaultBlockState());if(l.getBlockEntity(q) instanceof AndroidSpawnerBlockEntity s)s.configureFacility(id(),reserve,ranged);break;}}
    private String id(){return site.name().toLowerCase(java.util.Locale.ROOT);}
    private static BoundingBox box(BlockPos p){return new BoundingBox(p.getX()-36,p.getY()-18,p.getZ()-38,p.getX()+36,p.getY()+28,p.getZ()+38);}
    private static Block modBlock(String id,Block fallback){Block b=ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,id));return b==null||b==Blocks.AIR?fallback:b;}
    private static BlockState metal(){return modBlock("decorative.tritanium_plate",Blocks.IRON_BLOCK).defaultBlockState();}private static BlockState white(){return modBlock("decorative.white_plate",Blocks.QUARTZ_BLOCK).defaultBlockState();}private static BlockState dark(){return modBlock("decorative.carbon_fiber_plate",Blocks.DEEPSLATE_BRICKS).defaultBlockState();}private static BlockState black(){return modBlock("decorative.carbon_fiber_plate",Blocks.REINFORCED_DEEPSLATE).defaultBlockState();}private static BlockState floor(){return modBlock("decorative.floor_tiles",Blocks.SMOOTH_STONE).defaultBlockState();}private static BlockState darkFloor(){return modBlock("decorative.floor_tiles",Blocks.DEEPSLATE_TILES).defaultBlockState();}private static BlockState beam(){return modBlock("decorative.beams",Blocks.POLISHED_DEEPSLATE).defaultBlockState();}private static BlockState glass(){return modBlock("industrial_glass",Blocks.TINTED_GLASS).defaultBlockState();}private static BlockState stripe(){return modBlock("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE).defaultBlockState();}
    private void set(WorldGenLevel l,BoundingBox c,BlockPos p,BlockState s){if(!c.isInside(p)||!getBoundingBox().isInside(p)||l.getBlockState(p).is(Blocks.BEDROCK))return;l.setBlock(p,s,2);}
}
