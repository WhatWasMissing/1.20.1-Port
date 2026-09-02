package matteroverdrive.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class DataPadScreen extends Screen {
    private static final int PANEL_COLOR = 0xE6101820;
    private static final int BORDER_COLOR = 0xFF33CCFF;
    private static final int TEXT_COLOR = 0xFFE6F7FF;
    private static final int MUTED_COLOR = 0xFF9BB6C3;

    private static final List<String> TITLES = List.of(
            "Overview",
            "Matter Replication",
            "Power and Machines",
            "Fusion Reactor",
            "Android System",
            "Survival Progression",
            "Scan History"
    );

    private static final List<List<String>> GUIDE_PAGES = List.of(
            List.of(
                    "Matter Overdrive turns stored matter and Forge Energy into a connected technology chain.",
                    "Use the page buttons to review the current implemented systems. Use the Data Pad on a block to record its registry ID and matter value."
            ),
            List.of(
                    "Decompose supported items into matter, analyse items into patterns, store those patterns on Pattern Drives, then queue them through the Pattern Monitor and Replicator.",
                    "The Matter Scanner is the handheld analyser. Sneak-use it on powered Pattern Storage, then hold-use it on a valid block for three seconds. A successful scan consumes the block and adds 10% progress."
            ),
            List.of(
                    "Solar Panels generate daylight FE. Heavy Energy Cables carry FE. Charging Stations charge compatible batteries and handheld tools.",
                    "The Microwave cooks food. The Space-Time Accelerator spends FE and matter to pulse extra ticks into nearby random-tick blocks and block entities."
            ),
            List.of(
                    "Build the 11 x 11 reactor ring with the controller on an edge, coil/IO positions around the ring and the anomaly at its centre.",
                    "Feed matter through Reactor IO. Reactor output scales with anomaly mass and efficiency. Stabilizers require FE, a clear beam and correct facing."
            ),
            List.of(
                    "Blue Pills convert players, Red Pills deactivate Android state and Yellow Pills restore Android FE.",
                    "The Android Station installs Head, Chest, Arms and Legs parts. Installed parts consume Android FE for their active effects."
            ),
            List.of(
                    "Tritanium generates from Y -32 to 64. Dilithium generates from Y -64 to 16 in fresh Overworld chunks.",
                    "Process the ores in a furnace or blast furnace. Temporary survival recipes provide the Android pills until the full Mad Scientist quest route is restored."
            )
    );

    private final List<String> history;
    private int page;
    private Button previousButton;
    private Button nextButton;

    public DataPadScreen(List<String> history) {
        super(Component.literal("Matter Overdrive Data Pad"));
        this.history = new ArrayList<>(history);
    }

    @Override
    protected void init() {
        int y = height - 32;
        previousButton = addRenderableWidget(Button.builder(Component.literal("< Previous"),
                button -> setPage(page - 1)).bounds(width / 2 - 105, y, 96, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal("Next >"),
                button -> setPage(page + 1)).bounds(width / 2 + 9, y, 96, 20).build());
        setPage(page);
    }

    private void setPage(int nextPage) {
        page = Math.max(0, Math.min(TITLES.size() - 1, nextPage));
        if (previousButton != null) {
            previousButton.active = page > 0;
        }
        if (nextButton != null) {
            nextButton.active = page < TITLES.size() - 1;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = Math.max(12, width / 2 - 190);
        int right = Math.min(width - 12, width / 2 + 190);
        int top = 12;
        int bottom = height - 42;

        graphics.fill(left, top, right, bottom, PANEL_COLOR);
        graphics.fill(left, top, right, top + 1, BORDER_COLOR);
        graphics.fill(left, bottom - 1, right, bottom, BORDER_COLOR);
        graphics.fill(left, top, left + 1, bottom, BORDER_COLOR);
        graphics.fill(right - 1, top, right, bottom, BORDER_COLOR);

        graphics.drawCenteredString(font, title, width / 2, top + 10, BORDER_COLOR);
        graphics.drawCenteredString(font,
                Component.literal((page + 1) + "/" + TITLES.size() + " — " + TITLES.get(page)),
                width / 2, top + 26, TEXT_COLOR);

        int textX = left + 14;
        int textY = top + 48;
        int maxWidth = right - left - 28;
        if (page == TITLES.size() - 1) {
            textY = renderHistory(graphics, textX, textY, maxWidth);
        } else {
            for (String paragraph : GUIDE_PAGES.get(page)) {
                for (FormattedCharSequence line : font.split(Component.literal(paragraph), maxWidth)) {
                    graphics.drawString(font, line, textX, textY, TEXT_COLOR, false);
                    textY += 11;
                }
                textY += 7;
            }
        }

        graphics.drawString(font, Component.literal("Use on blocks to add scan-history entries."),
                textX, bottom - 15, MUTED_COLOR, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private int renderHistory(GuiGraphics graphics, int x, int y, int maxWidth) {
        if (history.isEmpty()) {
            graphics.drawString(font, Component.literal("No blocks recorded yet."),
                    x, y, MUTED_COLOR, false);
            return y + 11;
        }
        for (int i = 0; i < history.size(); i++) {
            Component entry = Component.literal((i + 1) + ". " + history.get(i));
            for (FormattedCharSequence line : font.split(entry, maxWidth)) {
                graphics.drawString(font, line, x, y, TEXT_COLOR, false);
                y += 10;
            }
            if (y > height - 70) {
                graphics.drawString(font, Component.literal("More entries are stored; increase GUI height to view."),
                        x, y, MUTED_COLOR, false);
                break;
            }
        }
        return y;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
