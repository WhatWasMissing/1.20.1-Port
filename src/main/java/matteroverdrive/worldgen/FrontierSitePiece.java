package matteroverdrive.worldgen;

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
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Playability-first rooms for Frontier Expedition sites. Each site has a readable
 * entrance-to-objective route, distinct landmark geometry and deterministic damage
 * that never removes the required path.
 */
public final class FrontierSitePiece extends StructurePiece {
    public enum Room {
        VAULT_ENTRY, VAULT_SECURITY, VAULT_ARCHIVE, VAULT_REFINERY, VAULT_CORE,
        FOUNDRY_ENTRY, FOUNDRY_CONTROL, FOUNDRY_FABRICATION, FOUNDRY_HANGAR, FOUNDRY_SALVAGE,
        QUARANTINE_ENTRY, QUARANTINE_DECON, QUARANTINE_OBSERVATION, QUARANTINE_CONTAINMENT, QUARANTINE_SECURITY,
        RECOVERY_ENTRY, RECOVERY_CONTROL, RECOVERY_PROCESSING, RECOVERY_STORAGE, RECOVERY_ARRAY,
        CORRIDOR_X, CORRIDOR_Z, SHAFT, CATWALK
    }

    private final FrontierSiteStructure.Kind site;
    private final Room room;
    private final BlockPos origin;
    private final int layout;

    public FrontierSitePiece(FrontierSiteStructure.Kind site, Room room, BlockPos origin, int layout) {
        super(ModStructures.FRONTIER_SITE_PIECE.get(), 0, boxFor(room, origin));
        this.site=site;this.room=room;this.origin=origin;this.layout=layout;
    }

    public FrontierSitePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.FRONTIER_SITE_PIECE.get(),tag);
        this.site=FrontierSiteStructure.Kind.valueOf(tag.getString("MOFrontierSite"));
        this.room=Room.valueOf(tag.getString("MOFrontierRoom"));
        this.origin=new BlockPos(tag.getInt("MOX"),tag.getInt("MOY"),tag.getInt("MOZ"));
        this.layout=Math.floorMod(tag.getInt("MOLayout"),3);
    }

    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag){tag.putString("MOFrontierSite",site.name());tag.putString("MOFrontierRoom",room.name());tag.putInt("MOX",origin.getX());tag.putInt("MOY",origin.getY());tag.putInt("MOZ",origin.getZ());tag.putInt("MOLayout",layout);}

    public static void assemble(StructurePiecesBuilder b, FrontierSiteStructure.Kind site, BlockPos c, int layout) {
        switch(site) {
            case DEEP_MATTER_VAULT -> assembleVault(b,site,c,layout);
            case AUTONOMOUS_DRONE_FOUNDRY -> assembleFoundry(b,site,c,layout);
            case ANOMALY_QUARANTINE_SITE -> assembleQuarantine(b,site,c,layout);
            case ORBITAL_RECOVERY_ARRAY -> assembleRecovery(b,site,c,layout);
        }
    }

    private static void assembleVault(StructurePiecesBuilder b,FrontierSiteStructure.Kind s,BlockPos c,int v){
        add(b,s,Room.VAULT_ENTRY,c.offset(0,14,-31),v);
        add(b,s,Room.SHAFT,c.offset(0,0,-24),v);
        add(b,s,Room.VAULT_SECURITY,c.offset(0,0,-15),v);
        add(b,s,Room.CORRIDOR_Z,c.offset(0,0,-8),v);
        add(b,s,Room.VAULT_CORE,c,v);
        int side=v==1?-1:1;
        add(b,s,Room.VAULT_ARCHIVE,c.offset(-21*side,0,2),v);
        add(b,s,Room.VAULT_REFINERY,c.offset(21*side,0,2),v);
        add(b,s,Room.CORRIDOR_X,c.offset(-12*side,0,1),v);
        add(b,s,Room.CORRIDOR_X,c.offset(12*side,0,1),v);
    }

    private static void assembleFoundry(StructurePiecesBuilder b,FrontierSiteStructure.Kind s,BlockPos c,int v){
        add(b,s,Room.FOUNDRY_ENTRY,c.offset(0,0,-31),v);
        add(b,s,Room.FOUNDRY_CONTROL,c.offset(0,0,-19),v);
        add(b,s,Room.CORRIDOR_Z,c.offset(0,0,-11),v);
        add(b,s,Room.FOUNDRY_FABRICATION,c,v);
        int side=v==1?-1:1;
        add(b,s,Room.FOUNDRY_HANGAR,c.offset(22*side,0,4),v);
        add(b,s,Room.CORRIDOR_X,c.offset(12*side,0,2),v);
        add(b,s,Room.FOUNDRY_SALVAGE,c.offset(-21*side,0,10),v);
        add(b,s,Room.CORRIDOR_X,c.offset(-12*side,0,6),v);
        add(b,s,Room.CATWALK,c.offset(0,7,8),v);
    }

    private static void assembleQuarantine(StructurePiecesBuilder b,FrontierSiteStructure.Kind s,BlockPos c,int v){
        add(b,s,Room.QUARANTINE_ENTRY,c.offset(0,8,-34),v);
        add(b,s,Room.SHAFT,c.offset(0,0,-27),v);
        add(b,s,Room.QUARANTINE_DECON,c.offset(0,0,-18),v);
        add(b,s,Room.QUARANTINE_SECURITY,c.offset(0,0,-8),v);
        add(b,s,Room.CORRIDOR_Z,c.offset(0,0,-3),v);
        add(b,s,Room.QUARANTINE_CONTAINMENT,c.offset(0,0,10),v);
        int side=v==1?-1:1;
        add(b,s,Room.QUARANTINE_OBSERVATION,c.offset(21*side,0,8),v);
        add(b,s,Room.CORRIDOR_X,c.offset(12*side,0,8),v);
    }

    private static void assembleRecovery(StructurePiecesBuilder b,FrontierSiteStructure.Kind s,BlockPos c,int v){
        add(b,s,Room.RECOVERY_ENTRY,c.offset(0,0,-30),v);
        add(b,s,Room.RECOVERY_CONTROL,c.offset(0,0,-18),v);
        add(b,s,Room.CORRIDOR_Z,c.offset(0,0,-10),v);
        int side=v==1?-1:1;
        add(b,s,Room.RECOVERY_PROCESSING,c.offset(-20*side,0,0),v);
        add(b,s,Room.RECOVERY_STORAGE,c.offset(20*side,0,0),v);
        add(b,s,Room.CORRIDOR_X,c.offset(-11*side,0,0),v);
        add(b,s,Room.CORRIDOR_X,c.offset(11*side,0,0),v);
        add(b,s,Room.RECOVERY_ARRAY,c.offset(0,0,22),v);
        add(b,s,Room.CORRIDOR_Z,c.offset(0,0,11),v);
        add(b,s,Room.CATWALK,c.offset(0,7,11),v);
    }

    private static void add(StructurePiecesBuilder b,FrontierSiteStructure.Kind s,Room r,BlockPos p,int v){b.addPiece(new FrontierSitePiece(s,r,p,v));}

    @Override public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox clip, ChunkPos chunkPos, BlockPos pivot){
        switch(room){
            case VAULT_ENTRY->entry(level,clip,true);
            case VAULT_SECURITY->security(level,clip);
            case VAULT_ARCHIVE->archive(level,clip);
            case VAULT_REFINERY->refinery(level,clip);
            case VAULT_CORE->vaultCore(level,clip);
            case FOUNDRY_ENTRY->entry(level,clip,false);
            case FOUNDRY_CONTROL->control(level,clip,false);
            case FOUNDRY_FABRICATION->fabrication(level,clip);
            case FOUNDRY_HANGAR->hangar(level,clip);
            case FOUNDRY_SALVAGE->salvage(level,clip);
            case QUARANTINE_ENTRY->entry(level,clip,true);
            case QUARANTINE_DECON->decon(level,clip);
            case QUARANTINE_SECURITY->security(level,clip);
            case QUARANTINE_OBSERVATION->observation(level,clip);
            case QUARANTINE_CONTAINMENT->containment(level,clip);
            case RECOVERY_ENTRY->entry(level,clip,false);
            case RECOVERY_CONTROL->control(level,clip,false);
            case RECOVERY_PROCESSING->processing(level,clip);
            case RECOVERY_STORAGE->storage(level,clip);
            case RECOVERY_ARRAY->recoveryArray(level,clip);
            case CORRIDOR_X->corridor(level,clip,true);
            case CORRIDOR_Z->corridor(level,clip,false);
            case SHAFT->steppedShaft(level,clip);
            case CATWALK->catwalk(level,clip);
        }
    }

    private void module(WorldGenLevel level,BoundingBox clip,int hx,int hz,int h,BlockState wall,BlockState floor,boolean fourDoors,Placement...ps){
        BlockState beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),glass=mod("industrial_glass",Blocks.TINTED_GLASS),lamp=mod("decorative.tritanium_lamp",Blocks.SEA_LANTERN);
        boolean damaged=damaged();
        for(int x=-hx;x<=hx;x++)for(int z=-hz;z<=hz;z++){
            boolean cut=Math.abs(x)>=hx-1&&Math.abs(z)>=hz-1;if(cut)continue;
            set(level,clip,origin.offset(x,0,z),floor);
            boolean edge=Math.abs(x)==hx||Math.abs(z)==hz||Math.abs(x)==hx-1&&Math.abs(z)>=hz-2||Math.abs(z)==hz-1&&Math.abs(x)>=hx-2;
            for(int y=1;y<=h;y++){
                boolean ns=Math.abs(x)<=1&&Math.abs(z)>=hz-1&&y<=3,ew=fourDoors&&Math.abs(z)<=1&&Math.abs(x)>=hx-1&&y<=3;
                if(!edge||ns||ew)set(level,clip,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());
                else if(y==1||y==h||((x+z)&5)==0)set(level,clip,origin.offset(x,y,z),beam);
                else if(y==2||y==3)set(level,clip,origin.offset(x,y,z),glass);
                else set(level,clip,origin.offset(x,y,z),wall);
            }
            set(level,clip,origin.offset(x,h+1,z),((x+z)&7)==0?beam:wall);
        }
        for(int z=-hz+3;z<=hz-3;z+=4)set(level,clip,origin.offset(0,h,z),lamp);
        for(Placement p:ps){boolean wreck=damaged&&p.x>0&&!p.id.startsWith("tritanium_crate")&&!p.id.equals("android_spawner");set(level,clip,origin.offset(p.x,p.y,p.z),wreck?mod("decorative.vent.dark",Blocks.IRON_BLOCK):mod(p.id,p.fallback));}
        if(damaged)damageOffRoute(level,clip,hx,hz,h);
        if(occupied()&&!isEntrance())set(level,clip,origin.offset(-hx+3,1,hz-3),mod("android_spawner",Blocks.IRON_BLOCK));
    }

    private void entry(WorldGenLevel level,BoundingBox clip,boolean reinforced){module(level,clip,6,8,5,reinforced?blackWall():paletteWall(),paletteFloor(),false,p(0,1,3,"holo_sign",Blocks.SEA_LANTERN));BlockState beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),stripe=mod("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE);for(int z=-13;z<=-8;z++)for(int x=-4;x<=4;x++)if(Math.abs(x)<=4-Math.abs(z+10)/2){set(level,clip,origin.offset(x,5,z),beam);set(level,clip,origin.offset(x,0,z),x==0?stripe:paletteFloor());}for(int y=1;y<=3;y++)for(int x=-1;x<=1;x++)set(level,clip,origin.offset(x,y,-8),Blocks.AIR.defaultBlockState());}
    private void security(WorldGenLevel level,BoundingBox clip){module(level,clip,8,7,6,blackWall(),darkFloor(),true,p(-4,1,2,"android_spawner",Blocks.IRON_BLOCK),p(4,1,2,"grid_capacitor",Blocks.IRON_BLOCK));for(int z=-3;z<=3;z++)for(int y=1;y<=3;y++){set(level,clip,origin.offset(-2,y,z),Blocks.IRON_BARS.defaultBlockState());set(level,clip,origin.offset(2,y,z),Blocks.IRON_BARS.defaultBlockState());}for(int y=1;y<=3;y++){set(level,clip,origin.offset(-2,y,0),Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(2,y,0),Blocks.AIR.defaultBlockState());}}
    private void archive(WorldGenLevel level,BoundingBox clip){module(level,clip,9,8,6,whiteWall(),darkFloor(),true,p(-5,1,3,"pattern_storage",Blocks.CHISELED_BOOKSHELF),p(0,1,3,"matter_analyzer",Blocks.LECTERN),p(5,1,3,"tritanium_crate_blue",Blocks.BARREL));for(int x=-6;x<=6;x+=3)for(int z=-4;z<=1;z+=5)set(level,clip,origin.offset(x,1,z),mod("decorative.holo_matrix",Blocks.SEA_LANTERN));}
    private void refinery(WorldGenLevel level,BoundingBox clip){module(level,clip,10,8,7,greenWall(),greenFloor(),true,p(-5,1,3,"matter_storage_matrix",Blocks.IRON_BLOCK),p(0,1,3,"decomposer",Blocks.BLAST_FURNACE),p(5,1,3,"matter_excavator",Blocks.BLAST_FURNACE));for(int z=-4;z<=5;z++)set(level,clip,origin.offset(0,1,z),mod("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE));}
    private void vaultCore(WorldGenLevel level,BoundingBox clip){module(level,clip,11,10,8,blackWall(),darkFloor(),true,p(0,1,-5,"facility_network_controller",Blocks.IRON_BLOCK));BlockState glass=mod("industrial_glass",Blocks.TINTED_GLASS),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE);for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)if(Math.abs(x)==5||Math.abs(z)==5)for(int y=1;y<=5;y++)set(level,clip,origin.offset(x,y,z),y==1||y==5?beam:glass);for(int y=1;y<=3;y++)for(int w=-1;w<=1;w++){set(level,clip,origin.offset(w,y,-5),Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(w,y,5),Blocks.AIR.defaultBlockState());}set(level,clip,origin.offset(0,1,0),mod("matter_storage_matrix",Blocks.IRON_BLOCK));set(level,clip,origin.offset(-7,1,4),mod("tritanium_crate_red",Blocks.BARREL));set(level,clip,origin.offset(7,1,4),mod("holographic_status_panel",Blocks.SEA_LANTERN));}
    private void control(WorldGenLevel level,BoundingBox clip,boolean dark){module(level,clip,9,8,6,dark?blackWall():whiteWall(),darkFloor(),true,p(0,1,-4,"facility_network_controller",Blocks.IRON_BLOCK),p(-4,1,2,"network_switch",Blocks.IRON_BLOCK),p(4,1,2,"holographic_status_panel",Blocks.SEA_LANTERN));for(int z=3;z<=5;z++)for(int x=-4;x<=4;x++)set(level,clip,origin.offset(x,1,z),darkFloor());}
    private void fabrication(WorldGenLevel level,BoundingBox clip){module(level,clip,11,9,8,paletteWall(),darkFloor(),true,p(-6,1,3,"drone_fabricator",Blocks.SMITHING_TABLE),p(0,1,3,"inscriber",Blocks.ANVIL),p(6,1,3,"android_station",Blocks.SMITHING_TABLE));BlockState stripe=mod("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE);for(int z=-5;z<=5;z++)for(int x=-2;x<=2;x++)set(level,clip,origin.offset(x,1,z),x==0?stripe:darkFloor());}
    private void hangar(WorldGenLevel level,BoundingBox clip){module(level,clip,13,11,9,paletteWall(),darkFloor(),true);BlockState stripe=mod("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE);for(int x=-9;x<=9;x++)for(int z=-7;z<=7;z++)if(Math.abs(z)==7||x%6==0)set(level,clip,origin.offset(x,1,z),stripe);for(int x:new int[]{-8,8}){set(level,clip,origin.offset(x,1,0),mod("charging_station",Blocks.LODESTONE));for(int y=1;y<=6;y++)set(level,clip,origin.offset(x,y,6),beam);}set(level,clip,origin.offset(0,1,6),mod("android_spawner",Blocks.IRON_BLOCK));}
    private void salvage(WorldGenLevel level,BoundingBox clip){BlockState floor=Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState(),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),wreck=mod("decorative.vent.dark",Blocks.IRON_BLOCK);for(int x=-10;x<=10;x++)for(int z=-9;z<=9;z++)if(Math.abs(x)+Math.abs(z)<=17)set(level,clip,origin.offset(x,0,z),floor);for(int z=-7;z<=7;z++)for(int x=-1;x<=1;x++)set(level,clip,origin.offset(x,1,z),Blocks.AIR.defaultBlockState());for(int x=-8;x<=8;x+=4){set(level,clip,origin.offset(x,1,4),wreck);set(level,clip,origin.offset(x,2,4),beam);}set(level,clip,origin.offset(-7,1,-4),mod("tritanium_crate",Blocks.BARREL));}
    private void decon(WorldGenLevel level,BoundingBox clip){module(level,clip,9,7,6,whiteWall(),paletteFloor(),true,p(-4,1,3,"matter_analyzer",Blocks.LECTERN),p(4,1,3,"charging_station",Blocks.LODESTONE));BlockState glass=mod("industrial_glass",Blocks.TINTED_GLASS);for(int x=-6;x<=6;x++)if(Math.abs(x)>1)for(int y=1;y<=4;y++)set(level,clip,origin.offset(x,y,0),glass);}
    private void observation(WorldGenLevel level,BoundingBox clip){module(level,clip,10,8,6,blackWall(),paletteFloor(),true,p(0,1,-4,"holographic_status_panel",Blocks.SEA_LANTERN),p(5,1,3,"tritanium_crate_blue",Blocks.BARREL));for(int x=-7;x<=7;x++)for(int y=2;y<=4;y++)set(level,clip,origin.offset(x,y,7),mod("industrial_glass",Blocks.TINTED_GLASS));}
    private void containment(WorldGenLevel level,BoundingBox clip){BlockState wall=blackWall(),floor=darkFloor(),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),glass=mod("industrial_glass",Blocks.TINTED_GLASS),lamp=mod("decorative.tritanium_lamp",Blocks.REDSTONE_LAMP);int r=12;for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){double d=Math.sqrt(x*x+z*z);if(d<=r-1){set(level,clip,origin.offset(x,0,z),floor);for(int y=1;y<=9;y++)set(level,clip,origin.offset(x,y,z),Blocks.AIR.defaultBlockState());}if(d>r-1.4&&d<=r+.2)for(int y=1;y<=9;y++)set(level,clip,origin.offset(x,y,z),y==1||y==9||((x+z)&4)==0?beam:y>=3&&y<=6?glass:wall);if(d<=r)set(level,clip,origin.offset(x,10,z),wall);}for(int y=1;y<=4;y++)for(int w=-1;w<=1;w++){set(level,clip,origin.offset(w,y,-r),Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(w,y,r),Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(-r,y,w),Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(r,y,w),Blocks.AIR.defaultBlockState());}for(int a=0;a<360;a+=45){double rad=Math.toRadians(a);int x=(int)Math.round(Math.cos(rad)*7),z=(int)Math.round(Math.sin(rad)*7);set(level,clip,origin.offset(x,2,z),beam);set(level,clip,origin.offset(x,3,z),lamp);}set(level,clip,origin.offset(0,1,0),mod("anomaly_containment_unit",Blocks.CRYING_OBSIDIAN));set(level,clip,origin.offset(-6,1,0),mod("gravitational_stabilizer",Blocks.OBSIDIAN));set(level,clip,origin.offset(6,1,0),mod("gravitational_stabilizer",Blocks.OBSIDIAN));set(level,clip,origin.offset(0,1,6),mod("facility_network_controller",Blocks.IRON_BLOCK));}
    private void processing(WorldGenLevel level,BoundingBox clip){module(level,clip,9,8,6,whiteWall(),paletteFloor(),true,p(-4,1,3,"matter_analyzer",Blocks.LECTERN),p(4,1,3,"decomposer",Blocks.BLAST_FURNACE));for(int z=-4;z<=5;z++)set(level,clip,origin.offset(0,1,z),mod("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE));}
    private void storage(WorldGenLevel level,BoundingBox clip){module(level,clip,9,8,6,paletteWall(),darkFloor(),true);for(int x=-6;x<=6;x+=3)for(int z=-4;z<=4;z+=4){String id=((x+z)&4)==0?"tritanium_crate_cyan":"tritanium_crate_lime";set(level,clip,origin.offset(x,1,z),mod(id,Blocks.BARREL));}}
    private void recoveryArray(WorldGenLevel level,BoundingBox clip){BlockState frame=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),floor=paletteFloor(),glass=mod("industrial_glass",Blocks.TINTED_GLASS),stripe=mod("decorative.tritanium_plate_stripe",Blocks.YELLOW_CONCRETE);for(int x=-14;x<=14;x++)for(int z=-14;z<=14;z++){double d=Math.sqrt(x*x+z*z);if(d<=13)set(level,clip,origin.offset(x,0,z),((Math.abs(x)+Math.abs(z))%7==0)?stripe:floor);}for(int y=1;y<=18;y++){set(level,clip,origin.offset(0,y,0),frame);int r=Math.max(1,7-y/3);for(int d=-r;d<=r;d++){if(y%3==0){set(level,clip,origin.offset(d,y,0),frame);set(level,clip,origin.offset(0,y,d),frame);}}}for(int r=3;r<=10;r+=2)for(int x=-r;x<=r;x++){int z=r-Math.abs(x);set(level,clip,origin.offset(x,10+r/2,z),glass);set(level,clip,origin.offset(x,10+r/2,-z),glass);}set(level,clip,origin.offset(0,1,-6),mod("quantum_power_relay",Blocks.RESPAWN_ANCHOR));set(level,clip,origin.offset(6,1,5),mod("android_spawner",Blocks.IRON_BLOCK));}
    private void corridor(WorldGenLevel level,BoundingBox clip,boolean xAxis){BlockState wall=site==FrontierSiteStructure.Kind.ANOMALY_QUARANTINE_SITE?blackWall():paletteWall(),floor=site==FrontierSiteStructure.Kind.DEEP_MATTER_VAULT?greenFloor():darkFloor(),beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE);for(int a=-7;a<=7;a++)for(int b=-2;b<=2;b++){int x=xAxis?a:b,z=xAxis?b:a;set(level,clip,origin.offset(x,0,z),floor);for(int y=1;y<=4;y++)set(level,clip,origin.offset(x,y,z),Math.abs(b)==2?(a%4==0?beam:wall):Blocks.AIR.defaultBlockState());set(level,clip,origin.offset(x,5,z),a%4==0?beam:wall);}}
    private void steppedShaft(WorldGenLevel level,BoundingBox clip){BlockState beam=mod("decorative.beams",Blocks.POLISHED_DEEPSLATE),wall=site==FrontierSiteStructure.Kind.ANOMALY_QUARANTINE_SITE?blackWall():paletteWall(),floor=darkFloor();for(int y=0;y<=15;y++)for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){boolean edge=Math.abs(x)==4||Math.abs(z)==4;set(level,clip,origin.offset(x,y,z),edge?(y%4==0?beam:wall):Blocks.AIR.defaultBlockState());}for(int i=0;i<=14;i++){int y=i;int x=i<7?-2:2;int z=-3+(i%7);for(int w=-1;w<=1;w++){set(level,clip,origin.offset(x+w,y,z),floor);for(int h=1;h<=3;h++)set(level,clip,origin.offset(x+w,y+h,z),Blocks.AIR.defaultBlockState());}}}
    private void catwalk(WorldGenLevel level,BoundingBox clip){BlockState floor=mod("decorative.floor_tiles",Blocks.IRON_BLOCK),rail=mod("decorative.beams",Blocks.IRON_BARS);for(int x=-14;x<=14;x++){for(int z=-1;z<=1;z++)set(level,clip,origin.offset(x,0,z),floor);set(level,clip,origin.offset(x,1,-2),rail);set(level,clip,origin.offset(x,1,2),rail);if(x%5==0){set(level,clip,origin.offset(x,-1,-1),rail);set(level,clip,origin.offset(x,-1,1),rail);}}}
    private void damageOffRoute(WorldGenLevel level,BoundingBox clip,int hx,int hz,int h){for(int y=h-2;y<=h+1;y++)for(int z=hz-4;z<=hz-2;z++)set(level,clip,origin.offset(hx-1,y,z),Blocks.AIR.defaultBlockState());for(int x=hx-4;x<=hx-2;x++)for(int z=hz-4;z<=hz-2;z++)if(((x+z)&1)==0)set(level,clip,origin.offset(x,1,z),Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());set(level,clip,origin.offset(-hx+3,h-1,hz-3),Blocks.COBWEB.defaultBlockState());}
    private boolean damaged(){return Math.floorMod(origin.getX()*31+origin.getZ()*17+room.ordinal()*13+layout,5)<=1;}
    private boolean occupied(){return Math.floorMod(origin.getX()*7+origin.getZ()*11+site.ordinal()*19+layout,4)!=0;}
    private boolean isEntrance(){return room==Room.VAULT_ENTRY||room==Room.FOUNDRY_ENTRY||room==Room.QUARANTINE_ENTRY||room==Room.RECOVERY_ENTRY;}
    private static BoundingBox boxFor(Room room,BlockPos p){int hx=switch(room){case VAULT_CORE,FOUNDRY_FABRICATION->12;case FOUNDRY_HANGAR,QUARANTINE_CONTAINMENT,RECOVERY_ARRAY->15;case FOUNDRY_SALVAGE->11;case CORRIDOR_X->8;case CORRIDOR_Z,SHAFT->5;case CATWALK->15;default->11;};int hz=switch(room){case VAULT_CORE,FOUNDRY_FABRICATION->11;case FOUNDRY_HANGAR,QUARANTINE_CONTAINMENT,RECOVERY_ARRAY->15;case FOUNDRY_SALVAGE->10;case CORRIDOR_Z->8;case CORRIDOR_X,SHAFT->5;case CATWALK->5;default->10;};int down=room==Room.SHAFT?1:2;int up=switch(room){case RECOVERY_ARRAY->22;case SHAFT->17;default->12;};return new BoundingBox(p.getX()-hx,p.getY()-down,p.getZ()-hz,p.getX()+hx,p.getY()+up,p.getZ()+hz);}
    private static BlockState mod(String id,Block fallback){ResourceLocation key=ResourceLocation.tryParse("matteroverdrive:"+id);Block block=key==null?null:ForgeRegistries.BLOCKS.getValue(key);return block==null||block==Blocks.AIR?fallback.defaultBlockState():block.defaultBlockState();}
    private static BlockState paletteWall(){return mod("decorative.tritanium_plate",Blocks.IRON_BLOCK);}private static BlockState whiteWall(){return mod("decorative.white_plate",Blocks.QUARTZ_BLOCK);}private static BlockState blackWall(){return mod("decorative.carbon_fiber_plate",Blocks.POLISHED_BLACKSTONE);}private static BlockState greenWall(){return mod("decorative.tritanium_plate_green",Blocks.OXIDIZED_COPPER);}private static BlockState paletteFloor(){return mod("decorative.floor_tiles",Blocks.SMOOTH_STONE);}private static BlockState darkFloor(){return mod("decorative.floor_tiles_dark",Blocks.DEEPSLATE_TILES);}private static BlockState greenFloor(){return mod("decorative.floor_tiles_green",Blocks.OXIDIZED_COPPER);}
    private void set(WorldGenLevel level,BoundingBox clip,BlockPos pos,BlockState state){if(!clip.isInside(pos)||!getBoundingBox().isInside(pos))return;if(level.getBlockState(pos).is(Blocks.BEDROCK))return;level.setBlock(pos,state,2);}
    private static Placement p(int x,int y,int z,String id,Block fallback){return new Placement(x,y,z,id,fallback);}private record Placement(int x,int y,int z,String id,Block fallback){}
}
