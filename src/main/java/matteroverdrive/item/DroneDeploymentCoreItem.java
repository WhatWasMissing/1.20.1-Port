package matteroverdrive.item;

import matteroverdrive.entity.DroneEntity;
import matteroverdrive.quest.ResearchProgression;
import matteroverdrive.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/** Configurable, single-use core that deploys an owned role-specific drone. */
public class DroneDeploymentCoreItem extends Item {
    private static final String ROLE = "DroneRole";
    private static final String LOGISTICS_TARGET = "DroneLogisticsTarget";
    private static final String LOGISTICS_DIMENSION = "DroneLogisticsDimension";

    public DroneDeploymentCoreItem(Properties properties) { super(properties); }

    public static byte role(ItemStack stack) {
        return (byte)Math.max(DroneEntity.ROLE_COMBAT,
                Math.min(DroneEntity.ROLE_REACTOR_MAINTENANCE, stack.getOrCreateTag().getByte(ROLE)));
    }

    /** Returns a target only in the current dimension; cores never create cross-dimension drone routes. */
    @Nullable public static net.minecraft.core.BlockPos logisticsTarget(ItemStack stack, Level level) {
        if (!stack.hasTag() || !level.dimension().location().toString().equals(stack.getTag().getString(LOGISTICS_DIMENSION))
                || !stack.getTag().contains(LOGISTICS_TARGET)) return null;
        return net.minecraft.core.BlockPos.of(stack.getTag().getLong(LOGISTICS_TARGET));
    }

    public static String roleName(byte role) {
        return switch (role) {
            case DroneEntity.ROLE_REPAIR -> "REPAIR";
            case DroneEntity.ROLE_LOGISTICS -> "LOGISTICS";
            case DroneEntity.ROLE_SURVEY -> "SURVEY";
            case DroneEntity.ROLE_REACTOR_MAINTENANCE -> "REACTOR MAINTENANCE";
            default -> "COMBAT";
        };
    }

    /** Role clearance is checked when configuring, deploying, and reprogramming; an NBT core alone never bypasses research. */
    public static ResearchProgression.Stage requiredResearch(byte role) {
        return switch (role) {
            case DroneEntity.ROLE_REPAIR -> ResearchProgression.Stage.MATTER_TECHNOLOGY;
            case DroneEntity.ROLE_LOGISTICS, DroneEntity.ROLE_SURVEY -> ResearchProgression.Stage.AUTOMATION_DRONES;
            case DroneEntity.ROLE_REACTOR_MAINTENANCE -> ResearchProgression.Stage.FUSION_RESEARCH;
            default -> ResearchProgression.Stage.SCIENTIST_RESEARCH;
        };
    }

    public static boolean hasResearchClearance(ServerPlayer player, byte role) {
        return ResearchProgression.atLeast(player, requiredResearch(role));
    }

    public static Component clearanceMessage(byte role) {
        return Component.literal(roleName(role) + " Drone requires " + requiredResearch(role).title + " clearance.")
                .withStyle(ChatFormatting.RED);
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown() || role(stack) != DroneEntity.ROLE_LOGISTICS) {
            return net.minecraft.world.InteractionResult.PASS;
        }
        BlockEntity target = context.getLevel().getBlockEntity(context.getClickedPos());
        if (target == null || !target.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            if (!context.getLevel().isClientSide) player.displayClientMessage(
                    Component.literal("Logistics target needs an item inventory.").withStyle(ChatFormatting.RED), true);
            return net.minecraft.world.InteractionResult.FAIL;
        }
        if (!context.getLevel().isClientSide) {
            stack.getOrCreateTag().putLong(LOGISTICS_TARGET, context.getClickedPos().asLong());
            stack.getOrCreateTag().putString(LOGISTICS_DIMENSION, context.getLevel().dimension().location().toString());
            player.displayClientMessage(Component.literal("Logistics core target: "
                    + context.getClickedPos().getX() + ", " + context.getClickedPos().getY() + ", " + context.getClickedPos().getZ())
                    .withStyle(ChatFormatting.AQUA), true);
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                if (!(player instanceof ServerPlayer server)) return InteractionResultHolder.fail(stack);
                byte current = role(stack), next = current;
                for (int attempts = 0; attempts < 5; attempts++) {
                    next = (byte)((next + 1) % 5);
                    if (hasResearchClearance(server, next)) break;
                }
                stack.getOrCreateTag().putByte(ROLE, next);
                player.displayClientMessage(Component.literal("Drone Core role: " + roleName(next))
                        .withStyle(ChatFormatting.AQUA), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.sidedSuccess(stack, true);

        byte selectedRole = role(stack);
        if (!hasResearchClearance(serverPlayer, selectedRole)) {
            player.displayClientMessage(clearanceMessage(selectedRole), true);
            return InteractionResultHolder.fail(stack);
        }
        Vec3 look = player.getLookAngle();
        Vec3 spawn = player.position().add(look.x * 1.8D, 1.25D, look.z * 1.8D);
        DroneEntity drone = ModEntities.DRONE.get().create(server);
        if (drone == null) return InteractionResultHolder.fail(stack);
        drone.moveTo(spawn.x, spawn.y, spawn.z, player.getYRot(), 0F);
        if (!server.noCollision(drone, drone.getBoundingBox())) {
            player.displayClientMessage(Component.literal("Deployment area obstructed")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        drone.setDroneType(selectedRole);
        drone.setOwnerUuid(player.getUUID());
        net.minecraft.core.BlockPos logisticsTarget = logisticsTarget(stack, server);
        drone.setLogisticsTarget(logisticsTarget);
        drone.setCommandMode(selectedRole == DroneEntity.ROLE_LOGISTICS && logisticsTarget != null
                ? DroneEntity.MODE_LOGISTICS : DroneEntity.MODE_FOLLOW);
        drone.setCustomName(Component.literal(roleName(selectedRole) + " DRONE"));
        drone.setCustomNameVisible(true);
        drone.setPersistenceRequired();
        server.addFreshEntity(drone);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        player.displayClientMessage(Component.literal(roleName(selectedRole) + " Drone deployed · Mode " + drone.commandModeName())
                .withStyle(ChatFormatting.GREEN), true);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        byte selected = role(stack);
        tooltip.add(Component.literal("Configured role: " + roleName(selected)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal(switch (selected) {
            case DroneEntity.ROLE_REPAIR -> "Repairs its operator and nearby linked drones.";
            case DroneEntity.ROLE_LOGISTICS -> "Collects nearby dropped items for its operator.";
            case DroneEntity.ROLE_SURVEY -> "Marks nearby hostile targets for the fleet.";
            case DroneEntity.ROLE_REACTOR_MAINTENANCE -> "Feeds nearby gravitational stabilizers from its FE reserve.";
            default -> "Engages hostiles with energy-powered ranged fire.";
        }).withStyle(ChatFormatting.GRAY));
        ResearchProgression.Stage clearance = requiredResearch(selected);
        if (clearance != ResearchProgression.Stage.SCIENTIST_RESEARCH) {
            tooltip.add(Component.literal("Requires research: " + clearance.title).withStyle(ChatFormatting.GOLD));
        }
        if (selected == DroneEntity.ROLE_LOGISTICS) {
            net.minecraft.core.BlockPos target = logisticsTarget(stack, level);
            tooltip.add(Component.literal(target == null ? "Sneak-use an inventory block to configure a delivery route."
                    : "Route target: " + target.getX() + ", " + target.getY() + ", " + target.getZ())
                    .withStyle(target == null ? ChatFormatting.GOLD : ChatFormatting.GREEN));
        }
        tooltip.add(Component.literal("Sneak + right-click: change role").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Right-click: deploy linked drone").withStyle(ChatFormatting.DARK_GRAY));
    }
}
