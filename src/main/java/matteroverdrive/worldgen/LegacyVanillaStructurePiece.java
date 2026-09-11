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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Vanilla-style replacements for the six classic Matter Overdrive structures.
 *
 * Contract: terrain -> obvious entrance -> readable critical path -> guarded focal
 * reward -> optional story spaces -> same route back out. These structures contain
 * no usable Matter Overdrive production infrastructure. Technology appears only as
 * inert scenery, guarded salvage/research caches, and environmental storytelling.
 */
public final class LegacyVanillaStructurePiece extends StructurePiece {
    private final LegacyParityStructureFeature.Kind kind;
    private final BlockPos origin;

    public LegacyVanillaStructurePiece(LegacyParityStructureFeature.Kind kind, BlockPos origin) {
        super(ModStructures.LEGACY_VANILLA_PIECE.get(), 0, boxFor(kind, origin));
        this.kind = kind;
        this.origin = origin;
    }

    public LegacyVanillaStructurePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.LEGACY_VANILLA_PIECE.get(), tag);
        this.kind = LegacyParityStructureFeature.Kind.valueOf(tag.getString("MOKind"));
        this.origin = new BlockPos(tag.getInt("MOX"), tag.getInt("MOY"), tag.getInt("MOZ"));
    }

    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("MOKind", kind.name());
        tag.putInt("MOX", origin.getX()); tag.putInt("MOY", origin.getY()); tag.putInt("MOZ", origin.getZ());
    }

    @Override public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                                     RandomSource random, BoundingBox clip, ChunkPos chunkPos, BlockPos pivot) {
        switch (kind) {
            case CRASHED_SHIP -> crashedShip(level, clip);
            case CARGO_SHIP -> cargoShip(level, clip);
            case UNDERWATER_BASE -> underwaterBase(level, clip);
            case MAD_SCIENTIST_HOUSE -> madScientistLab(level, clip);
            case ANDROID_HOUSE -> androidSafehouse(level, clip);
            case SAND_PIT -> excavation(level, clip);
        }
    }

    private void crashedShip(WorldGenLevel level, BoundingBox clip) {
        BlockState hull=metal(), floor=floor(), beam=beam(), glass=glass(), stripe=stripe();
        for(int z=-20;z<=18;z++) {
            int half=z<-14?Math.max(3,7-(Math.abs(z+14)/2)):z>12?Math.max(3,7-(z-12)):7;
            for(int x=-half;x<=half;x++) {
                set(level,clip,origin.offset(x,0,z),Math.abs(x)==half?beam:floor);
                for(int y=1;y<=6;y++) {
                    boolean wall=Math.abs(x)==half;
                    boolean breach=z>=5&&z<=12&&x>=half-1&&y<=4;
                    boolean entry=z<=-17&&Math.abs(x)<=2&&y<=3;
                    set(level,clip,origin.offset(x,y,z),wall&&!breach&&!entry?(y==3?glass:hull):Blocks.AIR.defaultBlockState());
                }
                if(Math.abs(x)<half)set(level,clip,origin.offset(x,7,z),((x+z)&5)==0?beam:hull);
            }
        }
        for(int z=-27;z<=-20;z++)for(int x=-2;x<=2;x++){set(level,clip,origin.offset(x,0,z),x==0?stripe:floor);for(int y=1;y<=3;y++)set(level,clip,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());}
        for(int z=-16;z<=14;z++)set(level,clip,origin.offset(0,0,z),stripe);
        set(level,clip,origin.offset(-4,1,-11),Blocks.REDSTONE_LAMP.defaultBlockState());
        set(level,clip,origin.offset(4,1,-11),Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
        set(level,clip,origin.offset(-5,1,3),Blocks.COBWEB.defaultBlockState());
        cache(level,clip,origin.offset(0,1,14),"salvage",true,"crashed_ship");
        cache(level,clip,origin.offset(-5,1,7),"story_cache",false,"crashed_ship");
    }

    private void cargoShip(WorldGenLevel level, BoundingBox clip) {
        BlockState hull=metal(), floor=floor(), beam=beam(), glass=glass(), stripe=stripe();
        for(int x=-30;x<=30;x++){
            int hz=Math.abs(x)>25?6:12;
            for(int z=-hz;z<=hz;z++){
                set(level,clip,origin.offset(x,0,z),Math.abs(z)==hz?beam:floor);
                for(int y=1;y<=8;y++){
                    boolean edge=Math.abs(z)==hz;
                    boolean loading=x>=-4&&x<=8&&Math.abs(z)==hz&&y<=4;
                    set(level,clip,origin.offset(x,y,z),edge&&!loading?(y>=3&&y<=5?glass:hull):Blocks.AIR.defaultBlockState());
                }
                if(Math.abs(z)<hz)set(level,clip,origin.offset(x,9,z),((x+z)&7)==0?beam:hull);
            }
        }
        for(int z=-19;z<=-12;z++)for(int x=-3;x<=3;x++){set(level,clip,origin.offset(x,0,z),x==0?stripe:floor);for(int y=1;y<=4;y++)set(level,clip,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());}
        for(int x=-26;x<=26;x++)for(int z=-2;z<=2;z++){set(level,clip,origin.offset(x,0,z),z==0?stripe:floor);for(int y=1;y<=5;y++)set(level,clip,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());}
        for(int cx:new int[]{-12,8})for(int x=-5;x<=5;x+=5)for(int z:new int[]{-7,7}){set(level,clip,origin.offset(cx+x,1,z),beam);if(((x+z)&1)==0)cache(level,clip,origin.offset(cx+x,2,z),"salvage",false,"cargo_ship");}
        cache(level,clip,origin.offset(25,1,0),"story_cache",true,"cargo_ship");
        set(level,clip,origin.offset(-26,1,0),Blocks.LECTERN.defaultBlockState());
    }

    private void underwaterBase(WorldGenLevel level, BoundingBox clip) {
        BlockState hull=metal(), floor=floor(), beam=beam(), glass=glass();
        pod(level,clip,origin,9,6,hull,floor,beam,glass);
        pod(level,clip,origin.offset(-17,0,0),6,5,hull,floor,beam,glass);
        pod(level,clip,origin.offset(17,0,0),6,5,hull,floor,beam,glass);
        pod(level,clip,origin.offset(0,0,17),6,5,hull,floor,beam,glass);
        tube(level,clip,origin.offset(-13,0,0),true,5); tube(level,clip,origin.offset(13,0,0),true,5); tube(level,clip,origin.offset(0,0,13),false,5);
        tube(level,clip,origin.offset(0,0,-14),false,8);
        for(int y=1;y<=3;y++)for(int x=-1;x<=1;x++)set(level,clip,origin.offset(x,y,-22),Blocks.AIR.defaultBlockState());
        cache(level,clip,origin.offset(0,1,17),"story_cache",true,"underwater_base");
        cache(level,clip,origin.offset(-17,1,0),"salvage",false,"underwater_base");
        set(level,clip,origin.offset(17,1,0),Blocks.CRYING_OBSIDIAN.defaultBlockState());
    }

    private void madScientistLab(WorldGenLevel level, BoundingBox clip) {
        BlockState wall=white(), floor=floor(), dark=dark(), beam=beam(), glass=glass();
        room(level,clip,origin,7,6,5,wall,floor,beam,glass);
        BlockPos stairStart=origin.offset(5,0,4);
        BlockState stair=Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,net.minecraft.core.Direction.SOUTH);
        for(int i=0;i<=7;i++){
            int y=-i,z=i;
            for(int x=-1;x<=1;x++){BlockPos p=stairStart.offset(x,y,z);set(level,clip,p,stair);for(int h=1;h<=3;h++)set(level,clip,p.above(h),Blocks.AIR.defaultBlockState());}
        }
        BlockPos lab=origin.offset(0,-7,14); room(level,clip,lab,10,8,5,dark,floor,beam,glass);
        for(int x:new int[]{-6,-3,3,6})set(level,clip,lab.offset(x,1,-3),Blocks.GLASS.defaultBlockState());
        set(level,clip,lab.offset(-7,1,4),Blocks.COBWEB.defaultBlockState());
        set(level,clip,lab.offset(7,1,4),Blocks.CRYING_OBSIDIAN.defaultBlockState());
        set(level,clip,lab.offset(0,1,-5),Blocks.LECTERN.defaultBlockState());
        cache(level,clip,lab.offset(0,1,4),"story_cache",true,"mad_scientist_house");
    }

    private void androidSafehouse(WorldGenLevel level, BoundingBox clip) {
        BlockState wall=white(), floor=floor(), beam=beam(), glass=glass(), dark=dark();
        room(level,clip,origin,7,8,6,wall,floor,beam,glass);
        room(level,clip,origin.offset(-13,0,3),5,6,5,dark,floor,beam,glass);
        room(level,clip,origin.offset(13,0,3),5,6,5,dark,floor,beam,glass);
        room(level,clip,origin.offset(0,0,14),8,5,5,dark,floor,beam,glass);
        corridorX(level,clip,origin.offset(-9,0,3),4); corridorX(level,clip,origin.offset(9,0,3),4); corridorZ(level,clip,origin.offset(0,0,10),4);
        corridorZ(level,clip,origin.offset(0,0,-10),4);
        cache(level,clip,origin.offset(0,1,14),"story_cache",true,"android_house");
        cache(level,clip,origin.offset(-13,1,3),"salvage",false,"android_house");
        set(level,clip,origin.offset(13,1,3),Blocks.REDSTONE_LAMP.defaultBlockState());
    }

    private void excavation(WorldGenLevel level, BoundingBox clip) {
        BlockState stone=Blocks.CUT_SANDSTONE.defaultBlockState(), floor=floor(), beam=beam(), stripe=stripe();
        for(int x=-17;x<=17;x++)for(int z=-17;z<=17;z++){
            int r=Math.max(Math.abs(x),Math.abs(z)); int depth=r<=6?10:r<=11?7:r<=15?3:1;
            for(int y=1;y<=depth;y++)set(level,clip,origin.offset(x,-y,z),Blocks.AIR.defaultBlockState());
            set(level,clip,origin.offset(x,-depth-1,z),stone);
        }
        for(int i=0;i<12;i++){int y=-1-Math.min(i,9);for(int w=-1;w<=1;w++){BlockPos q=origin.offset(-15+i,y,13+w);set(level,clip,q,stone);for(int h=1;h<=3;h++)set(level,clip,q.above(h),Blocks.AIR.defaultBlockState());}}
        for(int i=1;i<=13;i++){int y=-10;for(int w=-1;w<=1;w++){BlockPos q=origin.offset(-4+w,y,13-i);set(level,clip,q,stone);for(int h=1;h<=3;h++)set(level,clip,q.above(h),Blocks.AIR.defaultBlockState());}}
        BlockPos relic=origin.offset(0,-10,0);
        for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)if(Math.abs(x)+Math.abs(z)<=11)set(level,clip,relic.offset(x,0,z),Math.abs(x)<=1?stripe:floor);
        for(int y=1;y<=7;y++)set(level,clip,relic.offset(-6,y,-5),beam);
        cache(level,clip,relic.offset(0,1,4),"story_cache",true,"sand_pit");
        cache(level,clip,relic.offset(5,1,-3),"salvage",false,"sand_pit");
    }

    private void pod(WorldGenLevel level,BoundingBox clip,BlockPos c,int r,int h,BlockState wall,BlockState floor,BlockState beam,BlockState glass){
        for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){double d=Math.sqrt(x*x+z*z);if(d<=r-1){set(level,clip,c.offset(x,0,z),floor);for(int y=1;y<=h;y++)set(level,clip,c.offset(x,y,z),Blocks.AIR.defaultBlockState());}if(d>r-1.2&&d<=r+.3)for(int y=1;y<=h;y++)set(level,clip,c.offset(x,y,z),y==1||y==h?beam:glass);if(d<=r)set(level,clip,c.offset(x,h+1,z),wall);}for(int y=1;y<=3;y++)for(int w=-1;w<=1;w++){set(level,clip,c.offset(w,y,-r),Blocks.AIR.defaultBlockState());set(level,clip,c.offset(w,y,r),Blocks.AIR.defaultBlockState());set(level,clip,c.offset(-r,y,w),Blocks.AIR.defaultBlockState());set(level,clip,c.offset(r,y,w),Blocks.AIR.defaultBlockState());}}
    private void tube(WorldGenLevel level,BoundingBox clip,BlockPos c,boolean xAxis,int half){for(int a=-half;a<=half;a++)for(int b=-2;b<=2;b++){int x=xAxis?a:b,z=xAxis?b:a;set(level,clip,c.offset(x,0,z),floor());for(int y=1;y<=4;y++)set(level,clip,c.offset(x,y,z),Math.abs(b)==2?glass():Blocks.AIR.defaultBlockState());set(level,clip,c.offset(x,5,z),metal());}}
    private void room(WorldGenLevel level,BoundingBox clip,BlockPos c,int hx,int hz,int h,BlockState wall,BlockState floor,BlockState beam,BlockState glass){for(int x=-hx;x<=hx;x++)for(int z=-hz;z<=hz;z++){boolean cut=Math.abs(x)>=hx-1&&Math.abs(z)>=hz-1;if(cut)continue;set(level,clip,c.offset(x,0,z),floor);boolean edge=Math.abs(x)==hx||Math.abs(z)==hz||Math.abs(x)==hx-1&&Math.abs(z)>=hz-2||Math.abs(z)==hz-1&&Math.abs(x)>=hx-2;for(int y=1;y<=h;y++)set(level,clip,c.offset(x,y,z),edge?(y==2||y==3?glass:wall):Blocks.AIR.defaultBlockState());set(level,clip,c.offset(x,h+1,z),edge?beam:wall);}}
    private void corridorX(WorldGenLevel level,BoundingBox clip,BlockPos c,int half){for(int x=-half;x<=half;x++)for(int z=-1;z<=1;z++){set(level,clip,c.offset(x,0,z),floor());for(int y=1;y<=3;y++)set(level,clip,c.offset(x,y,z),Blocks.AIR.defaultBlockState());}}
    private void corridorZ(WorldGenLevel level,BoundingBox clip,BlockPos c,int half){for(int z=-half;z<=half;z++)for(int x=-1;x<=1;x++){set(level,clip,c.offset(x,0,z),floor());for(int y=1;y<=3;y++)set(level,clip,c.offset(x,y,z),Blocks.AIR.defaultBlockState());}}

    private void cache(WorldGenLevel level,BoundingBox clip,BlockPos pos,String table,boolean guarded,String profile){
        Block crate=modBlock("tritanium_crate",Blocks.BARREL);set(level,clip,pos,crate.defaultBlockState());
        if(level.getBlockEntity(pos) instanceof TritaniumCrateBlockEntity c)c.seedStructureLoot(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,"chests/facilities/"+table),level.getSeed()^pos.asLong());
        if(!guarded)return;
        BlockPos guard=pos.offset(3,0,0);if(!clip.isInside(guard)||!level.getBlockState(guard).isAir())guard=pos.offset(-3,0,0);
        if(clip.isInside(guard)&&level.getBlockState(guard).isAir()&&!level.getBlockState(guard.below()).isAir()){
            Block spawner=modBlock("android_spawner",Blocks.IRON_BLOCK);set(level,clip,guard,spawner.defaultBlockState());
            if(level.getBlockEntity(guard) instanceof AndroidSpawnerBlockEntity be)be.configureFacility(profile,profile.equals("mad_scientist_house")?2:3,60);
        }
    }

    private static BoundingBox boxFor(LegacyParityStructureFeature.Kind kind,BlockPos p){return switch(kind){case CRASHED_SHIP->new BoundingBox(p.getX()-10,p.getY()-2,p.getZ()-28,p.getX()+10,p.getY()+9,p.getZ()+20);case CARGO_SHIP->new BoundingBox(p.getX()-31,p.getY()-2,p.getZ()-20,p.getX()+31,p.getY()+11,p.getZ()+20);case UNDERWATER_BASE->new BoundingBox(p.getX()-25,p.getY()-2,p.getZ()-24,p.getX()+25,p.getY()+9,p.getZ()+24);case MAD_SCIENTIST_HOUSE->new BoundingBox(p.getX()-12,p.getY()-9,p.getZ()-8,p.getX()+12,p.getY()+7,p.getZ()+24);case ANDROID_HOUSE->new BoundingBox(p.getX()-20,p.getY()-2,p.getZ()-15,p.getX()+20,p.getY()+8,p.getZ()+20);case SAND_PIT->new BoundingBox(p.getX()-19,p.getY()-13,p.getZ()-19,p.getX()+19,p.getY()+8,p.getZ()+19);};}
    private static Block modBlock(String id,Block fallback){Block b=ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID,id));return b==null||b==Blocks.AIR?fallback:b;}
    private static BlockState metal(){return modBlock("decorative.tritanium_plate",Blocks.IRON_BLOCK).defaultBlockState();}private static BlockState white(){return modBlock("decorative.white_plate",Blocks.QUARTZ_BLOCK).defaultBlockState();}private static BlockState dark(){return modBlock("decorative.carbon_fiber_plate",Blocks.DEEPSLATE_BRICKS).defaultBlockState();}private static BlockState floor(){return modBlock("decorative.floor_tiles",Blocks.SMOOTH_STONE).defaultBlockState();}private static BlockState beam(){return modBlock("decorative.beams",Blocks.POLISHED_DEEPSLATE).defaultBlockState();}private static BlockState glass(){return modBlock("industrial_glass",Blocks.TINTED_GLASS).defaultBlockState();}private static BlockState stripe(){return modBlock("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE).defaultBlockState();}
    private void set(WorldGenLevel level,BoundingBox clip,BlockPos pos,BlockState state){if(!clip.isInside(pos)||!getBoundingBox().isInside(pos)||level.getBlockState(pos).is(Blocks.BEDROCK))return;level.setBlock(pos,state,2);}
}
