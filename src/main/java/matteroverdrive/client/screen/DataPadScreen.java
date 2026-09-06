package matteroverdrive.client.screen;

import matteroverdrive.client.GuideMeCompatEvents;
import matteroverdrive.item.ContractItem;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.quest.ContractStageSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DataPadScreen extends Screen {
    private static final int PANEL_COLOR = 0xE6101820;
    private static final int BORDER_COLOR = 0xFF33CCFF;
    private static final int TEXT_COLOR = 0xFFE6F7FF;
    private static final int MUTED_COLOR = 0xFF9BB6C3;
    private static final int ACTIVE_CONTRACTS_PAGE = 6;
    private static final int HISTORY_PAGE = 7;
    private static final int MAX_MANAGED_CONTRACTS = 4;

    private static final List<String> TITLES = List.of(
            "Overview",
            "Matter Replication",
            "Power and Machines",
            "Fusion Reactor",
            "Android System",
            "Survival Progression",
            "Active Contracts",
            "Scan History"
    );

    private static final List<List<String>> GUIDE_PAGES = List.of(
            List.of(
                    "Matter Overdrive turns stored matter and Forge Energy into a connected technology chain.",
                    "GuideME is the primary manual when installed. The pages here remain available as a lightweight fallback and the Data Pad still manages scanning and contracts."
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
                    "The Android Station installs Head, Chest, Arms and Legs parts. They unlock Cloak, Force Field, Sonic Shockwave and Ender Teleport; use V to cycle and B to activate by default."
            ),
            List.of(
                    "Tritanium generates from Y -32 to 64. Dilithium generates from Y -64 to 16 in fresh Overworld chunks.",
                    "Mine both ores with an iron-tier pickaxe or better, then process them in a furnace or blast furnace."
            )
    );

    private final List<String> history;
    private final List<Button> abandonButtons = new ArrayList<>();
    private final int[] managedSlots = new int[MAX_MANAGED_CONTRACTS];
    private int page;
    private int armedAbandonSlot = -1;
    private long abandonArmedUntil;
    private Button previousButton;
    private Button nextButton;
    private Button manualButton;

    public DataPadScreen(List<String> history) {
        super(Component.literal("Matter Overdrive Data Pad"));
        this.history = new ArrayList<>(history);
        java.util.Arrays.fill(managedSlots, -1);
    }

    @Override
    protected void init() {
        int y = height - 32;
        previousButton = addRenderableWidget(Button.builder(Component.literal("< Previous"),
                button -> setPage(page - 1)).bounds(width / 2 - 105, y, 96, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal("Next >"),
                button -> setPage(page + 1)).bounds(width / 2 + 9, y, 96, 20).build());

        manualButton = addRenderableWidget(Button.builder(Component.literal("GuideME Manual"), button -> openManual())
                .bounds(Math.max(14, width / 2 - 188), 18, 96, 20).build());
        manualButton.visible = net.minecraftforge.fml.ModList.get().isLoaded("guideme");

        int right = Math.min(width - 12, width / 2 + 190);
        int top = 12;
        for (int row = 0; row < MAX_MANAGED_CONTRACTS; row++) {
            final int buttonIndex = row;
            Button button = addRenderableWidget(Button.builder(Component.literal("ABANDON"),
                    ignored -> abandon(buttonIndex))
                    .bounds(right - 72, top + 48 + row * 42, 58, 16).build());
            abandonButtons.add(button);
        }
        setPage(page);
    }

    private void openManual() {
        GuideMeCompatEvents.openGuide();
    }

    private void setPage(int nextPage) {
        page = Math.max(0, Math.min(TITLES.size() - 1, nextPage));
        armedAbandonSlot = -1;
        if (previousButton != null) previousButton.active = page > 0;
        if (nextButton != null) nextButton.active = page < TITLES.size() - 1;
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
        List<ContractRef> refs = page == ACTIVE_CONTRACTS_PAGE ? contractRefs() : List.of();
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
        if (page == HISTORY_PAGE) {
            renderHistory(graphics, textX, textY, maxWidth);
        } else if (page == ACTIVE_CONTRACTS_PAGE) {
            renderContracts(graphics, textX, textY, maxWidth, bottom);
        } else {
            for (String paragraph : GUIDE_PAGES.get(page)) {
                for (FormattedCharSequence line : font.split(Component.literal(paragraph), maxWidth)) {
                    graphics.drawString(font, line, textX, textY, TEXT_COLOR, false);
                    textY += 11;
                }
                textY += 7;
            }
        }

        graphics.drawString(font, Component.literal(page == ACTIVE_CONTRACTS_PAGE
                        ? "Completed contracts redeem at a Contract Market. Abandon requires two clicks."
                        : "Use on blocks to add scan-history entries."),
                textX, bottom - 15, MUTED_COLOR, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderContracts(GuiGraphics graphics, int x, int y, int maxWidth, int bottom) {
        List<ContractRef> contracts = contractRefs();
        if (contracts.isEmpty()) {
            graphics.drawString(font, Component.literal("No active contracts in your inventory."), x, y, MUTED_COLOR, false);
            return;
        }

        int visible = Math.min(MAX_MANAGED_CONTRACTS, contracts.size());
        for (int i = 0; i < visible; i++) {
            ItemStack contract = contracts.get(i).stack();
            int titleColor = ContractItem.complete(contract) ? 0xFF57C47A : TEXT_COLOR;
            String stage = ContractStageSupport.stageLabel(contract);
            graphics.drawString(font, Component.literal((i + 1) + ". " + trim(ContractItem.title(contract), 31)), x, y, titleColor, false);
            String objective = ContractItem.objectiveText(contract);
            if (!stage.isBlank()) objective = stage + " — " + objective;
            graphics.drawString(font, Component.literal(trim(objective, 39)), x + 8, y + 11, MUTED_COLOR, false);
            String progress = ContractItem.complete(contract)
                    ? "READY TO REDEEM"
                    : "Progress " + ContractItem.progress(contract) + " / " + ContractItem.goal(contract);
            if (ContractItem.xp(contract) > 0) progress += "   XP " + ContractItem.xp(contract);
            graphics.drawString(font, Component.literal(trim(progress, 42)), x + 8, y + 22,
                    ContractItem.complete(contract) ? 0xFF57C47A : BORDER_COLOR, false);
            y += 42;
            if (y > bottom - 45) break;
        }
        if (contracts.size() > visible && y <= bottom - 30) {
            graphics.drawString(font, Component.literal("+ " + (contracts.size() - visible) + " more carried contract(s)"),
                    x, y, MUTED_COLOR, false);
        }
    }

    private int renderHistory(GuiGraphics graphics, int x, int y, int maxWidth) {
        if (history.isEmpty()) {
            graphics.drawString(font, Component.literal("No blocks recorded yet."), x, y, MUTED_COLOR, false);
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

    private static String trim(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 1)) + "…";
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private record ContractRef(int slot, ItemStack stack) {}
}
