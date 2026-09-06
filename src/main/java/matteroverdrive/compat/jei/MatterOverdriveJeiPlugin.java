package matteroverdrive.compat.jei;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class MatterOverdriveJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(MatterOverdrive.MOD_ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        info(registration, "decomposer",
                "Decomposes items with Matter values into stored Matter. Supply FE and an item with a Matter value.");
        info(registration, "matter_recycler",
                "Recycles items into Matter using less energy than the Decomposer, with recycler-specific processing rules.");
        info(registration, "matter_analyzer",
                "Analyzes items and creates Matter Overdrive pattern data for replication progression.");
        info(registration, "inscriber",
                "Upgrades Isolinear Circuits through the Matter Overdrive Mk1 to Mk4 progression chain.");
        info(registration, "pattern_storage",
                "Stores Pattern Drives and exposes stored patterns to the Matter Overdrive pattern network.");
        info(registration, "pattern_monitor",
                "Queues and monitors replication tasks on the Matter Overdrive pattern network.");
        info(registration, "replicator",
                "Replicates stored patterns by consuming Matter and FE. Pattern availability comes from Matter Overdrive pattern storage/networking.");
        info(registration, "charging_station",
                "Charges compatible FE-powered Matter Overdrive items and batteries.");
        info(registration, "solar_panel",
                "Generates FE from daylight and can feed adjacent Matter Overdrive energy consumers.");
        info(registration, "fusion_reactor_controller",
                "Controls the Matter Overdrive fusion reactor multiblock. See the Reactor Assembly Guide or Data Pad manual for structure details.");
        info(registration, "fusion_reactor_io",
                "Reactor interface for FE and Matter transfer between the fusion reactor and external systems.");
        info(registration, "gravitational_stabilizer",
                "Suppresses a nearby gravitational anomaly while powered and correctly aligned.");
        info(registration, "transporter",
                "Teleports entities to configured destinations using FE and Matter Overdrive transport data.");
        info(registration, "weapon_station",
                "Matter Overdrive weapon configuration and module station.");
        info(registration, "network_switch",
                "Configurable Matter Overdrive network switching and routing component.");
        info(registration, "network_router",
                "Routes Matter Overdrive network traffic according to configured destinations and filters.");
        info(registration, "star_map",
                "Opens the Matter Overdrive strategic Star Map system.");
    }

    private static void info(IRecipeRegistration registration, String blockId, String description) {
        registration.addIngredientInfo(ModBlocks.get(blockId).get(), Component.literal(description));
    }
}
