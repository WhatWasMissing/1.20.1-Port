package matteroverdrive.worldgen;

import com.mojang.serialization.Codec;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class LegacyStructureFeature extends Feature<NoneFeatureConfiguration> {
    public enum Kind { CRASHED_SHIP, CARGO_SHIP, UNDERWATER_BASE, MAD_SCIENTIST_HOUSE, ANDROID_HOUSE, SAND_PIT }
    private final Kind kind;
    public LegacyStructureFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) { super(codec); this.kind = kind; }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return switch (kind) {
            case CRASHED_SHIP -> crashedShip(context.level(), context.origin(), context.random());
            case CARGO_SHIP -> cargoShip(context.level(), context.origin(), context.random());
            case UNDERWATER_BASE -> underwaterBase(context.level(), context.origin());
            case MAD_SCIENTIST_HOUSE -> madScientistHouse(context.level(), context.origin());
            case ANDROID_HOUSE -> androidHouse(context.level(), context.origin(), context.random());
            case SAND_PIT -> sandPit(context.level(), context.origin(), context.random());
        };
    }

    private static boolean crashedShip(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!level.getFluidState(origin).isEmpty()) return false;
        BlockPos base = origin.below();
        BlockState hull = block("decorative.tritanium_plate");
        BlockState stripe = block("decorative.tritanium_plate_stripe");
        for (int z=-14; z<=14; z++) {
            int half = z < -9 || z > 10 ? 2 : 4;
            for (int x=-half; x<=half; x++) {
                if (z > 5 && Math.floorMod(x*31+z*17,19)<3) continue;
                set(level,base.offset(x,0,z),hull);
                if (Math.abs(x)==half && z%3!=0) set(level,base.offset(x,1,z),hull);
            }
        }
        for (int z=-8;z<=5;z+=3) { set(level,base.offset(-4,1,z),stripe); set(level,base.offset(4,1,z),stripe); }
        for (int x=-2;x<=2;x++) set(level,base.offset(x,2,-8),block("industrial_glass"));
        set(level,base.offset(0,1,3),block("tritanium_crate"));
        set(level,base.offset(2,1,-2),block("holo_sign"));
        for(int i=0;i<5;i++) set(level,base.offset(random.nextInt(9)-4,-1,random.nextInt(25)-12),Blocks.COARSE_DIRT.defaultBlockState());
        return true;
    }

    private static boolean cargoShip(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!level.getFluidState(origin).isEmpty()) return false;
        BlockPos base=origin.above();
        BlockState hull=block("decorative.tritanium_plate");
        BlockState floor=block("decorative.floor_tiles");
        for(int z=-20;z<=20;z++) {
            int half=Math.max(3,8-Math.abs(z)/5);
            for(int x=-half;x<=half;x++) { set(level,base.offset(x,0,z),floor); if(Math.abs(x)==half){set(level,base.offset(x,1,z),hull); if(z%2==0)set(level,base.offset(x,2,z),hull);} }
        }
        for(int z=-13;z<=13;z++){set(level,base.offset(-3,3,z),hull);set(level,base.offset(3,3,z),hull);if(z%2==0){set(level,base.offset(-2,3,z),block("industrial_glass"));set(level,base.offset(2,3,z),block("industrial_glass"));}}
        for(int z=-15;z<=15;z+=5){set(level,base.offset(0,1,z),block("decorative.tritanium_lamp"));if(random.nextBoolean())set(level,base.offset(2,1,z+1),block("tritanium_crate"));}
        set(level,base.offset(0,1,-17),block("holo_sign"));
        return true;
    }

    private static boolean underwaterBase(WorldGenLevel level, BlockPos origin) {
        if (level.getFluidState(origin.above(8)).isEmpty()) return false;
        BlockPos c=origin.above(); int r=10;
        BlockState hull=block("decorative.tritanium_plate");
        BlockState glass=block("industrial_glass");
        BlockState floor=block("decorative.floor_tiles");
        for(int x=-r;x<=r;x++) for(int z=-r;z<=r;z++) {
            double d=Math.sqrt(x*x+z*z);
            if(d<=r-1) {
                set(level,c.offset(x,0,z),floor);
                for(int y=1;y<=4;y++) set(level,c.offset(x,y,z),Blocks.AIR.defaultBlockState());
            }
            if(d>=r-1.2D&&d<=r+.2D) for(int y=1;y<=4;y++) set(level,c.offset(x,y,z),y==2?glass:hull);
        }
        for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)if(x*x+z*z<=38)set(level,c.offset(x,5,z),x*x+z*z>24?glass:hull);
        set(level,c.offset(0,1,0),block("matter_analyzer"));
        set(level,c.offset(3,1,0),block("tritanium_crate_blue"));
        return true;
    }

    private static boolean madScientistHouse(WorldGenLevel level, BlockPos origin) {
        if (!level.getFluidState(origin).isEmpty()) return false;
        BlockState plate=block("decorative.white_plate");
        BlockState floor=block("decorative.floor_tile_white");
        BlockState glass=block("industrial_glass");
        BlockState beam=block("decorative.beams");
        for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++){set(level,origin.offset(x,0,z),floor);if(Math.abs(x)==5||Math.abs(z)==5)for(int y=1;y<=4;y++){boolean window=y>=2&&y<=3&&((Math.abs(x)==5&&Math.abs(z)<=2)||(Math.abs(z)==5&&Math.abs(x)<=2));set(level,origin.offset(x,y,z),window?glass:plate);}set(level,origin.offset(x,5,z),plate);}
        for(int y=1;y<=4;y++){set(level,origin.offset(-4,y,-4),beam);set(level,origin.offset(4,y,-4),beam);set(level,origin.offset(-4,y,4),beam);set(level,origin.offset(4,y,4),beam);}
        set(level,origin.offset(0,1,2),block("inscriber"));
        set(level,origin.offset(2,1,2),block("decomposer"));
        set(level,origin.offset(-2,1,2),block("tritanium_crate"));
        return true;
    }

    private static boolean androidHouse(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!level.getFluidState(origin).isEmpty() || level.getBlockState(origin.below()).isAir()) return false;
        BlockState hull=block("decorative.tritanium_plate");
        BlockState white=block("decorative.white_plate");
        BlockState floor=block("decorative.floor_tiles");
        BlockState glass=block("industrial_glass");
        BlockState beam=block("decorative.beams");
        BlockState lamp=block("decorative.tritanium_lamp");
        int r=9;
        for(int x=-r;x<=r;x++) for(int z=-r;z<=r;z++) {
            set(level,origin.offset(x,0,z),floor);
            boolean edge=Math.abs(x)==r||Math.abs(z)==r;
            if(edge) {
                for(int y=1;y<=5;y++) {
                    boolean frontDoor=z==-r && Math.abs(x)<=1 && y<=3;
                    boolean window=y>=2&&y<=3&&((Math.abs(x)==r&&Math.abs(z)<=4)||(Math.abs(z)==r&&Math.abs(x)>=3&&Math.abs(x)<=6));
                    if(frontDoor) set(level,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());
                    else set(level,origin.offset(x,y,z),window?glass:((x+z+y)&1)==0?hull:white);
                }
            } else {
                for(int y=1;y<=4;y++) set(level,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());
            }
            set(level,origin.offset(x,6,z),((Math.abs(x)+Math.abs(z))%5==0)?white:hull);
        }
        for(int y=1;y<=5;y++) for(int sx:new int[]{-8,8}) for(int sz:new int[]{-8,8}) set(level,origin.offset(sx,y,sz),beam);
        for(int x=-4;x<=4;x++) {
            set(level,origin.offset(x,1,4),white);
            if(Math.abs(x)!=1) set(level,origin.offset(x,2,4),glass);
        }
        for(int z=-5;z<=5;z+=5) set(level,origin.offset(0,5,z),lamp);
        set(level,origin.offset(0,1,6),block("holo_sign"));
        set(level,origin.offset(-5,1,5),block("tritanium_crate_blue"));
        set(level,origin.offset(5,1,5),block("tritanium_crate"));
        set(level,origin.offset(-5,1,-4),block("matter_analyzer"));
        set(level,origin.offset(5,1,-4),random.nextBoolean()?block("inscriber"):block("decomposer"));
        return true;
    }

    private static boolean sandPit(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockState ground=level.getBlockState(origin.below());
        if (!(ground.is(Blocks.SAND)||ground.is(Blocks.RED_SAND)||ground.is(Blocks.SANDSTONE)||ground.is(Blocks.RED_SANDSTONE))) return false;
        if (!level.getFluidState(origin).isEmpty()) return false;
        int radius=15;
        for(int x=-radius;x<=radius;x++) for(int z=-radius;z<=radius;z++) {
            double d=Math.sqrt(x*x+z*z);
            if(d>radius+.25D) continue;
            int depth=d<5?4:d<9?3:d<12?2:d<14?1:0;
            BlockPos surface=origin.offset(x,-1,z);
            if(depth==0) {
                if(random.nextInt(5)==0) set(level,surface,Blocks.SANDSTONE.defaultBlockState());
                continue;
            }
            for(int y=0;y<depth;y++) set(level,surface.below(y),Blocks.AIR.defaultBlockState());
            set(level,surface.below(depth),d<7?Blocks.CUT_SANDSTONE.defaultBlockState():Blocks.SANDSTONE.defaultBlockState());
            if(d>=9&&random.nextInt(7)==0) set(level,surface.below(Math.max(0,depth-1)),Blocks.SAND.defaultBlockState());
        }
        BlockPos floor=origin.below(5);
        BlockState wreck=block("decorative.tritanium_plate");
        BlockState stripe=block("decorative.tritanium_plate_stripe");
        for(int z=-4;z<=4;z++) {
            int half=Math.max(1,3-Math.abs(z)/2);
            for(int x=-half;x<=half;x++) if(random.nextInt(6)!=0) set(level,floor.offset(x,0,z),wreck);
        }
        set(level,floor.offset(-2,1,0),stripe);
        set(level,floor.offset(2,1,0),stripe);
        set(level,floor.offset(0,1,1),block("tritanium_crate"));
        set(level,floor.offset(0,1,-2),block("holo_sign"));
        for(int i=0;i<8;i++) {
            int x=random.nextInt(23)-11;
            int z=random.nextInt(23)-11;
            if(x*x+z*z<120) set(level,origin.offset(x,-2-random.nextInt(2),z),Blocks.SANDSTONE.defaultBlockState());
        }
        return true;
    }

    private static BlockState block(String id){return ModBlocks.get(id).get().defaultBlockState();}
    private static void set(WorldGenLevel level, BlockPos pos, BlockState state){level.setBlock(pos,state,2);}
}
