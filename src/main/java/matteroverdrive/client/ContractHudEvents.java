package matteroverdrive.client;

import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.ContractItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
        int width = minecraft.getWindow().getGuiScaledWidth();
        int panelWidth = 166;
        int panelHeight = 18 + contracts.size() * 22;
        int x = width - panelWidth - 8;
        int y = 8;
        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xB010171D);
        graphics.fill(x, y, x + panelWidth, y + 2, 0xFF38C7E8);
        graphics.drawString(minecraft.font, "CONTRACTS", x + 6, y + 6, 0xEAF4F7, false);

        int rowY = y + 18;
        for (ItemStack contract : contracts) {
            int color = ContractItem.complete(contract) ? 0x57C47A : 0xEAF4F7;
            String title = trim(ContractItem.title(contract), 24);
            graphics.drawString(minecraft.font, title, x + 6, rowY, color, false);
            graphics.drawString(minecraft.font,
                    ContractItem.progress(contract) + " / " + ContractItem.goal(contract),
                    x + 6, rowY + 10, 0x9EB1B8, false);
            rowY += 22;
        }
    }

    private static String trim(String text, int max) {
        if (text == null) return "Contract";
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 1)) + "…";
    }
}
