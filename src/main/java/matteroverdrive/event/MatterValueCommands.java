package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.matter.MatterValueRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID)
public final class MatterValueCommands {
    private MatterValueCommands() {}

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("matteroverdrive")
                .then(Commands.literal("matter")
                        .then(Commands.literal("value")
                                .executes(context -> showHeldValue(context.getSource())))
                        .then(Commands.literal("audit")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> runAudit(context.getSource())))
                        .then(Commands.literal("clearcache")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> clearCache(context.getSource())))));
    }

    private static int showHeldValue(net.minecraft.commands.CommandSourceStack source) {
        try {
            ItemStack stack = source.getPlayerOrException().getMainHandItem();
            if (stack.isEmpty()) {
                source.sendFailure(Component.literal("Hold an item in your main hand."));
                return 0;
            }
            MatterValueRegistry.MatterValue result = MatterValueRegistry.getMatterValue(source.getLevel(), stack);
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            source.sendSuccess(() -> Component.literal(id + " = " + result.value() + " kM (" + result.source().name().toLowerCase() + ")"), false);
            return result.hasMatter() ? 1 : 0;
        } catch (Exception exception) {
            source.sendFailure(Component.literal("This command must be run by a player holding an item."));
            return 0;
        }
    }

    private static int runAudit(net.minecraft.commands.CommandSourceStack source) {
        Map<MatterValueRegistry.ValueSource, Integer> counts = new EnumMap<>(MatterValueRegistry.ValueSource.class);
        int total = 0;
        int valued = 0;
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || id.equals(BuiltInRegistries.ITEM.getDefaultKey())) continue;
            total++;
            MatterValueRegistry.MatterValue result = MatterValueRegistry.getMatterValue(source.getLevel(), new ItemStack(item));
            counts.merge(result.source(), 1, Integer::sum);
            if (result.hasMatter()) valued++;
        }
        int finalTotal = total;
        int finalValued = valued;
        source.sendSuccess(() -> Component.literal("Matter audit: " + finalValued + "/" + finalTotal + " valued | explicit="
                + counts.getOrDefault(MatterValueRegistry.ValueSource.EXPLICIT, 0)
                + " tag=" + counts.getOrDefault(MatterValueRegistry.ValueSource.TAG_BASE, 0)
                + " recipe=" + counts.getOrDefault(MatterValueRegistry.ValueSource.RECIPE, 0)
                + " fallback=" + counts.getOrDefault(MatterValueRegistry.ValueSource.FALLBACK, 0)
                + " dynamic=" + counts.getOrDefault(MatterValueRegistry.ValueSource.DYNAMIC, 0)
                + " unresolved=" + counts.getOrDefault(MatterValueRegistry.ValueSource.NONE, 0)), false);
        return finalValued;
    }

    private static int clearCache(net.minecraft.commands.CommandSourceStack source) {
        MatterValueRegistry.clearRecipeCache();
        source.sendSuccess(() -> Component.literal("Matter recipe cache cleared."), true);
        return 1;
    }
}
