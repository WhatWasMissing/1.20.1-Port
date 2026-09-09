package matteroverdrive.item;

import matteroverdrive.blockentity.QuantumPowerRelayBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Two-click explicit pairing tool for Quantum Power Relays. */
public class QuantumLinkerItem extends Item {
    private static final String DIMENSION = "LinkDimension";
    private static final String POSITION = "LinkPosition";

    public QuantumLinkerItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide || !(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
        if (!(be instanceof QuantumPowerRelayBlockEntity relay)) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();

        if (player.isCrouching()) {
            relay.clearLinks();
            tag.remove(DIMENSION); tag.remove(POSITION);
            player.sendSystemMessage(Component.literal("Cleared all links from this Quantum Power Relay").withStyle(ChatFormatting.YELLOW));
            return InteractionResult.CONSUME;
        }

        String currentDimension = context.getLevel().dimension().location().toString();
        if (!tag.contains(POSITION) || !tag.contains(DIMENSION)) {
            tag.putString(DIMENSION, currentDimension);
            tag.putLong(POSITION, context.getClickedPos().asLong());
            player.sendSystemMessage(Component.literal("Quantum Linker source set: " + context.getClickedPos().toShortString()).withStyle(ChatFormatting.AQUA));
            return InteractionResult.CONSUME;
        }

        String sourceDimension = tag.getString(DIMENSION);
        BlockPos sourcePos = BlockPos.of(tag.getLong(POSITION));
        tag.remove(DIMENSION); tag.remove(POSITION);
        if (!sourceDimension.equals(currentDimension)) {
            player.sendSystemMessage(Component.literal("Quantum relays must be in the same dimension").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        if (sourcePos.equals(context.getClickedPos())) {
            player.sendSystemMessage(Component.literal("Select a different Quantum Power Relay").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        if (sourcePos.distSqr(context.getClickedPos()) > (double)QuantumPowerRelayBlockEntity.RANGE * QuantumPowerRelayBlockEntity.RANGE) {
            player.sendSystemMessage(Component.literal("Relay is beyond the " + QuantumPowerRelayBlockEntity.RANGE + " block link range").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        if (!context.getLevel().hasChunkAt(sourcePos)) {
            player.sendSystemMessage(Component.literal("Source relay chunk must be loaded while creating the link").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        BlockEntity sourceBe = context.getLevel().getBlockEntity(sourcePos);
        if (!(sourceBe instanceof QuantumPowerRelayBlockEntity source)) {
            player.sendSystemMessage(Component.literal("Stored source relay no longer exists").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        if (source.getLinkCount() >= QuantumPowerRelayBlockEntity.MAX_LINKS || relay.getLinkCount() >= QuantumPowerRelayBlockEntity.MAX_LINKS) {
            player.sendSystemMessage(Component.literal("One of the relays has reached its link limit").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }
        boolean a = source.addLink(context.getClickedPos());
        boolean b = relay.addLink(sourcePos);
        if (a || b) player.sendSystemMessage(Component.literal("Quantum relays linked: " + sourcePos.toShortString() + " <-> " + context.getClickedPos().toShortString()).withStyle(ChatFormatting.GREEN));
        else player.sendSystemMessage(Component.literal("These relays are already linked").withStyle(ChatFormatting.GRAY));
        return InteractionResult.CONSUME;
    }
}