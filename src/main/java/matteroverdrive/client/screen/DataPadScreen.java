package matteroverdrive.client.screen;

import matteroverdrive.client.ClientGuideHooks;
import matteroverdrive.item.ContractItem;
import matteroverdrive.network.ModNetwork;
import matteroverdrive.pda.PdaMessage;
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

/** Compact PDA surface. The manual remains available with or without GuideME. */
public class DataPadScreen extends Screen {
    private static final int PANEL_COLOR = 0xE6101820;
    private static final int BORDER_COLOR = 0xFF33CCFF;
    private static final int TEXT_COLOR = 0xFFE6F7FF;
    private static final int MUTED_COLOR = 0xFF9BB6C3;
    private static final int CONTRACTS_PAGE = 1;
    private static final int MAX_MANAGED_CONTRACTS = 4;
    private final List<String> history;
    private final List<Button> abandonButtons = new ArrayList<>();
    private final int[] managedSlots = new int[MAX_MANAGED_CONTRACTS];
    private int page;
    private int armedAbandonSlot = -1;
    private long abandonArmedUntil;
    private Button logButton, contractsButton;

    public DataPadScreen(List<String> history) {
        super(PdaMessage.SCREEN_TITLE.component());
        this.history = new ArrayList<>(history);
        java.util.Arrays.fill(managedSlots, -1);
    }

    @Override protected void init() {
        int centre = width / 2;
        logButton = addRenderableWidget(Button.builder(PdaMessage.FIELD_LOG_BUTTON.component(), b -> setPage(0)).bounds(centre - 104, height - 31, 98, 20).build());
        contractsButton = addRenderableWidget(Button.builder(PdaMessage.CONTRACTS_BUTTON.component(), b -> setPage(1)).bounds(centre + 6, height - 31, 98, 20).build());
        addRenderableWidget(Button.builder(PdaMessage.MANUAL_BUTTON.component(), b -> ClientGuideHooks.openSystemGuide()).bounds(Math.max(14, centre - 188), 18, 72, 18).build());
        int right = Math.min(width - 12, centre + 190);
        for (int row = 0; row < MAX_MANAGED_CONTRACTS; row++) {
            final int index = row;
            abandonButtons.add(addRenderableWidget(Button.builder(PdaMessage.ABANDON_BUTTON.component(), b -> abandon(index)).bounds(right - 72, 70 + row * 40, 58, 16).build()));
        }
        setPage(0);
    }

    private void setPage(int next) {
        page = Math.max(0, Math.min(1, next));
        armedAbandonSlot = -1;
        if (logButton != null) logButton.active = page != 0;
        if (contractsButton != null) contractsButton.active = page != 1;
        refreshContractButtons();
    }

    private void abandon(int buttonIndex) {
        if (buttonIndex < 0 || buttonIndex >= managedSlots.length) return;
        int slot = managedSlots[buttonIndex]; if (slot < 0) return;
        long now = System.currentTimeMillis();
        if (armedAbandonSlot == slot && now <= abandonArmedUntil) { ModNetwork.requestContractAbandon(slot); armedAbandonSlot = -1; }
        else { armedAbandonSlot = slot; abandonArmedUntil = now + 3000L; }
        refreshContractButtons();
    }

    private List<ContractRef> contractRefs() {
        Minecraft mc = Minecraft.getInstance(); List<ContractRef> out = new ArrayList<>();
        if (mc.player == null) return out;
        for (int slot = 0; slot < mc.player.getInventory().items.size(); slot++) {
            ItemStack stack = mc.player.getInventory().items.get(slot);
            if (stack.getItem() instanceof ContractItem) out.add(new ContractRef(slot, stack));
        }
        return out;
    }

    private void refreshContractButtons() {
        if (abandonButtons.isEmpty()) return;
        List<ContractRef> refs = page == CONTRACTS_PAGE ? contractRefs() : List.of();
        if (armedAbandonSlot >= 0 && System.currentTimeMillis() > abandonArmedUntil) armedAbandonSlot = -1;
        for (int i = 0; i < abandonButtons.size(); i++) {
            Button button = abandonButtons.get(i); boolean visible = i < refs.size();
            button.visible = visible; button.active = visible; managedSlots[i] = visible ? refs.get(i).slot() : -1;
            button.setMessage(visible && managedSlots[i] == armedAbandonSlot
                    ? PdaMessage.ABANDON_CONFIRM_BUTTON.component()
                    : PdaMessage.ABANDON_BUTTON.component());
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshContractButtons(); renderBackground(graphics);
        int left = Math.max(12, width / 2 - 190), right = Math.min(width - 12, width / 2 + 190), top = 12, bottom = height - 42;
        graphics.fill(left, top, right, bottom, PANEL_COLOR);
        graphics.fill(left, top, right, top + 2, BORDER_COLOR);
        graphics.fill(left, bottom - 2, right, bottom, BORDER_COLOR);
        graphics.drawCenteredString(font, title, width / 2, top + 10, BORDER_COLOR);
        graphics.drawCenteredString(font, page == 0 ? PdaMessage.FIELD_LOG_HEADER.component() : PdaMessage.CONTRACTS_HEADER.component(), width / 2, top + 27, TEXT_COLOR);
        if (page == 0) renderLog(graphics, left + 14, top + 49, right - left - 28, bottom - 18);
        else renderContracts(graphics, left + 14, top + 49, right - left - 28, bottom - 18);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderLog(GuiGraphics g, int x, int y, int maxWidth, int bottom) {
        if (history.isEmpty()) { g.drawString(font, PdaMessage.EMPTY_LOG.component(), x, y, MUTED_COLOR, false); return; }
        for (String entry : history) {
            int color = entry.startsWith("---") ? BORDER_COLOR : TEXT_COLOR;
            for (FormattedCharSequence line : font.split(Component.literal(entry), maxWidth)) {
                if (y > bottom - 10) { g.drawString(font, PdaMessage.OLDER_ENTRIES.component(), x, bottom - 10, MUTED_COLOR, false); return; }
                g.drawString(font, line, x, y, color, false); y += 10;
            }
            y += 4;
        }
    }

    private void renderContracts(GuiGraphics g, int x, int y, int maxWidth, int bottom) {
        List<ContractRef> contracts = contractRefs();
        if (contracts.isEmpty()) { g.drawString(font, PdaMessage.NO_ACTIVE_CONTRACTS.component(), x, y, MUTED_COLOR, false); return; }
        for (int i = 0; i < Math.min(MAX_MANAGED_CONTRACTS, contracts.size()); i++) {
            ItemStack c = contracts.get(i).stack(); String stage = ContractStageSupport.stageLabel(c);
            g.drawString(font, Component.literal((i + 1) + ". " + trim(ContractItem.title(c), 34)), x, y, ContractItem.complete(c) ? 0xFF57C47A : TEXT_COLOR, false);
            String objective = ContractItem.objectiveText(c); if (!stage.isBlank()) objective = stage + " // " + objective;
            for (FormattedCharSequence line : font.split(Component.literal(objective), Math.max(100, maxWidth - 82))) {
                if (y + 12 > bottom) break;
                g.drawString(font, line, x + 8, y + 11, MUTED_COLOR, false); y += 10;
            }
            y += 20;
            if (y > bottom - 20) break;
        }
    }

    private static String trim(String text, int max) { return text.length() <= max ? text : text.substring(0, max - 1) + "…"; }
    @Override public boolean isPauseScreen() { return false; }
    private record ContractRef(int slot, ItemStack stack) {}
}
