package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.block.EnvironmentalRegulatorBlock;
import matteroverdrive.block.QuantumFluxReactorBlock;
import matteroverdrive.block.WallTerminalBlock;
import matteroverdrive.blockentity.EnvironmentalRegulatorBlockEntity;
import matteroverdrive.blockentity.QuantumFluxReactorBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** New content isolated from legacy registries so old save IDs remain untouched. */
public final class OverhaulContent {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MatterOverdrive.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MatterOverdrive.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MatterOverdrive.MOD_ID);
    public static final Map<String, RegistryObject<Block>> BLOCKS_BY_ID = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> BLOCK_ITEMS = new LinkedHashMap<>();

    public static final RegistryObject<Block> QUANTUM_FLUX_REACTOR = register("quantum_flux_reactor", () -> new QuantumFluxReactorBlock(machine()));
    public static final RegistryObject<Block> ENVIRONMENTAL_REGULATOR = register("environmental_regulator", () -> new EnvironmentalRegulatorBlock(machine()));
    public static final RegistryObject<Block> TRITANIUM_BULKHEAD_PANEL = register("tritanium_bulkhead_panel", () -> new Block(decor()));
    public static final RegistryObject<Block> HOLOGRAPHIC_FLOOR_PANEL = register("holographic_floor_panel", () -> new Block(decor().lightLevel(s -> 8)));
    public static final RegistryObject<Block> BLUE_HAZARD_PANEL = register("blue_hazard_panel", () -> new Block(decor().lightLevel(s -> 4)));
    public static final RegistryObject<Block> REACTOR_CASING = register("reactor_casing", () -> new Block(machine()));
    public static final RegistryObject<Block> WALL_TERMINAL = register("wall_terminal", () -> new WallTerminalBlock(decor().lightLevel(s -> 6)));
    public static final RegistryObject<Block> INDUSTRIAL_CEILING_GRID = register("industrial_ceiling_grid", () -> new Block(decor().noOcclusion()));

    public static final RegistryObject<BlockEntityType<QuantumFluxReactorBlockEntity>> QUANTUM_FLUX_REACTOR_BE = BLOCK_ENTITIES.register("quantum_flux_reactor",
            () -> BlockEntityType.Builder.of(QuantumFluxReactorBlockEntity::new, QUANTUM_FLUX_REACTOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<EnvironmentalRegulatorBlockEntity>> ENVIRONMENTAL_REGULATOR_BE = BLOCK_ENTITIES.register("environmental_regulator",
            () -> BlockEntityType.Builder.of(EnvironmentalRegulatorBlockEntity::new, ENVIRONMENTAL_REGULATOR.get()).build(null));

    private OverhaulContent() {}

    private static RegistryObject<Block> register(String id, java.util.function.Supplier<Block> factory) {
        RegistryObject<Block> block = BLOCKS.register(id, factory);
        BLOCKS_BY_ID.put(id, block);
        BLOCK_ITEMS.put(id, ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties())));
        return block;
    }

    private static BlockBehaviour.Properties machine() { return BlockBehaviour.Properties.of().strength(5.0F, 15.0F).requiresCorrectToolForDrops().sound(SoundType.METAL); }
    private static BlockBehaviour.Properties decor() { return BlockBehaviour.Properties.of().strength(3.0F, 8.0F).requiresCorrectToolForDrops().sound(SoundType.METAL); }
    public static Map<String, RegistryObject<Block>> blocks() { return Collections.unmodifiableMap(BLOCKS_BY_ID); }
}
