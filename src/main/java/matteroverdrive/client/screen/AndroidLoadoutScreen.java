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

/** Constellation-style build screen for Android Aspects, Fragments, Artifacts and Drone Commander specialisation. */
public class AndroidLoadoutScreen extends Screen {
    private static final int BACKDROP = 0xF3080A0D;
    private static final int PANEL = 0xD915171B;
    private static final int TEXT = 0xFFF0EEE8;
    private static final int MUTED = 0xFF8F979F;
    private static final int ACCENT = 0xFF73D9E6;
    private static final int GOLD = 0xFFE9C46A;
    private static final int SELECTED = 0xFFFFFFFF;
    private static final int LOCKED = 0xFF3E454C;
    private static final int DRONE = 0xFFB7D57A;

    private final Map<Button, AndroidLoadout.Aspect> aspectButtons = new LinkedHashMap<>();
    private final Map<Button, AndroidLoadout.Fragment> fragmentButtons = new LinkedHashMap<>();
    private final Map<Button, AndroidLoadout.Artifact> artifactButtons = new LinkedHashMap<>();
    private final Map<Button, AndroidLoadout.DronePerk> droneButtons = new LinkedHashMap<>();
    private int lastAspects;
    private int lastFragments;
    private int lastArtifact;
    private int lastDronePerks;
    private int view;

    public AndroidLoadoutScreen() {
        super(Component.literal("ANDROID MATRIX"));
    }

    @Override
    protected void init() {
        lastAspects = AndroidClientState.aspectMask();
        lastFragments = AndroidClientState.fragmentMask();
        lastArtifact = AndroidClientState.artifactOrdinal();
        lastDronePerks = AndroidClientState.dronePerkMask();
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        aspectButtons.clear();
        fragmentButtons.clear();
        artifactButtons.clear();
        droneButtons.clear();

        int top = 42;
        addRenderableWidget(tab("MATRIX", 18, top, 0));
        addRenderableWidget(tab("DRONE COMMANDER", 92, top, 1));
        addRenderableWidget(tab("ARTIFACT", 222, top, 2));

        if (view == 0) buildMatrixNodes();
        else if (view == 1) buildDroneTree();
        else buildArtifactNodes();

        addRenderableWidget(Button.builder(Component.literal("RESET LOADOUT"), b ->
                ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(2, 0)))
                .bounds(18, height - 30, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"), b -> onClose())
                .bounds(width - 90, height - 30, 72, 20).build());
    }

    private Button tab(String label, int x, int y, int target) {
        Button b = Button.builder(Component.literal((view == target ? "◆ " : "◇ ") + label), button -> {
            view = target;
            rebuild();
        }).bounds(x, y, target == 1 ? 122 : 68, 20).build();
        b.active = view != target;
        return b;
    }

    private void buildMatrixNodes() {
        int cx = Math.min(width / 2 - 70, 470);
        int cy = height / 2 + 10;
        AndroidLoadout.Aspect[] aspects = AndroidLoadout.Aspect.values();
        int[][] aspectPos = {
                {-165,-92},{-105,-118},{-45,-92},
                {-165,70},{-105,96},{-45,70},
                {45,-92},{105,-118},{165,-92},
                {45,70},{105,96},{165,70}
        };
        for (int i = 0; i < aspects.length; i++) {
            AndroidLoadout.Aspect aspect = aspects[i];
            int x = cx + aspectPos[i][0];
            int y = cy + aspectPos[i][1];
            boolean selected = (AndroidClientState.aspectMask() & (1 << i)) != 0;
            Button node = Button.builder(Component.literal(selected ? "✦" : "◇"), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(0, aspect.ordinal())))
                    .bounds(x - 12, y - 12, 24, 24).build();
            addRenderableWidget(node);
            aspectButtons.put(node, aspect);
        }

        AndroidLoadout.Fragment[] fragments = AndroidLoadout.Fragment.values();
        int left = 18;
        int startY = 92;
        for (int i = 0; i < fragments.length; i++) {
            AndroidLoadout.Fragment fragment = fragments[i];
            boolean selected = (AndroidClientState.fragmentMask() & (1 << i)) != 0;
            int col = i / 12;
            int row = i % 12;
            int x = left + col * 146;
            int y = startY + row * 20;
            Button node = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + shortName(fragment.displayName)), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(1, fragment.ordinal())))
                    .bounds(x, y, 140, 18).build();
            addRenderableWidget(node);
            fragmentButtons.put(node, fragment);
        }
    }

    private void buildDroneTree() {
        int left = 52;
        int right = Math.min(width - 310, 600);
        int y = height / 2;
        AndroidLoadout.DronePerk[] perks = AndroidLoadout.DronePerk.values();
        int step = Math.max(48, (right - left) / Math.max(1, perks.length - 1));
        for (int i = 0; i < perks.length; i++) {
            AndroidLoadout.DronePerk perk = perks[i];
            int x = left + i * step;
            int py = y + ((i & 1) == 0 ? -28 : 28);
            boolean selected = AndroidClientState.hasDronePerk(perk);
            Button node = Button.builder(Component.literal(selected ? "✦" : "◇"), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(4, perk.ordinal())))
                    .bounds(x - 13, py - 13, 26, 26).build();
            node.active = selected || AndroidClientState.level() >= perk.level;
            addRenderableWidget(node);
            droneButtons.put(node, perk);
        }
    }

    private void buildArtifactNodes() {
        int cx = Math.min(width / 2 - 70, 430);
        int cy = height / 2 + 12;
        AndroidLoadout.Artifact[] artifacts = AndroidLoadout.Artifact.values();
        for (int i = 1; i < artifacts.length; i++) {
            double angle = (Math.PI * 2.0D * (i - 1)) / (artifacts.length - 1) - Math.PI / 2.0D;
            int x = cx + (int)(Math.cos(angle) * 142.0D);
            int y = cy + (int)(Math.sin(angle) * 92.0D);
            AndroidLoadout.Artifact artifact = artifacts[i];
            boolean selected = AndroidClientState.artifactOrdinal() == i;
            Button node = Button.builder(Component.literal(selected ? "✦" : "◇"), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(3, artifact.ordinal())))
                    .bounds(x - 14, y - 14, 28, 28).build();
            addRenderableWidget(node);
            artifactButtons.put(node, artifact);
        }
        addRenderableWidget(Button.builder(Component.literal("UNEQUIP ARTIFACT"), b ->
                ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(3, AndroidLoadout.Artifact.NONE.ordinal())))
                .bounds(cx - 62, cy - 10, 124, 20).build());
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
        if (lastAspects != AndroidClientState.aspectMask() || lastFragments != AndroidClientState.fragmentMask()
                || lastArtifact != AndroidClientState.artifactOrdinal() || lastDronePerks != AndroidClientState.dronePerkMask()) {
            lastAspects = AndroidClientState.aspectMask();
            lastFragments = AndroidClientState.fragmentMask();
            lastArtifact = AndroidClientState.artifactOrdinal();
            lastDronePerks = AndroidClientState.dronePerkMask();
            rebuild();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, BACKDROP);
        drawAtmosphere(g);
        g.drawString(font, "ANDROID // CONSTELLATION MATRIX", 18, 14, TEXT, false);
        g.drawString(font, "Build identity through connected systems", 18, 27, MUTED, false);
        g.drawString(font, "LEVEL " + AndroidClientState.level() + "   FE " + AndroidClientState.energy(), width - 150, 16, ACCENT, false);

        if (view == 0) renderMatrixLines(g);
        else if (view == 1) renderDroneLines(g);
        else renderArtifactLines(g);

        super.render(g, mouseX, mouseY, partialTick);
        renderInspectionPanel(g);
    }

    private void drawAtmosphere(GuiGraphics g) {
        for (int x = 0; x < width; x += 54) g.fill(x, 62, x + 1, height - 40, 0x101F2B33);
        for (int y = 62; y < height - 40; y += 46) g.fill(0, y, width, y + 1, 0x101F2B33);
        g.fill(width - 286, 62, width - 18, height - 42, PANEL);
        g.fill(width - 283, 65, width - 21, height - 45, 0x261AEEF2);
    }

    private void renderMatrixLines(GuiGraphics g) {
        int cx = Math.min(width / 2 - 70, 470);
        int cy = height / 2 + 10;
        g.fill(cx - 34, cy - 34, cx + 34, cy + 34, 0x551AEEF2);
        g.fill(cx - 18, cy - 18, cx + 18, cy + 18, 0xAA20262C);
        g.drawCenteredString(font, "CORE", cx, cy - 4, GOLD);
        for (Button button : aspectButtons.keySet()) {
            drawLine(g, cx, cy, button.getX() + 12, button.getY() + 12, 0x665D6871);
        }
        g.drawString(font, "ASPECTS " + aspectCount() + "/" + AndroidLoadout.MAX_ASPECTS, 18, 70, GOLD, false);
        g.drawString(font, "FRAGMENTS " + fragmentCount() + "/" + fragmentCapacity(), 164, 70, ACCENT, false);
    }

    private void renderDroneLines(GuiGraphics g) {
        Button previous = null;
        for (Map.Entry<Button, AndroidLoadout.DronePerk> entry : droneButtons.entrySet()) {
            Button button = entry.getKey();
            if (previous != null) drawLine(g, previous.getX() + 13, previous.getY() + 13, button.getX() + 13, button.getY() + 13, 0x887A9164);
            previous = button;
            g.drawCenteredString(font, Integer.toString(entry.getValue().level), button.getX() + 13, button.getY() - 12,
                    AndroidClientState.level() >= entry.getValue().level ? DRONE : LOCKED);
        }
        g.drawString(font, "DRONE COMMANDER SPECIALISATION", 18, 70, DRONE, false);
        g.drawString(font, Integer.bitCount(AndroidClientState.dronePerkMask()) + "/" + AndroidLoadout.MAX_DRONE_PERKS + " nodes installed · sequential progression", 18, 82, MUTED, false);
    }

    private void renderArtifactLines(GuiGraphics g) {
        int cx = Math.min(width / 2 - 70, 430);
        int cy = height / 2 + 12;
        for (Button button : artifactButtons.keySet()) {
            drawLine(g, cx, cy, button.getX() + 14, button.getY() + 14, 0x77796B48);
        }
        g.drawString(font, "ARTIFACT CORE", 18, 70, GOLD, false);
        g.drawString(font, "Equip one build-defining artifact. Current: " + AndroidClientState.artifact().displayName, 18, 82, MUTED, false);
    }

    private void renderInspectionPanel(GuiGraphics g) {
        Object inspected = null;
        for (Map.Entry<Button, AndroidLoadout.Aspect> e : aspectButtons.entrySet()) if (e.getKey().isHoveredOrFocused()) { inspected = e.getValue(); break; }
        if (inspected == null) for (Map.Entry<Button, AndroidLoadout.Fragment> e : fragmentButtons.entrySet()) if (e.getKey().isHoveredOrFocused()) { inspected = e.getValue(); break; }
        if (inspected == null) for (Map.Entry<Button, AndroidLoadout.Artifact> e : artifactButtons.entrySet()) if (e.getKey().isHoveredOrFocused()) { inspected = e.getValue(); break; }
        if (inspected == null) for (Map.Entry<Button, AndroidLoadout.DronePerk> e : droneButtons.entrySet()) if (e.getKey().isHoveredOrFocused()) { inspected = e.getValue(); break; }

        int x = width - 268;
        int y = 82;
        g.drawString(font, "SYSTEM INSPECTION", x, y, MUTED, false);
        if (inspected == null) {
            g.drawString(font, view == 0 ? "Hover an Aspect or Fragment" : view == 1 ? "Hover a Drone Commander node" : "Hover an Artifact node", x, y + 24, TEXT, false);
            g.drawString(font, "to inspect its exact function.", x, y + 37, MUTED, false);
            if (view == 0) {
                g.drawString(font, "ASPECTS", x, y + 70, MUTED, false);
                g.drawString(font, aspectCount() + " / " + AndroidLoadout.MAX_ASPECTS, x, y + 83, GOLD, false);
                g.drawString(font, "FRAGMENTS", x, y + 106, MUTED, false);
                g.drawString(font, fragmentCount() + " / " + fragmentCapacity(), x, y + 119, ACCENT, false);
            } else if (view == 1) {
                g.drawString(font, "SPECIALISATION", x, y + 70, MUTED, false);
                g.drawString(font, Integer.bitCount(AndroidClientState.dronePerkMask()) + " / " + AndroidLoadout.MAX_DRONE_PERKS, x, y + 83, DRONE, false);
                drawWrapped(g, "Drone Commander nodes unlock by Android level and must be taken in sequence. They are a separate specialist layer from Ascension Points.", x, y + 110, 230, TEXT);
            } else {
                g.drawString(font, "EQUIPPED ARTIFACT", x, y + 70, MUTED, false);
                g.drawString(font, AndroidClientState.artifact().displayName, x, y + 83, GOLD, false);
                drawWrapped(g, AndroidClientState.artifact().description, x, y + 108, 230, TEXT);
            }
            return;
        }

        if (inspected instanceof AndroidLoadout.Aspect aspect) {
            boolean selected = (AndroidClientState.aspectMask() & (1 << aspect.ordinal())) != 0;
            g.drawString(font, aspect.displayName.toUpperCase(), x, y + 24, selected ? SELECTED : GOLD, false);
            g.drawString(font, "ASPECT · " + aspect.branch.toUpperCase(), x, y + 38, ACCENT, false);
            g.drawString(font, "+" + aspect.fragmentSlots + " FRAGMENT CAPACITY", x, y + 58, GOLD, false);
            drawWrapped(g, aspect.description, x, y + 82, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.Fragment fragment) {
            boolean selected = (AndroidClientState.fragmentMask() & (1 << fragment.ordinal())) != 0;
            g.drawString(font, fragment.displayName.toUpperCase(), x, y + 24, selected ? SELECTED : ACCENT, false);
            g.drawString(font, "FRAGMENT · 1 SLOT", x, y + 38, ACCENT, false);
            drawWrapped(g, fragment.description, x, y + 66, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.Artifact artifact) {
            boolean selected = AndroidClientState.artifactOrdinal() == artifact.ordinal();
            g.drawString(font, artifact.displayName.toUpperCase(), x, y + 24, selected ? SELECTED : GOLD, false);
            g.drawString(font, "ARTIFACT · " + (selected ? "EQUIPPED" : "AVAILABLE"), x, y + 38, GOLD, false);
            drawWrapped(g, artifact.description, x, y + 66, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.DronePerk perk) {
            boolean selected = AndroidClientState.hasDronePerk(perk);
            boolean locked = AndroidClientState.level() < perk.level;
            g.drawString(font, perk.displayName.toUpperCase(), x, y + 24, selected ? SELECTED : DRONE, false);
            g.drawString(font, "DRONE NODE · LEVEL " + perk.level + (selected ? " · INSTALLED" : locked ? " · LOCKED" : " · AVAILABLE"), x, y + 38, selected ? DRONE : locked ? LOCKED : TEXT, false);
            drawWrapped(g, perk.description, x, y + 66, 230, TEXT);
        }
    }

    private void drawWrapped(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        for (var line : font.split(Component.literal(text), maxWidth)) {
            g.drawString(font, line, x, y, color, false);
            y += 11;
        }
    }

    private void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps <= 0) return;
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            g.fill(x, y, x + 1, y + 1, color);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
