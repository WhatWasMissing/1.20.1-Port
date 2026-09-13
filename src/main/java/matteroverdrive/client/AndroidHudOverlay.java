package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.android.AndroidClassAbilities;
import matteroverdrive.android.AndroidData;
import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.android.AndroidUltimates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Compact top-left Android HUD. The lower-left chat area is deliberately kept clear. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AndroidHudOverlay {
    private static final int PANEL_WIDTH = 226;
    private static final int PANEL_HEIGHT = 118;
    private AndroidHudOverlay() {}

    @SubscribeEvent public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance(); if (!AndroidClientState.isActive() || mc.options.hideGui) return;
        GuiGraphics g = event.getGuiGraphics(); int x = 8, y = 8;
        int energy = AndroidClientState.energy(), capacity = Math.max(1, AndroidClientState.energyCapacity());
        int low = Math.max(1, capacity * 15 / 100);
        g.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xB8101820); g.fill(x, y, x + PANEL_WIDTH, y + 2, 0xFF53E6FF);
        AndroidLoadout.Specialization spec = AndroidClientState.specialization();
        g.drawString(mc.font, "ANDROID // " + spec.displayName.toUpperCase(), x + 7, y + 6, 0xFFE8F8FF, false);
        int bx=x+7, by=y+18, bw=PANEL_WIDTH-14; g.fill(bx,by,bx+bw,by+5,0xFF26333D);
        int fill=Math.max(0,Math.min(bw,Math.round(bw*energy/(float)capacity))); g.fill(bx,by,bx+fill,by+5,energy<low?0xFFFF8A2B:0xFF35CFFF);
        g.drawString(mc.font,compact(energy)+" / "+compact(capacity)+" FE",bx,by+7,energy<low?0xFFFFB45B:0xFFB5DFFF,false);
        int level=AndroidClientState.level(); int unspent=Math.max(0, AndroidData.skillPointsForLevel(level)-Long.bitCount(AndroidClientState.selectedPerks()));
        String progress="L"+level+(unspent>0?" // "+unspent+" POINT"+(unspent==1?"":"S"):"")+" // "+coreState();
        g.drawString(mc.font,progress,bx,y+35,coreStateColor(),false);
        g.drawString(mc.font,"LOADOUT // A"+AndroidClientState.aspectCount()+" F"+AndroidClientState.fragmentCount()+" // "+AndroidClientState.artifact().displayName,bx,y+47,0xFFB5C8D2,false);
        int sy=y+62;
        drawAbilitySlot(g,mc,bx,sy,bw,"H",AndroidClassAbilities.classAbilityName(spec),AndroidClientState.classAbilityCooldownTicks(),AndroidClassAbilities.classCooldownTicks(spec),AndroidClassAbilities.classEnergyCost(spec),AndroidClassAbilities.REQUIRED_LEVEL,energy);
        drawAbilitySlot(g,mc,bx,sy+18,bw,"N",AndroidClassAbilities.techAbilityName(spec),AndroidClientState.techAbilityCooldownTicks(),AndroidClassAbilities.techCooldownTicks(spec),AndroidClassAbilities.techEnergyCost(spec),AndroidClassAbilities.REQUIRED_LEVEL,energy);
        drawAbilitySlot(g,mc,bx,sy+36,bw,"G",spec.ultimate.displayName,AndroidClientState.ultimateCooldownTicks(),spec.ultimate.cooldownTicks,spec.ultimate.energyCost,AndroidUltimates.REQUIRED_LEVEL,energy);
    }
    private static String coreState(){if(AndroidClientState.energy()<=0)return"OFFLINE";if(AndroidClientState.isCloakEnabled()&&AndroidClientState.isForceFieldEnabled())return"CLOAK+FIELD";if(AndroidClientState.isForceFieldEnabled())return"FIELD";if(AndroidClientState.isCloakEnabled())return"CLOAK";if(AndroidClientState.cooldownTicks()>0)return String.format("%.1fs",AndroidClientState.cooldownTicks()/20.0D);return"READY";}
    private static int coreStateColor(){if(AndroidClientState.energy()<=0)return 0xFFFF6060;if(AndroidClientState.isCloakEnabled()||AndroidClientState.isForceFieldEnabled())return 0xFF65FF9A;if(AndroidClientState.cooldownTicks()>0)return 0xFFFFB45B;return 0xFFE8F8FF;}
    private static void drawAbilitySlot(GuiGraphics g,Minecraft mc,int x,int y,int width,String key,String name,int cooldown,int baseCooldown,int cost,int required,int energy){boolean locked=AndroidClientState.level()<required,insufficient=!locked&&cooldown<=0&&energy<cost,ready=!locked&&cooldown<=0&&!insufficient;int border=ready?0xFF65FF9A:locked?0xFF626A70:insufficient?0xFFFF6060:0xFFFFB45B;g.fill(x,y,x+width,y+16,0xA018242C);g.fill(x,y,x+2,y+16,border);String status=locked?"LOCK L"+required:cooldown>0?String.format("%.1fs",cooldown/20.0D):insufficient?"NEED "+compact(cost):"READY";String label=key+"  "+name;int maxLabel=Math.max(8,width/6);if(label.length()>maxLabel)label=label.substring(0,maxLabel-1)+"…";g.drawString(mc.font,label,x+5,y+4,0xFFE8F8FF,false);int rx=x+width-mc.font.width(status)-5;g.drawString(mc.font,status,rx,y+4,border,false);}
    private static String compact(int v){return v>=1000?String.format("%.1fk",v/1000.0D):Integer.toString(v);}
}
