package matteroverdrive.client.screen;

import matteroverdrive.menu.SpacetimeAcceleratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.Locale;

public class SpacetimeAcceleratorScreen extends AbstractContainerScreen<SpacetimeAcceleratorMenu> {
    private static final String[] PAGES={"HOME","TASKS","CONFIG","UPGRADES"};
    private int page;
    public SpacetimeAcceleratorScreen(SpacetimeAcceleratorMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=330;imageHeight=184;inventoryLabelY=91;}
    @Override protected void init(){super.init();rebuildButtons();}
    private void rebuildButtons(){
        clearWidgets();
        for(int i=0;i<PAGES.length;i++){final int target=i;Button tab=Button.builder(Component.literal(PAGES[i]),b->{page=target;rebuildButtons();}).bounds(leftPos+178+i*36,topPos+29,34,15).build();tab.active=page!=i;addRenderableWidget(tab);}
        if(page==0){addRenderableWidget(Button.builder(Component.literal("INF FE"),b->clickMenu(1)).bounds(leftPos+188,topPos+70,55,16).build());addRenderableWidget(Button.builder(Component.literal("MAT+"),b->clickMenu(2)).bounds(leftPos+248,topPos+70,55,16).build());}
        if(page==2)addRenderableWidget(Button.builder(Component.literal("CYCLE RS"),b->clickMenu(3)).bounds(leftPos+210,topPos+70,80,16).build());
    }
    private void clickMenu(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override public void render(GuiGraphics g,int mx,int my,float pt){renderBackground(g);super.render(g,mx,my,pt);renderTooltip(g,mx,my);}
    @Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){int x=leftPos,y=topPos;MachineScreenStyle.drawFrame(g,x,y,imageWidth,imageHeight,inventoryLabelY,MachineScreenStyle.PURPLE);MachineScreenStyle.drawSection(g,x+18,y+31,149,54);MachineScreenStyle.drawSection(g,x+176,y+25,145,65);MachineScreenStyle.drawVerticalBar(g,x+8,y+31,7,40,menu.getEnergy(),menu.getEnergyCapacity(),MachineScreenStyle.BLUE);MachineScreenStyle.drawVerticalBar(g,x+16,y+31,7,40,menu.getMatter(),menu.getMatterCapacity(),MachineScreenStyle.PURPLE);MachineScreenStyle.drawHorizontalBar(g,x+28,y+74,87,6,menu.getPulseTimer(),menu.getPulseInterval(),MachineScreenStyle.PURPLE);for(int slot=0;slot<4;slot++)MachineScreenStyle.drawSlot(g,x+52+slot*18,y+64);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,9,MachineScreenStyle.TEXT,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,MachineScreenStyle.MUTED,false);g.drawString(font,"FE "+menu.getEnergy()+"/"+menu.getEnergyCapacity(),28,32,MachineScreenStyle.MUTED,false);g.drawString(font,"Matter "+menu.getMatter()+"/"+menu.getMatterCapacity(),28,42,MachineScreenStyle.MUTED,false);g.drawString(font,"Pulse "+menu.getPulseInterval()+"t  Radius "+menu.getRadius(),28,52,MachineScreenStyle.MUTED,false);String status=menu.isActive()?"Active | "+menu.getEnergyPerTick()+" FE/t | last "+menu.getLastAcceleratedTargets():(menu.isRedstoneBlocked()?"Paused by redstone":"Needs FE + matter");int color=menu.isActive()?MachineScreenStyle.GREEN:(menu.isRedstoneBlocked()?MachineScreenStyle.RED:MachineScreenStyle.AMBER);g.drawString(font,status,28,62,color,false);switch(page){case 1->renderTasks(g);case 2->renderConfig(g);case 3->renderUpgrades(g);default->renderHome(g);}}
    private void renderHome(GuiGraphics g){g.drawString(font,"SPACE-TIME FIELD",201,51,MachineScreenStyle.PURPLE,false);g.drawString(font,menu.isActive()?"FIELD ACTIVE":"FIELD IDLE",188,62,menu.isActive()?MachineScreenStyle.GREEN:MachineScreenStyle.MUTED,false);g.drawString(font,"2r x 2r legacy footprint",188,86,MachineScreenStyle.MUTED,false);}
    private void renderTasks(GuiGraphics g){int interval=Math.max(1,menu.getPulseInterval()),timer=Math.min(interval,Math.max(0,menu.getPulseTimer())),percent=timer*100/interval;g.drawString(font,"ACCELERATION TASK",194,51,MachineScreenStyle.PURPLE,false);g.drawString(font,"Next pulse "+percent+"%",188,64,MachineScreenStyle.TEXT,false);g.drawString(font,"Interval "+interval+" t / "+String.format(Locale.ROOT,"%.2f",interval/20.0D)+" s",188,75,MachineScreenStyle.TEXT,false);g.drawString(font,"Last targets "+menu.getLastAcceleratedTargets(),188,86,MachineScreenStyle.CYAN,false);}
    private void renderConfig(GuiGraphics g){g.drawString(font,"CONFIGURATION",204,51,MachineScreenStyle.PURPLE,false);g.drawString(font,"Redstone: "+menu.getRedstoneModeLabel(),184,63,MachineScreenStyle.CYAN,false);g.drawString(font,"LOW: runs without signal",184,96,MachineScreenStyle.TEXT,false);g.drawString(font,"HIGH: runs with signal",184,108,MachineScreenStyle.TEXT,false);g.drawString(font,"DISABLED: ignores redstone",184,120,MachineScreenStyle.MUTED,false);g.drawString(font,"Old saves remain LOW for compatibility",184,136,MachineScreenStyle.MUTED,false);}
    private void renderUpgrades(GuiGraphics g){g.drawString(font,"1.12 UPGRADE EFFECTS",188,51,MachineScreenStyle.PURPLE,false);g.drawString(font,"Speed: pulse interval",188,63,MachineScreenStyle.TEXT,false);g.drawString(font,"Range: radius (x6 max)",188,74,MachineScreenStyle.TEXT,false);g.drawString(font,"Power/Storage/Matter affect cost",188,85,MachineScreenStyle.MUTED,false);}
}
