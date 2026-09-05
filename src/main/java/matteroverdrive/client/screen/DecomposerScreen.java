package matteroverdrive.client.screen;

import matteroverdrive.menu.DecomposerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class DecomposerScreen extends AbstractContainerScreen<DecomposerMenu> {
    private static final String[] PAGES={"HOME","TASKS","UPGRADES"};
    private int page;

    public DecomposerScreen(DecomposerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title); imageWidth=330; imageHeight=196; inventoryLabelY=103;
    }

    @Override protected void init(){super.init();rebuildButtons();}
    private void rebuildButtons(){
        clearWidgets();
        for(int i=0;i<PAGES.length;i++){final int target=i;Button tab=Button.builder(Component.literal(PAGES[i]),b->{page=target;rebuildButtons();}).bounds(leftPos+176+i*48,topPos+28,45,15).build();tab.active=page!=i;addRenderableWidget(tab);}
        addRenderableWidget(Button.builder(Component.literal("INF FE"),b->{if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1);}).bounds(leftPos+imageWidth-57,topPos+5,52,16).build());
    }

    @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick){renderBackground(graphics);super.render(graphics,mouseX,mouseY,partialTick);renderTooltip(graphics,mouseX,mouseY);}

    @Override protected void renderBg(GuiGraphics graphics,float partialTick,int mouseX,int mouseY){
        int x=leftPos,y=topPos;MachineScreenStyle.drawFrame(graphics,x,y,imageWidth,imageHeight,inventoryLabelY,MachineScreenStyle.CYAN);
        MachineScreenStyle.drawSection(graphics,x+17,y+32,142,62);MachineScreenStyle.drawSection(graphics,x+170,y+25,151,162);
        MachineScreenStyle.drawSlot(graphics,x+25,y+43);MachineScreenStyle.drawSlot(graphics,x+79,y+43);MachineScreenStyle.drawSlot(graphics,x+133,y+43);
        for(int slot=0;slot<4;slot++)MachineScreenStyle.drawSlot(graphics,x+52+slot*18,y+79);
        MachineScreenStyle.drawLegacyProgressArrow(graphics,x+50,y+44,menu.getProgress(),menu.getMaxProgress());
        MachineScreenStyle.drawLegacyEnergyMeter(graphics,x+6,y+30,menu.getEnergy(),menu.getEnergyCapacity());
        MachineScreenStyle.drawLegacyMatterMeter(graphics,x+154,y+30,menu.getMatter(),menu.getMatterCapacity());
    }

    @Override protected void renderLabels(GuiGraphics graphics,int mouseX,int mouseY){
        graphics.drawString(font,title,8,9,MachineScreenStyle.TEXT,false);graphics.drawString(font,playerInventoryTitle,8,inventoryLabelY,MachineScreenStyle.MUTED,false);
        graphics.drawString(font,menu.getEnergy()+" / "+menu.getEnergyCapacity()+" FE",18,29,MachineScreenStyle.MUTED,false);
        graphics.drawString(font,menu.getMatter()+" / "+menu.getMatterCapacity()+" kM",18,70,MachineScreenStyle.MUTED,false);
        int inputMatter=menu.getInputMatterValue();if(inputMatter>0)graphics.drawString(font,inputMatter+" kM | "+menu.getEnergyPerTick()+" FE/t",48,58,MachineScreenStyle.TEXT,false);
        graphics.drawString(font,String.format(Locale.ROOT,"Failure %.4f%%",menu.getFailureChancePercent()),48,68,MachineScreenStyle.DANGER,false);
        switch(page){case 1->renderTasks(graphics);case 2->renderUpgrades(graphics);default->renderHome(graphics);}
    }

    private void renderHome(GuiGraphics g){
        g.drawString(font,"DECOMPOSITION",207,51,MachineScreenStyle.CYAN,false);
        g.drawString(font,"Energy "+menu.getEnergy()+" / "+menu.getEnergyCapacity(),184,71,MachineScreenStyle.TEXT,false);
        g.drawString(font,"Matter "+menu.getMatter()+" / "+menu.getMatterCapacity(),184,86,MachineScreenStyle.BLUE,false);
        g.drawString(font,"Input value "+menu.getInputMatterValue()+" kM",184,101,menu.getInputMatterValue()>0?MachineScreenStyle.GREEN:MachineScreenStyle.MUTED,false);
        g.drawString(font,"Consumption "+menu.getEnergyPerTick()+" FE/t",184,116,MachineScreenStyle.CYAN,false);
        g.drawString(font,String.format(Locale.ROOT,"Failure %.4f%%",menu.getFailureChancePercent()),184,131,MachineScreenStyle.DANGER,false);
        g.drawString(font,"Physical IO + 4 upgrades left",184,151,MachineScreenStyle.MUTED,false);
    }

    private void renderTasks(GuiGraphics g){
        int max=Math.max(1,menu.getMaxProgress()),pct=Math.min(100,menu.getProgress()*100/max);
        g.drawString(font,"ACTIVE TASK",213,51,MachineScreenStyle.CYAN,false);
        g.drawString(font,menu.getInputMatterValue()>0?"Input recognized":"Waiting for valid input",184,72,menu.getInputMatterValue()>0?MachineScreenStyle.GREEN:MachineScreenStyle.AMBER,false);
        g.drawString(font,"Progress "+pct+"%",184,88,pct>0?MachineScreenStyle.GREEN:MachineScreenStyle.MUTED,false);
        g.drawString(font,"Cycle "+menu.getProgress()+" / "+menu.getMaxProgress()+" t",184,103,MachineScreenStyle.TEXT,false);
        g.drawString(font,"Matter yield "+menu.getInputMatterValue()+" kM",184,118,MachineScreenStyle.BLUE,false);
        g.drawString(font,"Energy demand "+menu.getEnergyPerTick()+" FE/t",184,133,MachineScreenStyle.CYAN,false);
        g.drawString(font,"Failures use the real machine roll",184,153,MachineScreenStyle.MUTED,false);
    }

    private void renderUpgrades(GuiGraphics g){
        g.drawString(font,"UPGRADES",220,51,MachineScreenStyle.CYAN,false);
        g.drawString(font,"4 physical upgrade slots",184,72,MachineScreenStyle.TEXT,false);
        g.drawString(font,"Speed: cycle throughput",184,90,MachineScreenStyle.MUTED,false);
        g.drawString(font,"Power: FE efficiency",184,105,MachineScreenStyle.MUTED,false);
        g.drawString(font,"Matter: storage/handling",184,120,MachineScreenStyle.MUTED,false);
        g.drawString(font,"Slots remain usable on all pages",184,145,MachineScreenStyle.CYAN,false);
    }
}
