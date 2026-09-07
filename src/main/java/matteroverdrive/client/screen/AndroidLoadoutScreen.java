package matteroverdrive.client.screen;

import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.client.AndroidClientState;
import matteroverdrive.network.AndroidLoadoutSelectPacket;
import matteroverdrive.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AndroidLoadoutScreen extends Screen {
    private int lastAspects;
    private int lastFragments;

    public AndroidLoadoutScreen() { super(Component.literal("ANDROID LOADOUT")); }

    @Override
    protected void init() {
        lastAspects = AndroidClientState.aspectMask();
        lastFragments = AndroidClientState.fragmentMask();
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        int left = 22;
        int aspectY = 72;
        AndroidLoadout.Aspect[] aspects = AndroidLoadout.Aspect.values();
        for (int i = 0; i < aspects.length; i++) {
            AndroidLoadout.Aspect aspect = aspects[i];
            boolean selected = (AndroidClientState.aspectMask() & (1 << i)) != 0;
            int x = left + (i % 3) * 150;
            int y = aspectY + (i / 3) * 34;
            Button button = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + aspect.displayName), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(0, aspect.ordinal())))
                    .bounds(x, y, 140, 24).build();
            addRenderableWidget(button);
        }

        int fragmentY = 170;
        AndroidLoadout.Fragment[] fragments = AndroidLoadout.Fragment.values();
        for (int i = 0; i < fragments.length; i++) {
            AndroidLoadout.Fragment fragment = fragments[i];
            boolean selected = (AndroidClientState.fragmentMask() & (1 << i)) != 0;
            int x = left + (i % 3) * 150;
            int y = fragmentY + (i / 3) * 26;
            addRenderableWidget(Button.builder(Component.literal((selected ? "✦ " : "◇ ") + shortName(fragment.displayName)), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(1, fragment.ordinal())))
                    .bounds(x, y, 140, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("RESET LOADOUT"), b ->
                ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(2, 0)))
                .bounds(22, height - 30, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"), b -> onClose())
                .bounds(width - 92, height - 30, 70, 20).build());
    }

    private static String shortName(String name) {
        return name.startsWith("Fragment of ") ? name.substring("Fragment of ".length()) : name;
    }

    private int aspectCount() { return Integer.bitCount(AndroidClientState.aspectMask()); }
    private int fragmentCount() { return Integer.bitCount(AndroidClientState.fragmentMask()); }
    private int fragmentCapacity() {
        int slots = 0;
        for (AndroidLoadout.Aspect aspect : AndroidLoadout.Aspect.values()) {
            if ((AndroidClientState.aspectMask() & (1 << aspect.ordinal())) != 0) slots += aspect.fragmentSlots;
        }
        return Math.min(AndroidLoadout.MAX_FRAGMENT_CAPACITY, slots);
    }

    @Override
    public void tick() {
        if (lastAspects != AndroidClientState.aspectMask() || lastFragments != AndroidClientState.fragmentMask()) {
            lastAspects = AndroidClientState.aspectMask();
            lastFragments = AndroidClientState.fragmentMask();
            rebuild();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xF30A0D12);
        g.drawString(font, "ANDROID // ASPECT & FRAGMENT MATRIX", 22, 18, 0xFFE7E9EC, false);
        g.drawString(font, "ASPECTS  " + aspectCount() + " / " + AndroidLoadout.MAX_ASPECTS, 22, 48, 0xFFE9C46A, false);
        g.drawString(font, "Each Aspect grants Fragment capacity and changes your combat identity.", 22, 59, 0xFF8E98A5, false);
        g.drawString(font, "FRAGMENTS  " + fragmentCount() + " / " + fragmentCapacity(), 22, 146, 0xFF67D9E8, false);
        g.drawString(font, "Fragments are free to swap and do not consume Ascension Points.", 22, 157, 0xFF8E98A5, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override public boolean isPauseScreen() { return false; }
}
