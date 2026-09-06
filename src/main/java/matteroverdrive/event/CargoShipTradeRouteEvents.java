package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.quest.ContractStageSupport;
import matteroverdrive.quest.LegacyStoryContracts;
import matteroverdrive.registry.ModBlocks;
import matteroverdrive.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Restores the legacy cargo-ship Trade Route acquisition without rewriting existing generated ships.
 * On first interaction with a cargo-ship quest crate, the nearby red/lime pair is populated exactly once.
 */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CargoShipTradeRouteEvents {
    private static final String SEEDED = "MatterOverdriveTradeRouteSeeded";
    private static final int SEARCH_X = 36;
    private static final int SEARCH_Y = 8;
    private static final int SEARCH_Z = 16;

    private CargoShipTradeRouteEvents() {}

    @SubscribeEvent
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        BlockPos clicked = event.getPos();
        if (!isQuestCrate(player, clicked)) return;

        CargoPair pair = findCargoPair(player, clicked);
        if (pair == null || !hasCargoSignature(player, clicked)) return;
        seedPair(player, pair);
    }

    private static boolean isQuestCrate(ServerPlayer player, BlockPos pos) {
        var block = player.level().getBlockState(pos).getBlock();
        return block == ModBlocks.get("tritanium_crate_red").get()
                || block == ModBlocks.get("tritanium_crate_lime").get();
    }

    private static CargoPair findCargoPair(ServerPlayer player, BlockPos origin) {
        BlockPos red = null;
        BlockPos lime = null;
        double redDistance = Double.MAX_VALUE;
        double limeDistance = Double.MAX_VALUE;
        for (int dx = -SEARCH_X; dx <= SEARCH_X; dx++) {
            for (int dy = -SEARCH_Y; dy <= SEARCH_Y; dy++) {
                for (int dz = -SEARCH_Z; dz <= SEARCH_Z; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    var block = player.level().getBlockState(pos).getBlock();
                    double distance = origin.distSqr(pos);
                    if (block == ModBlocks.get("tritanium_crate_red").get() && distance < redDistance) {
                        red = pos.immutable();
                        redDistance = distance;
                    } else if (block == ModBlocks.get("tritanium_crate_lime").get() && distance < limeDistance) {
                        lime = pos.immutable();
                        limeDistance = distance;
                    }
                }
            }
        }
        return red != null && lime != null ? new CargoPair(red, lime) : null;
    }

    private static boolean hasCargoSignature(ServerPlayer player, BlockPos origin) {
        boolean transporter = false;
        boolean networkSwitch = false;
        for (int dx = -SEARCH_X; dx <= SEARCH_X && !(transporter && networkSwitch); dx++) {
            for (int dy = -SEARCH_Y; dy <= SEARCH_Y && !(transporter && networkSwitch); dy++) {
                for (int dz = -SEARCH_Z; dz <= SEARCH_Z; dz++) {
                    var block = player.level().getBlockState(origin.offset(dx, dy, dz)).getBlock();
                    transporter |= block == ModBlocks.get("transporter").get();
                    networkSwitch |= block == ModBlocks.get("network_switch").get();
                    if (transporter && networkSwitch) return true;
                }
            }
        }
        return false;
    }

    private static void seedPair(ServerPlayer player, CargoPair pair) {
        if (!(player.level().getBlockEntity(pair.red()) instanceof TritaniumCrateBlockEntity redCrate)) return;
        if (!(player.level().getBlockEntity(pair.lime()) instanceof TritaniumCrateBlockEntity limeCrate)) return;
        if (redCrate.getPersistentData().getBoolean(SEEDED) && limeCrate.getPersistentData().getBoolean(SEEDED)) return;

        if (!redCrate.getPersistentData().getBoolean(SEEDED)) {
            ItemStack agreement = new ItemStack(ModItems.get("isolinear_circuit_mk1").get());
            agreement.setHoverName(Component.literal("Trade Route Agreement"));
            insertFirstFree(redCrate, agreement);
            redCrate.getPersistentData().putBoolean(SEEDED, true);
            redCrate.setChanged();
        }

        if (!limeCrate.getPersistentData().getBoolean(SEEDED)) {
            ItemStack contract = LegacyStoryContracts.create("trade_route", player.level().random);
            ContractStageSupport.setQuestPosition(contract, pair.red(), 0);
            insertFirstFree(limeCrate, contract);
            limeCrate.getPersistentData().putBoolean(SEEDED, true);
            limeCrate.setChanged();
        }
    }

    private static void insertFirstFree(TritaniumCrateBlockEntity crate, ItemStack stack) {
        for (int slot = 0; slot < crate.getInventory().getSlots(); slot++) {
            if (!crate.getInventory().getStackInSlot(slot).isEmpty()) continue;
            crate.getInventory().setStackInSlot(slot, stack);
            return;
        }
    }

    private record CargoPair(BlockPos red, BlockPos lime) {}
}
