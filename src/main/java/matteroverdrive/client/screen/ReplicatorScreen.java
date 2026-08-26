package matteroverdrive.client.screen;
import matteroverdrive.menu.ReplicatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public class ReplicatorScreen extends AbstractContainerScreen<ReplicatorMenu>{
 public ReplicatorScreen(ReplicatorMenu m,Inventory i,Component t){super(m,i,t);imageWidth=176;imageHeight=166;inventoryLabelY=73;}
 @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g);super.render(g,x,y,p);renderTooltip(g,x,y);}
 @Override protected void renderBg(GuiGraphics g,float p,int mx,int my){int x=leftPos,y=topPos;g.fill(x,y,x+176,y+166,0xFFC6C6C6);for(int[]s:new int[][]{{19,43},{61,43},{115,34},{139,52}}){g.fill(x+s[0],y+s[1],x+s[0]+20,y+s[1]+20,0xFF454545);g.fill(x+s[0]+1,y+s[1]+1,x+s[0]+19,y+s[1]+19,0xFF9A9A9A);}g.fill(x+82,y+48,x+110,y+53,0xFF5B5B5B);g.fill(x+82,y+48,x+82+scale(menu.getProgress(),menu.getMaxProgress(),28),y+53,0xFF2D9DC5);g.fill(x+8,y+20,x+14,y+69,0xFF4A4A4A);int eh=scale(menu.getEnergy(),menu.getEnergyCapacity(),48);g.fill(x+9,y+68-eh,x+13,y+68,0xFFCC3333);g.fill(x+162,y+20,x+168,y+69,0xFF4A4A4A);int mh=scale(menu.getMatter(),menu.getMatterCapacity(),48);g.fill(x+163,y+68-mh,x+167,y+68,0xFF3388CC);}
 @Override protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,6,0x404040,false);if(menu.getNetworkTaskAmount()>0)g.drawString(font,"Network x"+menu.getNetworkTaskAmount(),112,6,0x404040,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY,0x404040,false);g.drawString(font,menu.getEnergy()+" / "+menu.getEnergyCapacity()+" FE",18,20,0x404040,false);g.drawString(font,menu.getMatter()+" / "+menu.getMatterCapacity()+" kM",18,30,0x404040,false);if(menu.getPatternMatter()>0){g.drawString(font,"Pattern "+menu.getPatternProgress()+"%: "+menu.getPatternMatter()+" kM",18,58,0x404040,false);g.drawString(font,menu.getEnergyPerTick()+" FE/t  Fail "+String.format("%.1f",menu.getFailChance()*100)+"%",18,67,0x404040,false);}}
 private static int scale(int v,int max,int p){return max<=0||v<=0?0:Math.min(p,Math.max(1,(int)Math.round((double)v*p/max)));}
}
