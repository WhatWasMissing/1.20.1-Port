package matteroverdrive.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** Canonical non-Android progression spine for the modern port. */
public final class ResearchProgression {
    public static final String STAGE_KEY = "MatterOverdriveResearchStage";
    public static final String SEEN_KEY = "MatterOverdriveResearchStageSeen";

    public enum Stage {
        SCIENTIST_RESEARCH("Scientist Research", "Learn the programme, recover legacy research, and earn laboratory clearance."),
        MATTER_TECHNOLOGY("Matter Technology", "Measure matter values, decompose materials, store matter, and replicate useful patterns."),
        AUTOMATION_DRONES("Automation & Drones", "Connect machines, move matter and patterns, and delegate dangerous work to drones."),
        ADVANCED_POWER("Advanced Power", "Build a stable power grid and prove it can sustain automated research infrastructure."),
        FUSION_RESEARCH("Fusion Research", "Construct and operate the fusion reactor, its IO ring, stabilizers, and safety systems."),
        ANOMALY_ENGINEERING("Anomaly Engineering", "Control anomaly mass, containment, extraction, and high-energy experimental applications.");
        public final String title, description;
        Stage(String title,String description){this.title=title;this.description=description;}
    }

    private ResearchProgression() {}
    public static Stage stage(ServerPlayer p){CompoundTag d=data(p);int i=Math.max(0,Math.min(Stage.values().length-1,d.getInt(STAGE_KEY)));return Stage.values()[i];}
    public static boolean unlock(ServerPlayer p,Stage stage){CompoundTag d=data(p);int current=Math.max(0,d.getInt(STAGE_KEY));if(stage.ordinal()<=current)return false;d.putInt(STAGE_KEY,stage.ordinal());save(p,d);return true;}
    /** Evidence can never skip an intermediate clearance; campaign quests may still grant explicit milestones. */
    public static boolean unlockEvidence(ServerPlayer p,Stage supported){Stage current=stage(p);if(supported.ordinal()<=current.ordinal())return false;return unlock(p,Stage.values()[Math.min(current.ordinal()+1,supported.ordinal())]);}
    public static boolean atLeast(ServerPlayer p,Stage stage){return stage(p).ordinal()>=stage.ordinal();}
    public static String status(ServerPlayer p){Stage s=stage(p);return s.title+" - "+s.description;}
    public static List<String> roadmap(ServerPlayer p){Stage current=stage(p);return java.util.Arrays.stream(Stage.values()).map(s->(s.ordinal()<current.ordinal()?"COMPLETE: ":s==current?"ACTIVE: ":"LOCKED: ")+s.title+" - "+s.description).toList();}
    public static boolean markSeen(ServerPlayer p){CompoundTag d=data(p);int stage=stage(p).ordinal();if(d.getInt(SEEN_KEY)==stage+1)return false;d.putInt(SEEN_KEY,stage+1);save(p,d);return true;}
    private static CompoundTag data(ServerPlayer p){return p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);}
    private static void save(ServerPlayer p,CompoundTag d){p.getPersistentData().put(Player.PERSISTED_NBT_TAG,d);}
}
