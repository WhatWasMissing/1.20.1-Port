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
    public enum Kind { CRASHED_SHIP, CARGO_SHIP, UNDERWATER_BASE, MAD_SCIENTIST_HOUSE }
    private final Kind kind;
    public LegacyStructureFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) { super(codec); this.kind = kind; }

    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return switch (kind) {
            case CRASHED_SHIP -> crashedShip(context.level(), context.origin(), context.random());
            case CARGO_SHIP -> cargoShip(context.level(), context.origin(), context.random());
            case UNDERWATER_BASE -> underwaterBase(context.level(), context.origin());
            case MAD_SCIENTIST_HOUSE -> madScientistHouse(context.level(), context.origin());
        };
    }

    private static boolean crashedShip(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!level.getFluidState(origin).isEmpty()) return false;
        BlockPos base = origin.below();
        BlockState hull = ModBlocks.get("decorative.tritanium_plate").get().defaultBlockState();
        BlockState stripe = ModBlocks.get("decorative.tritanium_plate_stripe").get().defaultBlockState();
        for (int z=-14; z<=14; z++) {
            int half = z < -9 || z > 10 ? 2 : 4;
            for (int x=-half; x<=half; x++) {
                if (z > 5 && Math.floorMod(x*31+z*17,19)<3) continue;
                set(level,base.offset(x,0,z),hull);
                if (Math.abs(x)==half && z%3!=0) set(level,base.offset(x,1,z),hull);
            }
        }
        for (int z=-8;z<=5;z+=3) { set(level,base.offset(-4,1,z),stripe); set(level,base.offset(4,1,z),stripe); }
        for (int x=-2;x<=2;x++) set(level,base.offset(x,2,-8),ModBlocks.get("industrial_glass").get().defaultBlockState());
        set(level,base.offset(0,1,3),ModBlocks.get("tritanium_crate").get().defaultBlockState());
        set(level,base.offset(2,1,-2),ModBlocks.get("holo_sign").get().defaultBlockState());
        for(int i=0;i<5;i++) set(level,base.offset(random.nextInt(9)-4,-1,random.nextInt(25)-12),Blocks.COARSE_DIRT.defaultBlockState());
        return true;
    }

    private static boolean cargoShip(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!level.getFluidState(origin).isEmpty()) return false;
        BlockPos base=origin.above();
        BlockState hull=ModBlocks.get("decorative.tritanium_plate").get().defaultBlockState();
        BlockState floor=ModBlocks.get("decorative.floor_tiles").get().defaultBlockState();
        for(int z=-20;z<=20;z++) {
            int half=Math.max(3,8-Math.abs(z)/5);
            for(int x=-half;x<=half;x++) { set(level,base.offset(x,0,z),floor); if(Math.abs(x)==half){set(level,base.offset(x,1,z),hull); if(z%2==0)set(level,base.offset(x,2,z),hull);} }
        }
        for(int z=-13;z<=13;z++){set(level,base.offset(-3,3,z),hull);set(level,base.offset(3,3,z),hull);if(z%2==0){set(level,base.offset(-2,3,z),ModBlocks.get("industrial_glass").get().defaultBlockState());set(level,base.offset(2,3,z),ModBlocks.get("industrial_glass").get().defaultBlockState());}}
        for(int z=-15;z<=15;z+=5){set(level,base.offset(0,1,z),ModBlocks.get("decorative.tritanium_lamp").get().defaultBlockState());if(random.nextBoolean())set(level,base.offset(2,1,z+1),ModBlocks.get("tritanium_crate").get().defaultBlockState());}
        set(level,base.offset(0,1,-17),ModBlocks.get("holo_sign").get().defaultBlockState());
        return true;
    }

    private static boolean underwaterBase(WorldGenLevel level, BlockPos origin) {
        if (level.getFluidState(origin.above(8)).isEmpty()) return false;
        BlockPos c=origin.above(); int r=10;
        BlockState hull=ModBlocks.get("decorative.tritanium_plate").get().defaultBlockState();
        BlockState glass=ModBlocks.get("industrial_glass").get().defaultBlockState();
        BlockState floor=ModBlocks.get("decorative.floor_tiles").get().defaultBlockState();
        for(int x=-r;x<=r;x++) for(int z=-r;z<=r;z++) {
            double d=Math.sqrt(x*x+z*z);
            if(d<=r-1) {
                set(level,c.offset(x,0,z),floor);
                for(int y=1;y<=4;y++) set(level,c.offset(x,y,z),Blocks.AIR.defaultBlockState());
            }
            if(d>=r-1.2D&&d<=r+.2D) for(int y=1;y<=4;y++) set(level,c.offset(x,y,z),y==2?glass:hull);
        }
        for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++)if(x*x+z*z<=38)set(level,c.offset(x,5,z),x*x+z*z>24?glass:hull);
        set(level,c.offset(0,1,0),ModBlocks.get("matter_analyzer").get().defaultBlockState());
        set(level,c.offset(3,1,0),ModBlocks.get("tritanium_crate_blue").get().defaultBlockState());
        return true;
    }

    private static boolean madScientistHouse(WorldGenLevel level, BlockPos origin) {
        if (!level.getFluidState(origin).isEmpty()) return false;
        BlockState plate=ModBlocks.get("decorative.white_plate").get().defaultBlockState();
        BlockState floor=ModBlocks.get("decorative.floor_tile_white").get().defaultBlockState();
        BlockState glass=ModBlocks.get("industrial_glass").get().defaultBlockState();
        BlockState beam=ModBlocks.get("decorative.beams").get().defaultBlockState();
        for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++){set(level,origin.offset(x,0,z),floor);if(Math.abs(x)==5||Math.abs(z)==5)for(int y=1;y<=4;y++){boolean window=y>=2&&y<=3&&((Math.abs(x)==5&&Math.abs(z)<=2)||(Math.abs(z)==5&&Math.abs(x)<=2));set(level,origin.offset(x,y,z),window?glass:plate);}set(level,origin.offset(x,5,z),plate);}
        for(int y=1;y<=4;y++){set(level,origin.offset(-4,y,-4),beam);set(level,origin.offset(4,y,-4),beam);set(level,origin.offset(-4,y,4),beam);set(level,origin.offset(4,y,4),beam);}
        set(level,origin.offset(0,1,2),ModBlocks.get("inscriber").get().defaultBlockState());
        set(level,origin.offset(2,1,2),ModBlocks.get("decomposer").get().defaultBlockState());
        set(level,origin.offset(-2,1,2),ModBlocks.get("tritanium_crate").get().defaultBlockState());
        return true;
    }
    private static void set(WorldGenLevel level, BlockPos pos, BlockState state){level.setBlock(pos,state,2);}
}
