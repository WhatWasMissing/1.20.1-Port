package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.HoloSignBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Extra legacy block-entity registrations added during the parity pass. */
public final class ModExtraBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MatterOverdrive.MOD_ID);

    public static final RegistryObject<BlockEntityType<HoloSignBlockEntity>> HOLO_SIGN =
            BLOCK_ENTITIES.register("holo_sign", () -> BlockEntityType.Builder
                    .of(HoloSignBlockEntity::new, ModBlocks.get("holo_sign").get())
                    .build(null));

    private ModExtraBlockEntities() {}
}
