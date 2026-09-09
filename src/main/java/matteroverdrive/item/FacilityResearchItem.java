package matteroverdrive.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

/** Field research complements the scientist campaign without bypassing its objectives. */
public final class FacilityResearchItem extends Item {
    public enum Archive {
        SYNTHETIC_MANUFACTURING_PLANT("Synthetic assembly records", "Separate fabrication, charging and shipping lines with buffered storage.", 100),
        MATTER_REFINERY("Matter recovery survey", "Analyze recovered materials before archiving replication patterns.", 100),
        QUANTUM_RELAY_STATION("Quantum relay calibration", "Buffer relay endpoints and inspect network demand before adding load.", 150),
        ANDROID_COMMAND_BUNKER("Android command doctrine", "Guard squads defend their deployment station; clear security before salvaging it.", 150),
        FUSION_RESEARCH_COMPLEX("Fusion containment study", "Establish powered stabilizers and controlled matter input before reactor operation.", 200),
        BLACK_SITE("Restricted anomaly dossier", "Containment equipment is not proof that an abandoned experiment is safe.", 250);
        final String title, finding;
        final int xp;
        Archive(String title, String finding, int xp) { this.title=title; this.finding=finding; this.xp=xp; }
    }
    public FacilityResearchItem(Properties properties) { super(properties); }
    @Nullable private static Archive archive(ItemStack stack) {
        if (!stack.hasTag()) return null;
        try { return Archive.valueOf(stack.getTag().getString("FacilityArchive")); }
        catch (IllegalArgumentException ex) { return null; }
    }
    @Override public Component getName(ItemStack stack) {
        Archive archive = archive(stack);
        return Component.literal(archive == null ? "Recovered Research Dossier" : archive.title);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Archive archive = archive(stack);
        if (archive == null) return InteractionResultHolder.pass(stack);
        if (player instanceof ServerPlayer server) {
            CompoundTag persisted = server.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            CompoundTag discoveries = persisted.getCompound("MatterOverdriveFacilityResearch");
            if (!discoveries.getBoolean(archive.name())) {
                discoveries.putBoolean(archive.name(), true);
                persisted.put("MatterOverdriveFacilityResearch", discoveries);
                server.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
                server.giveExperiencePoints(archive.xp);
                String rewardId = switch (archive) {
                    case SYNTHETIC_MANUFACTURING_PLANT -> "upgrade_speed";
                    case MATTER_REFINERY -> "upgrade_matter_storage";
                    case QUANTUM_RELAY_STATION -> "upgrade_power_storage";
                    case ANDROID_COMMAND_BUNKER -> "weapon_module_holo_sights";
                    case FUSION_RESEARCH_COMPLEX -> "upgrade_failsafe";
                    case BLACK_SITE -> "chassis_optics_precision";
                };
                ItemStack reward = new ItemStack(matteroverdrive.registry.ModItems.get(rewardId).get());
                if (!server.getInventory().add(reward)) server.drop(reward, false);
                server.displayClientMessage(Component.literal("Research archived: " + archive.title + " (+" + archive.xp + " XP; research equipment delivered)"), false);
            } else server.displayClientMessage(Component.literal("Already archived: " + archive.title), false);
            server.displayClientMessage(Component.literal(archive.finding), false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("Use to archive field research. Keep or share after reading."));
        lines.add(Component.literal("First discovery awards XP once per facility family per player."));
    }
}
