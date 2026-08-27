package matteroverdrive.item;

import matteroverdrive.capability.IMatterStorage;
import matteroverdrive.capability.MachineMatterStorage;
import matteroverdrive.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.List;

public class MatterContainerItem extends Item {
    public static final int CAPACITY = 1000;
    private static final String SYNC_TAG = "MatterContainerSync";

    public MatterContainerItem(Properties properties) {
        super(properties.stacksTo(8));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockEntity targetEntity = level.getBlockEntity(context.getClickedPos());
        if (targetEntity == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        Direction side = context.getClickedFace();
        LazyOptional<IMatterStorage> targetOptional = targetEntity.getCapability(ModCapabilities.MATTER, side);
        LazyOptional<IMatterStorage> containerOptional = stack.getCapability(ModCapabilities.MATTER);
        if (!targetOptional.isPresent() || !containerOptional.isPresent()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        IMatterStorage target = targetOptional.orElseThrow(IllegalStateException::new);
        IMatterStorage container = containerOptional.orElseThrow(IllegalStateException::new);
        Player player = context.getPlayer();
        String targetName = targetEntity.getBlockState().getBlock().getName().getString();

        int containerBefore = container.getMatterStored();
        int targetBefore = target.getMatterStored();

        int movedToTarget = transferMatter(container, target);
        if (movedToTarget > 0) {
            syncTransfer(level, targetEntity, player, stack, container);
            sendDebug(player,
                    "[MO DEBUG] MATTER: moved " + movedToTarget + " from Matter Container -> " + targetName
                            + " | container " + containerBefore + " -> " + container.getMatterStored()
                            + "/" + container.getMatterCapacity()
                            + " | machine " + targetBefore + " -> " + target.getMatterStored()
                            + "/" + target.getMatterCapacity());
            return InteractionResult.CONSUME;
        }

        int movedFromTarget = transferMatter(target, container);
        if (movedFromTarget > 0) {
            syncTransfer(level, targetEntity, player, stack, container);
            sendDebug(player,
                    "[MO DEBUG] MATTER: pulled " + movedFromTarget + " from " + targetName + " -> Matter Container"
                            + " | machine " + targetBefore + " -> " + target.getMatterStored()
                            + "/" + target.getMatterCapacity()
                            + " | container " + containerBefore + " -> " + container.getMatterStored()
                            + "/" + container.getMatterCapacity());
            return InteractionResult.CONSUME;
        }

        sendDebug(player,
                "[MO DEBUG] MATTER: no transfer with " + targetName
                        + " | machine=" + target.getMatterStored() + "/" + target.getMatterCapacity()
                        + " receive=" + target.canReceive() + " extract=" + target.canExtract()
                        + " | container=" + container.getMatterStored() + "/" + container.getMatterCapacity());
        return InteractionResult.CONSUME;
    }

    private static void syncTransfer(Level level, BlockEntity targetEntity, @Nullable Player player,
                                     ItemStack stack, IMatterStorage container) {
        targetEntity.setChanged();
        level.sendBlockUpdated(targetEntity.getBlockPos(), targetEntity.getBlockState(), targetEntity.getBlockState(), 3);

        // Capability contents alone do not always make an ItemStack compare as changed
        // for vanilla slot synchronization. Mirror the current amount into a harmless
        // root tag so the held stack is guaranteed to differ and immediately broadcast
        // the player's inventory. The capability remains the authoritative storage.
        stack.getOrCreateTag().putInt(SYNC_TAG, container.getMatterStored());

        if (player != null) {
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            player.containerMenu.broadcastChanges();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.inventoryMenu.broadcastFullState();
            }
        }
    }

    private static void sendDebug(@Nullable Player player, String message) {
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
        }
    }

    private static int transferMatter(IMatterStorage source, IMatterStorage destination) {
        if (!source.canExtract() || !destination.canReceive()) {
            return 0;
        }

        int available = source.extractMatter(Integer.MAX_VALUE, true);
        if (available <= 0) {
            return 0;
        }

        int accepted = destination.receiveMatter(available, true);
        if (accepted <= 0) {
            return 0;
        }

        int extracted = source.extractMatter(accepted, false);
        if (extracted <= 0) {
            return 0;
        }

        int received = destination.receiveMatter(extracted, false);
        if (received < extracted) {
            source.receiveMatter(extracted - received, false);
        }
        return received;
    }

    public static int getMatter(ItemStack stack) {
        return stack.getCapability(ModCapabilities.MATTER)
                .map(IMatterStorage::getMatterStored)
                .orElse(0);
    }

    public static float getFillFraction(ItemStack stack) {
        return Math.min(1.0F, Math.max(0.0F, getMatter(stack) / (float) CAPACITY));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getMatter(stack) <= 0
                ? "item.matteroverdrive.matter_container_empty.name"
                : "item.matteroverdrive.matter_container_full.name");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Matter: " + getMatter(stack) + " / " + CAPACITY));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new MatterContainerCapabilityProvider();
    }

    private static final class MatterContainerCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
        private final MachineMatterStorage storage = new MachineMatterStorage(CAPACITY, true, true, null);
        private final LazyOptional<IMatterStorage> capability = LazyOptional.of(() -> storage);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
            return cap == ModCapabilities.MATTER ? capability.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Matter", storage.getMatterStored());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            storage.setMatterStored(nbt.getInt("Matter"));
        }
    }
}
