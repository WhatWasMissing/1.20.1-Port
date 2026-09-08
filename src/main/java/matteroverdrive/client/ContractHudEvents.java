package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.ContractItem;
import matteroverdrive.quest.ContractStageSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/** Compact replacement for the legacy quest HUD, driven directly by synchronized contract stacks. */
@Mod.EventBusSubscriber(modid = MatterOverdrive.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ContractHudEvents {
    private static final int MIN_PANEL_WIDTH = 150;
    private static final int MAX_PANEL_WIDTH = 210;
    private static final int MARGIN = 8;

    private ContractHudEvents() {}

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;

        List<ItemStack> contracts = new ArrayList<>();
        for (ItemStack stack : minecraft.player.getInventory().items) {
            if (stack.getItem() instanceof ContractItem) contracts.add(stack);
            if (contracts.size() >= 3) break;
        }
        if (contracts.isEmpty()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        int guiWidth = event.getWindow().getGuiScaledWidth();
        int guiHeight = event.getWindow().getGuiScaledHeight();

        // Size from the GUI-scaled viewport instead of assuming a fixed desktop-sized canvas.
        // This keeps the HUD usable when Minecraft GUI Scale is increased or the window is narrow.
        int panelWidth = Math.max(MIN_PANEL_WIDTH, Math.min(MAX_PANEL_WIDTH, guiWidth / 3));
        panelWidth = Math.min(panelWidth, Math.max(96, guiWidth - MARGIN * 2));
        int innerWidth = Math.max(72, panelWidth - 12);

        List<ContractLayout> layouts = new ArrayList<>();
        int panelHeight = 18;
        for (ItemStack contract : contracts) {
            String title = fit(font, ContractItem.title(contract), innerWidth);
            String stage = ContractStageSupport.stageLabel(contract);
            List<FormattedCharSequence> objective = font.split(
                    net.minecraft.network.chat.Component.literal(ContractItem.objectiveText(contract)), innerWidth);
            if (objective.isEmpty()) objective = List.of(FormattedCharSequence.forward("", net.minecraft.network.chat.Style.EMPTY));
            if (objective.size() > 2) objective = objective.subList(0, 2);

            String progress = ContractItem.complete(contract)
                    ? "READY TO REDEEM"
                    : ContractItem.progress(contract) + " / " + ContractItem.goal(contract);

            int rowHeight = 10; // title
            if (!stage.isBlank()) rowHeight += 10;
            rowHeight += objective.size() * 10;
            rowHeight += 10; // progress
            rowHeight += 3;  // breathing room
            layouts.add(new ContractLayout(contract, title, fit(font, stage, innerWidth), objective,
                    fit(font, progress, innerWidth), rowHeight));
            panelHeight += rowHeight;
        }

        // If three verbose contracts still exceed the scaled viewport, keep the panel on-screen.
        int maxPanelHeight = Math.max(40, guiHeight - MARGIN * 2);
        panelHeight = Math.min(panelHeight, maxPanelHeight);
        int x = Math.max(MARGIN, guiWidth - panelWidth - MARGIN);
        int y = MARGIN;

        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xB010171D);
        graphics.fill(x, y, x + panelWidth, y + 2, 0xFF38C7E8);
        graphics.drawString(font, "ACTIVE CONTRACTS", x + 6, y + 6, 0xEAF4F7, false);

        int rowY = y + 18;
        int bottom = y + panelHeight - 3;
        for (ContractLayout layout : layouts) {
            if (rowY + 10 > bottom) break;
            int color = ContractItem.complete(layout.contract()) ? 0x57C47A : 0xEAF4F7;
            graphics.drawString(font, layout.title(), x + 6, rowY, color, false);
            rowY += 10;

            if (!layout.stage().isBlank() && rowY + 9 <= bottom) {
                graphics.drawString(font, layout.stage(), x + 6, rowY, 0x78C9DC, false);
                rowY += 10;
            }

            for (FormattedCharSequence line : layout.objective()) {
                if (rowY + 9 > bottom) break;
                graphics.drawString(font, line, x + 6, rowY, 0x9EB1B8, false);
                rowY += 10;
            }

            if (rowY + 9 <= bottom) {
                graphics.drawString(font, layout.progress(), x + 6, rowY,
                        ContractItem.complete(layout.contract()) ? 0x57C47A : 0x78C9DC, false);
                rowY += 10;
            }
            rowY += 3;
        }
    }

    private static String fit(Font font, String text, int maxWidth) {
        if (text == null || text.isBlank()) return "";
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "…";
        int usable = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(text, usable) + ellipsis;
    }

    private record ContractLayout(ItemStack contract, String title, String stage,
                                  List<FormattedCharSequence> objective, String progress, int height) {}
}
