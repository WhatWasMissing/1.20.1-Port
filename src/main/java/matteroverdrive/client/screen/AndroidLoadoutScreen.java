package matteroverdrive.client.screen;

import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.client.AndroidClientState;
import matteroverdrive.network.AndroidLoadoutSelectPacket;
import matteroverdrive.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

public class AndroidLoadoutScreen extends Screen {
    private static final int BACKDROP = 0xF30A0D12;
    private static final int PANEL = 0xE5161B22;
    private static final int TEXT = 0xFFE7E9EC;
    private static final int MUTED = 0xFF8E98A5;
    private static final int ACCENT = 0xFF67D9E8;
    private static final int GOLD = 0xFFE9C46A;
    private static final int SELECTED = 0xFFF5F7FA;
    private static final int LOCKED = 0xFF414852;

    private final Map<Button, AndroidLoadout.Aspect> aspectButtons = new LinkedHashMap<>();
    private final Map<Button, AndroidLoadout.Fragment> fragmentButtons = new LinkedHashMap<>();
    private int lastAspects;
    private int lastFragments;

    public AndroidLoadoutScreen() {
        super(Component.literal("ANDROID LOADOUT"));
    }

    @Override
    protected void init() {
        lastAspects = AndroidClientState.aspectMask();
        lastFragments = AndroidClientState.fragmentMask();
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        aspectButtons.clear();
        fragmentButtons.clear();

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
            aspectButtons.put(button, aspect);
        }

        int fragmentY = 170;
        AndroidLoadout.Fragment[] fragments = AndroidLoadout.Fragment.values();
        for (int i = 0; i < fragments.length; i++) {
            AndroidLoadout.Fragment fragment = fragments[i];
            boolean selected = (AndroidClientState.fragmentMask() & (1 << i)) != 0;
            int x = left + (i % 3) * 150;
            int y = fragmentY + (i / 3) * 26;
            Button button = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + shortName(fragment.displayName)), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(1, fragment.ordinal())))
                    .bounds(x, y, 140, 20).build();
            addRenderableWidget(button);
            fragmentButtons.put(button, fragment);
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

    private int aspectCount() {
        return Integer.bitCount(AndroidClientState.aspectMask());
    }

    private int fragmentCount() {
        return Integer.bitCount(AndroidClientState.fragmentMask());
    }

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
        g.fill(0, 0, width, height, BACKDROP);
        g.drawString(font, "ANDROID // ASPECT & FRAGMENT MATRIX", 22, 18, TEXT, false);
        g.drawString(font, "ASPECTS  " + aspectCount() + " / " + AndroidLoadout.MAX_ASPECTS, 22, 48, GOLD, false);
        g.drawString(font, "Each Aspect grants Fragment capacity and changes your combat identity.", 22, 59, MUTED, false);
        g.drawString(font, "FRAGMENTS  " + fragmentCount() + " / " + fragmentCapacity(), 22, 146, ACCENT, false);
        g.drawString(font, "Fragments are free to swap and do not consume Ascension Points.", 22, 157, MUTED, false);

        renderInspectionPanel(g);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderInspectionPanel(GuiGraphics g) {
        AndroidLoadout.Aspect inspectedAspect = null;
        AndroidLoadout.Fragment inspectedFragment = null;

        for (Map.Entry<Button, AndroidLoadout.Aspect> entry : aspectButtons.entrySet()) {
            if (entry.getKey().isHoveredOrFocused()) {
                inspectedAspect = entry.getValue();
                break;
            }
        }
        if (inspectedAspect == null) {
            for (Map.Entry<Button, AndroidLoadout.Fragment> entry : fragmentButtons.entrySet()) {
                if (entry.getKey().isHoveredOrFocused()) {
                    inspectedFragment = entry.getValue();
                    break;
                }
            }
        }

        int x = Math.max(482, width - 286);
        int right = width - 18;
        int y = 48;
        if (x >= right - 120) return;

        g.fill(x - 10, y, right, height - 42, PANEL);
        g.fill(x - 7, y + 3, right - 3, height - 45, 0x221AEEF2);
        g.drawString(font, "LOADOUT INSPECTION", x, y + 12, MUTED, false);

        if (inspectedAspect == null && inspectedFragment == null) {
            g.drawString(font, "Hover an Aspect or Fragment", x, y + 38, TEXT, false);
            g.drawString(font, "to inspect exactly what it does.", x, y + 50, MUTED, false);
            g.drawString(font, "EQUIPPED ASPECTS", x, y + 82, MUTED, false);
            g.drawString(font, aspectCount() + " / " + AndroidLoadout.MAX_ASPECTS, x, y + 95,
                    aspectCount() > 0 ? GOLD : LOCKED, false);
            g.drawString(font, "FRAGMENT CAPACITY", x, y + 119, MUTED, false);
            g.drawString(font, fragmentCount() + " / " + fragmentCapacity(), x, y + 132,
                    fragmentCapacity() > 0 ? ACCENT : LOCKED, false);
            drawWrapped(g,
                    "Aspects define your build and grant Fragment slots. Fragments provide smaller modifiers and can be swapped freely.",
                    x, y + 164, Math.max(120, right - x - 12), TEXT);
            return;
        }

        if (inspectedAspect != null) {
            boolean selected = (AndroidClientState.aspectMask() & (1 << inspectedAspect.ordinal())) != 0;
            g.drawString(font, inspectedAspect.displayName.toUpperCase(), x, y + 36,
                    selected ? SELECTED : GOLD, false);
            g.drawString(font, "ASPECT · " + inspectedAspect.branch.toUpperCase(), x, y + 50, ACCENT, false);
            g.drawString(font, selected ? "EQUIPPED" : "AVAILABLE", x, y + 64,
                    selected ? ACCENT : MUTED, false);
            g.drawString(font, "FRAGMENT SLOTS", x, y + 90, MUTED, false);
            g.drawString(font, "+" + inspectedAspect.fragmentSlots + " CAPACITY", x, y + 103, GOLD, false);
            g.drawString(font, "EFFECT", x, y + 131, MUTED, false);
            drawWrapped(g, inspectedAspect.description, x, y + 145, Math.max(120, right - x - 12), TEXT);
            g.drawString(font, selected ? "Click to remove this Aspect." : "Click to equip this Aspect.",
                    x, height - 63, MUTED, false);
            return;
        }

        boolean selected = (AndroidClientState.fragmentMask() & (1 << inspectedFragment.ordinal())) != 0;
        g.drawString(font, inspectedFragment.displayName.toUpperCase(), x, y + 36,
                selected ? SELECTED : ACCENT, false);
        g.drawString(font, "FRAGMENT", x, y + 50, ACCENT, false);
        g.drawString(font, selected ? "EQUIPPED" : "AVAILABLE", x, y + 64,
                selected ? ACCENT : MUTED, false);
        g.drawString(font, "EFFECT", x, y + 92, MUTED, false);
        drawWrapped(g, inspectedFragment.description, x, y + 106, Math.max(120, right - x - 12), TEXT);
        g.drawString(font, "USES 1 FRAGMENT SLOT", x, y + 166, GOLD, false);
        g.drawString(font, selected ? "Click to remove this Fragment." : "Click to equip this Fragment.",
                x, height - 63, MUTED, false);
    }

    private void drawWrapped(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        for (var line : font.split(Component.literal(text), maxWidth)) {
            g.drawString(font, line, x, y, color, false);
            y += 11;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
