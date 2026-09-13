package matteroverdrive.registry;

import matteroverdrive.MatterOverdrive;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,MatterOverdrive.MOD_ID);
    public static final RegistryObject<CreativeModeTab> MAIN=CREATIVE_TABS.register("main",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.matteroverdrive")).icon(()->new ItemStack(ModItems.get("tritanium_ingot").get())).displayItems((parameters,output)->{ModItems.BLOCK_ITEMS.values().forEach(item->output.accept(item.get()));OverhaulContent.BLOCK_ITEMS.values().forEach(item->output.accept(item.get()));ModItems.STANDALONE_ITEMS.values().forEach(item->output.accept(item.get()));output.accept(ModExoticItems.VEX_MYTHOCLAST.get());ModDestinyItems.WEAPONS.values().forEach(item->output.accept(item.get()));}).build());
    private ModCreativeTabs(){}
}
