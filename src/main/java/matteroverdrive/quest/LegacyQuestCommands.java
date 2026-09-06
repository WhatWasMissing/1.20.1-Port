package matteroverdrive.quest;

import com.mojang.brigadier.arguments.StringArgumentType;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.ContractItem;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Admin/debug access to every source-defined story quest, mirroring the legacy QuestCommands role. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LegacyQuestCommands {
    private LegacyQuestCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("matteroverdrive")
                .then(Commands.literal("quest")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("list")
                                .executes(context -> list(context.getSource())))
                        .then(Commands.literal("give")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(LegacyStoryContracts.QUEST_IDS, builder))
                                        .executes(context -> give(context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "id")))))));
    }

    private static int list(net.minecraft.commands.CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Matter Overdrive story quests: "
                + String.join(", ", LegacyStoryContracts.QUEST_IDS)), false);
        return LegacyStoryContracts.QUEST_IDS.length;
    }

    private static int give(ServerPlayer player, String id) {
        ItemStack contract = LegacyStoryContracts.create(id, player.level().random);
        if (contract.isEmpty()) {
            player.sendSystemMessage(Component.literal("Unknown Matter Overdrive quest: " + id));
            return 0;
        }
        if (!player.getInventory().add(contract)) player.drop(contract, false);
        player.sendSystemMessage(Component.literal("Quest given: " + ContractItem.title(contract)));
        return 1;
    }
}
