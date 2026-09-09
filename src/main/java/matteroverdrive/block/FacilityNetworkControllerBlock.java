package matteroverdrive.block;

import matteroverdrive.network.FacilityNetworkTelemetry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Central telemetry/alarm console for machines reachable through the Matter Overdrive data network. */
public class FacilityNetworkControllerBlock extends Block {
    public FacilityNetworkControllerBlock(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        FacilityNetworkTelemetry.Snapshot snapshot = FacilityNetworkTelemetry.scan(level, pos);
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

    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return FacilityNetworkTelemetry.scan(level, pos).comparatorLevel();
    }
}