package matteroverdrive.event;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.blockentity.EnvironmentalRegulatorBlockEntity;
import matteroverdrive.blockentity.QuantumFluxReactorBlockEntity;
import matteroverdrive.progression.PlayerDiscoveryLog;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-authoritative consequences for technology hazards advertised by the HUD/manual. */
@Mod.EventBusSubscriber(modid=MatterOverdrive.MOD_ID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class EnvironmentalHazardEvents {
    private EnvironmentalHazardEvents(){}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){if(event.phase!=TickEvent.Phase.END||!(event.player instanceof ServerPlayer p)||p.tickCount%20!=0)return;if(EnvironmentalRegulatorBlockEntity.protects(p))return;QuantumFluxReactorBlockEntity reactor=findHotReactor(p);if(reactor==null)return;int heat=reactor.getHeat();p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,50,heat>=9200?1:0,true,false,true));p.addEffect(new MobEffectInstance(MobEffects.CONFUSION,60,0,true,false,true));if(heat>=9200&&p.tickCount%40==0)p.hurt(p.damageSources().magic(),1.0F);PlayerDiscoveryLog.record(p,"hazard:quantum_flux_heat","HAZARD // Quantum flux thermal leakage confirmed. A powered Environmental Regulator suppresses exposure within 12 blocks.");}
    private static QuantumFluxReactorBlockEntity findHotReactor(ServerPlayer p){BlockPos c=p.blockPosition();for(int dx=-8;dx<=8;dx++)for(int dy=-5;dy<=5;dy++)for(int dz=-8;dz<=8;dz++){if(p.level().getBlockEntity(c.offset(dx,dy,dz)) instanceof QuantumFluxReactorBlockEntity r&&r.isHazardous())return r;}return null;}
}
