package matteroverdrive.client.screen;

import matteroverdrive.menu.StarMapMenu;
import matteroverdrive.starmap.StarMapCatalog;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class StarMapScreen extends AbstractContainerScreen<StarMapMenu> {
    private enum View { GALAXY, QUADRANT, STAR }
    private View view=View.GALAXY; private StarMapCatalog.Quadrant quadrant; private StarMapCatalog.Star star;
    private float zoom=1F,panX,panY; private double dragX,dragY; private boolean dragging;
    public StarMapScreen(StarMapMenu m,Inventory i,Component t){super(m,i,t);imageWidth=222;imageHeight=196;inventoryLabelY=83;}
    @Override public void render(GuiGraphics g,int mx,int my,float pt){renderBackground(g);super.render(g,mx,my,pt);renderTooltip(g,mx,my);}
    @Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){MachineScreenStyle.drawFrame(g,leftPos,topPos,imageWidth,imageHeight,inventoryLabelY,MachineScreenStyle.CYAN);int x=leftPos+10,y=topPos+25,w=202,h=52;g.fill(x,y,x+w,y+h,0xEE081117);g.renderOutline(x,y,w,h,MachineScreenStyle.CYAN);int cx=x+w/2+Math.round(panX),cy=y+h/2+Math.round(panY);if(view==View.GALAXY){for(var q:StarMapCatalog.quadrants())dot(g,x,y,w,h,cx+Math.round(q.x()*zoom),cy+Math.round(q.y()*zoom),3,0xFFFFD56A);}else if(view==View.QUADRANT&&quadrant!=null){for(var s:quadrant.stars())dot(g,x,y,w,h,cx+Math.round(s.x()*zoom),cy+Math.round(s.y()*zoom),2,0xFF73E8FF);}else if(view==View.STAR&&star!=null){int c=star.planets().size();for(int i=0;i<c;i++){double a=Math.PI*2*i/c;dot(g,x,y,w,h,cx+(int)(Math.cos(a)*(10+i*5)*zoom),cy+(int)(Math.sin(a)*(7+i*3)*zoom),2,0xFFC7DCE3);}dot(g,x,y,w,h,cx,cy,3,0xFFFFD56A);}}
    private static void dot(GuiGraphics g,int x,int y,int w,int h,int sx,int sy,int s,int c){if(sx>x+2&&sx<x+w-2&&sy>y+2&&sy<y+h-2)g.fill(sx,sy,sx+s,sy+s,c);}
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,title,8,9,MachineScreenStyle.TEXT,false);String b=view==View.GALAXY?"Galaxy":view==View.QUADRANT?"Galaxy > "+quadrant.name():"Galaxy > "+quadrant.name()+" > "+star.name();g.drawString(font,b,12,81,MachineScreenStyle.CYAN,false);String d=view==View.GALAXY?"Click quadrant":view==View.QUADRANT?"Click star":star.planets().size()+" planets | "+star.temperature()+" K";g.drawString(font,d,12,92,MachineScreenStyle.TEXT,false);g.drawString(font,"Contracts "+menu.active()+" / ready "+menu.complete(),112,81,MachineScreenStyle.MUTED,false);g.drawString(font,String.format("%.1fx | wheel/drag | RMB back",zoom),95,92,MachineScreenStyle.MUTED,false);g.drawString(font,playerInventoryTitle,8,inventoryLabelY+28,MachineScreenStyle.MUTED,false);}
    @Override public boolean mouseScrolled(double mx,double my,double d){zoom=Math.max(.45F,Math.min(2.5F,zoom+(float)d*.15F));return true;}
    @Override public boolean mouseClicked(double mx,double my,int b){int x=leftPos+10,y=topPos+25;if(mx<x||mx>=x+202||my<y||my>=y+52)return super.mouseClicked(mx,my,b);if(b==1){if(view==View.STAR){view=View.QUADRANT;star=null;}else if(view==View.QUADRANT){view=View.GALAXY;quadrant=null;}reset();return true;}if(b==0){if(select(mx,my,x,y))return true;dragging=true;dragX=mx;dragY=my;return true;}return super.mouseClicked(mx,my,b);}
    private boolean select(double mx,double my,int x,int y){int cx=x+101+Math.round(panX),cy=y+26+Math.round(panY);if(view==View.GALAXY){for(var q:StarMapCatalog.quadrants())if(near(mx,my,cx+q.x()*zoom,cy+q.y()*zoom)){quadrant=q;view=View.QUADRANT;reset();return true;}}else if(view==View.QUADRANT&&quadrant!=null){for(var s:quadrant.stars())if(near(mx,my,cx+s.x()*zoom,cy+s.y()*zoom)){star=s;view=View.STAR;reset();return true;}}return false;}
    private static boolean near(double x,double y,double sx,double sy){return Math.abs(x-sx)<=5&&Math.abs(y-sy)<=5;} private void reset(){panX=panY=0;zoom=1;}
    @Override public boolean mouseDragged(double mx,double my,int b,double dx,double dy){if(dragging&&b==0){panX=Math.max(-80,Math.min(80,panX+(float)(mx-dragX)));panY=Math.max(-40,Math.min(40,panY+(float)(my-dragY)));dragX=mx;dragY=my;return true;}return super.mouseDragged(mx,my,b,dx,dy);}
    @Override public boolean mouseReleased(double mx,double my,int b){if(b==0)dragging=false;return super.mouseReleased(mx,my,b);}
}
