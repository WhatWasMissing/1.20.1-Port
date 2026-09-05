package matteroverdrive.client.screen;

import matteroverdrive.menu.StarMapMenu;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.starmap.StarMapCatalog;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public class StarMapScreen extends AbstractContainerScreen<StarMapMenu> {
    private enum View { GALAXY, QUADRANT, STAR, PLANET }

    private static final int MAP_X = 10;
    private static final int MAP_Y = 25;
    private static final int MAP_W = 260;
    private static final int MAP_H = 64;
    private static final int TRAVEL_X = 200;
    private static final int TRAVEL_Y = 103;
    private static final int TRAVEL_W = 68;
    private static final int TRAVEL_H = 25;

    private View view = View.GALAXY;
    private StarMapCatalog.Quadrant quadrant;
    private StarMapCatalog.Star star;
    private StarMapCatalog.Planet planet;
    private float zoom = 1F;
    private float panX;
    private float panY;
    private double dragX;
    private double dragY;
    private boolean dragging;

    public StarMapScreen(StarMapMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 280;
        imageHeight = 248;
        inventoryLabelY = 137;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineScreenStyle.drawFrame(graphics, leftPos, topPos, imageWidth, imageHeight,
                inventoryLabelY, MachineScreenStyle.CYAN);
        int x = leftPos + MAP_X;
        int y = topPos + MAP_Y;
        graphics.fill(x, y, x + MAP_W, y + MAP_H, 0xEE081117);
        graphics.renderOutline(x, y, MAP_W, MAP_H, MachineScreenStyle.CYAN);

        int cx = x + MAP_W / 2 + Math.round(panX);
        int cy = y + MAP_H / 2 + Math.round(panY);
        switch (view) {
            case GALAXY -> renderGalaxy(graphics, x, y, cx, cy);
            case QUADRANT -> renderQuadrant(graphics, x, y, cx, cy);
            case STAR, PLANET -> renderStarSystem(graphics, x, y, cx, cy);
        }
    }

    private void renderGalaxy(GuiGraphics graphics, int x, int y, int cx, int cy) {
        for (StarMapCatalog.Quadrant q : StarMapCatalog.quadrants()) {
            dot(graphics, x, y, cx + Math.round(q.x() * zoom), cy + Math.round(q.y() * zoom), 4, 0xFFFFD56A);
        }
    }

    private void renderQuadrant(GuiGraphics graphics, int x, int y, int cx, int cy) {
        if (quadrant == null) return;
        for (StarMapCatalog.Star entry : quadrant.stars()) {
            dot(graphics, x, y, cx + Math.round(entry.x() * zoom), cy + Math.round(entry.y() * zoom), 3, 0xFF73E8FF);
        }
    }

    private void renderStarSystem(GuiGraphics graphics, int x, int y, int cx, int cy) {
        if (star == null) return;
        int count = star.planets().size();
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2D * i / Math.max(1, count);
            int radiusX = Math.round((13 + i * 8) * zoom);
            int radiusY = Math.round((8 + i * 5) * zoom);
            StarMapCatalog.Planet entry = star.planets().get(i);
            int color = entry == planet ? 0xFF78FF9B : 0xFFC7DCE3;
            dot(graphics, x, y, cx + (int) Math.round(Math.cos(angle) * radiusX),
                    cy + (int) Math.round(Math.sin(angle) * radiusY), entry == planet ? 4 : 3, color);
        }
        dot(graphics, x, y, cx, cy, 5, 0xFFFFD56A);
    }

    private static void dot(GuiGraphics graphics, int x, int y, int sx, int sy, int size, int color) {
        if (sx > x + 2 && sx < x + MAP_W - 2 && sy > y + 2 && sy < y + MAP_H - 2) {
            graphics.fill(sx - size / 2, sy - size / 2, sx - size / 2 + size, sy - size / 2 + size, color);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, breadcrumb(), 12, 94, MachineScreenStyle.CYAN, false);
        graphics.drawString(font, "Contracts " + menu.active() + " / ready " + menu.complete(),
                171, 94, MachineScreenStyle.MUTED, false);

        if (view == View.PLANET && planet != null) {
            renderPlanetStats(graphics);
        } else {
            graphics.drawString(font, guidance(), 12, 104, MachineScreenStyle.TEXT, false);
            graphics.drawString(font, String.format(Locale.ROOT, "%.1fx | wheel/drag | RMB back", zoom),
                    154, 104, MachineScreenStyle.MUTED, false);
        }
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, MachineScreenStyle.MUTED, false);
    }

    private void renderPlanetStats(GuiGraphics graphics) {
        graphics.drawString(font, planet.type() + " | Orbit " + planet.orbit(), 12, 104, MachineScreenStyle.TEXT, false);
        graphics.drawString(font, "Hab " + planet.habitability() + "%  Temp " + planet.temperature() + " K",
                12, 114, planet.habitability() >= 60 ? MachineScreenStyle.GREEN : MachineScreenStyle.MUTED, false);
        graphics.drawString(font, "Gravity " + planet.gravityPercent() + "%  Moons " + planet.moons()
                        + "  Atmo " + (planet.atmosphere() ? "YES" : "NO"), 12, 124, MachineScreenStyle.CYAN, false);

        int color = menu.traveling() && !menu.encounterResolved()
                ? MachineScreenStyle.AMBER
                : canTravelToSelected() ? MachineScreenStyle.CYAN : MachineScreenStyle.MUTED;
        graphics.fill(TRAVEL_X, TRAVEL_Y, TRAVEL_X + TRAVEL_W, TRAVEL_Y + TRAVEL_H, 0xCC0C171D);
        graphics.renderOutline(TRAVEL_X, TRAVEL_Y, TRAVEL_W, TRAVEL_H, color);
        String top;
        String bottom;
        if (menu.traveling()) {
            top = "EN ROUTE";
            if (!menu.encounterResolved() && menu.encounterRemaining() > 0) {
                bottom = "EVENT " + Math.max(1, (menu.encounterRemaining() + 19) / 20) + "s";
            } else {
                bottom = Math.max(1, (menu.travelRemaining() + 19) / 20) + "s";
            }
        } else if (selectedIsCurrent()) {
            top = "CURRENT";
            bottom = "LOCATION";
        } else {
            top = "TRAVEL";
            int q = StarMapCatalog.quadrantIndex(quadrant);
            int s = StarMapCatalog.starIndex(quadrant, star);
            int p = StarMapCatalog.planetIndex(star, planet);
            int ticks = StarMapCatalog.travelTicks(menu.currentQuadrant(), menu.currentStar(), menu.currentPlanet(), q, s, p);
            bottom = Math.max(1, (ticks + 19) / 20) + "s";
        }
        graphics.drawCenteredString(font, top, TRAVEL_X + TRAVEL_W / 2, TRAVEL_Y + 4, color);
        graphics.drawCenteredString(font, bottom, TRAVEL_X + TRAVEL_W / 2, TRAVEL_Y + 14, MachineScreenStyle.MUTED);
    }

    private boolean selectedIsCurrent() {
        return StarMapCatalog.quadrantIndex(quadrant) == menu.currentQuadrant()
                && StarMapCatalog.starIndex(quadrant, star) == menu.currentStar()
                && StarMapCatalog.planetIndex(star, planet) == menu.currentPlanet();
    }

    private boolean canTravelToSelected() {
        return planet != null && !menu.traveling() && !selectedIsCurrent();
    }

    private String breadcrumb() {
        return switch (view) {
            case GALAXY -> "Galaxy";
            case QUADRANT -> "Galaxy > " + quadrant.name();
            case STAR -> "Galaxy > " + quadrant.name() + " > " + star.name();
            case PLANET -> "Galaxy > " + quadrant.name() + " > " + star.name() + " > " + planet.name();
        };
    }

    private String guidance() {
        return switch (view) {
            case GALAXY -> "Click quadrant";
            case QUADRANT -> "Click star";
            case STAR -> "Click planet | " + star.planets().size() + " planets | " + star.temperature() + " K star";
            case PLANET -> "Planet selected";
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        zoom = Math.max(.45F, Math.min(2.5F, zoom + (float) delta * .15F));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && view == View.PLANET && planet != null
                && mouseX >= leftPos + TRAVEL_X && mouseX < leftPos + TRAVEL_X + TRAVEL_W
                && mouseY >= topPos + TRAVEL_Y && mouseY < topPos + TRAVEL_Y + TRAVEL_H) {
            if (canTravelToSelected()) {
                ModNetwork.requestStarMapTravel(menu.mapPos(), StarMapCatalog.quadrantIndex(quadrant),
                        StarMapCatalog.starIndex(quadrant, star), StarMapCatalog.planetIndex(star, planet));
            }
            return true;
        }
        int x = leftPos + MAP_X;
        int y = topPos + MAP_Y;
        if (mouseX < x || mouseX >= x + MAP_W || mouseY < y || mouseY >= y + MAP_H) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (button == 1) {
            goBack();
            return true;
        }
        if (button == 0) {
            if (select(mouseX, mouseY, x, y)) return true;
            dragging = true;
            dragX = mouseX;
            dragY = mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void goBack() {
        switch (view) {
            case PLANET -> { view = View.STAR; planet = null; }
            case STAR -> { view = View.QUADRANT; star = null; planet = null; }
            case QUADRANT -> { view = View.GALAXY; quadrant = null; }
            case GALAXY -> { }
        }
        resetViewTransform();
    }

    private boolean select(double mouseX, double mouseY, int x, int y) {
        int cx = x + MAP_W / 2 + Math.round(panX);
        int cy = y + MAP_H / 2 + Math.round(panY);
        if (view == View.GALAXY) {
            for (StarMapCatalog.Quadrant q : StarMapCatalog.quadrants()) {
                if (near(mouseX, mouseY, cx + q.x() * zoom, cy + q.y() * zoom)) {
                    quadrant = q; view = View.QUADRANT; resetViewTransform(); return true;
                }
            }
        } else if (view == View.QUADRANT && quadrant != null) {
            for (StarMapCatalog.Star entry : quadrant.stars()) {
                if (near(mouseX, mouseY, cx + entry.x() * zoom, cy + entry.y() * zoom)) {
                    star = entry; view = View.STAR; resetViewTransform(); return true;
                }
            }
        } else if ((view == View.STAR || view == View.PLANET) && star != null) {
            int count = star.planets().size();
            for (int i = 0; i < count; i++) {
                double angle = Math.PI * 2D * i / Math.max(1, count);
                double sx = cx + Math.cos(angle) * (13 + i * 8) * zoom;
                double sy = cy + Math.sin(angle) * (8 + i * 5) * zoom;
                if (near(mouseX, mouseY, sx, sy)) {
                    planet = star.planets().get(i); view = View.PLANET; return true;
                }
            }
        }
        return false;
    }

    private static boolean near(double x, double y, double sx, double sy) {
        return Math.abs(x - sx) <= 6 && Math.abs(y - sy) <= 6;
    }

    private void resetViewTransform() {
        panX = 0; panY = 0; zoom = 1;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragXAmount, double dragYAmount) {
        if (dragging && button == 0) {
            panX = Math.max(-105, Math.min(105, panX + (float) (mouseX - dragX)));
            panY = Math.max(-48, Math.min(48, panY + (float) (mouseY - dragY)));
            dragX = mouseX;
            dragY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragXAmount, dragYAmount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
