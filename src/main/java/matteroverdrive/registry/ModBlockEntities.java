package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.DecomposerBlockEntity;
import matteroverdrive.blockentity.MatterAnalyzerBlockEntity;
import matteroverdrive.blockentity.InscriberBlockEntity;
import matteroverdrive.blockentity.MatterRecyclerBlockEntity;
import matteroverdrive.blockentity.ReplicatorBlockEntity;
import matteroverdrive.blockentity.SolarPanelBlockEntity;
import matteroverdrive.blockentity.TritaniumCrateBlockEntity;
import matteroverdrive.blockentity.PatternStorageBlockEntity;
import matteroverdrive.blockentity.PatternMonitorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MatterOverdrive.MOD_ID);
    public static final RegistryObject<BlockEntityType<InscriberBlockEntity>> INSCRIBER = BLOCK_ENTITIES.register("inscriber", () -> BlockEntityType.Builder.of(InscriberBlockEntity::new, ModBlocks.get("inscriber").get()).build(null));
    public static final RegistryObject<BlockEntityType<DecomposerBlockEntity>> DECOMPOSER = BLOCK_ENTITIES.register("decomposer", () -> BlockEntityType.Builder.of(DecomposerBlockEntity::new, ModBlocks.get("decomposer").get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterRecyclerBlockEntity>> MATTER_RECYCLER = BLOCK_ENTITIES.register("matter_recycler", () -> BlockEntityType.Builder.of(MatterRecyclerBlockEntity::new, ModBlocks.get("matter_recycler").get()).build(null));
    public static final RegistryObject<BlockEntityType<MatterAnalyzerBlockEntity>> MATTER_ANALYZER = BLOCK_ENTITIES.register("matter_analyzer", () -> BlockEntityType.Builder.of(MatterAnalyzerBlockEntity::new, ModBlocks.get("matter_analyzer").get()).build(null));
    public static final RegistryObject<BlockEntityType<ReplicatorBlockEntity>> REPLICATOR = BLOCK_ENTITIES.register("replicator", () -> BlockEntityType.Builder.of(ReplicatorBlockEntity::new, ModBlocks.get("replicator").get()).build(null));
    public static final RegistryObject<BlockEntityType<PatternStorageBlockEntity>> PATTERN_STORAGE = BLOCK_ENTITIES.register("pattern_storage", () -> BlockEntityType.Builder.of(PatternStorageBlockEntity::new, ModBlocks.get("pattern_storage").get()).build(null));
    public static final RegistryObject<BlockEntityType<PatternMonitorBlockEntity>> PATTERN_MONITOR = BLOCK_ENTITIES.register("pattern_monitor", () -> BlockEntityType.Builder.of(PatternMonitorBlockEntity::new, ModBlocks.get("pattern_monitor").get()).build(null));
    public static final RegistryObject<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL = BLOCK_ENTITIES.register("solar_panel", () -> BlockEntityType.Builder.of(SolarPanelBlockEntity::new, ModBlocks.get("solar_panel").get()).build(null));
    public static final RegistryObject<BlockEntityType<TritaniumCrateBlockEntity>> TRITANIUM_CRATE = BLOCK_ENTITIES.register("tritanium_crate", () -> BlockEntityType.Builder.of(TritaniumCrateBlockEntity::new,
            ModBlocks.get("tritanium_crate").get(), ModBlocks.get("tritanium_crate_black").get(), ModBlocks.get("tritanium_crate_blue").get(), ModBlocks.get("tritanium_crate_brown").get(),
            ModBlocks.get("tritanium_crate_cyan").get(), ModBlocks.get("tritanium_crate_gray").get(), ModBlocks.get("tritanium_crate_green").get(), ModBlocks.get("tritanium_crate_light_blue").get(),
            ModBlocks.get("tritanium_crate_lime").get(), ModBlocks.get("tritanium_crate_magenta").get(), ModBlocks.get("tritanium_crate_orange").get(), ModBlocks.get("tritanium_crate_pink").get(),
            ModBlocks.get("tritanium_crate_purple").get(), ModBlocks.get("tritanium_crate_red").get(), ModBlocks.get("tritanium_crate_silver").get(), ModBlocks.get("tritanium_crate_white").get(),
            ModBlocks.get("tritanium_crate_yellow").get()).build(null));
    private ModBlockEntities() {}
}
