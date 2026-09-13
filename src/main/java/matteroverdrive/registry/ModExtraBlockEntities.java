package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.AndroidInductionRelayBlockEntity;
import matteroverdrive.blockentity.GridCapacitorBlockEntity;
import matteroverdrive.blockentity.HoloSignBlockEntity;
import matteroverdrive.blockentity.HolographicStatusPanelBlockEntity;
import matteroverdrive.blockentity.HybridConduitBlockEntity;
import matteroverdrive.blockentity.MatterExcavatorBlockEntity;
import matteroverdrive.blockentity.MatterStorageMatrixBlockEntity;
import matteroverdrive.blockentity.QuantumPowerRelayBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Extra legacy and experimental block-entity registrations. */
public final class ModExtraBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MatterOverdrive.MOD_ID);
    public static final RegistryObject<BlockEntityType<HoloSignBlockEntity>> HOLO_SIGN = BLOCK_ENTITIES.register("holo_sign", () -> BlockEntityType.Builder.of(HoloSignBlockEntity::new, ModBlocks.get("holo_sign").get()).build(null));
    public static final RegistryObject<BlockEntityType<GridCapacitorBlockEntity>> GRID_CAPACITOR = BLOCK_ENTITIES.register("grid_capacitor", () -> BlockEntityType.Builder.of(GridCapacitorBlockEntity::new, ModBlocks.get("grid_capacitor").get()).build(null));
    public static final RegistryObject<BlockEntityType<AndroidInductionRelayBlockEntity>> ANDROID_INDUCTION_RELAY = BLOCK_ENTITIES.register("android_induction_relay", () -> BlockEntityType.Builder.of(AndroidInductionRelayBlockEntity::new, ModBlocks.get("android_induction_relay").get()).build(null));
    public static final RegistryObject<BlockEntityType<QuantumPowerRelayBlockEntity>> QUANTUM_POWER_RELAY = BLOCK_ENTITIES.register("quantum_power_relay", () -> BlockEntityType.Builder.of(QuantumPowerRelayBlockEntity::new, ModBlocks.get("quantum_power_relay").get()).build(null));
    public static final RegistryObject<BlockEntityType<HybridConduitBlockEntity>> HYBRID_CONDUIT = BLOCK_ENTITIES.register("hybrid_conduit", () -> BlockEntityType.Builder.of(HybridConduitBlockEntity::new, ModBlocks.get("hybrid_conduit").get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterStorageMatrixBlockEntity>> MATTER_STORAGE_MATRIX = BLOCK_ENTITIES.register("matter_storage_matrix", () -> BlockEntityType.Builder.of(MatterStorageMatrixBlockEntity::new, ModBlocks.get("matter_storage_matrix").get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterExcavatorBlockEntity>> MATTER_EXCAVATOR = BLOCK_ENTITIES.register("matter_excavator", () -> BlockEntityType.Builder.of(MatterExcavatorBlockEntity::new, ModBlocks.get("matter_excavator").get()).build(null));
    public static final RegistryObject<BlockEntityType<HolographicStatusPanelBlockEntity>> HOLOGRAPHIC_STATUS_PANEL = BLOCK_ENTITIES.register("holographic_status_panel", () -> BlockEntityType.Builder.of(HolographicStatusPanelBlockEntity::new, ModBlocks.get("holographic_status_panel").get()).build(null));
    private ModExtraBlockEntities() {}
}
