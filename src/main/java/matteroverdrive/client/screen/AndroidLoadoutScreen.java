package matteroverdrive.client.screen;

import matteroverdrive.android.AndroidLoadout;
import matteroverdrive.android.AndroidUltimates;
import matteroverdrive.client.AndroidClientState;
import matteroverdrive.network.AndroidLoadoutSelectPacket;
import matteroverdrive.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.LinkedHashMap;
import java.util.Map;

/** Destiny-inspired Android subclass screen with class, Ultimate, Aspect, Fragment and passive loadout slots. */
public class AndroidLoadoutScreen extends Screen {
    private static final int BACKDROP = 0xF3080A0D;
    private static final int PANEL = 0xE015171B;
    private static final int TEXT = 0xFFF0EEE8;
    private static final int MUTED = 0xFF8F979F;
    private static final int ACCENT = 0xFF73D9E6;
    private static final int GOLD = 0xFFE9C46A;
    private static final int DRONE = 0xFFB7D57A;

    private final Map<Button, Object> inspectables = new LinkedHashMap<>();
    private int lastAspects;
    private int lastFragments;
    private int lastArtifact;
    private int lastDronePerks;
    private int lastSpecialization;
    private int lastUltimateCooldown;
    private int view;

    private boolean hasInspectionPanel() { return width >= 640; }
    private int mainRight() { return hasInspectionPanel() ? width - 316 : Math.max(220, width - 18); }

    public AndroidLoadoutScreen() { super(Component.literal("ANDROID SUBCLASS")); }

    @Override protected void init() { cacheState(); rebuild(); }

    private void cacheState() {
        lastAspects = AndroidClientState.aspectMask();
        lastFragments = AndroidClientState.fragmentMask();
        lastArtifact = AndroidClientState.artifactOrdinal();
        lastDronePerks = AndroidClientState.dronePerkMask();
        lastSpecialization = AndroidClientState.specializationOrdinal();
        lastUltimateCooldown = AndroidClientState.ultimateCooldownTicks();
    }

    private void rebuild() {
        clearWidgets(); inspectables.clear();
        int top = 43;
        addRenderableWidget(viewTab("SUBCLASS", 18, top, 0, 86));
        addRenderableWidget(viewTab("DRONE COMMANDER", 108, top, 1, 126));
        addRenderableWidget(viewTab("PASSIVE", 238, top, 2, 78));
        if (view == 0) buildSubclassView(); else if (view == 1) buildDroneView(); else buildPassiveView();
        addRenderableWidget(Button.builder(Component.literal("RESET LOADOUT"), b -> ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(2, 0))).bounds(18, height - 30, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"), b -> onClose()).bounds(width - 90, height - 30, 72, 20).build());
    }

    private Button viewTab(String label, int x, int y, int target, int width) {
        Button button = Button.builder(Component.literal((view == target ? "◆ " : "◇ ") + label), b -> { view = target; rebuild(); }).bounds(x, y, width, 20).build();
        button.active = view != target; return button;
    }

    private void buildSubclassView() {
        int mainRight = mainRight(), subclassY = 78;
        int available = Math.max(220, mainRight - 36), classWidth = Math.max(54, Math.min(122, (available - 18) / 4)), startX = 18;
        AndroidLoadout.Specialization[] classes = AndroidLoadout.Specialization.values();
        for (int i = 0; i < classes.length; i++) {
            AndroidLoadout.Specialization specialization = classes[i]; boolean selected = AndroidClientState.specializationOrdinal() == i;
            Button button = Button.builder(Component.literal((selected ? "◆ " : "◇ ") + specialization.displayName), b -> ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(5, specialization.ordinal()))).bounds(startX + i * (classWidth + 6), subclassY, classWidth, 22).build();
            button.active = !selected; addRenderableWidget(button); inspectables.put(button, specialization);
        }
        AndroidLoadout.Specialization selectedClass = AndroidClientState.specialization(); AndroidLoadout.Ultimate ultimate = selectedClass.ultimate;
        Button ultimateNode = Button.builder(Component.literal("ULTIMATE // " + ultimate.displayName), b -> {}).bounds(18, 122, Math.max(250, mainRight - 36), 28).build();
        ultimateNode.active = false; addRenderableWidget(ultimateNode); inspectables.put(ultimateNode, ultimate);
        int aspectIndex = 0;
        for (AndroidLoadout.Aspect aspect : AndroidLoadout.Aspect.values()) {
            if (aspect.specialization() != selectedClass) continue;
            boolean selected = (AndroidClientState.aspectMask() & (1 << aspect.ordinal())) != 0; int x = 18 + aspectIndex * 150;
            Button node = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + aspect.displayName), b -> ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(0, aspect.ordinal()))).bounds(x, 180, 144, 24).build();
            addRenderableWidget(node); inspectables.put(node, aspect); aspectIndex++;
        }
        AndroidLoadout.Fragment[] fragments = AndroidLoadout.Fragment.values(); int cols = 4, gap = 5;
        int buttonWidth = Math.max(90, Math.min(130, (mainRight - 36 - gap * (cols - 1)) / cols));
        for (int i = 0; i < fragments.length; i++) {
            AndroidLoadout.Fragment fragment = fragments[i]; boolean selected = (AndroidClientState.fragmentMask() & (1 << i)) != 0;
            int x = 18 + (i % cols) * (buttonWidth + gap), y = 236 + (i / cols) * 23;
            Button node = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + shortName(fragment.displayName)), b -> ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(1, fragment.ordinal()))).bounds(x, y, buttonWidth, 19).build();
            addRenderableWidget(node); inspectables.put(node, fragment);
        }
    }

    private void buildDroneView() {
        int mainRight = mainRight(), centerY = height / 2; AndroidLoadout.DronePerk[] perks = AndroidLoadout.DronePerk.values();
        int left = 42, usable = Math.max(340, mainRight - 72), step = Math.max(38, usable / Math.max(1, perks.length - 1));
        for (int i = 0; i < perks.length; i++) {
            AndroidLoadout.DronePerk perk = perks[i]; boolean selected = AndroidClientState.hasDronePerk(perk); int x = left + i * step, y = centerY + ((i & 1) == 0 ? -34 : 34);
            Button node = Button.builder(Component.literal(selected ? "✦" : "◇"), b -> ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(4, perk.ordinal()))).bounds(x - 13, y - 13, 26, 26).build();
            node.active = selected || AndroidClientState.level() >= perk.level; addRenderableWidget(node); inspectables.put(node, perk);
        }
    }

    /* The old Artifact slot is intentionally retained in save/network data for backwards compatibility,
       but is now presented as a single freely selectable passive protocol. */
    private void buildPassiveView() {
        int mainRight = mainRight(), cols = 2;
        int buttonWidth = Math.max(150, Math.min(220, (mainRight - 54) / cols)), index = 0;
        for (int i = 1; i < AndroidLoadout.Artifact.values().length; i++) {
            AndroidLoadout.Artifact passive = AndroidLoadout.Artifact.values()[i]; boolean selected = AndroidClientState.artifactOrdinal() == i;
            int x = 18 + (index % cols) * (buttonWidth + 12), y = 104 + (index / cols) * 38;
            Button node = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + passiveName(passive)), b -> ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(3, passive.ordinal()))).bounds(x, y, buttonWidth, 28).build();
            addRenderableWidget(node); inspectables.put(node, passive); index++;
        }
        addRenderableWidget(Button.builder(Component.literal("NO PASSIVE"), b -> ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(3, AndroidLoadout.Artifact.NONE.ordinal()))).bounds(18, 104 + ((index + 1) / 2) * 38 + 8, 160, 20).build());
    }

    private static String passiveName(AndroidLoadout.Artifact passive) {
        return passive == AndroidLoadout.Artifact.NONE ? "No Passive" : passive.displayName;
    }

    private static String passiveDescription(AndroidLoadout.Artifact passive) {
        return passive.description;
    }

    private static String shortName(String name) { return name.startsWith("Fragment of ") ? name.substring("Fragment of ".length()) : name; }
    private int aspectCount() { return Integer.bitCount(AndroidClientState.aspectMask()); }
    private int fragmentCount() { return Integer.bitCount(AndroidClientState.fragmentMask()); }
    private int fragmentCapacity() {
        int slots = 0; for (AndroidLoadout.Aspect aspect : AndroidLoadout.Aspect.values()) if ((AndroidClientState.aspectMask() & (1 << aspect.ordinal())) != 0) slots += aspect.fragmentSlots;
        return Math.min(AndroidLoadout.MAX_FRAGMENT_CAPACITY, slots);
    }

    @Override public void tick() {
        if (lastAspects != AndroidClientState.aspectMask() || lastFragments != AndroidClientState.fragmentMask() || lastArtifact != AndroidClientState.artifactOrdinal() || lastDronePerks != AndroidClientState.dronePerkMask() || lastSpecialization != AndroidClientState.specializationOrdinal() || lastUltimateCooldown != AndroidClientState.ultimateCooldownTicks()) { cacheState(); rebuild(); }
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, BACKDROP); drawAtmosphere(g);
        g.drawString(font, fit("ANDROID // SUBCLASS MATRIX", width - 36), 18, 14, TEXT, false);
        g.drawString(font, fit("Define role, Ultimate, Aspects, Fragments and one Passive", width - 36), 18, 27, MUTED, false);
        g.drawString(font, fit("LEVEL " + AndroidClientState.level() + "   FE " + AndroidClientState.energy(), 150),
                Math.max(18, width - 168), 16, ACCENT, false);
        if (view == 0) renderSubclassLabels(g); else if (view == 1) renderDroneLabels(g); else renderPassiveLabels(g);
        super.render(g, mouseX, mouseY, partialTick); renderInspectionPanel(g);
    }

    private void drawAtmosphere(GuiGraphics g) {
        for (int x = 0; x < width; x += 54) g.fill(x, 62, x + 1, height - 40, 0x101F2B33);
        for (int y = 62; y < height - 40; y += 46) g.fill(0, y, width, y + 1, 0x101F2B33);
        if (width >= 640) {
            g.fill(width - 286, 62, width - 18, height - 42, PANEL);
            g.fill(width - 283, 65, width - 21, height - 45, 0x261AEEF2);
        }
    }

    private void renderSubclassLabels(GuiGraphics g) {
        AndroidLoadout.Ultimate ultimate = AndroidClientState.ultimate(); int cooldown = AndroidClientState.ultimateCooldownTicks();
        g.drawString(font, fit("SUBCLASS // " + AndroidClientState.specialization().displayName.toUpperCase(), mainRight() - 18), 18, 108, GOLD, false);
        String status = AndroidClientState.level() < AndroidUltimates.REQUIRED_LEVEL ? "LOCKED · LEVEL " + AndroidUltimates.REQUIRED_LEVEL : cooldown > 0 ? String.format("RECHARGING %.1fs", cooldown / 20.0D) : AndroidClientState.energy() < ultimate.energyCost ? "NEEDS " + ultimate.energyCost + " FE" : "READY · PRESS G";
        g.drawString(font, fit(status, mainRight() - 22), 22, 154, cooldown <= 0 && AndroidClientState.level() >= AndroidUltimates.REQUIRED_LEVEL ? ACCENT : MUTED, false);
        g.drawString(font, fit("ASPECTS " + aspectCount() + "/" + AndroidLoadout.MAX_ASPECTS, mainRight() - 18), 18, 166, GOLD, false);
        g.drawString(font, fit("FRAGMENTS " + fragmentCount() + "/" + fragmentCapacity(), mainRight() - 18), 18, 218, ACCENT, false);
    }

    private void renderDroneLabels(GuiGraphics g) {
        g.drawString(font, fit("DRONE COMMANDER SPECIALISATION", mainRight() - 18), 18, 78, DRONE, false);
        g.drawString(font, fit(Integer.bitCount(AndroidClientState.dronePerkMask()) + "/" + AndroidLoadout.MAX_DRONE_PERKS + " nodes installed · sequential progression", mainRight() - 18), 18, 91, MUTED, false);
    }

    private void renderPassiveLabels(GuiGraphics g) {
        g.drawString(font, fit("PASSIVE PROTOCOL", mainRight() - 18), 18, 78, GOLD, false);
        g.drawString(font, fit("Choose one always-on modifier · Current: " + passiveName(AndroidClientState.artifact()), mainRight() - 18), 18, 91, MUTED, false);
    }

    private void renderInspectionPanel(GuiGraphics g) {
        if (width < 640) return;
        Object inspected = null; for (Map.Entry<Button, Object> entry : inspectables.entrySet()) if (entry.getKey().isHoveredOrFocused()) { inspected = entry.getValue(); break; }
        int x = width - 268, y = 82;
        // Derive the usable text column from the panel instead of relying on a fixed
        // width; fixed widths can collapse under UI scaling and cause one-character wrapping.
        int panelWidth = Math.max(180, width - x - 18);
        g.drawString(font, fit("SYSTEM INSPECTION", panelWidth), x, y, MUTED, false);
        if (inspected == null) {
            g.drawString(font, fit(view == 0 ? AndroidClientState.specialization().displayName.toUpperCase() : view == 1 ? "DRONE COMMANDER" : "PASSIVE PROTOCOL", panelWidth), x, y + 24, TEXT, false);
            if (view == 0) {
                drawWrapped(g, AndroidClientState.specialization().description, x, y + 42, panelWidth, TEXT); g.drawString(font, "ULTIMATE", x, y + 92, MUTED, false);
                g.drawString(font, fit(AndroidClientState.ultimate().displayName, panelWidth), x, y + 105, GOLD, false); drawWrapped(g, AndroidClientState.ultimate().description, x, y + 122, panelWidth, TEXT); g.drawString(font, "COST " + AndroidClientState.ultimate().energyCost + " FE", x, y + 174, ACCENT, false);
            } else if (view == 1) drawWrapped(g, "A separate five-node specialist layer for owned drones. Nodes unlock by Android level and must be progressed in sequence.", x, y + 42, panelWidth, TEXT);
            else { g.drawString(font, fit(passiveName(AndroidClientState.artifact()), panelWidth), x, y + 24, GOLD, false); drawWrapped(g, passiveDescription(AndroidClientState.artifact()), x, y + 42, panelWidth, TEXT); }
            return;
        }
        if (inspected instanceof AndroidLoadout.Specialization specialization) {
            g.drawString(font, fit(specialization.displayName.toUpperCase(), panelWidth), x, y + 24, GOLD, false); drawWrapped(g, specialization.description, x, y + 42, panelWidth, TEXT); g.drawString(font, "ULTIMATE", x, y + 94, MUTED, false); g.drawString(font, fit(specialization.ultimate.displayName, panelWidth), x, y + 107, ACCENT, false); drawWrapped(g, specialization.ultimate.description, x, y + 124, panelWidth, TEXT);
        } else if (inspected instanceof AndroidLoadout.Ultimate ultimate) {
            g.drawString(font, fit(ultimate.displayName.toUpperCase(), panelWidth), x, y + 24, GOLD, false); drawWrapped(g, ultimate.description, x, y + 42, panelWidth, TEXT); g.drawString(font, "ENERGY COST " + ultimate.energyCost + " FE", x, y + 100, ACCENT, false); g.drawString(font, "BASE COOLDOWN " + String.format("%.0fs", ultimate.cooldownTicks / 20.0D), x, y + 114, MUTED, false);
        } else if (inspected instanceof AndroidLoadout.Aspect aspect) {
            g.drawString(font, fit(aspect.displayName.toUpperCase(), panelWidth), x, y + 24, GOLD, false); g.drawString(font, fit(aspect.branch.toUpperCase() + " ASPECT · " + aspect.fragmentSlots + " FRAGMENT SLOTS", panelWidth), x, y + 39, MUTED, false); drawWrapped(g, aspect.description, x, y + 60, panelWidth, TEXT);
        } else if (inspected instanceof AndroidLoadout.Fragment fragment) {
            g.drawString(font, fit(shortName(fragment.displayName).toUpperCase(), panelWidth), x, y + 24, ACCENT, false); g.drawString(font, "FRAGMENT", x, y + 39, MUTED, false); drawWrapped(g, fragment.description, x, y + 60, panelWidth, TEXT);
        } else if (inspected instanceof AndroidLoadout.Artifact passive) {
            g.drawString(font, fit(passiveName(passive).toUpperCase(), panelWidth), x, y + 24, GOLD, false); g.drawString(font, "SELECTABLE PASSIVE", x, y + 39, MUTED, false); drawWrapped(g, passiveDescription(passive), x, y + 60, panelWidth, TEXT);
        } else if (inspected instanceof AndroidLoadout.DronePerk perk) {
            g.drawString(font, fit(perk.displayName.toUpperCase(), panelWidth), x, y + 24, DRONE, false); g.drawString(font, fit("DRONE NODE · LEVEL " + perk.level, panelWidth), x, y + 39, MUTED, false); drawWrapped(g, perk.description, x, y + 60, panelWidth, TEXT);
        }
    }

    private void drawWrapped(GuiGraphics g, String text, int x, int y, int width, int color) {
        int lineY = y; for (FormattedCharSequence line : font.split(Component.literal(text), width)) { g.drawString(font, line, x, lineY, color, false); lineY += 11; }
    }

    private String fit(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        int available = Math.max(0, maxWidth - font.width("…"));
        return font.plainSubstrByWidth(text, available) + "…";
    }

    @Override public boolean isPauseScreen() { return false; }
}
