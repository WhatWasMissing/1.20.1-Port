package matteroverdrive.client.screen;

import matteroverdrive.menu.MatterAnalyzerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MatterAnalyzerScreen extends AbstractContainerScreen<MatterAnalyzerMenu> {
 private static final ResourceLocation SCAN=new ResourceLocation("matteroverdrive","textures/gui/elements/screen.png");private final float[] bars=new float[26];
 public MatterAnalyzerScreen(MatterAnalyzerMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=196;inventoryLabelY=103;}
 @Override protected void init(){super.init();addRenderableWidget(Button.builder(Component.literal("INF FE"),b->{if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1);}).bounds(leftPos+119,topPos+5,52,16).build());addRenderableWidget(Button.builder(Component.literal("RS"),b->{if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,2);}).bounds(leftPos+95,topPos+5,20,16).build());}
 @Override public void render(GuiGraphics g,int mx,int my,float pt){renderBackground(g);super.render(g,mx,my,pt);renderTooltip(g,mx,my);}@Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){int x=leftPos,y=topPos;MachineScreenStyle.drawFrame(g,x,y,imageWidth,imageHeight,inventoryLabelY,MachineScreenStyle.PURPLE);g.blit(SCAN,x+30,y+28,0,0,117,47,117,47);drawWave(g,x+37,y+36,menu.getProgress(),menu.getMaxProgress(),menu.getInputMatter());MachineScreenStyle.drawSlot(g,x+25,y+43);MachineScreenStyle.drawSlot(g,x+79,y+43);MachineScreenStyle.drawSlot(g,x+133,y+43);for(int s=0;s<4;s++)MachineScreenStyle.drawSlot(g,x+52+s*18,y+79);MachineScreenStyle.drawVerticalBar(g,x+8,y+30,7,40,menu.getEnergy(),menu.getEnergyCapacity(),MachineScreenStyle.RED);}
 private void drawWave(GuiGraphics g,int x,int y,int v,int max,int seed){float p=max<=0?0:Math.min(1F,(float)v/max);int active=(int)Math.floor(p*26F);for(int i=0;i<26;i++){float target=0;if(i<active){double n=Math.sin((seed+17)*.019+i*.73)+Math.sin((seed+3)*.007+i*1.91)*.5;target=(float)Math.max(.08,Math.min(1,(n+1.5)/3));}bars[i]+=(target-bars[i])*.12F;int h=Math.round(bars[i]*30);if(h>0){int bx=x+i*4;g.fill(bx,y+31-h,bx+2,y+31,0xFFBFE4E6);g.fill(bx,y+29-h,bx+2,y+30-h,0xFF73E8FF);}}}
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,9,MachineScreenStyle.TEXT,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,MachineScreenStyle.MUTED,false);String rs=switch(menu.getRedstoneMode()){case 0->"LOW";case 1->"HIGH";default->"NONE";};g.drawString(font,"RS "+rs,55,9,MachineScreenStyle.CYAN,false);g.drawString(font,menu.getEnergy()+" / "+menu.getEnergyCapacity()+" FE",18,29,MachineScreenStyle.MUTED,false);g.drawString(font,"Pattern "+menu.getPatternProgress()+"%",104,76,MachineScreenStyle.PURPLE,false);if(menu.getInputMatter()>0)g.drawString(font,menu.getInputMatter()+" kM | "+menu.getEnergyPerTick()+" FE/t",18,87,MachineScreenStyle.TEXT,false);}
}
