package matteroverdrive.block;

import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.network.FacilityNetworkTelemetry;
import matteroverdrive.network.MatterNetworkUtil;
import matteroverdrive.quest.ResearchProgression;
import matteroverdrive.world.TechnologySiteDiscoverySavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.LazyOptional;

/** Player-facing remote access point for a bounded Matter Overdrive matter network. */
public final class MatterNetworkTerminalBlock extends Block {
    private static final int TRANSFER_PER_USE = 1_000;

    public MatterNetworkTerminalBlock(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer server)) return InteractionResult.PASS;

        FacilityNetworkTelemetry.Snapshot snapshot = FacilityNetworkTelemetry.scan(level, pos);
        ItemStack held = player.getItemInHand(hand);
        LazyOptional<IMatterStorage> heldCapability = held.getCapability(ModCapabilities.MATTER);
        IMatterStorage container = heldCapability.orElse(null);
        if (container == null) {
            sendStatus(server, snapshot, 0, "Insert a Matter Container to transfer through this terminal.");
            return InteractionResult.CONSUME;
        }

        int moved = MatterNetworkUtil.pushMatter(level, pos, container, TRANSFER_PER_USE, level.getGameTime());
        String direction = "container -> network";
        if (moved == 0) {
            moved = MatterNetworkUtil.pullMatter(level, pos, container, TRANSFER_PER_USE, level.getGameTime());
            direction = "network -> container";
        }
        if (moved > 0) {
            held.getOrCreateTag().putInt("MatterContainerSync", container.getMatterStored());
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
        }
        sendStatus(server, snapshot, moved, moved > 0 ? moved + " Matter moved (" + direction + ")." : "No transfer available: check pipe route, side policy, or capacity.");
        return InteractionResult.CONSUME;
    }

    private static void sendStatus(ServerPlayer player, FacilityNetworkTelemetry.Snapshot snapshot, int moved, String action) {
        player.sendSystemMessage(Component.literal("[MATTER NETWORK TERMINAL]").withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal("Matter " + snapshot.matterStored() + "/" + snapshot.matterCapacity() + " (" + snapshot.matterPercent() + "%) | endpoints " + snapshot.matterEndpoints()).withStyle(ChatFormatting.GRAY));
        TechnologySiteDiscoverySavedData sites = TechnologySiteDiscoverySavedData.get(player.serverLevel());
        int chainStage = sites.chainStage(player.getUUID());
        player.sendSystemMessage(Component.literal("Research " + ResearchProgression.stage(player).title
                + " | field sites " + sites.count(player.getUUID())
                + " | investigation " + chainStage + "/" + sites.chainLength()).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("Next investigation: "
                + TechnologySiteDiscoverySavedData.nextChainSite(player.getUUID(), chainStage)).withStyle(ChatFormatting.DARK_AQUA));
        if (snapshot.encounterFactions().isEmpty()) {
            player.sendSystemMessage(Component.literal("Security: no Android encounters in terminal range.").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            String factions = snapshot.encounterFactions().entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .sorted().collect(java.util.stream.Collectors.joining(", "));
            player.sendSystemMessage(Component.literal("Security encounters: " + factions).withStyle(ChatFormatting.RED));
        }
        player.sendSystemMessage(Component.literal(action).withStyle(moved > 0 ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        if (!snapshot.alarms().isEmpty()) player.sendSystemMessage(Component.literal("Alarms: " + snapshot.alarms().size()).withStyle(ChatFormatting.RED));
        player.sendSystemMessage(Component.literal("Use with a Matter Container to transfer up to " + TRANSFER_PER_USE + " Matter per request.").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return MatterNetworkTelemetryComparator.level(level, pos);
    }

    private static final class MatterNetworkTelemetryComparator {
        private static int level(Level level, BlockPos pos) {
            FacilityNetworkTelemetry.Snapshot snapshot = FacilityNetworkTelemetry.scan(level, pos);
            return snapshot.matterCapacity() <= 0 ? 0 : Math.max(0, Math.min(15, (int) ((long) snapshot.matterStored() * 15L / snapshot.matterCapacity())));
        }
    }
}
