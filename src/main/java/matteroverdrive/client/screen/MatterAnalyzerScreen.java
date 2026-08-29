package matteroverdrive.client.screen;
import matteroverdrive.menu.MatterAnalyzerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class MatterAnalyzerScreen extends AbstractContainerScreen<MatterAnalyzerMenu>{
    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
        }).bounds(leftPos + imageWidth - 72, topPos + 4, 68, 20).build());
    }

 public MatterAnalyzerScreen(MatterAnalyzerMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=196;inventoryLabelY=103;}
 @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g);super.render(g,x,y,p);renderTooltip(g,x,y);}
 @Override protected void renderBg(GuiGraphics g,float p,int mx,int my){int x=leftPos,y=topPos;g.fill(x,y,x+176,y+196,0xFFC6C6C6);for(int sx:new int[]{25,79,133}){g.fill(x+sx,y+43,x+sx+20,y+63,0xFF454545);g.fill(x+sx+1,y+44,x+sx+19,y+62,0xFF9A9A9A);}for(int slot=0;slot<4;slot++){int sx=52+slot*18;g.fill(x+sx,y+79,x+sx+20,y+99,0xFF454545);g.fill(x+sx+1,y+80,x+sx+19,y+98,0xFF9A9A9A);}g.fill(x+48,y+48,x+91,y+53,0xFF5B5B5B);g.fill(x+48,y+48,x+48+scale(menu.getProgress(),menu.getMaxProgress(),42),y+53,0xFFAA66CC);g.fill(x+8,y+20,x+14,y+69,0xFF4A4A4A);int eh=scale(menu.getEnergy(),menu.getEnergyCapacity(),48);g.fill(x+9,y+68-eh,x+13,y+68,0xFFCC3333);}
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,6,0x404040,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,0x404040,false);g.drawString(font,menu.getEnergy()+" / "+menu.getEnergyCapacity()+" FE",18,20,0x404040,false);g.drawString(font,"Pattern: "+menu.getPatternProgress()+"%",96,20,0x404040,false);if(menu.getInputMatter()>0)g.drawString(font,menu.getInputMatter()+" kM  |  "+menu.getEnergyPerTick()+" FE/t",48,58,0x404040,false);
  String cycle=String.format(java.util.Locale.ROOT,"[DEBUG] %dt | %.2fs",menu.getMaxProgress(),menu.getMaxProgress()/20.0D);
  g.drawString(font,cycle,48,68,0x5B236E,false);}
 private static int scale(int v,int max,int p){return max<=0||v<=0?0:Math.min(p,Math.max(1,(int)Math.round((double)v*p/max)));}
}
