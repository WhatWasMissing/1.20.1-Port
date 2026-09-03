package matteroverdrive.client.screen;

import matteroverdrive.android.AndroidData;
import matteroverdrive.client.AndroidClientState;
import matteroverdrive.network.AndroidPerkSelectPacket;
import matteroverdrive.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

public class AndroidSkillTreeScreen extends Screen {
    private static final int PANEL = 0xF010141C;
    private static final int GRID = 0xFF242A34;
    private static final int GOLD = 0xFFE0A800;
    private static final int LOCKED = 0xFF69717C;
    private static final int SELECTED = 0xFF54E68A;
    private static final int PENDING = 0xFF62E6FF;

    private final Map<Button, AndroidData.Perk> perkButtons = new LinkedHashMap<>();
    private long displayedPerks;
    private int displayedLevel;
    private int pendingPerk = -1;
    private boolean resetArmed;

    public AndroidSkillTreeScreen() {
        super(Component.literal("Android Skill Tree"));
    }

    @Override
    protected void init() {
        displayedPerks = AndroidClientState.selectedPerks();
        displayedLevel = AndroidClientState.level();
        rebuildPerkButtons();
    }

    private void rebuildPerkButtons() {
        clearWidgets();
        perkButtons.clear();
        int center = width / 2;
        int top = 54;
        int[] columns = {center - 305, center - 95, center + 115};

        for (AndroidData.Perk perk : AndroidData.Perk.values()) {
            int x = columns[Math.max(0, Math.min(2, perk.branch))];
            int y = top + (perk.level - 1) * 34;
            boolean selected = AndroidClientState.hasPerk(perk);
            boolean levelChosen = hasSelectedPerkAtLevel(perk.level);
            boolean pending = pendingPerk == perk.ordinal();
            Component label = Component.literal("L" + perk.level + "  " + perk.displayName)
                    .withStyle(selected ? ChatFormatting.GREEN
                            : pending ? ChatFormatting.AQUA
                            : perk.level <= AndroidClientState.level() && !levelChosen
                            ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY);
            Button button = Button.builder(label, pressed -> {
                pendingPerk = perk.ordinal();
                resetArmed = false;
                rebuildPerkButtons();
            }).bounds(x, y, 190, 26).build();
            button.active = !selected && !levelChosen
                    && AndroidClientState.isActive()
                    && AndroidClientState.level() >= perk.level;
            addRenderableWidget(button);
            perkButtons.put(button, perk);
        }

        Button reset = Button.builder(Component.literal(resetArmed
                        ? "Confirm reset (25k FE)" : "Reset perks"),
                button -> {
                    if (resetArmed) {
                        ModNetwork.CHANNEL.sendToServer(new AndroidPerkSelectPacket(-1));
                        resetArmed = false;
                        pendingPerk = -1;
                    } else {
                        resetArmed = true;
                        pendingPerk = -1;
                    }
                    rebuildPerkButtons();
                }).bounds(width / 2 - 180, height - 27, 140, 20).build();
        reset.active = AndroidClientState.selectedPerks() != 0L;
        addRenderableWidget(reset);

        Button confirm = Button.builder(Component.literal(pendingPerk >= 0
                        ? "Confirm perk" : "Select a perk"),
                button -> {
                    if (pendingPerk >= 0) {
                        ModNetwork.CHANNEL.sendToServer(new AndroidPerkSelectPacket(pendingPerk));
                        pendingPerk = -1;
                        rebuildPerkButtons();
                    }
                }).bounds(width / 2 - 30, height - 27, 120, 20).build();
        confirm.active = pendingPerk >= 0;
        addRenderableWidget(confirm);

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 + 100, height - 27, 80, 20).build());
    }

    private boolean hasSelectedPerkAtLevel(int level) {
        for (AndroidData.Perk perk : AndroidData.Perk.values()) {
            if (perk.level == level && AndroidClientState.hasPerk(perk)) return true;
        }
        return false;
    }

    @Override
    public void tick() {
        if (displayedPerks != AndroidClientState.selectedPerks()
                || displayedLevel != AndroidClientState.level()) {
            displayedPerks = AndroidClientState.selectedPerks();
            displayedLevel = AndroidClientState.level();
            rebuildPerkButtons();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(8, 8, width - 8, height - 36, PANEL);
        for (int x = 12; x < width - 12; x += 16) {
            graphics.fill(x, 12, x + 1, height - 40, GRID);
        }
        for (int y = 12; y < height - 40; y += 16) {
            graphics.fill(12, y, width - 12, y + 1, GRID);
        }

        int center = width / 2;
        int top = 54;
        int[] lineX = {center - 210, center, center + 210};
        graphics.drawCenteredString(font, title, center, 13, 0xFF62E6FF);
        int spent = Long.bitCount(AndroidClientState.selectedPerks());
        int available = Math.max(0, AndroidClientState.level() - spent);
        graphics.drawCenteredString(font,
                Component.literal("Level " + AndroidClientState.level() + "   Available points: " + available
                        + "   Click a node, then confirm"),
                center, 26, available > 0 ? GOLD : 0xFFB5C4CF);
        graphics.drawCenteredString(font, Component.literal("ASSAULT"), lineX[0], 43, GOLD);
        graphics.drawCenteredString(font, Component.literal("UTILITY"), lineX[1], 43, PENDING);
        graphics.drawCenteredString(font, Component.literal("SURVIVAL / MOBILITY"), lineX[2], 43, GOLD);

        for (int branch = 0; branch < 3; branch++) {
            for (int level = 1; level < AndroidData.MAX_LEVEL; level++) {
                int y1 = top + (level - 1) * 34 + 26;
                int y2 = top + level * 34;
                graphics.fill(lineX[branch] - 1, y1, lineX[branch] + 1, y2, GOLD);
            }
        }
        for (int level = 1; level <= AndroidData.MAX_LEVEL; level++) {
            int y = top + (level - 1) * 34 + 12;
            int color = hasSelectedPerkAtLevel(level) ? SELECTED
                    : level <= AndroidClientState.level() ? GOLD : LOCKED;
            graphics.fill(lineX[0] + 94, y, lineX[1] - 94, y + 2, color);
            graphics.fill(lineX[1] + 94, y, lineX[2] - 94, y + 2, color);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        String detail = resetArmed
                ? "Resetting refunds all perk points, disables toggled abilities and costs 25,000 Android FE."
                : "Hover a perk to view its effect.";
        int detailColor = resetArmed ? 0xFFFFB45B : 0xFFB5C4CF;
        for (Map.Entry<Button, AndroidData.Perk> entry : perkButtons.entrySet()) {
            if (entry.getKey().isHoveredOrFocused()) {
                AndroidData.Perk perk = entry.getValue();
                detail = perk.description;
                detailColor = AndroidClientState.hasPerk(perk) ? SELECTED
                        : pendingPerk == perk.ordinal() ? PENDING
                        : perk.level <= AndroidClientState.level() ? GOLD : LOCKED;
                break;
            }
        }
        graphics.drawCenteredString(font, Component.literal(detail), center, height - 48, detailColor);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
