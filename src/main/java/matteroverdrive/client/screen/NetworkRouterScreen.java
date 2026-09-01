package matteroverdrive.client.screen;

import matteroverdrive.menu.NetworkRouterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NetworkRouterScreen extends AbstractContainerScreen<NetworkRouterMenu> {
    public NetworkRouterScreen(NetworkRouterMenu menu, Inventory inventory, Component title) { super(menu,inventory,title); imageWidth=176;imageHeight=197;inventoryLabelY=84; }
    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick){renderBackground(graphics);super.render(graphics,mouseX,mouseY,partialTick);renderTooltip(graphics,mouseX,mouseY);}
    @Override protected void renderBg(GuiGraphics graphics,float partialTick,int mouseX,int mouseY){
        MachineScreenStyle.drawFrame(graphics,leftPos,topPos,imageWidth,imageHeight,inventoryLabelY,MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics,leftPos+17,topPos+29,142,40);
        MachineScreenStyle.drawDebugPanel(graphics,leftPos+17,topPos+71,142,13);
        MachineScreenStyle.drawSlot(graphics,leftPos+79,topPos+41);
    }
    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        graphics.drawString(font,title,8,9,MachineScreenStyle.TEXT,false);
        graphics.drawString(font,menu.filtered()?"Whitelist filter":"Any item filter",48,31,MachineScreenStyle.MUTED,false);
        graphics.drawString(font,"Endpoints "+menu.endpoints()+" | Nodes "+menu.nodes()+" | Pylons "+menu.pylons(),20,57,MachineScreenStyle.TEXT,false);
        graphics.drawString(font,"FE "+menu.energy(),20,73,MachineScreenStyle.DEBUG,false);
        graphics.drawString(font,"Moved "+menu.lastMoved()+" item(s)/t",100,73,MachineScreenStyle.DEBUG,false);
        graphics.drawString(font,playerInventoryTitle,8,inventoryLabelY,MachineScreenStyle.MUTED,false);
    }
}