package matteroverdrive.block;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.network.FacilityNetworkTelemetry;
import matteroverdrive.world.FacilityRestorationSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;

/** Central telemetry/alarm console and recovery interface for generated technology facilities. */
public class FacilityNetworkControllerBlock extends Block {
    public FacilityNetworkControllerBlock(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        FacilityNetworkTelemetry.Snapshot snapshot = FacilityNetworkTelemetry.scan(level, pos);

        if (level instanceof ServerLevel server) {
            FacilityRestorationSavedData.FacilityLocation location = FacilityRestorationSavedData.locate(server, pos);
            if (location != null) {
                handleRestoration(server, location, snapshot, player, hand);
            }
        }

        player.sendSystemMessage(Component.literal("[MATTER OVERDRIVE FACILITY CONTROL]").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Nodes " + snapshot.nodes() + " | FE endpoints " + snapshot.energyEndpoints() + " | Matter endpoints " + snapshot.matterEndpoints()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("FE " + snapshot.energyStored() + "/" + snapshot.energyCapacity() + " (" + snapshot.energyPercent() + "%)")
                .withStyle(snapshot.energyPercent() < 10 ? ChatFormatting.RED : ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("Matter " + snapshot.matterStored() + "/" + snapshot.matterCapacity() + " (" + snapshot.matterPercent() + "%)")
                .withStyle(snapshot.fullMatterEndpoints() > 0 ? ChatFormatting.YELLOW : ChatFormatting.LIGHT_PURPLE));

        if (snapshot.alarms().isEmpty()) player.sendSystemMessage(Component.literal("SYSTEMS NOMINAL").withStyle(ChatFormatting.GREEN));
        else {
            player.sendSystemMessage(Component.literal(snapshot.severity() >= 2 ? "CRITICAL ALARMS" : "FACILITY WARNINGS")
                    .withStyle(snapshot.severity() >= 2 ? ChatFormatting.RED : ChatFormatting.YELLOW));
            for (String alarm : snapshot.alarms()) player.sendSystemMessage(Component.literal(" - " + alarm).withStyle(ChatFormatting.YELLOW));
        }

        if (player.isCrouching()) {
            player.sendSystemMessage(Component.literal("Connected device inventory:").withStyle(ChatFormatting.DARK_AQUA));
            snapshot.deviceTypes().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey())
                    .forEach(entry -> player.sendSystemMessage(Component.literal(" - " + entry.getKey() + " x" + entry.getValue()).withStyle(ChatFormatting.GRAY)));
        } else player.sendSystemMessage(Component.literal("Sneak + use for connected device inventory. Comparator output reports alarm severity.").withStyle(ChatFormatting.DARK_GRAY));
        return InteractionResult.CONSUME;
    }

    private void handleRestoration(ServerLevel level, FacilityRestorationSavedData.FacilityLocation location,
                                   FacilityNetworkTelemetry.Snapshot snapshot, Player player, InteractionHand hand) {
        FacilityRestorationSavedData data = FacilityRestorationSavedData.get(level);
        int stage = data.stage(location);
        player.sendSystemMessage(Component.literal("[" + location.profile().displayName().toUpperCase() + " / RECOVERY]")
                .withStyle(ChatFormatting.GOLD));

        if (player.isCrouching()) {
            sendRestorationStatus(player, stage);
            return;
        }

        ItemStack held = player.getItemInHand(hand);
        switch (stage) {
            case FacilityRestorationSavedData.STAGE_POWER -> {
                boolean networkPower = snapshot.energyStored() > 0;
                if (!networkPower && !isEmergencyPowerCell(held)) {
                    player.sendSystemMessage(Component.literal("EMERGENCY POWER OFFLINE - connect stored FE or install a Battery / Energy Pack.")
                            .withStyle(ChatFormatting.RED));
                    return;
                }
                if (!networkPower) consumeOne(player, held);
                data.setStage(location, FacilityRestorationSavedData.STAGE_REPAIR);
                player.sendSystemMessage(Component.literal("Emergency bus online. Control hardware diagnostics are now available.")
                        .withStyle(ChatFormatting.GREEN));
            }
            case FacilityRestorationSavedData.STAGE_REPAIR -> {
                if (!isRepairCircuit(held)) {
                    player.sendSystemMessage(Component.literal("CONTROL HARDWARE DAMAGED - install an Isolinear Circuit Mk1 or better.")
                            .withStyle(ChatFormatting.YELLOW));
                    return;
                }
                consumeOne(player, held);
                data.setStage(location, FacilityRestorationSavedData.STAGE_RESEARCH);
                player.sendSystemMessage(Component.literal("Control hardware repaired. SECURE ACCESS RELEASED. Recover the facility research dossier.")
                        .withStyle(ChatFormatting.AQUA));
            }
            case FacilityRestorationSavedData.STAGE_RESEARCH -> {
                if (!matchesResearch(held, location.profile().archiveId())) {
                    player.sendSystemMessage(Component.literal("Secure access online. Return the matching Recovered Research Dossier to complete recovery.")
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                    return;
                }
                data.setStage(location, FacilityRestorationSavedData.STAGE_COMPLETE);
                player.sendSystemMessage(Component.literal("FACILITY RESTORED - research verified and unused emergency security reserves ordered to stand down.")
                        .withStyle(ChatFormatting.GREEN));
            }
            default -> player.sendSystemMessage(Component.literal("FACILITY RESTORED - secure access and recovery systems remain online.")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    private static void sendRestorationStatus(Player player, int stage) {
        Component status = switch (stage) {
            case FacilityRestorationSavedData.STAGE_POWER -> Component.literal("Objective 1/3: restore emergency power").withStyle(ChatFormatting.RED);
            case FacilityRestorationSavedData.STAGE_REPAIR -> Component.literal("Objective 2/3: repair control hardware").withStyle(ChatFormatting.YELLOW);
            case FacilityRestorationSavedData.STAGE_RESEARCH -> Component.literal("Objective 3/3: recover and verify the facility dossier (secure access online)").withStyle(ChatFormatting.LIGHT_PURPLE);
            default -> Component.literal("Recovery complete: secure access online").withStyle(ChatFormatting.GREEN);
        };
        player.sendSystemMessage(status);
    }

    private static boolean isEmergencyPowerCell(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null || !MatterOverdrive.MOD_ID.equals(id.getNamespace())) return false;
        return switch (id.getPath()) {
            case "battery", "hc_battery", "energy_pack", "creative_battery" -> true;
            default -> false;
        };
    }

    private static boolean isRepairCircuit(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && MatterOverdrive.MOD_ID.equals(id.getNamespace())
                && id.getPath().startsWith("isolinear_circuit_mk");
    }

    private static boolean matchesResearch(ItemStack stack, String archiveId) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && MatterOverdrive.MOD_ID.equals(id.getNamespace())
                && id.getPath().equals("facility_research") && stack.hasTag()
                && archiveId.equals(stack.getTag().getString("FacilityArchive"));
    }

    private static void consumeOne(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return FacilityNetworkTelemetry.scan(level, pos).comparatorLevel();
    }
}
