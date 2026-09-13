package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.registry.OverhaulContent;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=MatterOverdrive.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class OverhaulClientEntityEvents {
    private OverhaulClientEntityEvents(){}
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(OverhaulContent.FIELD_SCIENTIST.get(),ctx->cast(new VillagerSkinRenderer(ctx,"field_scientist")));e.registerEntityRenderer(OverhaulContent.SYSTEMS_ENGINEER.get(),ctx->cast(new VillagerSkinRenderer(ctx,"systems_engineer")));e.registerEntityRenderer(OverhaulContent.ASSIMILATOR.get(),ctx->cast(new ZombieSkinRenderer(ctx,"assimilator")));e.registerEntityRenderer(OverhaulContent.PHASE_STALKER.get(),ctx->cast(new ZombieSkinRenderer(ctx,"phase_stalker")));}
    @SuppressWarnings("unchecked") private static <T extends net.minecraft.world.entity.Entity> EntityRenderer<T> cast(EntityRenderer<?> renderer){return(EntityRenderer<T>)renderer;}
    private static final class VillagerSkinRenderer extends VillagerRenderer{private final ResourceLocation skin;private VillagerSkinRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context ctx,String id){super(ctx);skin=new ResourceLocation(MatterOverdrive.MOD_ID,"textures/entity/"+id+".png");}@Override public ResourceLocation getTextureLocation(Villager entity){return skin;}}
    private static final class ZombieSkinRenderer extends ZombieRenderer{private final ResourceLocation skin;private ZombieSkinRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context ctx,String id){super(ctx);skin=new ResourceLocation(MatterOverdrive.MOD_ID,"textures/entity/"+id+".png");}@Override public ResourceLocation getTextureLocation(Zombie entity){return skin;}}
}
