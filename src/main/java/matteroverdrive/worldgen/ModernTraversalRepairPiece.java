package matteroverdrive.worldgen;

import matteroverdrive.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** Final critical-path overlays authored after the modern exploration shell. */
public final class ModernTraversalRepairPiece extends StructurePiece {
    private final TechnologyFacilityStructure.Kind facility;
    private final BlockPos origin;

    public ModernTraversalRepairPiece(TechnologyFacilityStructure.Kind facility, BlockPos origin) {
        super(ModStructures.MODERN_TRAVERSAL_REPAIR_PIECE.get(),0,box(facility,origin));
        this.facility=facility;this.origin=origin;
    }
    public ModernTraversalRepairPiece(StructurePieceSerializationContext context, CompoundTag tag){
        super(ModStructures.MODERN_TRAVERSAL_REPAIR_PIECE.get(),tag);
        facility=TechnologyFacilityStructure.Kind.valueOf(tag.getString("MOFacility"));
        origin=new BlockPos(tag.getInt("MOX"),tag.getInt("MOY"),tag.getInt("MOZ"));
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag){tag.putString("MOFacility",facility.name());tag.putInt("MOX",origin.getX());tag.putInt("MOY",origin.getY());tag.putInt("MOZ",origin.getZ());}

    @Override public void postProcess(WorldGenLevel level, StructureManager sm, ChunkGenerator cg, RandomSource random, BoundingBox clip, ChunkPos cp, BlockPos pivot){
        if(facility==TechnologyFacilityStructure.Kind.MATTER_REFINERY) refineryTransition(level,clip);
        else if(facility==TechnologyFacilityStructure.Kind.BLACK_SITE) blackSiteTransition(level,clip);
    }

    private void refineryTransition(WorldGenLevel l,BoundingBox c){
        // Core west doorway y=0 -> excavation east doorway y=-2. Three-wide, normal movement only.
        int[] xs={-11,-12,-13,-14}; int[] ys={0,0,-1,-2};
        for(int i=0;i<xs.length;i++)for(int z=1;z<=3;z++){
            BlockPos p=origin.offset(xs[i],ys[i],z);
            set(l,c,p,Blocks.SMOOTH_STONE.defaultBlockState());
            for(int h=1;h<=3;h++)set(l,c,p.above(h),Blocks.AIR.defaultBlockState());
        }
    }

    private void blackSiteTransition(WorldGenLevel l,BoundingBox c){
        // Core south door ends at z=9; deep stair begins at z=12. Bridge the three cells explicitly.
        for(int z=9;z<=12;z++)for(int x=-1;x<=1;x++){
            BlockPos p=origin.offset(x,0,z);
            set(l,c,p,Blocks.DEEPSLATE_TILES.defaultBlockState());
            for(int h=1;h<=3;h++)set(l,c,p.above(h),Blocks.AIR.defaultBlockState());
        }
    }

    private static BoundingBox box(TechnologyFacilityStructure.Kind f,BlockPos p){
        return f==TechnologyFacilityStructure.Kind.MATTER_REFINERY
                ?new BoundingBox(p.getX()-15,p.getY()-3,p.getZ(),p.getX()-9,p.getY()+4,p.getZ()+4)
                :new BoundingBox(p.getX()-2,p.getY()-1,p.getZ()+8,p.getX()+2,p.getY()+4,p.getZ()+13);
    }
    private void set(WorldGenLevel l,BoundingBox c,BlockPos p,BlockState s){if(c.isInside(p)&&getBoundingBox().isInside(p)&&!l.getBlockState(p).is(Blocks.BEDROCK))l.setBlock(p,s,2);}
}
