package matteroverdrive.quest;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.FusionReactorControllerBlockEntity;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Bounded operator controls and telemetry for the nearest loaded fusion reactor. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ReactorCommands {
    private ReactorCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("matteroverdrive")
                .then(Commands.literal("reactor")
                        .then(Commands.literal("status")
                                .executes(context -> status(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("scram").requires(source -> source.hasPermission(2))
                                .executes(context -> mutate(context.getSource().getPlayerOrException(), true)))
                        .then(Commands.literal("reset").requires(source -> source.hasPermission(2))
                                .executes(context -> mutate(context.getSource().getPlayerOrException(), false)))));
    }

    private static int status(ServerPlayer player) {
        FusionReactorControllerBlockEntity reactor = nearest(player);
        if (reactor == null) {
            player.sendSystemMessage(Component.literal("No loaded Fusion Reactor Controller within 32 blocks."));
            return 0;
        }
        player.sendSystemMessage(Component.literal("Reactor " + reactor.getOperatingMode().label
                + " | heat " + reactor.getHeat() + "/1000"
                + " | stability " + reactor.getStability() + "/1000"
                + " | " + reactor.getFault()));
        return 1;
    }

    private static int mutate(ServerPlayer player, boolean scram) {
        FusionReactorControllerBlockEntity reactor = nearest(player);
        if (reactor == null) {
            player.sendSystemMessage(Component.literal("No loaded Fusion Reactor Controller within 32 blocks."));
            return 0;
        }
        boolean changed = scram ? reactor.emergencyScram() : reactor.resetContainment();
        player.sendSystemMessage(Component.literal(scram
                ? (changed ? "Emergency SCRAM engaged." : "SCRAM unavailable.")
                : (changed ? "Containment reset and reactor enabled." : "Reset denied: heat must be below 500, stability above zero, and structure valid.")));
        return changed ? 1 : 0;
    }

    private static FusionReactorControllerBlockEntity nearest(ServerPlayer player) {
        FusionReactorControllerBlockEntity reactor = null;
        double nearest = Double.MAX_VALUE;
        for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
                player.blockPosition().offset(-32, -16, -32),
                player.blockPosition().offset(32, 16, 32))) {
            if (player.serverLevel().getBlockEntity(pos) instanceof FusionReactorControllerBlockEntity candidate) {
                double distance = candidate.getBlockPos().distSqr(player.blockPosition());
                if (distance < nearest) {
                    nearest = distance;
                    reactor = candidate;
                }
            }
        }
        return reactor;
    }
}
