package matteroverdrive.client.screen;

import matteroverdrive.client.ClientDocumentationOpener;
import matteroverdrive.client.GuideMeCompatEvents;
import matteroverdrive.item.ContractItem;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.quest.ContractStageSupport;
import matteroverdrive.world.StructureLoreCatalog;
import matteroverdrive.world.StructureLoreCatalog.LoreRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Player-specific Matter Overdrive PDA.
 *
 * This screen owns discovery/progression presentation. GuideME remains the full
 * technical manual and is deliberately opened only from the dedicated manual
 * action, never as a replacement for the PDA itself.
 */
public class DataPadScreen extends Screen {
    private static final int BACKGROUND = 0xF0061018;
    private static final int PANEL = 0xE80A1822;
    private static final int PANEL_ALT = 0xD90C202B;
    private static final int CYAN = 0xFF38D8F2;
    private static final int CYAN_DIM = 0xFF18879A;
    private static final int ORANGE = 0xFFFFA642;
    private static final int TEXT = 0xFFE9FBFF;
    private static final int MUTED = 0xFF8EADB7;
    private static final int GOOD = 0xFF62E39A;
    private static final int LOCKED = 0xFF526771;
    private static final int MAX_MANAGED_CONTRACTS = 4;

    private enum Tab {
        OVERVIEW("OVERVIEW"),
        DATA_BANK("DATA BANK"),
        FACILITIES("FACILITIES"),
        RESEARCH("RESEARCH"),
        OPERATIONS("OPERATIONS"),
        CONTRACTS("CONTRACTS"),
        SCANS("SCAN LOG");

        private final String label;
        Tab(String label) { this.label = label; }
    }

    private final List<String> journal;
    private final int loreMask;
    private final List<Button> tabButtons = new ArrayList<>();
    private final List<Button> abandonButtons = new ArrayList<>();
    private final int[] managedSlots = new int[MAX_MANAGED_CONTRACTS];

    private Tab tab = Tab.OVERVIEW;
    private int archiveCursor;
    private int facilityPage;
    private int armedAbandonSlot = -1;
    private long abandonArmedUntil;
    private Button previousButton;
    private Button nextButton;
    private Button manualButton;

    public DataPadScreen(List<String> journal, int loreMask) {
        super(Component.literal("Matter Overdrive PDA"));
        this.journal = new ArrayList<>(journal);
        this.loreMask = loreMask & StructureLoreCatalog.ALL_RECORDS_MASK;
        this.archiveCursor = firstRecoveredOrZero();
        Arrays.fill(managedSlots, -1);
    }

    @Override
    protected void init() {
        tabButtons.clear();
        abandonButtons.clear();
        Arrays.fill(managedSlots, -1);

        int left = panelLeft();
        int top = panelTop();
        int sidebarX = left + 8;
        int buttonY = top + 42;
        for (Tab value : Tab.values()) {
            Button button = addRenderableWidget(Button.builder(Component.literal(value.label),
                    ignored -> selectTab(value)).bounds(sidebarX, buttonY, 100, 18).build());
            tabButtons.add(button);
            buttonY += 21;
        }

        String manualLabel = GuideMeCompatEvents.isAvailable() ? "TECH MANUAL • GUIDEME" : "TECH MANUAL • BUILT-IN";
        manualButton = addRenderableWidget(Button.builder(Component.literal(manualLabel),
                ignored -> ClientDocumentationOpener.openTechnicalManual())
                .bounds(sidebarX, panelBottom() - 28, 100, 20).build());

        int contentLeft = left + 122;
        int contentRight = panelRight() - 10;
        int navWidth = 76;
        int navY = panelBottom() - 28;
        previousButton = addRenderableWidget(Button.builder(Component.literal("< PREV"), ignored -> navigate(-1))
                .bounds(contentLeft, navY, navWidth, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal("NEXT >"), ignored -> navigate(1))
                .bounds(contentRight - navWidth, navY, navWidth, 20).build());

        for (int row = 0; row < MAX_MANAGED_CONTRACTS; row++) {
            final int buttonIndex = row;
            Button button = addRenderableWidget(Button.builder(Component.literal("ABANDON"), ignored -> abandon(buttonIndex))
                    .bounds(contentRight - 66, top + 66 + row * 43, 58, 16).build());
            abandonButtons.add(button);
        }
        refreshControls();
    }

    private void selectTab(Tab next) {
        tab = next;
        armedAbandonSlot = -1;
        refreshControls();
    }

    private void navigate(int direction) {
        if (tab == Tab.DATA_BANK) {
            archiveCursor = Math.max(0, Math.min(StructureLoreCatalog.RECORD_COUNT - 1, archiveCursor + direction));
        } else if (tab == Tab.FACILITIES) {
            facilityPage = Math.max(0, Math.min(1, facilityPage + direction));
        }
        refreshControls();
    }

    private void refreshControls() {
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).active = Tab.values()[i] != tab;
        }
        boolean navigable = tab == Tab.DATA_BANK || tab == Tab.FACILITIES;
        if (previousButton != null) {
            previousButton.visible = navigable;
            previousButton.active = tab == Tab.DATA_BANK ? archiveCursor > 0 : facilityPage > 0;
        }
        if (nextButton != null) {
            nextButton.visible = navigable;
            nextButton.active = tab == Tab.DATA_BANK
                    ? archiveCursor < StructureLoreCatalog.RECORD_COUNT - 1
                    : facilityPage < 1;
        }
        refreshContractButtons();
    }

    private void abandon(int buttonIndex) {
        if (buttonIndex < 0 || buttonIndex >= managedSlots.length) return;
        int slot = managedSlots[buttonIndex];
        if (slot < 0) return;
        long now = System.currentTimeMillis();
        if (armedAbandonSlot == slot && now <= abandonArmedUntil) {
            ModNetwork.requestContractAbandon(slot);
            armedAbandonSlot = -1;
            abandonArmedUntil = 0L;
        } else {
            armedAbandonSlot = slot;
            abandonArmedUntil = now + 3000L;
        }
        refreshContractButtons();
    }

    private List<ContractRef> contractRefs() {
        Minecraft minecraft = Minecraft.getInstance();
        List<ContractRef> contracts = new ArrayList<>();
        if (minecraft.player == null) return contracts;
        for (int slot = 0; slot < minecraft.player.getInventory().items.size(); slot++) {
            ItemStack stack = minecraft.player.getInventory().items.get(slot);
            if (stack.getItem() instanceof ContractItem) contracts.add(new ContractRef(slot, stack));
        }
        return contracts;
    }

    private void refreshContractButtons() {
        if (abandonButtons.isEmpty()) return;
        List<ContractRef> refs = tab == Tab.CONTRACTS ? contractRefs() : List.of();
        long now = System.currentTimeMillis();
        if (armedAbandonSlot >= 0 && now > abandonArmedUntil) armedAbandonSlot = -1;
        for (int i = 0; i < abandonButtons.size(); i++) {
            Button button = abandonButtons.get(i);
            boolean visible = i < refs.size();
            button.visible = visible;
            button.active = visible;
            managedSlots[i] = visible ? refs.get(i).slot() : -1;
            button.setMessage(Component.literal(visible && managedSlots[i] == armedAbandonSlot ? "ABANDON?" : "ABANDON"));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshContractButtons();
        graphics.fill(0, 0, width, height, BACKGROUND);

        int left = panelLeft();
        int right = panelRight();
        int top = panelTop();
        int bottom = panelBottom();
        int contentLeft = left + 118;

        graphics.fill(left, top, right, bottom, PANEL);
        graphics.fill(left, top, left + 112, bottom, PANEL_ALT);
        border(graphics, left, top, right, bottom, CYAN_DIM);
        graphics.fill(left + 112, top, left + 113, bottom, CYAN_DIM);

        graphics.drawString(font, Component.literal("MO // PERSONAL DATA ASSISTANT"), left + 9, top + 9, CYAN, false);
        graphics.drawString(font, Component.literal("ARCHIVE " + recoveredCount() + "/" + StructureLoreCatalog.RECORD_COUNT),
                right - 92, top + 9, recoveredCount() == StructureLoreCatalog.RECORD_COUNT ? GOOD : ORANGE, false);
        graphics.drawString(font, Component.literal(tab.label), contentLeft + 10, top + 30, TEXT, false);
        graphics.fill(contentLeft + 10, top + 43, right - 10, top + 44, CYAN_DIM);

        int x = contentLeft + 10;
        int y = top + 54;
        int maxWidth = Math.max(80, right - x - 12);
        int contentBottom = bottom - 38;

        switch (tab) {
            case OVERVIEW -> renderOverview(graphics, x, y, maxWidth, contentBottom);
            case DATA_BANK -> renderDataBank(graphics, x, y, maxWidth, contentBottom);
            case FACILITIES -> renderFacilities(graphics, x, y, maxWidth, contentBottom);
            case RESEARCH -> renderJournalSection(graphics, x, y, maxWidth, contentBottom, researchLines(), "No research programme data available.");
            case OPERATIONS -> renderJournalSection(graphics, x, y, maxWidth, contentBottom, operationLines(), "No active Field Operations data available.");
            case CONTRACTS -> renderContracts(graphics, x, y, maxWidth, contentBottom);
            case SCANS -> renderJournalSection(graphics, x, y, maxWidth, contentBottom, scanLines(), "No block scans recorded yet.");
        }

        graphics.drawString(font,
                Component.literal(GuideMeCompatEvents.isAvailable()
                        ? "TECHNICAL MANUAL LINK: GUIDEME ONLINE"
                        : "TECHNICAL MANUAL LINK: INTERNAL ARCHIVE"),
                left + 9, bottom - 41, GuideMeCompatEvents.isAvailable() ? GOOD : MUTED, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderOverview(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        graphics.drawString(font, Component.literal("OVERDRIVE INCIDENT RECONSTRUCTION"), x, y, ORANGE, false);
        y += 14;
        y = paragraph(graphics, "Recovered records: " + recoveredCount() + "/" + StructureLoreCatalog.RECORD_COUNT
                + ". Records are displayed in canonical historical order regardless of the order you discover structures.", x, y, maxWidth, TEXT);
        y += 5;
        int next = firstMissing();
        if (next < 0) {
            y = paragraph(graphics, "ARCHIVE COMPLETE — the Closed Loop has been reconstructed. Further answers lie beyond the recovered incident chain.",
                    x, y, maxWidth, GOOD);
        } else {
            LoreRecord record = StructureLoreCatalog.byBit(next);
            y = paragraph(graphics, "Unresolved archive slots remain. Cross-references in recovered records may identify new facilities without requiring a fixed discovery order.",
                    x, y, maxWidth, MUTED);
            if (record != null) {
                y += 4;
                graphics.drawString(font, Component.literal("Next canonical gap: Archive Entry " + record.archiveIndex()), x, y, CYAN, false);
                y += 12;
            }
        }
        y += 7;
        graphics.drawString(font, Component.literal("CURRENT FIELD JOURNAL"), x, y, CYAN, false);
        y += 13;
        List<String> summary = researchLines();
        for (int i = 0; i < Math.min(6, summary.size()) && y < bottom - 10; i++) {
            y = paragraph(graphics, summary.get(i), x, y, maxWidth, i == 0 ? TEXT : MUTED);
        }
    }

    private void renderDataBank(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        LoreRecord record = StructureLoreCatalog.byBit(archiveCursor);
        if (record == null) return;
        boolean recovered = StructureLoreCatalog.recovered(loreMask, record);

        graphics.drawString(font, Component.literal("ARCHIVE ENTRY " + record.archiveIndex() + "/" + StructureLoreCatalog.RECORD_COUNT), x, y, CYAN, false);
        graphics.drawString(font, Component.literal(record.chapter()), x + Math.max(120, maxWidth - 128), y, MUTED, false);
        y += 16;

        if (!recovered) {
            graphics.drawString(font, Component.literal("[ RECORD NOT RECOVERED ]"), x, y, LOCKED, false);
            y += 15;
            paragraph(graphics,
                    "This historical slot is unresolved. Explore Matter Overdrive structures and recover local facility records to reconstruct it.",
                    x, y, maxWidth, MUTED);
            return;
        }

        graphics.drawString(font, Component.literal(record.title()), x, y, ORANGE, false);
        y += 13;
        graphics.drawString(font, Component.literal(record.facility()), x, y, CYAN, false);
        y += 12;
        graphics.drawString(font, Component.literal("SOURCE: " + record.author()), x, y, MUTED, false);
        y += 18;
        y = paragraph(graphics, record.summary(), x, y, maxWidth, TEXT);
        y += 9;
        y = paragraph(graphics, record.link(), x, y, maxWidth, CYAN);
        y += 9;
        if (y < bottom - 20) {
            graphics.drawString(font, Component.literal("RECOVERY STATUS: AUTHENTICATED"), x, y, GOOD, false);
            graphics.drawString(font, Component.literal("RECONSTRUCTION: " + recoveredCount() + "/" + StructureLoreCatalog.RECORD_COUNT),
                    x, y + 12, ORANGE, false);
        }
    }

    private void renderFacilities(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        int from = facilityPage * 8;
        int to = Math.min(from + 8, StructureLoreCatalog.RECORD_COUNT);
        graphics.drawString(font, Component.literal("DISCOVERED FACILITY INDEX // " + (facilityPage + 1) + "/2"), x, y, CYAN, false);
        y += 16;
        for (int i = from; i < to && y < bottom - 12; i++) {
            LoreRecord record = StructureLoreCatalog.byBit(i);
            boolean recovered = StructureLoreCatalog.recovered(loreMask, record);
            String prefix = recovered ? "[✓] " : "[ ] ";
            String name = recovered ? record.facility() : "UNKNOWN FACILITY";
            int color = recovered ? TEXT : LOCKED;
            graphics.drawString(font, Component.literal(prefix + String.format("%02d", record.archiveIndex()) + "  " + fit(name, maxWidth - 46)), x, y, color, false);
            y += 11;
            if (recovered) {
                graphics.drawString(font, Component.literal("    " + fit(record.chapter(), maxWidth - 24)), x, y, MUTED, false);
                y += 11;
            } else {
                y += 5;
            }
        }
    }

    private void renderContracts(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        List<ContractRef> contracts = contractRefs();
        if (contracts.isEmpty()) {
            graphics.drawString(font, Component.literal("No active contracts in your inventory."), x, y, MUTED, false);
            return;
        }

        int visible = Math.min(MAX_MANAGED_CONTRACTS, contracts.size());
        for (int i = 0; i < visible; i++) {
            ItemStack contract = contracts.get(i).stack();
            int titleColor = ContractItem.complete(contract) ? GOOD : TEXT;
            String stage = ContractStageSupport.stageLabel(contract);
            graphics.drawString(font, Component.literal(fit((i + 1) + ". " + ContractItem.title(contract), maxWidth - 70)), x, y, titleColor, false);
            String objective = ContractItem.objectiveText(contract);
            if (!stage.isBlank()) objective = stage + " — " + objective;
            graphics.drawString(font, Component.literal(fit(objective, Math.max(1, maxWidth - 78))), x + 8, y + 11, MUTED, false);
            String progress = ContractItem.complete(contract)
                    ? "READY TO REDEEM"
                    : "Progress " + ContractItem.progress(contract) + " / " + ContractItem.goal(contract);
            if (ContractItem.xp(contract) > 0) progress += "   XP " + ContractItem.xp(contract);
            graphics.drawString(font, Component.literal(fit(progress, Math.max(1, maxWidth - 78))), x + 8, y + 22,
                    ContractItem.complete(contract) ? GOOD : CYAN, false);
            y += 43;
            if (y > bottom - 36) break;
        }
        if (contracts.size() > visible && y <= bottom - 16) {
            graphics.drawString(font, Component.literal("+ " + (contracts.size() - visible) + " more carried contract(s)"), x, y, MUTED, false);
        }
    }

    private void renderJournalSection(GuiGraphics graphics, int x, int y, int maxWidth, int bottom,
                                      List<String> lines, String emptyMessage) {
        if (lines.isEmpty()) {
            graphics.drawString(font, Component.literal(emptyMessage), x, y, MUTED, false);
            return;
        }
        for (String line : lines) {
            if (y >= bottom - 9) {
                graphics.drawString(font, Component.literal("… additional entries retained in PDA memory"), x, y, MUTED, false);
                break;
            }
            int color = line.startsWith("===") || line.startsWith("---") ? CYAN : TEXT;
            y = paragraph(graphics, stripHeading(line), x, y, maxWidth, color);
            y += 2;
        }
    }

    private List<String> researchLines() {
        List<String> result = new ArrayList<>();
        boolean progression = false;
        for (String line : journal) {
            if (line.equals("--- Field Operations ---")) break;
            result.add(line);
        }
        for (String line : journal) {
            if (line.equals("--- Progression ---")) {
                progression = true;
                result.add(line);
                continue;
            }
            if (line.equals("--- Scan History ---")) break;
            if (progression) result.add(line);
        }
        return result;
    }

    private List<String> operationLines() {
        return between("--- Field Operations ---", "--- Progression ---");
    }

    private List<String> scanLines() {
        List<String> lines = new ArrayList<>();
        boolean capture = false;
        for (String line : journal) {
            if (line.equals("--- Scan History ---")) {
                capture = true;
                continue;
            }
            if (capture) lines.add(line);
        }
        return lines;
    }

    private List<String> between(String start, String end) {
        List<String> lines = new ArrayList<>();
        boolean capture = false;
        for (String line : journal) {
            if (line.equals(start)) {
                capture = true;
                continue;
            }
            if (capture && line.equals(end)) break;
            if (capture) lines.add(line);
        }
        return lines;
    }

    private int paragraph(GuiGraphics graphics, String text, int x, int y, int maxWidth, int color) {
        for (FormattedCharSequence line : font.split(Component.literal(text), maxWidth)) {
            graphics.drawString(font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    private int firstRecoveredOrZero() {
        for (int i = 0; i < StructureLoreCatalog.RECORD_COUNT; i++) {
            if ((loreMask & (1 << i)) != 0) return i;
        }
        return 0;
    }

    private int firstMissing() {
        for (int i = 0; i < StructureLoreCatalog.RECORD_COUNT; i++) {
            if ((loreMask & (1 << i)) == 0) return i;
        }
        return -1;
    }

    private int recoveredCount() {
        return Integer.bitCount(loreMask);
    }

    private int panelLeft() { return Math.max(8, width / 2 - Math.min(270, Math.max(210, width / 2 - 10))); }
    private int panelRight() { return Math.min(width - 8, width / 2 + Math.min(270, Math.max(210, width / 2 - 10))); }
    private int panelTop() { return 8; }
    private int panelBottom() { return height - 8; }

    private void border(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        graphics.fill(left, top, right, top + 1, color);
        graphics.fill(left, bottom - 1, right, bottom, color);
        graphics.fill(left, top, left + 1, bottom, color);
        graphics.fill(right - 1, top, right, bottom, color);
    }

    private String fit(String text, int maxWidth) {
        if (text == null || font.width(text) <= maxWidth) return text == null ? "" : text;
        int usable = Math.max(0, maxWidth - font.width("…"));
        return font.plainSubstrByWidth(text, usable) + "…";
    }

    private static String stripHeading(String value) {
        return value.replace("===", "").replace("---", "").trim();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record ContractRef(int slot, ItemStack stack) {}
}
