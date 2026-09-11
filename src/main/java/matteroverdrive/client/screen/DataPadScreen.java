package matteroverdrive.client.screen;

import matteroverdrive.client.ClientDocumentationOpener;
import matteroverdrive.client.GuideMeCompatEvents;
import matteroverdrive.client.PdaNarrationController;
import matteroverdrive.item.ContractItem;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.quest.ContractStageSupport;
import matteroverdrive.world.AmbientLoreCatalog;
import matteroverdrive.world.AmbientLoreCatalog.Entry;
import matteroverdrive.world.StructureLoreCatalog;
import matteroverdrive.world.StructureLoreCatalog.LoreRecord;
import matteroverdrive.world.StructureLoreCatalog.Reconstruction;
import matteroverdrive.world.TechnologyLoreCatalog;
import matteroverdrive.world.TechnologyLoreCatalog.TechRecord;
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

/** Player-specific Matter Overdrive PDA. */
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
    private static final int DANGER = 0xFFFF6666;
    private static final int MAX_MANAGED_CONTRACTS = 4;
    private static final int ARCHIVE_SECTIONS = 3;

    private enum Tab {
        OVERVIEW("OVERVIEW"),
        DATA_BANK("DATA BANK"),
        INCIDENT("INCIDENT"),
        FACILITIES("FACILITIES"),
        FIELD_LOGS("FIELD LOGS"),
        TECHNOLOGY("TECHNOLOGY"),
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
    private int archiveSection;
    private int reconstructionCursor;
    private int facilityPage;
    private int ambientCursor;
    private int technologyCursor;
    private int armedAbandonSlot = -1;
    private long abandonArmedUntil;
    private Button previousButton;
    private Button nextButton;
    private Button manualButton;
    private Button readAloudButton;
    private Button stopNarrationButton;

    public DataPadScreen(List<String> journal, int loreMask) {
        super(Component.literal("Matter Overdrive PDA"));
        this.journal = new ArrayList<>(journal);
        this.loreMask = loreMask & StructureLoreCatalog.ALL_RECORDS_MASK;
        this.archiveCursor = firstRecoveredArchiveOrZero();
        this.reconstructionCursor = firstIncompleteReconstructionOrLast();
        this.ambientCursor = Math.max(0, ambientEntries().size() - 1);
        this.technologyCursor = Math.max(0, technologyEntries().size() - 1);
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
        int buttonY = top + 40;
        int manualY = panelBottom() - 28;
        int available = Math.max(Tab.values().length * 9, manualY - buttonY - 5);
        int spacing = Math.max(9, Math.min(17, available / Tab.values().length));
        int buttonHeight = Math.max(8, spacing - 2);
        for (Tab value : Tab.values()) {
            Button button = addRenderableWidget(Button.builder(Component.literal(value.label),
                    ignored -> selectTab(value)).bounds(sidebarX, buttonY, 100, buttonHeight).build());
            tabButtons.add(button);
            buttonY += spacing;
        }

        String manualLabel = GuideMeCompatEvents.isAvailable() ? "TECH MANUAL • GUIDEME" : "TECH MANUAL • BUILT-IN";
        manualButton = addRenderableWidget(Button.builder(Component.literal(manualLabel),
                ignored -> ClientDocumentationOpener.openTechnicalManual())
                .bounds(sidebarX, manualY, 100, 20).build());

        int contentLeft = left + 122;
        int contentRight = panelRight() - 10;
        int navWidth = 76;
        int navY = panelBottom() - 28;
        previousButton = addRenderableWidget(Button.builder(Component.literal("< PREV"), ignored -> navigate(-1))
                .bounds(contentLeft, navY, navWidth, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal("NEXT >"), ignored -> navigate(1))
                .bounds(contentRight - navWidth, navY, navWidth, 20).build());

        int center = (contentLeft + contentRight) / 2;
        readAloudButton = addRenderableWidget(Button.builder(Component.literal("READ ALOUD"), ignored -> readCurrentLore())
                .bounds(center - 76, navY, 92, 20).build());
        stopNarrationButton = addRenderableWidget(Button.builder(Component.literal("STOP"), ignored -> PdaNarrationController.stop())
                .bounds(center + 20, navY, 56, 20).build());

        for (int row = 0; row < MAX_MANAGED_CONTRACTS; row++) {
            final int buttonIndex = row;
            Button button = addRenderableWidget(Button.builder(Component.literal("ABANDON"), ignored -> abandon(buttonIndex))
                    .bounds(contentRight - 66, top + 66 + row * 43, 58, 16).build());
            abandonButtons.add(button);
        }
        refreshControls();
    }

    private void selectTab(Tab next) {
        if (tab != next) PdaNarrationController.stop();
        tab = next;
        armedAbandonSlot = -1;
        refreshControls();
    }

    private void navigate(int direction) {
        if (direction == 0) return;
        PdaNarrationController.stop();
        if (tab == Tab.DATA_BANK) {
            if (direction > 0) {
                if (archiveSection < ARCHIVE_SECTIONS - 1) {
                    archiveSection++;
                } else if (archiveCursor < StructureLoreCatalog.RECORD_COUNT - 1) {
                    archiveCursor++;
                    archiveSection = 0;
                }
            } else if (archiveSection > 0) {
                archiveSection--;
            } else if (archiveCursor > 0) {
                archiveCursor--;
                archiveSection = ARCHIVE_SECTIONS - 1;
            }
        } else if (tab == Tab.INCIDENT) {
            reconstructionCursor = Math.max(0, Math.min(
                    StructureLoreCatalog.reconstructions().size() - 1,
                    reconstructionCursor + direction));
        } else if (tab == Tab.FACILITIES) {
            facilityPage = Math.max(0, Math.min(1, facilityPage + direction));
        } else if (tab == Tab.FIELD_LOGS) {
            int size = ambientEntries().size();
            if (size > 0) ambientCursor = Math.max(0, Math.min(size - 1, ambientCursor + direction));
        } else if (tab == Tab.TECHNOLOGY) {
            int size = technologyEntries().size();
            if (size > 0) technologyCursor = Math.max(0, Math.min(size - 1, technologyCursor + direction));
        }
        refreshControls();
    }

    private void refreshControls() {
        for (int i = 0; i < tabButtons.size(); i++) tabButtons.get(i).active = Tab.values()[i] != tab;
        List<Entry> ambient = ambientEntries();
        List<TechRecord> technology = technologyEntries();
        boolean navigable = tab == Tab.DATA_BANK || tab == Tab.INCIDENT || tab == Tab.FACILITIES
                || tab == Tab.FIELD_LOGS || tab == Tab.TECHNOLOGY;
        if (previousButton != null) {
            previousButton.visible = navigable;
            previousButton.active = switch (tab) {
                case DATA_BANK -> archiveCursor > 0 || archiveSection > 0;
                case INCIDENT -> reconstructionCursor > 0;
                case FACILITIES -> facilityPage > 0;
                case FIELD_LOGS -> ambientCursor > 0 && !ambient.isEmpty();
                case TECHNOLOGY -> technologyCursor > 0 && !technology.isEmpty();
                default -> false;
            };
        }
        if (nextButton != null) {
            nextButton.visible = navigable;
            nextButton.active = switch (tab) {
                case DATA_BANK -> archiveCursor < StructureLoreCatalog.RECORD_COUNT - 1 || archiveSection < ARCHIVE_SECTIONS - 1;
                case INCIDENT -> reconstructionCursor < StructureLoreCatalog.reconstructions().size() - 1;
                case FACILITIES -> facilityPage < 1;
                case FIELD_LOGS -> !ambient.isEmpty() && ambientCursor < ambient.size() - 1;
                case TECHNOLOGY -> !technology.isEmpty() && technologyCursor < technology.size() - 1;
                default -> false;
            };
        }

        boolean narratable = canNarrateCurrentLore();
        boolean narrationTab = tab == Tab.DATA_BANK || tab == Tab.INCIDENT || tab == Tab.FIELD_LOGS || tab == Tab.TECHNOLOGY;
        if (readAloudButton != null) {
            readAloudButton.visible = narrationTab;
            readAloudButton.active = narratable;
        }
        if (stopNarrationButton != null) {
            stopNarrationButton.visible = narrationTab;
            stopNarrationButton.active = narratable;
        }
        refreshContractButtons();
    }

    private boolean canNarrateCurrentLore() {
        if (tab == Tab.DATA_BANK) {
            LoreRecord record = StructureLoreCatalog.byArchiveIndex(archiveCursor + 1);
            return record != null && StructureLoreCatalog.recovered(loreMask, record);
        }
        if (tab == Tab.INCIDENT) {
            Reconstruction reconstruction = StructureLoreCatalog.reconstructionByIndex(reconstructionCursor);
            return reconstruction != null && StructureLoreCatalog.reconstructionUnlocked(loreMask, reconstruction);
        }
        if (tab == Tab.FIELD_LOGS) return currentAmbientEntry() != null;
        if (tab == Tab.TECHNOLOGY) return currentTechnologyEntry() != null;
        return false;
    }

    private void readCurrentLore() {
        String narration = currentLoreNarration();
        if (!narration.isBlank()) PdaNarrationController.read(narration);
    }

    private String currentLoreNarration() {
        if (tab == Tab.DATA_BANK) {
            LoreRecord record = StructureLoreCatalog.byArchiveIndex(archiveCursor + 1);
            if (record == null || !StructureLoreCatalog.recovered(loreMask, record)) return "";
            return switch (archiveSection) {
                case 0 -> record.title() + ". " + record.facility() + ". " + record.timestamp() + ". Source: "
                        + record.author() + ". Site function. " + record.sitePurpose() + ". Primary record. " + record.summary();
                case 1 -> record.title() + ". Recovered excerpt. " + record.excerpt()
                        + ". P D A forensic analysis. " + record.analysis();
                default -> record.title() + ". Incident implication. " + record.implication()
                        + ". Cross reference. " + record.link();
            };
        }
        if (tab == Tab.INCIDENT) {
            Reconstruction reconstruction = StructureLoreCatalog.reconstructionByIndex(reconstructionCursor);
            if (reconstruction == null || !StructureLoreCatalog.reconstructionUnlocked(loreMask, reconstruction)) return "";
            return reconstruction.title() + ". " + reconstruction.subtitle() + ". " + reconstruction.lead()
                    + " " + reconstruction.evidence() + " " + reconstruction.analysis() + " " + reconstruction.conclusion();
        }
        if (tab == Tab.FIELD_LOGS) {
            Entry entry = currentAmbientEntry();
            return entry == null ? "" : entry.title() + ". Source: " + entry.source() + ". Recovered excerpt. "
                    + entry.excerpt() + ". P D A analysis. " + entry.analysis();
        }
        if (tab == Tab.TECHNOLOGY) {
            TechRecord entry = currentTechnologyEntry();
            return entry == null ? "" : entry.title() + ". " + entry.category() + ". Function. " + entry.function()
                    + ". Archive context. " + entry.lore() + ". Field note. " + entry.fieldNote();
        }
        return "";
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
            case INCIDENT -> renderIncident(graphics, x, y, maxWidth, contentBottom);
            case FACILITIES -> renderFacilities(graphics, x, y, maxWidth, contentBottom);
            case FIELD_LOGS -> renderFieldLogs(graphics, x, y, maxWidth, contentBottom);
            case TECHNOLOGY -> renderTechnology(graphics, x, y, maxWidth, contentBottom);
            case RESEARCH -> renderJournalSection(graphics, x, y, maxWidth, contentBottom, researchLines(), "No research programme data available.");
            case OPERATIONS -> renderJournalSection(graphics, x, y, maxWidth, contentBottom, operationLines(), "No active Field Operations data available.");
            case CONTRACTS -> renderContracts(graphics, x, y, maxWidth, contentBottom);
            case SCANS -> renderJournalSection(graphics, x, y, maxWidth, contentBottom, scanLines(), "No block scans recorded yet.");
        }

        if ((tab == Tab.DATA_BANK || tab == Tab.INCIDENT || tab == Tab.FIELD_LOGS || tab == Tab.TECHNOLOGY)
                && canNarrateCurrentLore()) {
            graphics.drawString(font, Component.literal("AUDIO LOG: READ ALOUD AVAILABLE"),
                    contentLeft + 10, bottom - 41, CYAN, false);
        } else {
            graphics.drawString(font,
                    Component.literal(GuideMeCompatEvents.isAvailable()
                            ? "TECHNICAL MANUAL LINK: GUIDEME ONLINE"
                            : "TECHNICAL MANUAL LINK: INTERNAL ARCHIVE"),
                    left + 9, bottom - 41, GuideMeCompatEvents.isAvailable() ? GOOD : MUTED, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderOverview(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        graphics.drawString(font, Component.literal("OVERDRIVE INCIDENT RECONSTRUCTION"), x, y, ORANGE, false);
        y += 14;
        y = paragraph(graphics, "Recovered primary records: " + recoveredCount() + "/" + StructureLoreCatalog.RECORD_COUNT
                + ". Authenticated reconstructions: " + unlockedReconstructions() + "/"
                + StructureLoreCatalog.reconstructions().size() + ".", x, y, maxWidth, TEXT);
        y += 5;
        int ambient = ambientEntries().size();
        y = paragraph(graphics, "Optional physical records: " + ambient + "/" + AmbientLoreCatalog.count()
                + ". These preserve personal, maintenance and operational details without gating the main Incident reconstruction.",
                x, y, maxWidth, ambient == AmbientLoreCatalog.count() ? GOOD : MUTED);
        y += 5;
        int technology = technologyEntries().size();
        y = paragraph(graphics, "Technology codex: " + technology + "/" + TechnologyLoreCatalog.count()
                + ". Major machines, field tools and equipment unlock when first acquired, crafted or placed.",
                x, y, maxWidth, technology == TechnologyLoreCatalog.count() ? GOOD : MUTED);
        y += 6;
        y = paragraph(graphics,
                "Archive entries are displayed in actual incident chronology. Your discovery order remains non-linear: any facility can provide evidence for later or earlier events.",
                x, y, maxWidth, MUTED);
        y += 7;

        if (recoveredCount() == StructureLoreCatalog.RECORD_COUNT) {
            y = paragraph(graphics,
                    "ARCHIVE COMPLETE — THE CLOSED LOOP is authenticated. M-0, OVERDRIVE and the ICARUS event form a causal chain with no identified external beginning.",
                    x, y, maxWidth, GOOD);
            y += 5;
            y = paragraph(graphics, "STANDING INSTRUCTION: DO NOT COMPLETE THE LOOP.", x, y, maxWidth, DANGER);
        } else {
            int next = firstMissingArchive();
            if (next >= 0) {
                LoreRecord record = StructureLoreCatalog.byArchiveIndex(next + 1);
                y = paragraph(graphics,
                        "Unresolved evidence remains. Cross-references may reveal facilities without requiring a fixed quest sequence.",
                        x, y, maxWidth, MUTED);
                y += 4;
                graphics.drawString(font,
                        Component.literal("Earliest canonical gap: Archive Entry " + (record == null ? next + 1 : record.archiveIndex())),
                        x, y, CYAN, false);
                y += 13;
            }
        }

        if (y < bottom - 40) {
            Reconstruction nextReconstruction = nextIncompleteReconstruction();
            graphics.drawString(font, Component.literal("ACTIVE THEORY"), x, y, CYAN, false);
            y += 13;
            if (nextReconstruction == null) {
                paragraph(graphics, "All five incident reconstructions are authenticated.", x, y, maxWidth, GOOD);
            } else {
                int have = StructureLoreCatalog.reconstructionEvidenceCount(loreMask, nextReconstruction);
                y = paragraph(graphics, nextReconstruction.title() + " — evidence " + have + "/"
                        + nextReconstruction.evidenceRequired(), x, y, maxWidth, ORANGE);
                paragraph(graphics, nextReconstruction.subtitle(), x, y + 2, maxWidth, MUTED);
            }
        }
    }

    private void renderDataBank(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        LoreRecord record = StructureLoreCatalog.byArchiveIndex(archiveCursor + 1);
        if (record == null) return;
        boolean recovered = StructureLoreCatalog.recovered(loreMask, record);

        graphics.drawString(font,
                Component.literal("ARCHIVE " + record.archiveIndex() + "/" + StructureLoreCatalog.RECORD_COUNT
                        + " // FILE " + (archiveSection + 1) + "/" + ARCHIVE_SECTIONS),
                x, y, CYAN, false);
        graphics.drawString(font, Component.literal(record.timestamp()), x + Math.max(130, maxWidth - 92), y, MUTED, false);
        y += 16;

        if (!recovered) {
            graphics.drawString(font, Component.literal("[ RECORD NOT RECOVERED ]"), x, y, LOCKED, false);
            y += 15;
            paragraph(graphics,
                    "This chronological slot is unresolved. Locate Matter Overdrive facilities and recover their local records to authenticate the missing evidence.",
                    x, y, maxWidth, MUTED);
            return;
        }

        if (archiveSection == 0) {
            graphics.drawString(font, Component.literal(record.title()), x, y, ORANGE, false);
            y += 13;
            graphics.drawString(font, Component.literal(record.facility()), x, y, CYAN, false);
            y += 12;
            graphics.drawString(font, Component.literal(record.classification()), x, y, MUTED, false);
            y += 12;
            graphics.drawString(font, Component.literal("SOURCE: " + record.author()), x, y, MUTED, false);
            y += 17;
            graphics.drawString(font, Component.literal("SITE FUNCTION"), x, y, CYAN, false);
            y += 12;
            y = paragraph(graphics, record.sitePurpose(), x, y, maxWidth, TEXT);
            y += 7;
            if (y < bottom - 20) {
                graphics.drawString(font, Component.literal("PRIMARY RECORD"), x, y, CYAN, false);
                y += 12;
                paragraph(graphics, record.summary(), x, y, maxWidth, TEXT);
            }
        } else if (archiveSection == 1) {
            graphics.drawString(font, Component.literal(record.title()), x, y, ORANGE, false);
            y += 15;
            graphics.drawString(font, Component.literal("RECOVERED EXCERPT"), x, y, CYAN, false);
            y += 13;
            y = paragraph(graphics, record.excerpt(), x, y, maxWidth, TEXT);
            y += 8;
            if (y < bottom - 25) {
                graphics.drawString(font, Component.literal("PDA FORENSIC ANALYSIS"), x, y, CYAN, false);
                y += 13;
                paragraph(graphics, record.analysis(), x, y, maxWidth, MUTED);
            }
        } else {
            graphics.drawString(font, Component.literal(record.title()), x, y, ORANGE, false);
            y += 15;
            graphics.drawString(font, Component.literal("INCIDENT IMPLICATION"), x, y, CYAN, false);
            y += 13;
            y = paragraph(graphics, record.implication(), x, y, maxWidth, TEXT);
            y += 9;
            graphics.drawString(font, Component.literal("CROSS-REFERENCE"), x, y, CYAN, false);
            y += 13;
            y = paragraph(graphics, record.link(), x, y, maxWidth, MUTED);
            y += 9;
            if (y < bottom - 22) {
                graphics.drawString(font, Component.literal("RECOVERY STATUS: AUTHENTICATED"), x, y, GOOD, false);
                graphics.drawString(font,
                        Component.literal("RECONSTRUCTION: " + recoveredCount() + "/" + StructureLoreCatalog.RECORD_COUNT),
                        x, y + 12, ORANGE, false);
            }
        }
    }

    private void renderIncident(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        Reconstruction reconstruction = StructureLoreCatalog.reconstructionByIndex(reconstructionCursor);
        if (reconstruction == null) return;
        boolean unlocked = StructureLoreCatalog.reconstructionUnlocked(loreMask, reconstruction);
        int have = StructureLoreCatalog.reconstructionEvidenceCount(loreMask, reconstruction);

        graphics.drawString(font,
                Component.literal("RECONSTRUCTION " + (reconstructionCursor + 1) + "/"
                        + StructureLoreCatalog.reconstructions().size()), x, y, CYAN, false);
        graphics.drawString(font,
                Component.literal("EVIDENCE " + have + "/" + reconstruction.evidenceRequired()),
                x + Math.max(128, maxWidth - 92), y, unlocked ? GOOD : ORANGE, false);
        y += 16;
        graphics.drawString(font, Component.literal(reconstruction.title()), x, y, unlocked ? ORANGE : LOCKED, false);
        y += 13;
        y = paragraph(graphics, reconstruction.subtitle(), x, y, maxWidth, MUTED);
        y += 7;

        if (!unlocked) {
            graphics.drawString(font, Component.literal("[ INSUFFICIENT CORROBORATION ]"), x, y, LOCKED, false);
            y += 15;
            y = paragraph(graphics,
                    "This theory remains quarantined until every required source has been independently recovered.",
                    x, y, maxWidth, MUTED);
            y += 7;
            graphics.drawString(font, Component.literal("REQUIRED SOURCE CHAIN"), x, y, CYAN, false);
            y += 13;
            for (LoreRecord record : StructureLoreCatalog.archiveRecords()) {
                if ((reconstruction.requiredMask() & record.mask()) == 0 || y >= bottom - 10) continue;
                boolean recovered = StructureLoreCatalog.recovered(loreMask, record);
                graphics.drawString(font,
                        Component.literal((recovered ? "[✓] " : "[ ] ") + "Archive " + record.archiveIndex() + " — "
                                + (recovered ? fit(record.facility(), maxWidth - 92) : "UNRESOLVED SOURCE")),
                        x, y, recovered ? TEXT : LOCKED, false);
                y += 11;
            }
            return;
        }

        graphics.drawString(font, Component.literal("AUTHENTICATED RECONSTRUCTION"), x, y, GOOD, false);
        y += 15;
        y = paragraph(graphics, reconstruction.lead(), x, y, maxWidth, TEXT);
        y += 7;
        if (y < bottom - 28) y = paragraph(graphics, reconstruction.evidence(), x, y, maxWidth, TEXT);
        y += 7;
        if (y < bottom - 28) y = paragraph(graphics, reconstruction.analysis(), x, y, maxWidth, MUTED);
        y += 8;
        if (y < bottom - 20) paragraph(graphics, reconstruction.conclusion(), x, y, maxWidth,
                reconstruction.id().equals("closed_loop") ? DANGER : ORANGE);
    }

    private void renderFacilities(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        int from = facilityPage * 8;
        int to = Math.min(from + 8, StructureLoreCatalog.RECORD_COUNT);
        graphics.drawString(font, Component.literal("DISCOVERED FACILITY INDEX // " + (facilityPage + 1) + "/2"), x, y, CYAN, false);
        y += 16;
        for (int i = from; i < to && y < bottom - 12; i++) {
            LoreRecord record = StructureLoreCatalog.byArchiveIndex(i + 1);
            if (record == null) continue;
            boolean recovered = StructureLoreCatalog.recovered(loreMask, record);
            String prefix = recovered ? "[✓] " : "[ ] ";
            String name = recovered ? record.facility() : "UNKNOWN FACILITY";
            int color = recovered ? TEXT : LOCKED;
            graphics.drawString(font,
                    Component.literal(prefix + String.format("%02d", record.archiveIndex()) + "  " + fit(name, maxWidth - 46)),
                    x, y, color, false);
            y += 11;
            if (recovered) {
                graphics.drawString(font,
                        Component.literal("    " + fit(record.timestamp() + " // " + record.chapter(), maxWidth - 24)),
                        x, y, MUTED, false);
                y += 11;
            } else {
                y += 5;
            }
        }
    }

    private void renderFieldLogs(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        List<Entry> entries = ambientEntries();
        if (entries.isEmpty()) {
            graphics.drawString(font, Component.literal("NO OPTIONAL FIELD LOGS AUTHENTICATED"), x, y, LOCKED, false);
            y += 16;
            paragraph(graphics,
                    "Search facility crates, salvage caches and archive rooms for physical records. Right-click a recovered fragment to authenticate it into this index.",
                    x, y, maxWidth, MUTED);
            return;
        }
        ambientCursor = Math.max(0, Math.min(ambientCursor, entries.size() - 1));
        Entry entry = entries.get(ambientCursor);
        graphics.drawString(font,
                Component.literal("PHYSICAL RECORD " + (ambientCursor + 1) + "/" + entries.size()
                        + " // COLLECTION " + entries.size() + "/" + AmbientLoreCatalog.count()),
                x, y, CYAN, false);
        y += 15;
        graphics.drawString(font, Component.literal(entry.title()), x, y, ORANGE, false);
        y += 13;
        graphics.drawString(font, Component.literal(entry.classification()), x, y, MUTED, false);
        y += 11;
        graphics.drawString(font, Component.literal("SITE: " + entry.siteId().replace('_', ' ').toUpperCase()), x, y, CYAN, false);
        y += 11;
        graphics.drawString(font, Component.literal("SOURCE: " + fit(entry.source(), maxWidth - 50)), x, y, MUTED, false);
        y += 16;
        graphics.drawString(font, Component.literal("RECOVERED EXCERPT"), x, y, CYAN, false);
        y += 12;
        y = paragraph(graphics, entry.excerpt(), x, y, maxWidth, TEXT);
        y += 7;
        if (y < bottom - 30) {
            graphics.drawString(font, Component.literal("PDA ANALYSIS"), x, y, CYAN, false);
            y += 12;
            paragraph(graphics, entry.analysis(), x, y, maxWidth, MUTED);
        }
    }

    private void renderTechnology(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        List<TechRecord> entries = technologyEntries();
        if (entries.isEmpty()) {
            graphics.drawString(font, Component.literal("NO MAJOR TECHNOLOGY INDEXED"), x, y, LOCKED, false);
            y += 16;
            paragraph(graphics,
                    "Craft, recover, pick up or place major Matter Overdrive machines and equipment. The first encounter with each technology authenticates its codex entry and queues a short PDA callout.",
                    x, y, maxWidth, MUTED);
            return;
        }
        technologyCursor = Math.max(0, Math.min(technologyCursor, entries.size() - 1));
        TechRecord entry = entries.get(technologyCursor);
        graphics.drawString(font,
                Component.literal("TECH RECORD " + (technologyCursor + 1) + "/" + entries.size()
                        + " // INDEX " + entries.size() + "/" + TechnologyLoreCatalog.count()),
                x, y, CYAN, false);
        y += 15;
        graphics.drawString(font, Component.literal(entry.title()), x, y, ORANGE, false);
        y += 13;
        graphics.drawString(font, Component.literal(entry.category()), x, y, CYAN, false);
        y += 11;
        graphics.drawString(font, Component.literal("REGISTRY: matteroverdrive:" + entry.id()), x, y, MUTED, false);
        y += 16;

        graphics.drawString(font, Component.literal("FUNCTION"), x, y, CYAN, false);
        y += 12;
        y = paragraph(graphics, entry.function(), x, y, maxWidth, TEXT);
        y += 7;

        if (y < bottom - 42) {
            graphics.drawString(font, Component.literal("ARCHIVE CONTEXT"), x, y, CYAN, false);
            y += 12;
            y = paragraph(graphics, entry.lore(), x, y, maxWidth, MUTED);
            y += 7;
        }
        if (y < bottom - 28) {
            graphics.drawString(font, Component.literal("FIELD NOTE"), x, y, CYAN, false);
            y += 12;
            paragraph(graphics, entry.fieldNote(), x, y, maxWidth, GOOD);
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
            String progress = ContractItem.complete(contract) ? "READY TO REDEEM"
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
            if (line.equals("--- Recovered Logs ---") || line.equals("--- Field Operations ---")) break;
            if (!line.startsWith("@lore:") && !line.startsWith("@tech:")) result.add(line);
        }
        for (String line : journal) {
            if (line.equals("--- Progression ---")) {
                progression = true;
                result.add(line);
                continue;
            }
            if (line.equals("--- Scan History ---")) break;
            if (progression && !line.startsWith("@lore:") && !line.startsWith("@tech:")) result.add(line);
        }
        return result;
    }

    private List<String> operationLines() { return between("--- Field Operations ---", "--- Progression ---"); }

    private List<String> scanLines() {
        List<String> lines = new ArrayList<>();
        boolean capture = false;
        for (String line : journal) {
            if (line.equals("--- Scan History ---")) { capture = true; continue; }
            if (capture) lines.add(line);
        }
        return lines;
    }

    private List<Entry> ambientEntries() {
        List<Entry> entries = new ArrayList<>();
        for (String line : journal) {
            if (!line.startsWith("@lore:")) continue;
            Entry entry = AmbientLoreCatalog.byId(line.substring("@lore:".length()));
            if (entry != null) entries.add(entry);
        }
        return List.copyOf(entries);
    }

    private Entry currentAmbientEntry() {
        List<Entry> entries = ambientEntries();
        if (entries.isEmpty()) return null;
        ambientCursor = Math.max(0, Math.min(ambientCursor, entries.size() - 1));
        return entries.get(ambientCursor);
    }

    private List<TechRecord> technologyEntries() {
        List<TechRecord> entries = new ArrayList<>();
        for (String line : journal) {
            if (!line.startsWith("@tech:")) continue;
            TechRecord entry = TechnologyLoreCatalog.byItemId(line.substring("@tech:".length()));
            if (entry != null) entries.add(entry);
        }
        return List.copyOf(entries);
    }

    private TechRecord currentTechnologyEntry() {
        List<TechRecord> entries = technologyEntries();
        if (entries.isEmpty()) return null;
        technologyCursor = Math.max(0, Math.min(technologyCursor, entries.size() - 1));
        return entries.get(technologyCursor);
    }

    private List<String> between(String start, String end) {
        List<String> lines = new ArrayList<>();
        boolean capture = false;
        for (String line : journal) {
            if (line.equals(start)) { capture = true; continue; }
            if (capture && line.equals(end)) break;
            if (capture && !line.startsWith("@lore:") && !line.startsWith("@tech:")) lines.add(line);
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

    private int firstRecoveredArchiveOrZero() {
        for (int archive = 1; archive <= StructureLoreCatalog.RECORD_COUNT; archive++) {
            LoreRecord record = StructureLoreCatalog.byArchiveIndex(archive);
            if (StructureLoreCatalog.recovered(loreMask, record)) return archive - 1;
        }
        return 0;
    }

    private int firstMissingArchive() {
        for (int archive = 1; archive <= StructureLoreCatalog.RECORD_COUNT; archive++) {
            LoreRecord record = StructureLoreCatalog.byArchiveIndex(archive);
            if (!StructureLoreCatalog.recovered(loreMask, record)) return archive - 1;
        }
        return -1;
    }

    private int firstIncompleteReconstructionOrLast() {
        List<Reconstruction> reconstructions = StructureLoreCatalog.reconstructions();
        for (int i = 0; i < reconstructions.size(); i++) {
            if (!StructureLoreCatalog.reconstructionUnlocked(loreMask, reconstructions.get(i))) return i;
        }
        return Math.max(0, reconstructions.size() - 1);
    }

    private Reconstruction nextIncompleteReconstruction() {
        for (Reconstruction reconstruction : StructureLoreCatalog.reconstructions()) {
            if (!StructureLoreCatalog.reconstructionUnlocked(loreMask, reconstruction)) return reconstruction;
        }
        return null;
    }

    private int recoveredCount() { return Integer.bitCount(loreMask); }
    private int unlockedReconstructions() { return StructureLoreCatalog.unlockedReconstructionCount(loreMask); }

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

    private static String stripHeading(String value) { return value.replace("===", "").replace("---", "").trim(); }

    @Override
    public void removed() {
        PdaNarrationController.stop();
        super.removed();
    }

    @Override public boolean isPauseScreen() { return false; }

    private record ContractRef(int slot, ItemStack stack) {}
}
