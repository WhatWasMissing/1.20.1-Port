package matteroverdrive.client.screen;

import matteroverdrive.entity.DroneEntity;
import matteroverdrive.network.DroneCommandPacket;
import matteroverdrive.network.DroneStatusPacket;
import matteroverdrive.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Operator-facing list and command console for currently loaded linked drones. */
public class DroneManagementScreen extends Screen {
    private static final int BACKDROP = 0xF20A0D11;
    private static final int PANEL = 0xE0161D24;
    private static final int TEXT = 0xFFEAF4F7;
    private static final int MUTED = 0xFF92A5AF;
    private static final int ACCENT = 0xFF73D9E6;
    private static final int GREEN = 0xFF65D990;
    private static final int GOLD = 0xFFE9C46A;

    private final List<DroneStatusPacket.DroneInfo> drones;
    private int listTop;
    private int visibleRows;

    public DroneManagementScreen(List<DroneStatusPacket.DroneInfo> drones) {
        super(Component.literal("DRONE MANAGEMENT"));
        this.drones = List.copyOf(drones);
    }

    @Override
    protected void init() {
        boolean compact = width < 560;
        int gap = 4;
        int columns = width < 290 ? 2 : compact ? 3 : 5;
        int availableWidth = Math.max(columns * 40 + gap * (columns - 1), width - 36);
        int buttonWidth = Math.max(40, Math.min(96, (availableWidth - gap * (columns - 1)) / columns));
        int totalWidth = columns * buttonWidth + (columns - 1) * gap;
        int left = Math.max(8, (width - totalWidth) / 2);
        int controlsY = 54;

        String[] labels = {"FOLLOW ALL", "HOLD ALL", "PATROL ALL", "LOGISTICS ALL", "DEFEND ALL", "PASSIVE ALL", "AGGRESSIVE"};
        byte[] modes = {DroneEntity.MODE_FOLLOW, DroneEntity.MODE_HOLD, DroneEntity.MODE_PATROL,
                DroneEntity.MODE_LOGISTICS, DroneEntity.MODE_DEFENSIVE, DroneEntity.MODE_PASSIVE, DroneEntity.MODE_AGGRESSIVE};
        for (int i = 0; i < labels.length; i++) {
            int row = i / columns;
            int col = i % columns;
            addModeButton(labels[i], left + col * (buttonWidth + gap), controlsY + row * 24, buttonWidth, modes[i]);
        }

        int recallIndex = labels.length;
        int recallRow = recallIndex / columns;
        int recallCol = recallIndex % columns;
        String recallLabel = buttonWidth < 86 ? "RECALL" : "RECALL ALL";
        addRenderableWidget(Button.builder(Component.literal(recallLabel), button ->
                        ModNetwork.CHANNEL.sendToServer(DroneCommandPacket.recallAll()))
                .bounds(left + recallCol * (buttonWidth + gap), controlsY + recallRow * 24, buttonWidth, 20).build());

        int controlRows = (labels.length + 1 + columns - 1) / columns;
        listTop = controlsY + controlRows * 24 + 14;
        visibleRows = Math.max(0, Math.min(8, Math.min(drones.size(), (height - listTop - 42) / 30)));
        for (int i = 0; i < visibleRows; i++) {
            DroneStatusPacket.DroneInfo info = drones.get(i);
            int rowY = listTop + i * 30;
            int modeWidth = width < 430 ? 54 : 70;
            addRenderableWidget(Button.builder(Component.literal(width < 430 ? "MODE" : "CYCLE MODE"), button ->
                            ModNetwork.CHANNEL.sendToServer(DroneCommandPacket.setOne(info.id(), nextMode(info.mode()))))
                    .bounds(width - modeWidth - 18, rowY + 4, modeWidth, 20).build());
        }

        int footerWidth = Math.max(48, Math.min(80, (width - 28) / 2));
        addRenderableWidget(Button.builder(Component.literal("REFRESH"), button ->
                        ModNetwork.CHANNEL.sendToServer(DroneCommandPacket.requestStatus()))
                .bounds(8, height - 30, footerWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"), button -> onClose())
                .bounds(width - footerWidth - 8, height - 30, footerWidth, 20).build());
    }

    private void addModeButton(String label, int x, int y, int buttonWidth, byte mode) {
        String shown = buttonWidth < 86 ? label.replace(" ALL", "") : label;
        addRenderableWidget(Button.builder(Component.literal(shown), button ->
                        ModNetwork.CHANNEL.sendToServer(DroneCommandPacket.setAll(mode)))
                .bounds(x, y, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, BACKDROP);
        g.drawString(font, "ANDROID // DRONE COMMAND", 18, 14, TEXT, false);
        g.drawString(font, fit("Loaded linked drones within 192 blocks · M opens this console", width - 36), 18, 27, MUTED, false);
        g.drawString(font, fit("PATROL guards a 16-block envelope; RECALL returns loaded linked drones safely.", width - 36), 18, 39, GOLD, false);

        if (drones.isEmpty()) {
            int boxX = 18;
            int boxRight = Math.max(boxX + 120, width - 18);
            g.fill(boxX, listTop, boxRight, Math.min(height - 40, listTop + 66), PANEL);
            g.drawString(font, fit("NO ACTIVE LINKED DRONES FOUND", boxRight - boxX - 24), boxX + 12, listTop + 14, MUTED, false);
            g.drawString(font, fit("The console reports loaded drones within management range.", boxRight - boxX - 24), boxX + 12, listTop + 32, TEXT, false);
            g.drawString(font, fit("Move closer to a distant drone and press REFRESH.", boxRight - boxX - 24), boxX + 12, listTop + 44, MUTED, false);
        }

        for (int i = 0; i < visibleRows; i++) {
            DroneStatusPacket.DroneInfo info = drones.get(i);
            int rowY = listTop + i * 30;
            g.fill(18, rowY, width - 18, rowY + 27, PANEL);
            g.fill(18, rowY, 21, rowY + 27, modeColor(info.mode()));
            int textWidth = Math.max(60, width - 150);
            String label = info.name().isBlank() ? "Drone" : info.name();
            g.drawString(font, fit(label + "  //  " + roleName(info.role()) + "  //  " + modeName(info.mode()), textWidth), 30, rowY + 5, TEXT, false);
            String position = info.mode() == DroneEntity.MODE_PATROL && info.patrolAnchor() != null
                    ? "   ANCHOR " + info.patrolAnchor().getX() + " " + info.patrolAnchor().getY() + " " + info.patrolAnchor().getZ()
                    : info.mode() == DroneEntity.MODE_LOGISTICS && info.logisticsTarget() != null
                    ? "   ROUTE " + info.logisticsTarget().getX() + " " + info.logisticsTarget().getY() + " " + info.logisticsTarget().getZ()
                    : "   DIST " + info.distance() + "m";
            g.drawString(font, fit("HP " + info.health() + "/" + info.maxHealth() + "   FE " + info.energy() + "/" + info.maxEnergy() + position, textWidth),
                    30, rowY + 16, info.health() * 3 < info.maxHealth() ? GOLD : MUTED, false);
        }

        if (drones.size() > visibleRows && listTop + visibleRows * 30 + 10 < height - 32) {
            g.drawString(font, fit("+" + (drones.size() - visibleRows) + " MORE ACTIVE DRONES", width - 36), 18,
                    listTop + visibleRows * 30 + 4, MUTED, false);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private String fit(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        int available = Math.max(0, maxWidth - font.width("…"));
        return font.plainSubstrByWidth(text, available) + "…";
    }

    private static byte nextMode(byte mode) {
        return switch (mode) {
            case DroneEntity.MODE_FOLLOW -> DroneEntity.MODE_HOLD;
            case DroneEntity.MODE_HOLD -> DroneEntity.MODE_DEFENSIVE;
            case DroneEntity.MODE_DEFENSIVE -> DroneEntity.MODE_PASSIVE;
            case DroneEntity.MODE_PASSIVE -> DroneEntity.MODE_AGGRESSIVE;
            case DroneEntity.MODE_AGGRESSIVE -> DroneEntity.MODE_PATROL;
            case DroneEntity.MODE_PATROL -> DroneEntity.MODE_LOGISTICS;
            default -> DroneEntity.MODE_FOLLOW;
        };
    }

    private static String modeName(byte mode) {
        return switch (mode) {
            case DroneEntity.MODE_DEFENSIVE -> "DEFENSIVE";
            case DroneEntity.MODE_PASSIVE -> "PASSIVE";
            case DroneEntity.MODE_AGGRESSIVE -> "AGGRESSIVE";
            case DroneEntity.MODE_HOLD -> "HOLD";
            case DroneEntity.MODE_PATROL -> "PATROL";
            case DroneEntity.MODE_LOGISTICS -> "LOGISTICS";
            default -> "FOLLOW";
        };
    }

    private static String roleName(byte role) {
        return switch (role) {
            case DroneEntity.ROLE_REPAIR -> "REPAIR";
            case DroneEntity.ROLE_LOGISTICS -> "LOGISTICS";
            case DroneEntity.ROLE_SURVEY -> "SURVEY";
            case DroneEntity.ROLE_REACTOR_MAINTENANCE -> "REACTOR";
            default -> "COMBAT";
        };
    }

    private static int modeColor(byte mode) {
        return switch (mode) {
            case DroneEntity.MODE_HOLD -> GOLD;
            case DroneEntity.MODE_PATROL -> 0xFFB58AF0;
            case DroneEntity.MODE_LOGISTICS -> 0xFFFFB86B;
            case DroneEntity.MODE_AGGRESSIVE -> 0xFFE07171;
            case DroneEntity.MODE_DEFENSIVE -> ACCENT;
            case DroneEntity.MODE_PASSIVE -> 0xFF9AA4AB;
            default -> GREEN;
        };
    }
}
