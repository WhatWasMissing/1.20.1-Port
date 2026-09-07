package matteroverdrive.client.screen;

import matteroverdrive.android.AndroidClasses;
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

/** Three-class Android loadout screen: class -> specialisation -> ultimate -> aspects -> fragments. */
public class AndroidClassLoadoutScreen extends Screen {
    private static final int BACKDROP = 0xF3080A0D;
    private static final int PANEL = 0xE015171B;
    private static final int TEXT = 0xFFF0EEE8;
    private static final int MUTED = 0xFF8F979F;
    private static final int ACCENT = 0xFF73D9E6;
    private static final int GOLD = 0xFFE9C46A;
    private static final int GREEN = 0xFFB7D57A;

    private final Map<Button, Object> inspectables = new LinkedHashMap<>();
    private int view;
    private int lastAspects;
    private int lastFragments;
    private int lastArtifact;
    private int lastDronePerks;
    private int lastSpecialization;
    private int lastUltimateCooldown;

    public AndroidClassLoadoutScreen() {
        super(Component.literal("ANDROID CLASS MATRIX"));
    }

    @Override
    protected void init() {
        cacheState();
        rebuild();
    }

    private void cacheState() {
        lastAspects = AndroidClientState.aspectMask();
        lastFragments = AndroidClientState.fragmentMask();
        lastArtifact = AndroidClientState.artifactOrdinal();
        lastDronePerks = AndroidClientState.dronePerkMask();
        lastSpecialization = AndroidClientState.specializationOrdinal();
        lastUltimateCooldown = AndroidClientState.ultimateCooldownTicks();
    }

    private void rebuild() {
        clearWidgets();
        inspectables.clear();

        addRenderableWidget(tab("CLASS BUILD", 18, 42, 0, 96));
        addRenderableWidget(tab("DRONE MATRIX", 118, 42, 1, 104));
        addRenderableWidget(tab("ARTIFACT", 226, 42, 2, 82));

        if (view == 0) buildClassView();
        else if (view == 1) buildDroneView();
        else buildArtifactView();

        addRenderableWidget(Button.builder(Component.literal("RESET LOADOUT"), b ->
                ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(2, 0)))
                .bounds(18, height - 30, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"), b -> onClose())
                .bounds(width - 90, height - 30, 72, 20).build());
    }

    private Button tab(String label, int x, int y, int target, int width) {
        Button button = Button.builder(Component.literal((view == target ? "◆ " : "◇ ") + label), b -> {
            view = target;
            rebuild();
        }).bounds(x, y, width, 20).build();
        button.active = view != target;
        return button;
    }

    private int mainRight() {
        return Math.max(470, width - 304);
    }

    private void buildClassView() {
        int right = mainRight();
        AndroidClasses.AndroidClass currentClass = AndroidClasses.fromSpecialization(AndroidClientState.specialization());
        AndroidClasses.AndroidClass[] classes = AndroidClasses.AndroidClass.values();
        int classWidth = Math.max(118, Math.min(164, (right - 48) / 3));
        for (int i = 0; i < classes.length; i++) {
            AndroidClasses.AndroidClass androidClass = classes[i];
            boolean selected = currentClass == androidClass;
            Button button = Button.builder(Component.literal((selected ? "◆ " : "◇ ") + androidClass.displayName), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(5, androidClass.defaultSpecialization.ordinal())))
                    .bounds(18 + i * (classWidth + 6), 76, classWidth, 24).build();
            button.active = !selected;
            addRenderableWidget(button);
            inspectables.put(button, androidClass);
        }

        AndroidLoadout.Specialization[] specializations = AndroidClasses.specializations(currentClass);
        int specWidth = Math.max(132, Math.min(196, (right - 42) / Math.max(1, specializations.length)));
        for (int i = 0; i < specializations.length; i++) {
            AndroidLoadout.Specialization specialization = specializations[i];
            boolean selected = AndroidClientState.specialization() == specialization;
            Button button = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + specialization.displayName), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(5, specialization.ordinal())))
                    .bounds(18 + i * (specWidth + 8), 116, specWidth, 20).build();
            button.active = !selected;
            addRenderableWidget(button);
            inspectables.put(button, specialization);
        }

        AndroidLoadout.Ultimate ultimate = AndroidClientState.ultimate();
        Button ultimateButton = Button.builder(Component.literal("ULTIMATE // " + ultimate.displayName), b -> {})
                .bounds(18, 156, Math.max(280, right - 36), 28).build();
        ultimateButton.active = false;
        addRenderableWidget(ultimateButton);
        inspectables.put(ultimateButton, ultimate);

        int aspectX = 18;
        int aspectIndex = 0;
        for (AndroidLoadout.Aspect aspect : AndroidLoadout.Aspect.values()) {
            if (aspect.specialization() != AndroidClientState.specialization()) continue;
            boolean selected = (AndroidClientState.aspectMask() & (1 << aspect.ordinal())) != 0;
            int x = aspectX + aspectIndex * 154;
            Button button = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + aspect.displayName), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(0, aspect.ordinal())))
                    .bounds(x, 216, 148, 22).build();
            addRenderableWidget(button);
            inspectables.put(button, aspect);
            aspectIndex++;
        }

        AndroidLoadout.Fragment[] fragments = AndroidLoadout.Fragment.values();
        int cols = 3;
        int gap = 6;
        int fragmentWidth = Math.max(110, Math.min(150, (right - 36 - gap * (cols - 1)) / cols));
        for (int i = 0; i < fragments.length; i++) {
            AndroidLoadout.Fragment fragment = fragments[i];
            boolean selected = (AndroidClientState.fragmentMask() & (1 << i)) != 0;
            int col = i % cols;
            int row = i / cols;
            int x = 18 + col * (fragmentWidth + gap);
            int y = 272 + row * 21;
            Button button = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + shortName(fragment.displayName)), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(1, fragment.ordinal())))
                    .bounds(x, y, fragmentWidth, 18).build();
            addRenderableWidget(button);
            inspectables.put(button, fragment);
        }
    }

    private void buildDroneView() {
        int right = mainRight();
        AndroidLoadout.DronePerk[] perks = AndroidLoadout.DronePerk.values();
        int left = 48;
        int yBase = height / 2;
        int usable = Math.max(360, right - 80);
        int step = Math.max(40, usable / Math.max(1, perks.length - 1));
        for (int i = 0; i < perks.length; i++) {
            AndroidLoadout.DronePerk perk = perks[i];
            boolean selected = AndroidClientState.hasDronePerk(perk);
            int x = left + i * step;
            int y = yBase + ((i & 1) == 0 ? -34 : 34);
            Button button = Button.builder(Component.literal(selected ? "✦" : "◇"), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(4, perk.ordinal())))
                    .bounds(x - 13, y - 13, 26, 26).build();
            button.active = selected || AndroidClientState.level() >= perk.level;
            addRenderableWidget(button);
            inspectables.put(button, perk);
        }
    }

    private void buildArtifactView() {
        int right = mainRight();
        int buttonWidth = Math.max(160, Math.min(220, (right - 52) / 2));
        int index = 0;
        for (int i = 1; i < AndroidLoadout.Artifact.values().length; i++) {
            AndroidLoadout.Artifact artifact = AndroidLoadout.Artifact.values()[i];
            boolean selected = AndroidClientState.artifactOrdinal() == i;
            int col = index % 2;
            int row = index / 2;
            Button button = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + artifact.displayName), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(3, artifact.ordinal())))
                    .bounds(18 + col * (buttonWidth + 10), 100 + row * 36, buttonWidth, 26).build();
            addRenderableWidget(button);
            inspectables.put(button, artifact);
            index++;
        }
        addRenderableWidget(Button.builder(Component.literal("UNEQUIP ARTIFACT"), b ->
                ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(3, AndroidLoadout.Artifact.NONE.ordinal())))
                .bounds(18, 100 + ((index + 1) / 2) * 36 + 8, 160, 20).build());
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
                || lastArtifact != AndroidClientState.artifactOrdinal() || lastDronePerks != AndroidClientState.dronePerkMask()
                || lastSpecialization != AndroidClientState.specializationOrdinal()
                || lastUltimateCooldown != AndroidClientState.ultimateCooldownTicks()) {
            cacheState();
            rebuild();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, BACKDROP);
        drawAtmosphere(g);
        AndroidClasses.AndroidClass androidClass = AndroidClasses.fromSpecialization(AndroidClientState.specialization());
        g.drawString(font, "ANDROID // CLASS MATRIX", 18, 14, TEXT, false);
        g.drawString(font, "Three combat frames · swappable specialisations", 18, 27, MUTED, false);
        g.drawString(font, "LEVEL " + AndroidClientState.level() + "   FE " + AndroidClientState.energy(), width - 175, 16, ACCENT, false);

        if (view == 0) {
            g.drawString(font, "CLASS // " + androidClass.displayName.toUpperCase(), 18, 104, GOLD, false);
            g.drawString(font, AndroidClientState.specialization().displayName.toUpperCase() + " SPECIALISATION", 18, 142, MUTED, false);
            AndroidLoadout.Ultimate ultimate = AndroidClientState.ultimate();
            int cooldown = AndroidClientState.ultimateCooldownTicks();
            String status = AndroidClientState.level() < AndroidUltimates.REQUIRED_LEVEL ? "LOCKED · LEVEL " + AndroidUltimates.REQUIRED_LEVEL
                    : cooldown > 0 ? String.format("RECHARGING %.1fs", cooldown / 20.0D)
                    : AndroidClientState.energy() < ultimate.energyCost ? "NEEDS " + ultimate.energyCost + " FE" : "READY · PRESS G";
            g.drawString(font, status, 22, 188, cooldown <= 0 ? ACCENT : MUTED, false);
            g.drawString(font, "ASPECTS " + aspectCount() + "/" + AndroidLoadout.MAX_ASPECTS, 18, 202, GOLD, false);
            g.drawString(font, "FRAGMENTS " + fragmentCount() + "/" + fragmentCapacity(), 18, 254, ACCENT, false);
        } else if (view == 1) {
            g.drawString(font, "DRONE COMMAND MATRIX", 18, 78, GREEN, false);
            g.drawString(font, Integer.bitCount(AndroidClientState.dronePerkMask()) + "/" + AndroidLoadout.MAX_DRONE_PERKS + " nodes installed", 18, 92, MUTED, false);
        } else {
            g.drawString(font, "ARTIFACT CORE", 18, 78, GOLD, false);
            g.drawString(font, "Current: " + AndroidClientState.artifact().displayName, 18, 92, MUTED, false);
        }

        super.render(g, mouseX, mouseY, partialTick);
        renderInspectionPanel(g);
    }

    private void drawAtmosphere(GuiGraphics g) {
        for (int x = 0; x < width; x += 54) g.fill(x, 62, x + 1, height - 40, 0x101F2B33);
        for (int y = 62; y < height - 40; y += 46) g.fill(0, y, width, y + 1, 0x101F2B33);
        g.fill(width - 286, 62, width - 18, height - 42, PANEL);
        g.fill(width - 283, 65, width - 21, height - 45, 0x261AEEF2);
    }

    private void renderInspectionPanel(GuiGraphics g) {
        Object inspected = null;
        for (Map.Entry<Button, Object> entry : inspectables.entrySet()) {
            if (entry.getKey().isHoveredOrFocused()) {
                inspected = entry.getValue();
                break;
            }
        }
        int x = width - 268;
        int y = 82;
        g.drawString(font, "SYSTEM INSPECTION", x, y, MUTED, false);
        if (inspected == null) {
            AndroidClasses.AndroidClass androidClass = AndroidClasses.fromSpecialization(AndroidClientState.specialization());
            g.drawString(font, androidClass.displayName.toUpperCase(), x, y + 24, GOLD, false);
            g.drawString(font, androidClass.subtitle.toUpperCase(), x, y + 38, MUTED, false);
            drawWrapped(g, androidClass.description, x, y + 58, 230, TEXT);
            g.drawString(font, "SPECIALISATION", x, y + 110, MUTED, false);
            g.drawString(font, AndroidClientState.specialization().displayName, x, y + 124, ACCENT, false);
            g.drawString(font, "ULTIMATE", x, y + 150, MUTED, false);
            g.drawString(font, AndroidClientState.ultimate().displayName, x, y + 164, GOLD, false);
            return;
        }

        if (inspected instanceof AndroidClasses.AndroidClass androidClass) {
            g.drawString(font, androidClass.displayName.toUpperCase(), x, y + 24, GOLD, false);
            g.drawString(font, androidClass.subtitle.toUpperCase(), x, y + 38, MUTED, false);
            drawWrapped(g, androidClass.description, x, y + 58, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.Specialization specialization) {
            g.drawString(font, specialization.displayName.toUpperCase(), x, y + 24, GOLD, false);
            drawWrapped(g, specialization.description, x, y + 44, 230, TEXT);
            g.drawString(font, "ULTIMATE", x, y + 100, MUTED, false);
            g.drawString(font, specialization.ultimate.displayName, x, y + 114, ACCENT, false);
        } else if (inspected instanceof AndroidLoadout.Ultimate ultimate) {
            g.drawString(font, ultimate.displayName.toUpperCase(), x, y + 24, GOLD, false);
            drawWrapped(g, ultimate.description, x, y + 44, 230, TEXT);
            g.drawString(font, "COST " + ultimate.energyCost + " FE", x, y + 102, ACCENT, false);
            g.drawString(font, String.format("COOLDOWN %.0fs", ultimate.cooldownTicks / 20.0D), x, y + 116, MUTED, false);
        } else if (inspected instanceof AndroidLoadout.Aspect aspect) {
            g.drawString(font, aspect.displayName.toUpperCase(), x, y + 24, GOLD, false);
            g.drawString(font, aspect.fragmentSlots + " FRAGMENT SLOTS", x, y + 38, ACCENT, false);
            drawWrapped(g, aspect.description, x, y + 58, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.Fragment fragment) {
            g.drawString(font, shortName(fragment.displayName).toUpperCase(), x, y + 24, ACCENT, false);
            drawWrapped(g, fragment.description, x, y + 44, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.DronePerk perk) {
            g.drawString(font, perk.displayName.toUpperCase(), x, y + 24, GREEN, false);
            g.drawString(font, "LEVEL " + perk.level, x, y + 38, MUTED, false);
            drawWrapped(g, perk.description, x, y + 58, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.Artifact artifact) {
            g.drawString(font, artifact.displayName.toUpperCase(), x, y + 24, GOLD, false);
            drawWrapped(g, artifact.description, x, y + 44, 230, TEXT);
        }
    }

    private void drawWrapped(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        int lineY = y;
        for (FormattedCharSequence line : font.split(Component.literal(text), maxWidth)) {
            g.drawString(font, line, x, lineY, color, false);
            lineY += 12;
        }
    }
}
