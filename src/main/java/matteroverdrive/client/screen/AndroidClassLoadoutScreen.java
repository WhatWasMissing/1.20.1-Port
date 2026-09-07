package matteroverdrive.client.screen;

import matteroverdrive.android.AndroidClassAbilities;
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

/** Final unified Android build screen: 3 classes x 3 subclasses with unique H/N/G abilities. */
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
    private int lastAspects, lastFragments, lastArtifact, lastDronePerks, lastSpecialization, lastUltimateCooldown;

    public AndroidClassLoadoutScreen() { super(Component.literal("ANDROID CLASS MATRIX")); }

    @Override protected void init() { cacheState(); rebuild(); }

    private void cacheState() {
        lastAspects = AndroidClientState.aspectMask();
        lastFragments = AndroidClientState.fragmentMask();
        lastArtifact = AndroidClientState.artifactOrdinal();
        lastDronePerks = AndroidClientState.dronePerkMask();
        lastSpecialization = AndroidClientState.specializationOrdinal();
        lastUltimateCooldown = AndroidClientState.ultimateCooldownTicks();
    }

    private int panelLeft() { return Math.max(350, width - 286); }
    private int mainRight() { return Math.max(330, panelLeft() - 12); }
    private int mainWidth() { return Math.max(300, mainRight() - 18); }

    private void rebuild() {
        clearWidgets();
        inspectables.clear();
        addRenderableWidget(tab("CLASS BUILD", 18, 42, 0, 96));
        addRenderableWidget(tab("DRONE MATRIX", 118, 42, 1, 104));
        addRenderableWidget(tab("PASSIVE", 226, 42, 2, 82));
        if (view == 0) buildClassView();
        else if (view == 1) buildDroneView();
        else buildPassiveView();
        addRenderableWidget(Button.builder(Component.literal("RESET LOADOUT"), b ->
                ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(2, 0)))
                .bounds(18, height - 28, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"), b -> onClose())
                .bounds(width - 90, height - 28, 72, 20).build());
    }

    private Button tab(String label, int x, int y, int target, int buttonWidth) {
        Button button = Button.builder(Component.literal((view == target ? "◆ " : "◇ ") + label), b -> {
            view = target;
            rebuild();
        }).bounds(x, y, buttonWidth, 20).build();
        button.active = view != target;
        return button;
    }

    private void buildClassView() {
        int right = mainRight();
        AndroidLoadout.Specialization selectedSpec = AndroidClientState.specialization();
        AndroidClasses.AndroidClass currentClass = AndroidClasses.fromSpecialization(selectedSpec);

        int classWidth = Math.max(92, Math.min(150, (mainWidth() - 12) / 3));
        AndroidClasses.AndroidClass[] classes = AndroidClasses.AndroidClass.values();
        for (int i = 0; i < classes.length; i++) {
            AndroidClasses.AndroidClass androidClass = classes[i];
            boolean selected = currentClass == androidClass;
            Button button = Button.builder(Component.literal((selected ? "◆ " : "◇ ") + androidClass.displayName), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(5, androidClass.defaultSpecialization.ordinal())))
                    .bounds(18 + i * (classWidth + 5), 72, classWidth, 22).build();
            button.active = !selected;
            addRenderableWidget(button);
            inspectables.put(button, androidClass);
        }

        AndroidLoadout.Specialization[] specs = AndroidClasses.specializations(currentClass);
        int specWidth = Math.max(88, Math.min(160, (mainWidth() - 12) / 3));
        for (int i = 0; i < specs.length; i++) {
            AndroidLoadout.Specialization spec = specs[i];
            boolean selected = selectedSpec == spec;
            Button button = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + spec.displayName), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(5, spec.ordinal())))
                    .bounds(18 + i * (specWidth + 5), 106, specWidth, 20).build();
            button.active = !selected;
            addRenderableWidget(button);
            inspectables.put(button, spec);
        }

        AbilityInfo classAbility = new AbilityInfo("CLASS ABILITY", AndroidClassAbilities.classAbilityName(selectedSpec),
                AndroidClassAbilities.classAbilityDescription(selectedSpec), "H",
                AndroidClassAbilities.classEnergyCost(selectedSpec), AndroidClassAbilities.classCooldownTicks(selectedSpec));
        AbilityInfo techAbility = new AbilityInfo("TECH ABILITY", AndroidClassAbilities.techAbilityName(selectedSpec),
                AndroidClassAbilities.techAbilityDescription(selectedSpec), "N",
                AndroidClassAbilities.techEnergyCost(selectedSpec), AndroidClassAbilities.techCooldownTicks(selectedSpec));
        int abilityWidth = Math.max(130, (mainWidth() - 6) / 2);
        Button h = Button.builder(Component.literal("H // " + classAbility.name), b -> {})
                .bounds(18, 140, abilityWidth, 24).build();
        h.active = false; addRenderableWidget(h); inspectables.put(h, classAbility);
        Button n = Button.builder(Component.literal("N // " + techAbility.name), b -> {})
                .bounds(24 + abilityWidth, 140, abilityWidth, 24).build();
        n.active = false; addRenderableWidget(n); inspectables.put(n, techAbility);

        AndroidLoadout.Ultimate ultimate = selectedSpec.ultimate;
        Button g = Button.builder(Component.literal("G // ULTIMATE // " + ultimate.displayName), b -> {})
                .bounds(18, 174, Math.max(260, mainWidth()), 26).build();
        g.active = false; addRenderableWidget(g); inspectables.put(g, ultimate);

        int aspectIndex = 0;
        for (AndroidLoadout.Aspect aspect : AndroidLoadout.Aspect.values()) {
            if (aspect.specialization() != selectedSpec) continue;
            boolean selected = (AndroidClientState.aspectMask() & (1 << aspect.ordinal())) != 0;
            int aspectWidth = Math.max(100, Math.min(145, (mainWidth() - 10) / 3));
            int x = 18 + aspectIndex * (aspectWidth + 5);
            Button button = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + aspect.displayName), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(0, aspect.ordinal())))
                    .bounds(x, 222, aspectWidth, 20).build();
            addRenderableWidget(button); inspectables.put(button, aspect); aspectIndex++;
        }

        AndroidLoadout.Fragment[] fragments = AndroidLoadout.Fragment.values();
        int cols = width < 760 ? 3 : 4;
        int gap = 4;
        int fragmentWidth = Math.max(80, Math.min(125, (mainWidth() - gap * (cols - 1)) / cols));
        int fragmentY = 270;
        int rowHeight = 19;
        for (int i = 0; i < fragments.length; i++) {
            AndroidLoadout.Fragment fragment = fragments[i];
            boolean selected = (AndroidClientState.fragmentMask() & (1 << i)) != 0;
            int x = 18 + (i % cols) * (fragmentWidth + gap);
            int y = fragmentY + (i / cols) * rowHeight;
            if (y + 17 >= height - 34) continue;
            Button button = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + shortName(fragment.displayName)), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(1, fragment.ordinal())))
                    .bounds(x, y, fragmentWidth, 16).build();
            addRenderableWidget(button); inspectables.put(button, fragment);
        }
    }

    private void buildDroneView() {
        AndroidLoadout.DronePerk[] perks = AndroidLoadout.DronePerk.values();
        int left = 48;
        int usable = Math.max(270, mainWidth() - 55);
        int step = Math.max(32, usable / Math.max(1, perks.length - 1));
        int yBase = Math.max(150, height / 2);
        for (int i = 0; i < perks.length; i++) {
            AndroidLoadout.DronePerk perk = perks[i];
            boolean selected = AndroidClientState.hasDronePerk(perk);
            int x = left + i * step;
            int y = yBase + ((i & 1) == 0 ? -30 : 30);
            Button button = Button.builder(Component.literal(selected ? "✦" : "◇"), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(4, perk.ordinal())))
                    .bounds(x - 12, y - 12, 24, 24).build();
            button.active = selected || AndroidClientState.level() >= perk.level;
            addRenderableWidget(button); inspectables.put(button, perk);
        }
    }

    private void buildPassiveView() {
        int buttonWidth = Math.max(135, Math.min(220, (mainWidth() - 10) / 2));
        int index = 0;
        for (int i = 1; i < AndroidLoadout.Artifact.values().length; i++) {
            AndroidLoadout.Artifact passive = AndroidLoadout.Artifact.values()[i];
            boolean selected = AndroidClientState.artifactOrdinal() == i;
            int x = 18 + (index % 2) * (buttonWidth + 8);
            int y = 100 + (index / 2) * 36;
            Button button = Button.builder(Component.literal((selected ? "✦ " : "◇ ") + passive.displayName), b ->
                    ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(3, passive.ordinal())))
                    .bounds(x, y, buttonWidth, 26).build();
            addRenderableWidget(button); inspectables.put(button, passive); index++;
        }
        addRenderableWidget(Button.builder(Component.literal("NO PASSIVE"), b ->
                ModNetwork.CHANNEL.sendToServer(new AndroidLoadoutSelectPacket(3, AndroidLoadout.Artifact.NONE.ordinal())))
                .bounds(18, 100 + ((index + 1) / 2) * 36 + 8, 150, 20).build());
    }

    private static String shortName(String name) { return name.startsWith("Fragment of ") ? name.substring(12) : name; }
    private int aspectCount() { return Integer.bitCount(AndroidClientState.aspectMask()); }
    private int fragmentCount() { return Integer.bitCount(AndroidClientState.fragmentMask()); }
    private int fragmentCapacity() {
        int slots = 0;
        for (AndroidLoadout.Aspect aspect : AndroidLoadout.Aspect.values())
            if ((AndroidClientState.aspectMask() & (1 << aspect.ordinal())) != 0) slots += aspect.fragmentSlots;
        return Math.min(AndroidLoadout.MAX_FRAGMENT_CAPACITY, slots);
    }

    @Override public void tick() {
        if (lastAspects != AndroidClientState.aspectMask() || lastFragments != AndroidClientState.fragmentMask()
                || lastArtifact != AndroidClientState.artifactOrdinal() || lastDronePerks != AndroidClientState.dronePerkMask()
                || lastSpecialization != AndroidClientState.specializationOrdinal()
                || lastUltimateCooldown != AndroidClientState.ultimateCooldownTicks()) {
            cacheState(); rebuild();
        }
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, BACKDROP);
        drawAtmosphere(g);
        AndroidClasses.AndroidClass androidClass = AndroidClasses.fromSpecialization(AndroidClientState.specialization());
        g.drawString(font, "ANDROID // CLASS MATRIX", 18, 14, TEXT, false);
        g.drawString(font, "3 classes × 3 subclasses · unique H / N / G kits · Aspects · Fragments · Passive", 18, 27, MUTED, false);
        g.drawString(font, "LV " + AndroidClientState.level() + "   FE " + AndroidClientState.energy(), Math.max(18, width - 170), 16, ACCENT, false);

        if (view == 0) {
            AndroidLoadout.Specialization spec = AndroidClientState.specialization();
            AndroidLoadout.Ultimate ultimate = spec.ultimate;
            int cooldown = AndroidClientState.ultimateCooldownTicks();
            g.drawString(font, "CLASS // " + androidClass.displayName.toUpperCase(), 18, 97, GOLD, false);
            g.drawString(font, spec.displayName.toUpperCase() + " SUBCLASS", 18, 129, MUTED, false);
            g.drawString(font, "SUBCLASS ABILITIES", 18, 166, ACCENT, false);
            String status = AndroidClientState.level() < AndroidUltimates.REQUIRED_LEVEL ? "ULTIMATE LOCKED · LEVEL " + AndroidUltimates.REQUIRED_LEVEL
                    : cooldown > 0 ? String.format("ULTIMATE RECHARGING %.1fs", cooldown / 20.0D)
                    : AndroidClientState.energy() < ultimate.energyCost ? "ULTIMATE NEEDS " + ultimate.energyCost + " FE" : "ULTIMATE READY · G";
            g.drawString(font, status, 20, 203, cooldown <= 0 ? ACCENT : MUTED, false);
            g.drawString(font, "ASPECTS " + aspectCount() + "/" + AndroidLoadout.MAX_ASPECTS, 18, 208, GOLD, false);
            g.drawString(font, "FRAGMENTS " + fragmentCount() + "/" + fragmentCapacity(), 18, 252, ACCENT, false);
        } else if (view == 1) {
            g.drawString(font, "DRONE COMMAND MATRIX", 18, 78, GREEN, false);
            g.drawString(font, Integer.bitCount(AndroidClientState.dronePerkMask()) + "/" + AndroidLoadout.MAX_DRONE_PERKS + " nodes installed · level-gated progression", 18, 92, MUTED, false);
        } else {
            g.drawString(font, "PASSIVE PROTOCOL", 18, 78, GOLD, false);
            g.drawString(font, "One high-impact always-on modifier · Current: " + AndroidClientState.artifact().displayName, 18, 92, MUTED, false);
        }
        super.render(g, mouseX, mouseY, partialTick);
        renderInspectionPanel(g);
    }

    private void drawAtmosphere(GuiGraphics g) {
        for (int x = 0; x < width; x += 54) g.fill(x, 62, x + 1, height - 36, 0x101F2B33);
        for (int y = 62; y < height - 36; y += 46) g.fill(0, y, width, y + 1, 0x101F2B33);
        if (width >= 640) {
            int left = panelLeft();
            g.fill(left, 62, width - 18, height - 36, PANEL);
            g.fill(left + 3, 65, width - 21, height - 39, 0x261AEEF2);
        }
    }

    private void renderInspectionPanel(GuiGraphics g) {
        if (width < 640) return;
        Object inspected = null;
        for (Map.Entry<Button, Object> entry : inspectables.entrySet()) if (entry.getKey().isHoveredOrFocused()) { inspected = entry.getValue(); break; }
        int x = panelLeft() + 18, y = 82;
        g.drawString(font, "SYSTEM INSPECTION", x, y, MUTED, false);
        if (inspected == null) {
            AndroidLoadout.Specialization spec = AndroidClientState.specialization();
            AndroidClasses.AndroidClass cls = AndroidClasses.fromSpecialization(spec);
            g.drawString(font, cls.displayName.toUpperCase(), x, y + 22, GOLD, false);
            g.drawString(font, spec.displayName.toUpperCase(), x, y + 36, ACCENT, false);
            drawWrapped(g, spec.description, x, y + 54, 230, TEXT);
            g.drawString(font, "H  " + AndroidClassAbilities.classAbilityName(spec), x, y + 102, ACCENT, false);
            g.drawString(font, "N  " + AndroidClassAbilities.techAbilityName(spec), x, y + 118, ACCENT, false);
            g.drawString(font, "G  " + spec.ultimate.displayName, x, y + 134, GOLD, false);
            g.drawString(font, "PASSIVE  " + AndroidClientState.artifact().displayName, x, y + 158, MUTED, false);
            return;
        }
        if (inspected instanceof AndroidClasses.AndroidClass cls) {
            g.drawString(font, cls.displayName.toUpperCase(), x, y + 22, GOLD, false);
            g.drawString(font, cls.subtitle.toUpperCase(), x, y + 36, MUTED, false);
            drawWrapped(g, cls.description, x, y + 54, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.Specialization spec) {
            g.drawString(font, spec.displayName.toUpperCase(), x, y + 22, GOLD, false);
            drawWrapped(g, spec.description, x, y + 40, 230, TEXT);
            g.drawString(font, "H  " + AndroidClassAbilities.classAbilityName(spec), x, y + 91, ACCENT, false);
            g.drawString(font, "N  " + AndroidClassAbilities.techAbilityName(spec), x, y + 107, ACCENT, false);
            g.drawString(font, "G  " + spec.ultimate.displayName, x, y + 123, GOLD, false);
        } else if (inspected instanceof AbilityInfo ability) {
            g.drawString(font, ability.type, x, y + 22, MUTED, false);
            g.drawString(font, ability.name.toUpperCase(), x, y + 38, ACCENT, false);
            g.drawString(font, "KEY " + ability.key + " · " + ability.energyCost + " FE · " + String.format("%.0fs", ability.cooldownTicks / 20.0D), x, y + 54, GOLD, false);
            drawWrapped(g, ability.description, x, y + 74, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.Ultimate ultimate) {
            g.drawString(font, ultimate.displayName.toUpperCase(), x, y + 22, GOLD, false);
            drawWrapped(g, ultimate.description, x, y + 40, 230, TEXT);
            g.drawString(font, "KEY G · " + ultimate.energyCost + " FE · " + String.format("%.0fs", ultimate.cooldownTicks / 20.0D), x, y + 100, ACCENT, false);
        } else if (inspected instanceof AndroidLoadout.Aspect aspect) {
            g.drawString(font, aspect.displayName.toUpperCase(), x, y + 22, GOLD, false);
            g.drawString(font, aspect.fragmentSlots + " FRAGMENT SLOTS", x, y + 36, ACCENT, false);
            drawWrapped(g, aspect.description, x, y + 54, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.Fragment fragment) {
            g.drawString(font, shortName(fragment.displayName).toUpperCase(), x, y + 22, ACCENT, false);
            drawWrapped(g, fragment.description, x, y + 40, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.DronePerk perk) {
            g.drawString(font, perk.displayName.toUpperCase(), x, y + 22, GREEN, false);
            g.drawString(font, "LEVEL " + perk.level, x, y + 36, MUTED, false);
            drawWrapped(g, perk.description, x, y + 54, 230, TEXT);
        } else if (inspected instanceof AndroidLoadout.Artifact passive) {
            g.drawString(font, passive.displayName.toUpperCase(), x, y + 22, GOLD, false);
            g.drawString(font, "SELECTABLE PASSIVE", x, y + 36, MUTED, false);
            drawWrapped(g, passive.description, x, y + 54, 230, TEXT);
        }
    }

    private void drawWrapped(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        for (FormattedCharSequence line : font.split(Component.literal(text), maxWidth)) {
            g.drawString(font, line, x, y, color, false); y += 12;
        }
    }

    private record AbilityInfo(String type, String name, String description, String key, int energyCost, int cooldownTicks) {}
}
