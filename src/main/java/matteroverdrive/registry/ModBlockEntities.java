package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.DecomposerBlockEntity;
import matteroverdrive.blockentity.MatterAnalyzerBlockEntity;
import matteroverdrive.blockentity.MatterRecyclerBlockEntity;
import matteroverdrive.blockentity.ReplicatorBlockEntity;
import matteroverdrive.blockentity.SolarPanelBlockEntity;
import matteroverdrive.blockentity.PatternStorageBlockEntity;
import matteroverdrive.blockentity.PatternMonitorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MatterOverdrive.MOD_ID);
    public static final RegistryObject<BlockEntityType<DecomposerBlockEntity>> DECOMPOSER = BLOCK_ENTITIES.register("decomposer", () -> BlockEntityType.Builder.of(DecomposerBlockEntity::new, ModBlocks.get("decomposer").get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterRecyclerBlockEntity>> MATTER_RECYCLER = BLOCK_ENTITIES.register("matter_recycler", () -> BlockEntityType.Builder.of(MatterRecyclerBlockEntity::new, ModBlocks.get("matter_recycler").get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterAnalyzerBlockEntity>> MATTER_ANALYZER = BLOCK_ENTITIES.register("matter_analyzer", () -> BlockEntityType.Builder.of(MatterAnalyzerBlockEntity::new, ModBlocks.get("matter_analyzer").get()).build(null));
    public static final RegistryObject<BlockEntityType<ReplicatorBlockEntity>> REPLICATOR = BLOCK_ENTITIES.register("replicator", () -> BlockEntityType.Builder.of(ReplicatorBlockEntity::new, ModBlocks.get("replicator").get()).build(null));
    public static final RegistryObject<BlockEntityType<PatternStorageBlockEntity>> PATTERN_STORAGE = BLOCK_ENTITIES.register("pattern_storage", () -> BlockEntityType.Builder.of(PatternStorageBlockEntity::new, ModBlocks.get("pattern_storage").get()).build(null));
    public static final RegistryObject<BlockEntityType<PatternMonitorBlockEntity>> PATTERN_MONITOR = BLOCK_ENTITIES.register("pattern_monitor", () -> BlockEntityType.Builder.of(PatternMonitorBlockEntity::new, ModBlocks.get("pattern_monitor").get()).build(null));
    public static final RegistryObject<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL = BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder.of(SolarPanelBlockEntity::new, ModBlocks.get("solar_panel").get()).build(null));
    private ModBlockEntities() {}
}
