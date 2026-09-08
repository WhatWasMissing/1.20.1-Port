package matteroverdrive.block;

import matteroverdrive.blockentity.GravitationalAnomalyBlockEntity;
import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.network.MatterNetworkUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Central telemetry console for machines reachable through the Matter Overdrive data network. */
public class FacilityNetworkControllerBlock extends Block {
    public FacilityNetworkControllerBlock(Properties properties) { super(properties); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        List<BlockEntity> nodes = MatterNetworkUtil.findConnected(level, pos, BlockEntity.class);
        long energyStored = 0L, energyCapacity = 0L, matterStored = 0L, matterCapacity = 0L;
        int powered = 0, matterNodes = 0, anomalies = 0;
        Map<String, Integer> types = new TreeMap<>();

        for (BlockEntity node : nodes) {
            types.merge(pretty(node.getClass().getSimpleName()), 1, Integer::sum);
            var energy = node.getCapability(ForgeCapabilities.ENERGY).resolve();
            if (energy.isPresent()) {
                energyStored += energy.get().getEnergyStored();
                energyCapacity += energy.get().getMaxEnergyStored();
                powered++;
            }
            var matter = node.getCapability(ModCapabilities.MATTER).resolve();
            if (matter.isPresent()) {
                IMatterStorage storage = matter.get();
                matterStored += storage.getMatterStored();
                matterCapacity += storage.getMatterCapacity();
                matterNodes++;
            }
            if (node instanceof GravitationalAnomalyBlockEntity) anomalies++;
        }

        player.sendSystemMessage(Component.literal("[Matter Overdrive Facility Network]").withStyle(ChatFormatting.AQUA));
        if (nodes.isEmpty()) {
            player.sendSystemMessage(Component.literal("No connected network clients. Attach the controller to network pipe/router infrastructure.").withStyle(ChatFormatting.YELLOW));
            return InteractionResult.CONSUME;
        }
        player.sendSystemMessage(Component.literal("Nodes: " + nodes.size() + " | FE endpoints: " + powered + " | Matter endpoints: " + matterNodes).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("FE: " + energyStored + " / " + energyCapacity).withStyle(energyCapacity > 0 && energyStored * 10L < energyCapacity ? ChatFormatting.RED : ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("Matter: " + matterStored + " / " + matterCapacity).withStyle(matterCapacity > 0 && matterStored * 10L < matterCapacity ? ChatFormatting.YELLOW : ChatFormatting.LIGHT_PURPLE));
        if (anomalies > 0) player.sendSystemMessage(Component.literal("WARNING: " + anomalies + " anomaly node(s) detected.").withStyle(ChatFormatting.RED));

        if (player.isCrouching()) {
            player.sendSystemMessage(Component.literal("Connected device inventory:").withStyle(ChatFormatting.DARK_AQUA));
            types.forEach((name, count) -> player.sendSystemMessage(Component.literal(" - " + name + " x" + count).withStyle(ChatFormatting.GRAY)));
        } else {
            player.sendSystemMessage(Component.literal("Sneak + use for connected device inventory.").withStyle(ChatFormatting.DARK_GRAY));
        }
        return InteractionResult.CONSUME;
    }

    private static String pretty(String name) {
        String cleaned = name.replace("BlockEntity", "");
        return cleaned.replaceAll("([a-z])([A-Z])", "$1 $2");
    }
}