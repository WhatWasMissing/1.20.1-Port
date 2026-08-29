package matteroverdrive.client.screen;

import matteroverdrive.menu.MatterRecyclerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MatterRecyclerScreen extends AbstractContainerScreen<MatterRecyclerMenu>{
    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("INF FE"), button -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
        }).bounds(leftPos + imageWidth - 72, topPos + 4, 68, 20).build());
    }

    public MatterRecyclerScreen(MatterRecyclerMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=184;inventoryLabelY=91;}
    @Override public void render(GuiGraphics g,int mx,int my,float pt){renderBackground(g);super.render(g,mx,my,pt);renderTooltip(g,mx,my);}
    @Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){int x=leftPos,y=topPos;panel(g,x,y);slot(g,x+25,y+43);slot(g,x+79,y+43);slot(g,x+133,y+43);for(int slot=0;slot<4;slot++)slot(g,x+52+slot*18,y+65);bar(g,x+48,y+48,42,5,menu.getProgress(),menu.getMaxProgress(),0xFF51A86B);vbar(g,x+8,y+20,48,menu.getEnergy(),menu.getEnergyCapacity(),0xFFCC3333);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,6,0x404040,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,0x404040,false);g.drawString(font,menu.getEnergy()+" / "+menu.getEnergyCapacity()+" FE",18,20,0x404040,false);if(menu.getMatter()>0)g.drawString(font,menu.getMatter()+" kM  |  "+menu.getEnergyPerTick()+" FE/t",48,58,0x404040,false);}
    private static void panel(GuiGraphics g,int x,int y){g.fill(x,y,x+176,y+184,0xFFC6C6C6);g.fill(x,y,x+176,y+1,0xFF373737);g.fill(x,y+183,x+176,y+184,0xFF373737);g.fill(x,y,x+1,y+184,0xFF373737);g.fill(x+175,y,x+176,y+184,0xFF373737);}private static void slot(GuiGraphics g,int x,int y){g.fill(x,y,x+20,y+20,0xFF454545);g.fill(x+1,y+1,x+19,y+19,0xFF9A9A9A);}private static void bar(GuiGraphics g,int x,int y,int w,int h,int v,int max,int c){g.fill(x,y,x+w+1,y+h,0xFF5B5B5B);int q=scale(v,max,w);g.fill(x,y,x+q,y+h,c);}private static void vbar(GuiGraphics g,int x,int y,int h,int v,int max,int c){g.fill(x,y,x+6,y+h+1,0xFF4A4A4A);int q=scale(v,max,h);g.fill(x+1,y+h-q,x+5,y+h,c);}private static int scale(int v,int max,int p){return max<=0||v<=0?0:Math.min(p,Math.max(1,(int)Math.round((double)v*p/max)));}
}
