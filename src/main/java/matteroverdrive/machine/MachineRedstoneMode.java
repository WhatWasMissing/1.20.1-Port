package matteroverdrive.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;

/** Shared legacy machine mode semantics: LOW=0, HIGH=1, NONE/DISABLED=2. */
public enum MachineRedstoneMode implements StringRepresentable {
    LOW(0,"low"), HIGH(1,"high"), NONE(2,"none");

    /** Integer compatibility alias used by the newer synchronized machine menus. */
    public static final int DISABLED = 2;

    private final int id;
    private final String name;

    MachineRedstoneMode(int id,String name){this.id=id;this.name=name;}

    public int id(){return id;}
    @Override public String getSerializedName(){return name;}

    public boolean allows(Level level, BlockPos pos){
        return this==NONE || (this==HIGH)==level.hasNeighborSignal(pos);
    }

    public MachineRedstoneMode next(){
        return switch(this){case LOW->HIGH;case HIGH->NONE;case NONE->LOW;};
    }

    public static MachineRedstoneMode byId(int id){return id==0?LOW:id==1?HIGH:NONE;}

    public static int sanitize(int mode){return mode<0||mode>2?DISABLED:mode;}
    public static int next(int mode){return (sanitize(mode)+1)%3;}
    public static boolean allowsWork(Level level,BlockPos pos,int mode){return byId(sanitize(mode)).allows(level,pos);}
    public static String label(int mode){return switch(byId(sanitize(mode))){case LOW->"LOW";case HIGH->"HIGH";case NONE->"DISABLED";};}
}
