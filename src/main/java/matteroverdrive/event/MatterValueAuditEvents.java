package matteroverdrive.event;

import com.mojang.logging.LogUtils;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.matter.MatterValueRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID)
public final class MatterValueAuditEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FALLBACK_SAMPLE_LIMIT = 40;

    private MatterValueAuditEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MatterValueRegistry.clearRecipeCache();

        Map<MatterValueRegistry.ValueSource, Integer> counts =
                new EnumMap<>(MatterValueRegistry.ValueSource.class);
        List<ResourceLocation> fallbackSamples = new ArrayList<>();
        int total = 0;
        int valued = 0;

        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || id.getPath().equals("air")) {
                continue;
            }

            total++;
            MatterValueRegistry.MatterValue value = MatterValueRegistry.getMatterValue(
                    event.getServer().overworld(), new ItemStack(item));
            counts.merge(value.source(), 1, Integer::sum);
            if (value.hasMatter()) {
                valued++;
            }
            if (value.source() == MatterValueRegistry.ValueSource.FALLBACK
                    && fallbackSamples.size() < FALLBACK_SAMPLE_LIMIT) {
                fallbackSamples.add(id);
            }
        }

        LOGGER.info(
                "MATTER VALUE AUDIT: valued={}/{} explicit={} tagBase={} recipe={} fallback={} dynamic={} unresolved={}",
                valued,
                total,
                counts.getOrDefault(MatterValueRegistry.ValueSource.EXPLICIT, 0),
                counts.getOrDefault(MatterValueRegistry.ValueSource.TAG_BASE, 0),
                counts.getOrDefault(MatterValueRegistry.ValueSource.RECIPE, 0),
                counts.getOrDefault(MatterValueRegistry.ValueSource.FALLBACK, 0),
                counts.getOrDefault(MatterValueRegistry.ValueSource.DYNAMIC, 0),
                counts.getOrDefault(MatterValueRegistry.ValueSource.NONE, 0));

        if (!fallbackSamples.isEmpty()) {
            LOGGER.info("MATTER VALUE AUDIT: fallback sample (first {}): {}",
                    fallbackSamples.size(), fallbackSamples);
        }
    }
}
