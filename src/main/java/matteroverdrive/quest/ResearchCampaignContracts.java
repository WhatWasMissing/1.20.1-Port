package matteroverdrive.quest;

import matteroverdrive.item.ContractItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Post-legacy technology campaign spanning Matter Technology through Anomaly Engineering. */
public final class ResearchCampaignContracts {
    public static final String[] QUEST_IDS = {
            "matter_scanner","matter_decomposer","matter_storage","pattern_archival","replication_test",
            "network_backbone","matter_logistics","transport_trial",
            "solar_baseline","charge_reserve","heavy_distribution",
            "containment_field","reactor_core","reactor_io","fusion_load",
            "event_horizon","spacetime_control","anomaly_mastery"
    };
    private ResearchCampaignContracts() {}

    public static ItemStack create(String id, RandomSource random) {
        if (id == null) return ItemStack.EMPTY;
        return switch (id) {
            case "matter_scanner" -> ContractItem.create(id,"The Value of Everything","craft",List.of("matteroverdrive:matter_scanner"),1,120,false,List.of(new ContractItem.RewardSpec("matteroverdrive:matter_container",1)));
            case "matter_decomposer" -> ContractItem.create(id,"Break It Down","place",List.of("matteroverdrive:decomposer"),1,160,false,List.of(new ContractItem.RewardSpec("matteroverdrive:upgrade_speed",1)));
            case "matter_storage" -> ContractItem.create(id,"Matter Reserve","craft",List.of("matteroverdrive:matter_container"),2,160,false,List.of(new ContractItem.RewardSpec("matteroverdrive:upgrade_matter_storage",1)));
            case "pattern_archival" -> ContractItem.create(id,"Pattern Recognition","place",List.of("matteroverdrive:pattern_storage"),1,180,false,List.of(new ContractItem.RewardSpec("matteroverdrive:pattern_drive",1)));
            case "replication_test" -> ContractItem.create(id,"Make Something From Nothing","place",List.of("matteroverdrive:replicator"),1,240,false,List.of(new ContractItem.RewardSpec("matteroverdrive:upgrade_power_storage",1)));
            case "network_backbone" -> ContractStageSupport.createStaged(id,"Network Backbone",220,List.of(new ContractItem.RewardSpec("matteroverdrive:network_flash_drive",1)),List.of(new ContractStageSupport.StageSpec("place",List.of("matteroverdrive:network_router"),1),new ContractStageSupport.StageSpec("place",List.of("matteroverdrive:network_switch"),1),new ContractStageSupport.StageSpec("place",List.of("matteroverdrive:network_pipe"),4)));
            case "matter_logistics" -> ContractStageSupport.createStaged(id,"Matter Logistics",220,List.of(new ContractItem.RewardSpec("matteroverdrive:upgrade_range",1)),List.of(new ContractStageSupport.StageSpec("place",List.of("matteroverdrive:matter_pipe"),4),new ContractStageSupport.StageSpec("place",List.of("matteroverdrive:pattern_monitor"),1)));
            case "transport_trial" -> ContractItem.create(id,"Instant Freight","transport",List.of(),1,260,false,List.of(new ContractItem.RewardSpec("matteroverdrive:transport_flash_drive",1)));
            case "solar_baseline" -> ContractItem.create(id,"A Stable Baseline","place",List.of("matteroverdrive:solar_panel"),3,200,false,List.of(new ContractItem.RewardSpec("matteroverdrive:battery",1)));
            case "charge_reserve" -> ContractItem.create(id,"Stored Potential","place",List.of("matteroverdrive:charging_station"),1,220,false,List.of(new ContractItem.RewardSpec("matteroverdrive:hc_battery",1)));
            case "heavy_distribution" -> ContractItem.create(id,"Heavy Distribution","place",List.of("matteroverdrive:heavy_matter_pipe"),6,240,false,List.of(new ContractItem.RewardSpec("matteroverdrive:upgrade_power",1)));
            case "containment_field" -> ContractItem.create(id,"Containment First","place",List.of("matteroverdrive:gravitational_stabilizer"),4,300,false,List.of(new ContractItem.RewardSpec("matteroverdrive:reactor_assembly_guide",1)));
            case "reactor_core" -> ContractStageSupport.createStaged(id,"Build the Impossible",360,List.of(new ContractItem.RewardSpec("matteroverdrive:upgrade_failsafe",1)),List.of(new ContractStageSupport.StageSpec("place",List.of("matteroverdrive:fusion_reactor_controller"),1),new ContractStageSupport.StageSpec("place",List.of("matteroverdrive:fusion_reactor_coil"),4)));
            case "reactor_io" -> ContractItem.create(id,"Feed and Draw","place",List.of("matteroverdrive:fusion_reactor_io"),1,300,false,List.of(new ContractItem.RewardSpec("matteroverdrive:reactor_remote",1)));
            case "fusion_load" -> ContractStageSupport.createStaged(id,"Sustained Experiment",360,List.of(new ContractItem.RewardSpec("matteroverdrive:upgrade_power_storage",2)),List.of(new ContractStageSupport.StageSpec("place",List.of("matteroverdrive:heavy_matter_pipe"),4),new ContractStageSupport.StageSpec("block_interact",List.of("matteroverdrive:fusion_reactor_controller"),1)));
            case "event_horizon" -> ContractItem.create(id,"Across the Horizon","anomaly",List.of(),1,500,false,List.of(new ContractItem.RewardSpec("matteroverdrive:spacetime_equalizer",1)));
            case "spacetime_control" -> ContractStageSupport.createStaged(id,"Spacetime Control",480,List.of(new ContractItem.RewardSpec("matteroverdrive:quantum_fold_manipulator",1)),List.of(new ContractStageSupport.StageSpec("place",List.of("matteroverdrive:spacetime_accelerator"),1),new ContractStageSupport.StageSpec("craft",List.of("matteroverdrive:spacetime_equalizer"),1)));
            case "anomaly_mastery" -> ContractStageSupport.createStaged(id,"Anomaly Engineering",800,List.of(new ContractItem.RewardSpec("matteroverdrive:tritanium_ingot",16),new ContractItem.RewardSpec("matteroverdrive:dilithium_crystal",8)),List.of(new ContractStageSupport.StageSpec("place",List.of("matteroverdrive:gravitational_stabilizer"),4),new ContractStageSupport.StageSpec("block_interact",List.of("matteroverdrive:fusion_reactor_controller"),1),new ContractStageSupport.StageSpec("anomaly",List.of(),1)));
            default -> ItemStack.EMPTY;
        };
    }

    public static ResearchProgression.Stage stageForIndex(int index) {
        if (index < 5) return ResearchProgression.Stage.MATTER_TECHNOLOGY;
        if (index < 8) return ResearchProgression.Stage.AUTOMATION_DRONES;
        if (index < 11) return ResearchProgression.Stage.ADVANCED_POWER;
        if (index < 15) return ResearchProgression.Stage.FUSION_RESEARCH;
        return ResearchProgression.Stage.ANOMALY_ENGINEERING;
    }
}
