package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.entity.MadScientistEntity;
import matteroverdrive.quest.ContractStageSupport;
import matteroverdrive.quest.LegacyStoryContracts;
import matteroverdrive.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Backwards-compatible restoration of legacy story quest loot in already-generated structures.
 * Exact original injection points were crashed-spacecraft Tritanium Crates and the Mad Scientist House crate.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LegacyQuestAcquisitionEvents {
    private static final String CRASH_SEEDED = "MatterOverdriveCrashLandingSeeded";
    private static final String GMO_SEEDED = "MatterOverdriveGmoSeeded";

    private LegacyQuestAcquisitionEvents() {}

    @SubscribeEvent
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
        BlockPos pos = event.getPos();
        if (player.level().getBlockState(pos).getBlock() != ModBlocks.get("tritanium_crate").get()) return;
        if (!(player.level().getBlockEntity(pos) instanceof TritaniumCrateBlockEntity crate)) return;

        if (!crate.getPersistentData().getBoolean(GMO_SEEDED) && isMadScientistHouse(player, pos)) {
            seedGmo(player, crate);
            return;
        }
        if (!crate.getPersistentData().getBoolean(CRASH_SEEDED) && isCrashedSpacecraft(player, pos)) {
            seedCrashLanding(player, crate, pos);
        }
    }

    private static void seedCrashLanding(net.minecraft.server.level.ServerPlayer player,
                                         TritaniumCrateBlockEntity crate, BlockPos pos) {
        ItemStack contract = LegacyStoryContracts.create("crash_landing", player.level().random);
        // Legacy generator stored the source crate position; We Must Know copies it and allows radius 4.
        ContractStageSupport.setQuestPosition(contract, pos, 4);
        insertFirstFree(crate, contract);
        crate.getPersistentData().putBoolean(CRASH_SEEDED, true);
        crate.setChanged();
    }

    private static void seedGmo(net.minecraft.server.level.ServerPlayer player, TritaniumCrateBlockEntity crate) {
        insertFirstFree(crate, LegacyStoryContracts.create("gmo", player.level().random));
        insertFirstFree(crate, LegacyStoryContracts.createMadScientistDataPad());
        crate.getPersistentData().putBoolean(GMO_SEEDED, true);
        crate.setChanged();
    }

    private static boolean isMadScientistHouse(net.minecraft.server.level.ServerPlayer player, BlockPos origin) {
        boolean inscriber = false;
        boolean decomposer = false;
        for (int dx = -8; dx <= 8 && !(inscriber && decomposer); dx++) {
            for (int dy = -5; dy <= 5 && !(inscriber && decomposer); dy++) {
                for (int dz = -8; dz <= 8; dz++) {
                    var block = player.level().getBlockState(origin.offset(dx, dy, dz)).getBlock();
                    inscriber |= block == ModBlocks.get("inscriber").get();
                    decomposer |= block == ModBlocks.get("decomposer").get();
                    if (inscriber && decomposer) break;
                }
            }
        }
        if (!(inscriber && decomposer)) return false;
        return !player.level().getEntitiesOfClass(MadScientistEntity.class, new AABB(origin).inflate(12.0D)).isEmpty();
    }

    private static boolean isCrashedSpacecraft(net.minecraft.server.level.ServerPlayer player, BlockPos origin) {
        // Cargo ships also contain holograms/crates, but their Transporter distinguishes them from the crash site.
        boolean holo = false;
        boolean transporter = false;
        int hull = 0;
        for (int dx = -12; dx <= 12; dx++) {
            for (int dy = -6; dy <= 6; dy++) {
                for (int dz = -20; dz <= 20; dz++) {
                    var block = player.level().getBlockState(origin.offset(dx, dy, dz)).getBlock();
                    if (block == ModBlocks.get("holo_sign").get()) holo = true;
                    if (block == ModBlocks.get("transporter").get()) transporter = true;
                    if (block == ModBlocks.get("decorative.tritanium_plate").get()
                            || block == ModBlocks.get("decorative.tritanium_plate_stripe").get()) hull++;
                }
            }
        }
        return holo && !transporter && hull >= 12;
    }

    private static void insertFirstFree(TritaniumCrateBlockEntity crate, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        for (int slot = 0; slot < crate.getInventory().getSlots(); slot++) {
            if (!crate.getInventory().getStackInSlot(slot).isEmpty()) continue;
            crate.getInventory().setStackInSlot(slot, stack);
            return;
        }
    }
}
