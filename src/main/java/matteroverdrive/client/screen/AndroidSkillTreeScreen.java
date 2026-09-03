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

    private final Map<Button, AndroidData.Perk> perkButtons = new LinkedHashMap<>();
    private long displayedPerks;
    private int displayedLevel;

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
        int top = 52;
        for (AndroidData.Perk perk : AndroidData.Perk.values()) {
            int x = perk.branch == 0 ? center - 222 : center + 22;
            int y = top + (perk.level - 1) * 34;
            boolean selected = AndroidClientState.hasPerk(perk);
            boolean levelChosen = hasSelectedPerkAtLevel(perk.level);
            Component label = Component.literal("L" + perk.level + "  " + perk.displayName)
                    .withStyle(selected ? ChatFormatting.GREEN
                            : perk.level <= AndroidClientState.level() && !levelChosen
                            ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY);
            Button button = Button.builder(label, pressed -> {
                ModNetwork.CHANNEL.sendToServer(new AndroidPerkSelectPacket(perk.ordinal()));
            }).bounds(x, y, 200, 26).build();
            button.active = !selected && !levelChosen
                    && AndroidClientState.isActive()
                    && AndroidClientState.level() >= perk.level;
            addRenderableWidget(button);
            perkButtons.put(button, perk);
        }
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 - 40, height - 27, 80, 20).build());
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
        graphics.fill(12, 10, width - 12, height - 36, PANEL);
        for (int x = 16; x < width - 16; x += 16) {
            graphics.fill(x, 14, x + 1, height - 40, GRID);
        }
        for (int y = 14; y < height - 40; y += 16) {
            graphics.fill(16, y, width - 16, y + 1, GRID);
        }

        int center = width / 2;
        int top = 52;
        graphics.drawCenteredString(font, title, center, 16, 0xFF62E6FF);
        int spent = Long.bitCount(AndroidClientState.selectedPerks());
        int available = Math.max(0, AndroidClientState.level() - spent);
        graphics.drawCenteredString(font,
                Component.literal("Level " + AndroidClientState.level() + "   Available points: " + available),
                center, 29, available > 0 ? GOLD : 0xFFB5C4CF);
        graphics.drawCenteredString(font, Component.literal("ASSAULT"), center - 122, 42, GOLD);
        graphics.drawCenteredString(font, Component.literal("SURVIVAL / MOBILITY"), center + 122, 42, GOLD);

        for (int level = 1; level < AndroidData.MAX_LEVEL; level++) {
            int y1 = top + (level - 1) * 34 + 26;
            int y2 = top + level * 34;
            graphics.fill(center - 123, y1, center - 121, y2, GOLD);
            graphics.fill(center + 121, y1, center + 123, y2, GOLD);
        }
        for (int level = 1; level <= AndroidData.MAX_LEVEL; level++) {
            int y = top + (level - 1) * 34 + 12;
            graphics.fill(center - 22, y, center + 22, y + 2,
                    hasSelectedPerkAtLevel(level) ? SELECTED : level <= AndroidClientState.level() ? GOLD : LOCKED);
        }

        super.render(graphics, mouseX, mouseY, partialTick);

        for (Map.Entry<Button, AndroidData.Perk> entry : perkButtons.entrySet()) {
            if (entry.getKey().isHoveredOrFocused()) {
                AndroidData.Perk perk = entry.getValue();
                int color = AndroidClientState.hasPerk(perk) ? SELECTED
                        : perk.level <= AndroidClientState.level() ? GOLD : LOCKED;
                graphics.drawCenteredString(font, Component.literal(perk.description), center, height - 48, color);
                break;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
