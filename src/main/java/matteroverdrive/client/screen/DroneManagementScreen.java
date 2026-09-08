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

    public DroneManagementScreen(List<DroneStatusPacket.DroneInfo> drones) {
        super(Component.literal("DRONE MANAGEMENT"));
        this.drones = List.copyOf(drones);
    }

    @Override
    protected void init() {
        int center = width / 2;
        int controlsY = 54;
        addModeButton("FOLLOW ALL", center - 250, controlsY, DroneEntity.MODE_FOLLOW);
        addModeButton("HOLD ALL", center - 148, controlsY, DroneEntity.MODE_HOLD);
        addModeButton("DEFEND ALL", center - 46, controlsY, DroneEntity.MODE_DEFENSIVE);
        addModeButton("PASSIVE ALL", center + 56, controlsY, DroneEntity.MODE_PASSIVE);
        addModeButton("AGGRESSIVE", center + 158, controlsY, DroneEntity.MODE_AGGRESSIVE);

        int listTop = 92;
        int visible = Math.min(8, drones.size());
        for (int i = 0; i < visible; i++) {
            DroneStatusPacket.DroneInfo info = drones.get(i);
            int rowY = listTop + i * 30;
            addRenderableWidget(Button.builder(Component.literal("MODE"), button ->
                            ModNetwork.CHANNEL.sendToServer(DroneCommandPacket.setOne(info.id(), nextMode(info.mode()))))
                    .bounds(width - 106, rowY + 4, 70, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("REFRESH"), button ->
                        ModNetwork.CHANNEL.sendToServer(DroneCommandPacket.requestStatus()))
                .bounds(18, height - 30, 80, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"), button -> onClose())
                .bounds(width - 98, height - 30, 80, 20).build());
    }

    private void addModeButton(String label, int x, int y, byte mode) {
        addRenderableWidget(Button.builder(Component.literal(label), button ->
                        ModNetwork.CHANNEL.sendToServer(DroneCommandPacket.setAll(mode)))
                .bounds(x, y, 96, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, BACKDROP);
        g.drawString(font, "ANDROID // DRONE COMMAND", 18, 16, TEXT, false);
        g.drawString(font, "Loaded linked drones within 192 blocks · M opens this console", 18, 30, MUTED, false);
        g.drawString(font, "HOLD stops escort movement and keeps the drone at its current position.", 18, 42, GOLD, false);

        int listTop = 92;
        int visible = Math.min(8, drones.size());
        if (drones.isEmpty()) {
            int boxX = Math.max(18, width / 2 - 190);
            g.fill(boxX, listTop, Math.min(width - 18, boxX + 380), listTop + 66, PANEL);
            g.drawString(font, "NO ACTIVE LINKED DRONES FOUND", boxX + 12, listTop + 14, MUTED, false);
            g.drawString(font, "The console reports loaded drones within management range.", boxX + 12, listTop + 32, TEXT, false);
            g.drawString(font, "Move closer to a distant drone and press REFRESH.", boxX + 12, listTop + 44, MUTED, false);
        }

        for (int i = 0; i < visible; i++) {
            DroneStatusPacket.DroneInfo info = drones.get(i);
            int rowY = listTop + i * 30;
            g.fill(18, rowY, width - 18, rowY + 27, PANEL);
            g.fill(18, rowY, 21, rowY + 27, modeColor(info.mode()));
            String label = info.name().isBlank() ? "Drone" : info.name();
            g.drawString(font, label + "  //  " + modeName(info.mode()), 30, rowY + 5, TEXT, false);
            g.drawString(font, "HP " + info.health() + "/" + info.maxHealth() + "   DIST " + info.distance() + "m",
                    30, rowY + 16, info.health() * 3 < info.maxHealth() ? GOLD : MUTED, false);
        }

        if (drones.size() > visible) {
            g.drawString(font, "+" + (drones.size() - visible) + " MORE ACTIVE DRONES", 18,
                    listTop + visible * 30 + 4, MUTED, false);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    private static byte nextMode(byte mode) {
        return switch (mode) {
            case DroneEntity.MODE_FOLLOW -> DroneEntity.MODE_HOLD;
            case DroneEntity.MODE_HOLD -> DroneEntity.MODE_DEFENSIVE;
            case DroneEntity.MODE_DEFENSIVE -> DroneEntity.MODE_PASSIVE;
            case DroneEntity.MODE_PASSIVE -> DroneEntity.MODE_AGGRESSIVE;
            default -> DroneEntity.MODE_FOLLOW;
        };
    }

    private static String modeName(byte mode) {
        return switch (mode) {
            case DroneEntity.MODE_DEFENSIVE -> "DEFENSIVE";
            case DroneEntity.MODE_PASSIVE -> "PASSIVE";
            case DroneEntity.MODE_AGGRESSIVE -> "AGGRESSIVE";
            case DroneEntity.MODE_HOLD -> "HOLD";
            default -> "FOLLOW";
        };
    }

    private static int modeColor(byte mode) {
        return switch (mode) {
            case DroneEntity.MODE_HOLD -> GOLD;
            case DroneEntity.MODE_AGGRESSIVE -> 0xFFE07171;
            case DroneEntity.MODE_DEFENSIVE -> ACCENT;
            case DroneEntity.MODE_PASSIVE -> 0xFF9AA4AB;
            default -> GREEN;
        };
    }
}
