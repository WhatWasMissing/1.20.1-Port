package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.ModItems;
import matteroverdrive.world.FrontierSecuritySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Turns hostile encounters inside frontier facilities into persistent clearance objectives. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FrontierThreatEvents {
    private static final ResourceKey<Structure> DEEP_MATTER_VAULT = key("deep_matter_vault");
    private static final ResourceKey<Structure> AUTONOMOUS_DRONE_FOUNDRY = key("autonomous_drone_foundry");
    private static final ResourceKey<Structure> ANOMALY_QUARANTINE_SITE = key("anomaly_quarantine_site");
    private static final ResourceKey<Structure> ORBITAL_RECOVERY_ARRAY = key("orbital_recovery_array");

    private FrontierThreatEvents() {}

    @SubscribeEvent
    public static void killed(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getEntity() instanceof Mob mob) || mob.getType().getCategory() != MobCategory.MONSTER) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        SiteMatch site = match(level, event.getEntity().blockPosition(), DEEP_MATTER_VAULT, "deep_matter_vault", 4);
        if (site == null) site = match(level, event.getEntity().blockPosition(), AUTONOMOUS_DRONE_FOUNDRY, "autonomous_drone_foundry", 6);
        if (site == null) site = match(level, event.getEntity().blockPosition(), ANOMALY_QUARANTINE_SITE, "anomaly_quarantine_site", 5);
        if (site == null) site = match(level, event.getEntity().blockPosition(), ORBITAL_RECOVERY_ARRAY, "orbital_recovery_array", 4);
        if (site == null) return;

        FrontierSecuritySavedData.ClearanceResult result = FrontierSecuritySavedData.get(level)
                .recordKill(level, player.getUUID(), site.site(), site.startChunk(), site.target());
        if (result.rejected()) return;
        if (!result.completed()) {
            player.displayClientMessage(Component.literal("Frontier security: " + result.progress() + "/" + result.target() + " hostiles cleared")
                    .withStyle(ChatFormatting.GRAY), true);
            return;
        }

        player.giveExperiencePoints(60);
        ItemStack dossier = new ItemStack(ModItems.get("facility_research").get());
        dossier.getOrCreateTag().putString("FrontierSecurityArchive", site.site().toUpperCase(java.util.Locale.ROOT));
        dossier.getOrCreateTag().putBoolean("Secured", true);
        if (!player.getInventory().add(dossier)) player.drop(dossier, false);
        player.sendSystemMessage(Component.literal("FACILITY SECURED: hostile control broken at " + readable(site.site()) + ". Security archive recovered.")
                .withStyle(ChatFormatting.GREEN));
    }

    private static SiteMatch match(ServerLevel level, BlockPos pos, ResourceKey<Structure> key, String site, int target) {
        StructureStart start = level.structureManager().getStructureWithPieceAt(pos, key);
        if (start == null || !start.isValid()) return null;
        return new SiteMatch(site, target, start.getChunkPos().toLong());
    }

    private static ResourceKey<Structure> key(String path) {
        return ResourceKey.create(Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath(MatterOverdrive.MOD_ID, path));
    }

    private static String readable(String id) {
        StringBuilder value = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) continue;
            if (value.length() > 0) value.append(' ');
            value.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return value.toString();
    }

    private record SiteMatch(String site, int target, long startChunk) {}
}
