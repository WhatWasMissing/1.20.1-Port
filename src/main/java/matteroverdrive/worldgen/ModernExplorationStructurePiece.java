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

/**
 * Exploration-native replacements for the six modern technology facilities.
 * New worlds get dungeon-style facilities, not free functional infrastructure.
 */
public final class ModernExplorationStructurePiece extends StructurePiece {
    private final TechnologyFacilityStructure.Kind facility;
    private final BlockPos origin;
    private final int layout;

    public ModernExplorationStructurePiece(TechnologyFacilityStructure.Kind facility, BlockPos origin, int layout) {
        super(ModStructures.MODERN_EXPLORATION_PIECE.get(), 0, box(origin));
        this.facility = facility;
        this.origin = origin;
        this.layout = Math.floorMod(layout, 3);
    }

    public ModernExplorationStructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.MODERN_EXPLORATION_PIECE.get(), tag);
        this.facility = TechnologyFacilityStructure.Kind.valueOf(tag.getString("MOFacility"));
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
        this.layout = Math.floorMod(tag.getInt("MOLayout"), 3);
    }

    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("MOFacility", facility.name());
        tag.putInt("MOX", origin.getX()); tag.putInt("MOY", origin.getY()); tag.putInt("MOZ", origin.getZ());
        tag.putInt("MOLayout", layout);
    }

    @Override public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                                     RandomSource random, BoundingBox clip, ChunkPos chunkPos, BlockPos pivot) {
        switch (facility) {
            case SYNTHETIC_MANUFACTURING_PLANT -> plant(level, clip);
            case MATTER_REFINERY -> refinery(level, clip);
            case QUANTUM_RELAY_STATION -> relay(level, clip);
            case ANDROID_COMMAND_BUNKER -> bunker(level, clip);
            case FUSION_RESEARCH_COMPLEX -> fusion(level, clip);
            case BLACK_SITE -> blackSite(level, clip);
        }
    }

    private void plant(WorldGenLevel l, BoundingBox c) {
        BlockState wall=metal(), floor=floor(), dark=dark(), glass=glass();
        // Gate -> checkpoint -> production hall -> assembly/fabrication wings -> guarded dispatch archive.
        room(l,c,origin.offset(0,0,-27),7,5,5,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,-20),5);
        room(l,c,origin.offset(0,0,-14),8,5,5,dark,floor,glass); corridorZ(l,c,origin.offset(0,0,-7),5);
        room(l,c,origin,12,10,8,wall,floor,glass);
        room(l,c,origin.offset(-20,0,3),7,7,6,dark,floor,glass); corridorX(l,c,origin.offset(-13,0,3),5);
        room(l,c,origin.offset(20,0,3),7,7,6,dark,floor,glass); corridorX(l,c,origin.offset(13,0,3),5);
        room(l,c,origin.offset(0,0,20),10,7,6,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,13),5);
        assemblyScenery(l,c,origin.offset(-20,0,3)); assemblyScenery(l,c,origin.offset(20,0,3));
        story(l,c,origin.offset(0,1,-14),Blocks.LECTERN.defaultBlockState());
        cache(l,c,origin.offset(0,1,20),"synthetic_manufacturing_plant",true,4,65);
        cache(l,c,origin.offset(-20,1,3),"story_cache",false,0,0);
    }

    private void refinery(WorldGenLevel l, BoundingBox c) {
        BlockState wall=green(), floor=greenFloor(), glass=glass();
        room(l,c,origin.offset(0,0,-26),7,5,5,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,-19),5);
        room(l,c,origin.offset(0,0,-13),8,5,5,dark(),floor,glass); corridorZ(l,c,origin.offset(0,0,-7),4);
        room(l,c,origin,12,10,8,wall,floor,glass);
        // Extraction pit is optional; processing archive is the guarded focal reward.
        room(l,c,origin.offset(-20,-2,2),7,7,6,dark(),floor,glass); steppedDown(l,c,origin.offset(-13,0,2),-1,8);
        room(l,c,origin.offset(0,0,20),9,7,6,white(),floor,glass); corridorZ(l,c,origin.offset(0,0,13),5);
        room(l,c,origin.offset(20,0,10),7,7,6,wall,floor,glass); corridorX(l,c,origin.offset(13,0,10),5);
        refineryScenery(l,c,origin); story(l,c,origin.offset(-20,0,2),Blocks.CRYING_OBSIDIAN.defaultBlockState());
        cache(l,c,origin.offset(0,1,20),"matter_refinery",true,4,45);
        cache(l,c,origin.offset(20,1,10),"story_cache",false,0,0);
    }

    private void relay(WorldGenLevel l, BoundingBox c) {
        BlockState wall=metal(), floor=floor(), glass=glass();
        room(l,c,origin.offset(0,0,-29),7,5,6,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,-21),6);
        room(l,c,origin.offset(0,0,-13),9,6,6,white(),floor,glass); corridorZ(l,c,origin.offset(0,0,-6),4);
        room(l,c,origin,11,10,9,wall,floor,glass);
        room(l,c,origin.offset(-19,0,5),7,7,6,dark(),floor,glass); corridorX(l,c,origin.offset(-12,0,4),5);
        room(l,c,origin.offset(19,0,5),7,7,6,dark(),floor,glass); corridorX(l,c,origin.offset(12,0,4),5);
        relayMast(l,c,origin.offset(-20,0,17)); relayMast(l,c,origin.offset(20,0,17));
        story(l,c,origin.offset(0,1,-13),Blocks.LECTERN.defaultBlockState());
        cache(l,c,origin.offset(0,1,4),"quantum_relay_station",true,4,70);
        cache(l,c,origin.offset(19,1,5),"story_cache",false,0,0);
    }

    private void bunker(WorldGenLevel l, BoundingBox c) {
        BlockState wall=dark(), floor=floor(), glass=glass();
        // Surface entrance descends continuously to bunker level.
        room(l,c,origin.offset(0,8,-27),7,5,5,metal(),floor,glass); stairNorthSouth(l,c,origin.offset(0,8,-21),8,12);
        room(l,c,origin.offset(0,0,-13),8,6,5,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,-7),4);
        room(l,c,origin,11,9,7,wall,floor,glass);
        room(l,c,origin.offset(-20,0,3),8,7,6,wall,floor,glass); corridorX(l,c,origin.offset(-13,0,3),5);
        room(l,c,origin.offset(20,0,3),8,7,6,wall,floor,glass); corridorX(l,c,origin.offset(13,0,3),5);
        room(l,c,origin.offset(0,0,20),9,7,6,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,13),5);
        barracksScenery(l,c,origin.offset(20,0,3)); droneScenery(l,c,origin.offset(-20,0,3));
        story(l,c,origin.offset(0,1,0),Blocks.LECTERN.defaultBlockState());
        cache(l,c,origin.offset(0,1,20),"android_command_bunker",true,5,75);
        cache(l,c,origin.offset(-20,1,3),"story_cache",false,0,0);
    }

    private void fusion(WorldGenLevel l, BoundingBox c) {
        BlockState wall=metal(), floor=darkFloor(), glass=glass();
        room(l,c,origin.offset(0,0,-29),7,5,6,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,-21),5);
        room(l,c,origin.offset(0,0,-14),9,6,6,white(),floor,glass); corridorZ(l,c,origin.offset(0,0,-7),4);
        circularCore(l,c,origin,14,9);
        room(l,c,origin.offset(-23,0,2),8,7,6,dark(),floor,glass); corridorX(l,c,origin.offset(-14,0,2),6);
        room(l,c,origin.offset(23,0,2),8,7,6,dark(),floor,glass); corridorX(l,c,origin.offset(14,0,2),6);
        // Broken coils/stabilizer pedestals sell the reactor story without giving machinery.
        for(int x:new int[]{-8,8})for(int z:new int[]{-5,5})story(l,c,origin.offset(x,1,z),Blocks.CUT_COPPER.defaultBlockState());
        story(l,c,origin.offset(0,1,-14),Blocks.LECTERN.defaultBlockState());
        cache(l,c,origin.offset(0,1,5),"fusion_research_complex",true,5,75);
        cache(l,c,origin.offset(-23,1,2),"story_cache",false,0,0);
    }

    private void blackSite(WorldGenLevel l, BoundingBox c) {
        BlockState wall=black(), floor=darkFloor(), glass=glass();
        room(l,c,origin.offset(0,12,-29),6,5,5,wall,floor,glass); stairNorthSouth(l,c,origin.offset(0,12,-23),12,16);
        room(l,c,origin.offset(0,0,-15),8,6,5,wall,floor,glass); corridorZ(l,c,origin.offset(0,0,-8),5);
        room(l,c,origin,11,9,7,wall,floor,glass);
        room(l,c,origin.offset(-20,0,2),8,7,6,black(),floor,glass); corridorX(l,c,origin.offset(-13,0,2),5);
        room(l,c,origin.offset(20,0,2),8,7,6,black(),floor,glass); corridorX(l,c,origin.offset(13,0,2),5);
        containmentScenery(l,c,origin.offset(20,0,2)); experimentScenery(l,c,origin.offset(-20,0,2));
        // Core -> deep descent -> vault. No one-way drop.
        stairNorthSouth(l,c,origin.offset(0,0,12),8,12);
        corridorZ(l,c,origin.offset(0,-8,19),5);
        room(l,c,origin.offset(0,-8,28),10,7,6,wall,floor,glass);
        story(l,c,origin.offset(0,1,0),Blocks.LECTERN.defaultBlockState());
        cache(l,c,origin.offset(0,-7,28),"black_site",true,6,90);
        cache(l,c,origin.offset(-20,1,2),"story_cache",false,0,0);
    }

    private void room(WorldGenLevel l, BoundingBox clip, BlockPos o, int hx, int hz, int h, BlockState wall, BlockState floor, BlockState glass) {
        BlockState beam=beam();
        for(int x=-hx;x<=hx;x++)for(int z=-hz;z<=hz;z++){
            boolean cut=Math.abs(x)>=hx-1&&Math.abs(z)>=hz-1; if(cut)continue;
            set(l,clip,o.offset(x,0,z),floor);
            boolean edge=Math.abs(x)==hx||Math.abs(z)==hz||Math.abs(x)==hx-1&&Math.abs(z)>=hz-2||Math.abs(z)==hz-1&&Math.abs(x)>=hx-2;
            for(int y=1;y<=h;y++){
                boolean door=(Math.abs(x)<=1&&Math.abs(z)>=hz-1&&y<=3)||(Math.abs(z)<=1&&Math.abs(x)>=hx-1&&y<=3);
                set(l,clip,o.offset(x,y,z),!edge||door?Blocks.AIR.defaultBlockState():(y==2||y==3?glass:(y==1||y==h?beam:wall)));
            }
            set(l,clip,o.offset(x,h+1,z),edge?beam:wall);
        }
        // Readable centre lane.
        for(int z=-hz+2;z<=hz-2;z++)set(l,clip,o.offset(0,0,z),stripe());
    }

    private void corridorZ(WorldGenLevel l,BoundingBox c,BlockPos o,int half){for(int z=-half;z<=half;z++)for(int x=-1;x<=1;x++){set(l,c,o.offset(x,0,z),x==0?stripe():floor());for(int y=1;y<=3;y++)set(l,c,o.offset(x,y,z),Blocks.AIR.defaultBlockState());}}
    private void corridorX(WorldGenLevel l,BoundingBox c,BlockPos o,int half){for(int x=-half;x<=half;x++)for(int z=-1;z<=1;z++){set(l,c,o.offset(x,0,z),z==0?stripe():floor());for(int y=1;y<=3;y++)set(l,c,o.offset(x,y,z),Blocks.AIR.defaultBlockState());}}

    private void stairNorthSouth(WorldGenLevel l,BoundingBox c,BlockPos top,int drop,int run){
        for(int i=0;i<=run;i++){int y=-Math.min(drop,(i*drop)/Math.max(1,run));for(int x=-1;x<=1;x++){BlockPos p=top.offset(x,y,i);set(l,c,p,floor());for(int h=1;h<=3;h++)set(l,c,p.above(h),Blocks.AIR.defaultBlockState());}}
    }
    private void steppedDown(WorldGenLevel l,BoundingBox c,BlockPos start,int dy,int run){for(int i=0;i<=run;i++)for(int w=-1;w<=1;w++){BlockPos p=start.offset(i,dy*Math.min(i,2),w);set(l,c,p,floor());for(int h=1;h<=3;h++)set(l,c,p.above(h),Blocks.AIR.defaultBlockState());}}

    private void circularCore(WorldGenLevel l,BoundingBox c,BlockPos o,int r,int h){
        for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){double d=Math.sqrt(x*x+z*z);if(d<=r-1){set(l,c,o.offset(x,0,z),floor());for(int y=1;y<=h;y++)set(l,c,o.offset(x,y,z),Blocks.AIR.defaultBlockState());}if(d>r-1.5&&d<=r+.2)for(int y=1;y<=h;y++)set(l,c,o.offset(x,y,z),(y==1||y==h)?beam():metal());if(d<=r)set(l,c,o.offset(x,h+1,z),dark());}
        for(int a=-2;a<=2;a++)for(int y=1;y<=3;y++){set(l,c,o.offset(a,y,-r),Blocks.AIR.defaultBlockState());set(l,c,o.offset(a,y,r),Blocks.AIR.defaultBlockState());set(l,c,o.offset(-r,y,a),Blocks.AIR.defaultBlockState());set(l,c,o.offset(r,y,a),Blocks.AIR.defaultBlockState());}
    }
    private void relayMast(WorldGenLevel l,BoundingBox c,BlockPos o){for(int y=0;y<=18;y++){set(l,c,o.above(y),beam());if(y%4==0){set(l,c,o.offset(1,y,0),Blocks.IRON_BARS.defaultBlockState());set(l,c,o.offset(-1,y,0),Blocks.IRON_BARS.defaultBlockState());}}}
    private void assemblyScenery(WorldGenLevel l,BoundingBox c,BlockPos o){for(int z=-4;z<=4;z+=4){set(l,c,o.offset(-4,1,z),Blocks.ANVIL.defaultBlockState());set(l,c,o.offset(4,1,z),Blocks.SMITHING_TABLE.defaultBlockState());}}
    private void refineryScenery(WorldGenLevel l,BoundingBox c,BlockPos o){for(int x=-6;x<=6;x+=4){set(l,c,o.offset(x,1,4),Blocks.CUT_COPPER.defaultBlockState());set(l,c,o.offset(x,2,4),Blocks.IRON_BARS.defaultBlockState());}}
    private void barracksScenery(WorldGenLevel l,BoundingBox c,BlockPos o){for(int z=-4;z<=4;z+=4){set(l,c,o.offset(-4,1,z),Blocks.IRON_BARS.defaultBlockState());set(l,c,o.offset(4,1,z),Blocks.IRON_BARS.defaultBlockState());}}
    private void droneScenery(WorldGenLevel l,BoundingBox c,BlockPos o){for(int x=-4;x<=4;x+=4)set(l,c,o.offset(x,1,3),Blocks.CHAIN.defaultBlockState());}
    private void containmentScenery(WorldGenLevel l,BoundingBox c,BlockPos o){for(int x=-4;x<=4;x+=4)for(int z=-3;z<=3;z+=6)set(l,c,o.offset(x,1,z),Blocks.OBSIDIAN.defaultBlockState());}
    private void experimentScenery(WorldGenLevel l,BoundingBox c,BlockPos o){for(int x=-4;x<=4;x+=4)set(l,c,o.offset(x,1,2),Blocks.GLASS.defaultBlockState());}
    private void story(WorldGenLevel l,BoundingBox c,BlockPos p,BlockState s){set(l,c,p,s);if(l.getBlockState(p.above()).isAir())set(l,c,p.above(),Blocks.COBWEB.defaultBlockState());}

    private void cache(WorldGenLevel l,BoundingBox c,BlockPos p,String table,boolean guarded,int reserve,int ranged){
        Block crate=modBlock("tritanium_crate",Blocks.BARREL);set(l,c,p,crate.defaultBlockState());
        if(l.getBlockEntity(p) instanceof TritaniumCrateBlockEntity be)be.seedStructureLoot(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"chests/facilities/"+table),l.getSeed()^p.asLong()^facility.ordinal());
        if(!guarded)return;
        for(BlockPos off:new BlockPos[]{new BlockPos(3,0,0),new BlockPos(-3,0,0),new BlockPos(0,0,3),new BlockPos(0,0,-3)}){
            BlockPos q=p.offset(off);if(!c.isInside(q)||!l.getBlockState(q).isAir()||!l.getBlockState(q.above()).isAir()||l.getBlockState(q.below()).isAir())continue;
            Block sp=modBlock("android_spawner",Blocks.IRON_BLOCK);set(l,c,q,sp.defaultBlockState());if(l.getBlockEntity(q) instanceof AndroidSpawnerBlockEntity s)s.configureFacility(id(),reserve,ranged);break;
        }
    }

    private String id(){return facility.name().toLowerCase(java.util.Locale.ROOT);}
    private static BoundingBox box(BlockPos p){return new BoundingBox(p.getX()-40,p.getY()-16,p.getZ()-40,p.getX()+40,p.getY()+24,p.getZ()+40);}
    private static Block modBlock(String id,Block fallback){Block b=ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,id));return b==null||b==Blocks.AIR?fallback:b;}
    private static BlockState metal(){return modBlock("decorative.tritanium_plate",Blocks.IRON_BLOCK).defaultBlockState();}private static BlockState white(){return modBlock("decorative.white_plate",Blocks.QUARTZ_BLOCK).defaultBlockState();}private static BlockState dark(){return modBlock("decorative.carbon_fiber_plate",Blocks.DEEPSLATE_BRICKS).defaultBlockState();}private static BlockState black(){return modBlock("decorative.carbon_fiber_plate",Blocks.REINFORCED_DEEPSLATE).defaultBlockState();}private static BlockState green(){return modBlock("decorative.tritanium_plate_green",Blocks.OXIDIZED_COPPER).defaultBlockState();}private static BlockState floor(){return modBlock("decorative.floor_tiles",Blocks.SMOOTH_STONE).defaultBlockState();}private static BlockState darkFloor(){return modBlock("decorative.floor_tiles",Blocks.DEEPSLATE_TILES).defaultBlockState();}private static BlockState greenFloor(){return modBlock("decorative.floor_tiles_green",Blocks.OXIDIZED_COPPER).defaultBlockState();}private static BlockState beam(){return modBlock("decorative.beams",Blocks.POLISHED_DEEPSLATE).defaultBlockState();}private static BlockState glass(){return modBlock("industrial_glass",Blocks.TINTED_GLASS).defaultBlockState();}private static BlockState stripe(){return modBlock("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE).defaultBlockState();}
    private void set(WorldGenLevel l,BoundingBox c,BlockPos p,BlockState s){if(!c.isInside(p)||!getBoundingBox().isInside(p)||l.getBlockState(p).is(Blocks.BEDROCK))return;l.setBlock(p,s,2);}
}
