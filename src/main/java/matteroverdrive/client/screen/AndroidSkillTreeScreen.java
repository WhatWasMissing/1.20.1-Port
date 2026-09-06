package matteroverdrive.client.screen;

import matteroverdrive.android.AndroidData;
import matteroverdrive.client.AndroidClientState;
import matteroverdrive.network.AndroidPerkSelectPacket;
import matteroverdrive.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** A clean constellation-style Android progression screen inspired by modern sci-fi loadout UIs. */
public class AndroidSkillTreeScreen extends Screen {
    private static final int BACKDROP = 0xF40A0C10;
    private static final int PANEL = 0xD9161B22;
    private static final int PANEL_LIGHT = 0xCC202731;
    private static final int TEXT = 0xFFE7E9EC;
    private static final int MUTED = 0xFF8E98A5;
    private static final int ACCENT = 0xFF67D9E8;
    private static final int GOLD = 0xFFE9C46A;
    private static final int SELECTED = 0xFFF5F7FA;
    private static final int LOCKED = 0xFF414852;
    private static final int DANGER = 0xFFE07A5F;

    private final Map<Button, AndroidData.Perk> perkButtons = new LinkedHashMap<>();
    private long displayedPerks;
    private int displayedLevel;
    private int pendingPerk = -1;
    private boolean pendingRefund;
    private boolean resetArmed;

    public AndroidSkillTreeScreen() { super(Component.literal("ANDROID SUBSYSTEM")); }

    @Override
    protected void init() {
        displayedPerks = AndroidClientState.selectedPerks();
        displayedLevel = AndroidClientState.level();
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        perkButtons.clear();

        int treeLeft = Math.max(130, width / 2 - 300);
        int treeRight = Math.min(width - 210, width / 2 + 300);
        int usable = Math.max(300, treeRight - treeLeft);
        int step = Math.max(32, usable / 9);
        int[] rows = {height / 2 - 72, height / 2, height / 2 + 72};

        for (AndroidData.Perk perk : AndroidData.Perk.values()) {
            int x = treeLeft + (perk.level - 2) * step - 14;
            int y = rows[Math.max(0, Math.min(2, perk.branch))] - 14;
            boolean selected = AndroidClientState.hasPerk(perk);
            boolean pending = pendingPerk == perk.ordinal();
            boolean affordable = AndroidClientState.isActive()
                    && AndroidClientState.level() >= perk.level
                    && availablePoints() > 0;

            Component glyph = Component.literal(selected ? "✦" : pending ? "◆" : "◇");
            Button node = Button.builder(glyph, button -> {
                pendingPerk = perk.ordinal();
                pendingRefund = selected;
                resetArmed = false;
                rebuild();
            }).bounds(x, y, 28, 28).build();
            node.active = selected || affordable;
            addRenderableWidget(node);
            perkButtons.put(node, perk);
        }

        int footerY = height - 31;
        Button reset = Button.builder(Component.literal(resetArmed ? "CONFIRM WIPE · 50K FE" : "RESET BUILD"), button -> {
            if (resetArmed) {
                ModNetwork.CHANNEL.sendToServer(new AndroidPerkSelectPacket(-1));
                resetArmed = false;
            } else {
                resetArmed = true;
            }
            pendingPerk = -1;
            pendingRefund = false;
            rebuild();
        }).bounds(18, footerY, 150, 20).build();
        reset.active = AndroidClientState.selectedPerks() != 0L;
        addRenderableWidget(reset);

        Button confirm = Button.builder(Component.literal(pendingPerk < 0 ? "SELECT NODE"
                        : pendingRefund ? "REFUND · 10K FE" : "UNLOCK NODE"), button -> {
            if (pendingPerk >= 0) {
                int request = pendingRefund ? -2 - pendingPerk : pendingPerk;
                ModNetwork.CHANNEL.sendToServer(new AndroidPerkSelectPacket(request));
                pendingPerk = -1;
                pendingRefund = false;
                rebuild();
            }
        }).bounds(width / 2 - 65, footerY, 130, 20).build();
        confirm.active = pendingPerk >= 0;
        addRenderableWidget(confirm);

        addRenderableWidget(Button.builder(Component.literal("CLOSE"), b -> onClose())
                .bounds(width - 98, footerY, 80, 20).build());
    }

    private int availablePoints() {
        return Math.max(0, AndroidData.skillPointsForLevel(AndroidClientState.level())
                - Long.bitCount(AndroidClientState.selectedPerks()));
    }

    @Override
    public void tick() {
        if (displayedPerks != AndroidClientState.selectedPerks() || displayedLevel != AndroidClientState.level()) {
            displayedPerks = AndroidClientState.selectedPerks();
            displayedLevel = AndroidClientState.level();
            rebuild();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, BACKDROP);
        drawAtmosphere(g);

        int level = AndroidClientState.level();
        int points = availablePoints();
        int currentXp = AndroidClientState.experience();
        int levelStart = AndroidData.experienceForLevel(level);
        int levelEnd = level >= AndroidData.MAX_LEVEL ? levelStart : AndroidData.experienceForLevel(level + 1);
        int progress = level >= AndroidData.MAX_LEVEL ? 100 : Math.max(0, Math.min(100,
                (int)(100.0 * (currentXp - levelStart) / Math.max(1, levelEnd - levelStart))));

        g.drawString(font, "ANDROID // SYNTHETIC ASCENSION", 18, 16, TEXT, false);
        g.drawString(font, "SUBSYSTEM CALIBRATION", 18, 29, MUTED, false);
        g.drawString(font, "POWER " + AndroidClientState.energy() + " / " + AndroidData.ENERGY_CAPACITY,
                width - 190, 17, ACCENT, false);

        g.fill(18, 46, width - 18, 47, 0x55FFFFFF);
        g.drawString(font, "LEVEL", 18, 59, MUTED, false);
        g.drawString(font, Integer.toString(level), 18, 72, TEXT, false);
        g.drawString(font, "ASCENSION POINTS", 72, 59, MUTED, false);
        g.drawString(font, points + " AVAILABLE", 72, 72, points > 0 ? GOLD : TEXT, false);
        g.drawString(font, level >= AndroidData.MAX_LEVEL ? "MAXIMUM SYNTHESIS" : currentXp + " / " + levelEnd + " XP",
                205, 72, MUTED, false);
        g.fill(205, 60, width - 210, 64, 0x553B424C);
        g.fill(205, 60, 205 + (width - 415) * progress / 100, 64, ACCENT);

        int treeLeft = Math.max(130, width / 2 - 300);
        int treeRight = Math.min(width - 210, width / 2 + 300);
        int usable = Math.max(300, treeRight - treeLeft);
        int step = Math.max(32, usable / 9);
        int[] rows = {height / 2 - 72, height / 2, height / 2 + 72};
        String[] branchNames = {"ASSAULT", "CHASSIS", "UTILITY"};
        String[] branchSub = {"OFFENSIVE SYSTEMS", "SURVIVABILITY", "COGNITION & SUPPORT"};

        for (int branch = 0; branch < 3; branch++) {
            int y = rows[branch];
            g.drawString(font, branchNames[branch], 18, y - 9, branch == 0 ? DANGER : branch == 1 ? GOLD : ACCENT, false);
            g.drawString(font, branchSub[branch], 18, y + 4, MUTED, false);
            g.fill(treeLeft, y - 1, treeRight, y + 1, 0x66737D88);
            for (int levelGate = 2; levelGate <= 10; levelGate++) {
                int x = treeLeft + (levelGate - 2) * step;
                boolean unlocked = level >= levelGate;
                g.fill(x - 2, y - 2, x + 2, y + 2, unlocked ? ACCENT : LOCKED);
            }
        }

        for (int levelGate = 2; levelGate <= 10; levelGate++) {
            int x = treeLeft + (levelGate - 2) * step;
            g.drawCenteredString(font, Integer.toString(levelGate), x, rows[0] - 32, level >= levelGate ? TEXT : LOCKED);
        }

        super.render(g, mouseX, mouseY, partialTick);
        renderInspectionPanel(g, mouseX, mouseY);
    }

    private void drawAtmosphere(GuiGraphics g) {
        g.fill(width - 260, 48, width - 18, height - 42, PANEL);
        g.fill(width - 257, 51, width - 21, height - 45, 0x331AEEF2);
        for (int x = 0; x < width; x += 48) g.fill(x, 0, x + 1, height, 0x111D2834);
        for (int y = 96; y < height - 40; y += 48) g.fill(0, y, width, y + 1, 0x111D2834);
    }

    private void renderInspectionPanel(GuiGraphics g, int mouseX, int mouseY) {
        AndroidData.Perk inspected = pendingPerk >= 0 ? AndroidData.Perk.values()[pendingPerk] : null;
        for (Map.Entry<Button, AndroidData.Perk> entry : perkButtons.entrySet()) {
            if (entry.getKey().isHoveredOrFocused()) { inspected = entry.getValue(); break; }
        }

        int x = width - 244;
        int y = 110;
        g.drawString(font, "NODE INSPECTION", x, y, MUTED, false);
        if (inspected == null) {
            g.drawString(font, "Select a subsystem node", x, y + 22, TEXT, false);
            g.drawString(font, "to inspect its function.", x, y + 34, MUTED, false);
            g.drawString(font, "One point is awarded", x, y + 62, MUTED, false);
            g.drawString(font, "every two Android levels.", x, y + 74, MUTED, false);
            g.drawString(font, "Maximum build: 5 perks.", x, y + 86, GOLD, false);
            return;
        }

        boolean owned = AndroidClientState.hasPerk(inspected);
        boolean levelLocked = AndroidClientState.level() < inspected.level;
        g.drawString(font, inspected.displayName.toUpperCase(), x, y + 22, owned ? SELECTED : GOLD, false);
        g.drawString(font, "TIER " + inspected.level + " · " + (owned ? "INSTALLED" : levelLocked ? "LOCKED" : "AVAILABLE"),
                x, y + 36, owned ? ACCENT : levelLocked ? LOCKED : TEXT, false);
        drawWrapped(g, inspected.description, x, y + 58, 220, TEXT);

        int infoY = y + 116;
        g.fill(x, infoY, width - 30, infoY + 1, 0x55FFFFFF);
        if (owned) {
            g.drawString(font, "REFUND COST", x, infoY + 12, MUTED, false);
            g.drawString(font, "10,000 FE", x, infoY + 25, ACCENT, false);
        } else if (levelLocked) {
            g.drawString(font, "REQUIRES ANDROID LEVEL " + inspected.level, x, infoY + 14, DANGER, false);
        } else if (availablePoints() <= 0) {
            g.drawString(font, "NO ASCENSION POINT AVAILABLE", x, infoY + 14, DANGER, false);
        } else {
            g.drawString(font, "COST", x, infoY + 12, MUTED, false);
            g.drawString(font, "1 ASCENSION POINT", x, infoY + 25, GOLD, false);
        }
    }

    private void drawWrapped(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        for (var line : font.split(Component.literal(text), maxWidth)) {
            g.drawString(font, line, x, y, color, false);
            y += 11;
        }
    }

    @Override public boolean isPauseScreen() { return false; }
}
