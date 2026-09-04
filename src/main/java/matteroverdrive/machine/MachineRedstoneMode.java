package matteroverdrive.machine;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

/** Shared legacy machine mode semantics: LOW=0, HIGH=1, NONE=2. */
public enum MachineRedstoneMode implements StringRepresentable {
    LOW(0,"low"), HIGH(1,"high"), NONE(2,"none");
    private final int id; private final String name;
    MachineRedstoneMode(int id,String name){this.id=id;this.name=name;}
    public int id(){return id;} @Override public String getSerializedName(){return name;}
    public boolean allows(Level level, BlockPos pos){return this==NONE || (this==HIGH)==level.hasNeighborSignal(pos);}
    public MachineRedstoneMode next(){return switch(this){case LOW->HIGH;case HIGH->NONE;case NONE->LOW;};}
    public static MachineRedstoneMode byId(int id){return id==0?LOW:id==1?HIGH:NONE;}
}
