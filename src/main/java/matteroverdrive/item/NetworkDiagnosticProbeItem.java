package matteroverdrive.item;

import matteroverdrive.blockentity.AndroidInductionRelayBlockEntity;
import matteroverdrive.blockentity.MatterExcavatorBlockEntity;
import matteroverdrive.blockentity.NetworkRouterBlockEntity;
import matteroverdrive.blockentity.NetworkSwitchBlockEntity;
import matteroverdrive.blockentity.QuantumPowerRelayBlockEntity;
import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import matteroverdrive.machine.MachineSideConfigurationData;
import matteroverdrive.network.FacilityNetworkTelemetry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Handheld network inspector/configurator inspired by conduit/network probes. */
public class NetworkDiagnosticProbeItem extends Item {
    private static final String MODE = "ProbeMode";
    public enum Mode { DIAGNOSTIC, ENERGY, MATTER, ITEMS, NETWORK, RANGE }
    public NetworkDiagnosticProbeItem(Properties properties) { super(properties); }
    public static Mode mode(ItemStack stack) { int ordinal = stack.getOrCreateTag().getInt(MODE); return ordinal >= 0 && ordinal < Mode.values().length ? Mode.values()[ordinal] : Mode.DIAGNOSTIC; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand); if (!player.isCrouching()) return InteractionResultHolder.pass(stack);
        Mode next = Mode.values()[(mode(stack).ordinal() + 1) % Mode.values().length]; stack.getOrCreateTag().putInt(MODE, next.ordinal());
        if (!level.isClientSide) player.sendSystemMessage(Component.literal("Network Probe mode -> " + next.name()).withStyle(ChatFormatting.AQUA));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        Player raw = context.getPlayer(); if (!(raw instanceof ServerPlayer player)) return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        Level level = context.getLevel(); BlockPos pos = context.getClickedPos(); Direction face = context.getClickedFace(); ItemStack stack = context.getItemInHand(); BlockEntity be = level.getBlockEntity(pos); Mode mode = mode(stack);
        if (player.isCrouching()) {
            if (mode == Mode.ENERGY || mode == Mode.MATTER || mode == Mode.ITEMS) {
                MachineSideConfigurationData.Resource resource = switch (mode) { case ENERGY -> MachineSideConfigurationData.Resource.ENERGY; case MATTER -> MachineSideConfigurationData.Resource.MATTER; default -> MachineSideConfigurationData.Resource.ITEMS; };
                MachineSideConfigurationData.Mode next = MachineSideConfigurationData.get(player.serverLevel()).cycle(pos, face, resource);
                player.sendSystemMessage(Component.literal(resource + " " + face + " -> " + next).withStyle(ChatFormatting.AQUA)); return InteractionResult.CONSUME;
            }
            if (mode == Mode.NETWORK) {
                if (be instanceof NetworkRouterBlockEntity router) {
                    if (face == Direction.UP || face == Direction.DOWN) player.sendSystemMessage(Component.literal("Router priority -> " + router.cyclePriority()).withStyle(ChatFormatting.GOLD));
                    else player.sendSystemMessage(Component.literal("Router channel -> " + router.cycleChannel()).withStyle(ChatFormatting.AQUA));
                } else if (be instanceof NetworkSwitchBlockEntity networkSwitch) player.sendSystemMessage(Component.literal("Switch channel -> " + networkSwitch.cycleChannel()).withStyle(ChatFormatting.AQUA));
                else if (be instanceof QuantumPowerRelayBlockEntity) player.sendSystemMessage(Component.literal("Quantum relay pairing uses the Quantum Linker; the probe will not create broadcast links.").withStyle(ChatFormatting.YELLOW));
                else player.sendSystemMessage(Component.literal("No configurable network channel on this block").withStyle(ChatFormatting.DARK_GRAY));
                return InteractionResult.CONSUME;
            }
            if (mode == Mode.RANGE) { showRange(player.serverLevel(), pos, rangeOf(be)); return InteractionResult.CONSUME; }
        }

        player.sendSystemMessage(Component.literal("[MO PROBE] " + level.getBlockState(pos).getBlock().getName().getString() + " @ " + pos.toShortString()).withStyle(ChatFormatting.AQUA));
        if (be == null) { player.sendSystemMessage(Component.literal("No block entity").withStyle(ChatFormatting.DARK_GRAY)); return InteractionResult.CONSUME; }
        IEnergyStorage energy = be.getCapability(ForgeCapabilities.ENERGY, face).orElse(null); if (energy != null) player.sendSystemMessage(Component.literal("FE " + energy.getEnergyStored() + "/" + energy.getMaxEnergyStored() + " | " + sideSummary(level,pos,face,MachineSideConfigurationData.Resource.ENERGY)).withStyle(ChatFormatting.YELLOW));
        IMatterStorage matter = be.getCapability(ModCapabilities.MATTER, face).orElse(null); if (matter != null) player.sendSystemMessage(Component.literal("Matter " + matter.getMatterStored() + "/" + matter.getMatterCapacity() + " | " + sideSummary(level,pos,face,MachineSideConfigurationData.Resource.MATTER)).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (be.getCapability(ForgeCapabilities.ITEM_HANDLER, face).isPresent()) player.sendSystemMessage(Component.literal("Items | " + sideSummary(level,pos,face,MachineSideConfigurationData.Resource.ITEMS)).withStyle(ChatFormatting.GREEN));
        if (be instanceof NetworkRouterBlockEntity router) player.sendSystemMessage(Component.literal("Router CH " + router.getChannel() + " PRI " + router.getPriority()).withStyle(ChatFormatting.AQUA));
        if (be instanceof NetworkSwitchBlockEntity sw) player.sendSystemMessage(Component.literal("Switch CH " + sw.getChannel() + " " + (sw.isEnabled()?"ONLINE":"ISOLATED")).withStyle(sw.isEnabled()?ChatFormatting.GREEN:ChatFormatting.RED));
        if (be instanceof QuantumPowerRelayBlockEntity relay) player.sendSystemMessage(Component.literal("Quantum relay LINKS " + relay.getLinkCount() + "/" + QuantumPowerRelayBlockEntity.MAX_LINKS + " RANGE " + QuantumPowerRelayBlockEntity.RANGE).withStyle(ChatFormatting.AQUA));
        if (be instanceof MatterExcavatorBlockEntity excavator) player.sendSystemMessage(Component.literal("Excavator filter " + excavator.getFilter() + " radius " + excavator.getRadius()).withStyle(ChatFormatting.GRAY));
        if (be instanceof AndroidInductionRelayBlockEntity) player.sendSystemMessage(Component.literal("Android induction relay detected").withStyle(ChatFormatting.AQUA));
        if (mode == Mode.DIAGNOSTIC) { FacilityNetworkTelemetry.Snapshot snapshot = FacilityNetworkTelemetry.scan(level, pos); if (snapshot.nodes() > 0) player.sendSystemMessage(Component.literal("Network: " + snapshot.nodes() + " nodes | FE " + snapshot.energyPercent() + "% | Matter " + snapshot.matterPercent() + "% | alarms " + snapshot.alarms().size()).withStyle(ChatFormatting.GRAY)); }
        return InteractionResult.CONSUME;
    }

    private static String sideSummary(Level level, BlockPos pos, Direction face, MachineSideConfigurationData.Resource resource) { return face + "=" + MachineSideConfigurationData.mode(level,pos,face,resource); }
    private static int rangeOf(BlockEntity be) { if (be instanceof MatterExcavatorBlockEntity excavator) return excavator.getRadius(); if (be instanceof AndroidInductionRelayBlockEntity relay) return relay.getRange(); if (be instanceof QuantumPowerRelayBlockEntity) return QuantumPowerRelayBlockEntity.RANGE; return 0; }
    private static void showRange(ServerLevel level, BlockPos center, int radius) {
        if (radius <= 0) return; int y = center.getY() + 1; int step = Math.max(1, radius / 12);
        for (int d=-radius; d<=radius; d+=step) { level.sendParticles(ParticleTypes.END_ROD, center.getX()+d+0.5, y, center.getZ()-radius+0.5, 1,0,0,0,0); level.sendParticles(ParticleTypes.END_ROD, center.getX()+d+0.5, y, center.getZ()+radius+0.5, 1,0,0,0,0); level.sendParticles(ParticleTypes.END_ROD, center.getX()-radius+0.5, y, center.getZ()+d+0.5, 1,0,0,0,0); level.sendParticles(ParticleTypes.END_ROD, center.getX()+radius+0.5, y, center.getZ()+d+0.5, 1,0,0,0,0); }
    }
    @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) { tooltip.add(Component.literal("Mode: " + mode(stack).name()).withStyle(ChatFormatting.AQUA)); tooltip.add(Component.literal("Sneak + right-click air: cycle mode").withStyle(ChatFormatting.GRAY)); tooltip.add(Component.literal("Use block: inspect | Sneak-use: configure selected side/network/range").withStyle(ChatFormatting.GRAY)); }
}
