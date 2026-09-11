package matteroverdrive.client.screen;

import matteroverdrive.menu.ChargingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ChargingStationScreen extends AbstractContainerScreen<ChargingStationMenu> {
    private static final String[] PAGES={"HOME","ANDROID","CONFIG","UPGRADES"};private int page;
    public ChargingStationScreen(ChargingStationMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=330;imageHeight=213;inventoryLabelY=101;}
    @Override protected void init(){super.init();rebuildButtons();}
    private void rebuildButtons(){
        clearWidgets();
        // The right panel is only 145px wide; four single-row tabs made labels collide.
        for(int i=0;i<PAGES.length;i++){
            final int target=i;
            int column=i&1, row=i>>1;
            Button tab=Button.builder(Component.literal(PAGES[i]),b->{page=target;rebuildButtons();})
                    .bounds(leftPos+178+column*72,topPos+27+row*17,68,15).build();
            tab.active=page!=i;
            addRenderableWidget(tab);
        }
        if(page==0)addRenderableWidget(Button.builder(Component.literal("INF FE"),b->clickMenu(1)).bounds(leftPos+257,topPos+72,56,16).build());
        if(page==2)addRenderableWidget(Button.builder(Component.literal("CYCLE RS"),b->clickMenu(2)).bounds(leftPos+210,topPos+74,80,16).build());
    }
    private void clickMenu(int id){if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}
    @Override public void render(GuiGraphics g,int mx,int my,float pt){renderBackground(g);super.render(g,mx,my,pt);renderTooltip(g,mx,my);}
    @Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){int x=leftPos,y=topPos;MachineScreenStyle.drawFrame(g,x,y,imageWidth,imageHeight,inventoryLabelY,MachineScreenStyle.CYAN);MachineScreenStyle.drawSection(g,x+17,y+27,142,64);MachineScreenStyle.drawSection(g,x+176,y+25,145,70);MachineScreenStyle.drawVerticalBar(g,x+8,y+31,7,44,menu.stationEnergy(),menu.stationCapacity(),MachineScreenStyle.BLUE);MachineScreenStyle.drawSlot(g,x+79,y+41);MachineScreenStyle.drawHorizontalBar(g,x+25,y+58,126,5,menu.batteryEnergy(),menu.batteryCapacity(),MachineScreenStyle.CYAN);for(int s=0;s<4;s++)MachineScreenStyle.drawSlot(g,x+52+s*18,y+67);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,9,MachineScreenStyle.TEXT,false);g.drawString(font,"ITEM CHARGING",49,30,MachineScreenStyle.CYAN,false);g.drawString(font,MachineScreenStyle.fit(font,menu.batteryCapacity()>0?menu.batteryEnergy()+" / "+menu.batteryCapacity()+" FE":"Insert rechargeable item",125),27,48,menu.batteryCapacity()>0?MachineScreenStyle.TEXT:MachineScreenStyle.MUTED,false);g.drawString(font,MachineScreenStyle.fit(font,"Item "+menu.lastItemTransferred()+" FE/t",130),25,84,MachineScreenStyle.MUTED,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,MachineScreenStyle.MUTED,false);switch(page){case 1->renderAndroid(g);case 2->renderConfig(g);case 3->renderUpgrades(g);default->renderHome(g);}}
    private void renderHome(GuiGraphics g){g.drawString(font,MachineScreenStyle.fit(font,"CHARGING STATION",132),184,51,MachineScreenStyle.CYAN,false);g.drawString(font,MachineScreenStyle.fit(font,"Buffer "+menu.stationEnergy()+"/"+menu.stationCapacity(),132),184,64,MachineScreenStyle.TEXT,false);g.drawString(font,MachineScreenStyle.fit(font,"Android + item charging active",132),184,78,MachineScreenStyle.MUTED,false);}
    private void renderAndroid(GuiGraphics g){g.drawString(font,"ANDROID + DRONE BAY",186,51,MachineScreenStyle.CYAN,false);g.drawString(font,MachineScreenStyle.fit(font,"Range "+menu.androidRange()+" blocks",132),184,64,MachineScreenStyle.TEXT,false);g.drawString(font,MachineScreenStyle.fit(font,"Androids: "+menu.androidsCharged()+" | "+menu.lastAndroidTransferred()+" FE/t",132),184,75,menu.androidsCharged()>0?MachineScreenStyle.GREEN:MachineScreenStyle.MUTED,false);g.drawString(font,MachineScreenStyle.fit(font,"Drones: "+menu.dronesCharged()+" | "+menu.lastDroneTransferred()+" FE/t",132),184,86,menu.dronesCharged()>0?MachineScreenStyle.GREEN:MachineScreenStyle.MUTED,false);g.drawString(font,MachineScreenStyle.fit(font,"Repair: "+menu.dronesRepaired()+" | "+menu.lastDroneRepairEnergy()+" FE/s",132),184,97,menu.dronesRepaired()>0?MachineScreenStyle.GREEN:MachineScreenStyle.MUTED,false);}
    private void renderConfig(GuiGraphics g){g.drawString(font,"CONFIGURATION",204,51,MachineScreenStyle.CYAN,false);g.drawString(font,MachineScreenStyle.fit(font,"Redstone: "+menu.redstoneModeLabel(),145),184,63,MachineScreenStyle.CYAN,false);g.drawString(font,"Signal gates outgoing charge",184,94,MachineScreenStyle.TEXT,false);g.drawString(font,"but input/buffer charging remains",184,106,MachineScreenStyle.MUTED,false);g.drawString(font,"LOW off | HIGH on | DISABLED",184,119,MachineScreenStyle.MUTED,false);}
    private void renderUpgrades(GuiGraphics g){g.drawString(font,"LEGACY UPGRADES",203,51,MachineScreenStyle.CYAN,false);g.drawString(font,"Range: wireless radius",184,64,MachineScreenStyle.TEXT,false);g.drawString(font,"Power: Android charge rate",184,75,MachineScreenStyle.TEXT,false);g.drawString(font,"Storage: internal FE buffer",184,86,MachineScreenStyle.TEXT,false);}
}
