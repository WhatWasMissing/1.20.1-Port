package matteroverdrive.quest;

import com.mojang.brigadier.arguments.StringArgumentType;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.world.TechnologySiteDiscoverySavedData;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side research diagnostics and bounded milestone controls for test worlds. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ResearchCommands {
    private ResearchCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("matteroverdrive")
                .then(Commands.literal("research")
                        .then(Commands.literal("status")
                                .executes(context -> status(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("advance")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("stage", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                java.util.Arrays.stream(ResearchProgression.Stage.values()).map(Enum::name).toList(), builder))
                                        .executes(context -> advance(context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "stage")))))));
    }

    private static int status(ServerPlayer player) {
        ResearchProgression.Stage stage = ResearchProgression.stage(player);
        TechnologySiteDiscoverySavedData sites = TechnologySiteDiscoverySavedData.get(player.serverLevel());
        player.sendSystemMessage(Component.literal("Research: " + stage.title
                + " | field sites " + sites.count(player.getUUID())
                + " | investigation " + sites.chainStage(player.getUUID()) + "/" + sites.chainLength()
                + " | next " + TechnologySiteDiscoverySavedData.nextChainSite(player.getUUID(), sites.chainStage(player.getUUID()))));
        return stage.ordinal() + 1;
    }

    private static int advance(ServerPlayer player, String rawStage) {
        final ResearchProgression.Stage target;
        try {
            target = ResearchProgression.Stage.valueOf(rawStage.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            player.sendSystemMessage(Component.literal("Unknown research stage: " + rawStage));
            return 0;
        }
        boolean changed = ResearchProgression.unlockEvidence(player, target);
        player.sendSystemMessage(Component.literal(changed
                ? "Research clearance advanced to " + ResearchProgression.stage(player).title + "."
                : "Research clearance unchanged; evidence only advances one stage at a time."));
        return changed ? 1 : 0;
    }
}
