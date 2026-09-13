package matteroverdrive.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.NativeDestinyWeaponItem;
import matteroverdrive.item.weapon.NativeDestinyWeaponProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight native loader for the Bedrock/Blockbench gun geometry and animation data
 * used by the Destiny weapon set. This intentionally uses only vanilla Minecraft model
 * classes so the weapons do not require Point Blank, GeckoLib, or another renderer mod.
 */
public final class NativeDestinyVisualLibrary {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, WeaponVisual> VISUALS = new HashMap<>();
    private static boolean loaded;

    private NativeDestinyVisualLibrary() {}

    public static WeaponVisual get(String id) {
        ensureLoaded();
        return VISUALS.get(id);
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        Minecraft minecraft = Minecraft.getInstance();
        for (NativeDestinyWeaponProfile profile : NativeDestinyWeaponProfile.values()) {
            String id = profile.id();
            ResourceLocation geometryLocation = new ResourceLocation(
                    MatterOverdrive.MOD_ID, "native_destiny/geometry/" + id + ".geo.json");
            ResourceLocation animationLocation = new ResourceLocation(
                    MatterOverdrive.MOD_ID, "native_destiny/animations/" + id + ".animation.json");
            ResourceLocation transformLocation = new ResourceLocation(
                    MatterOverdrive.MOD_ID, "native_destiny/transforms/" + id + ".json");
            if (minecraft.getResourceManager().getResource(geometryLocation).isEmpty()
                    || minecraft.getResourceManager().getResource(animationLocation).isEmpty()
                    || minecraft.getResourceManager().getResource(transformLocation).isEmpty()) {
                LOGGER.warn("Missing native Destiny geometry, animation, or transform resource for {}", id);
                continue;
            }
            try (var geometryReader = new InputStreamReader(
                    minecraft.getResourceManager().getResource(geometryLocation).orElseThrow().open(),
                    StandardCharsets.UTF_8);
                 var animationReader = new InputStreamReader(
                    minecraft.getResourceManager().getResource(animationLocation).orElseThrow().open(),
                    StandardCharsets.UTF_8);
                 var transformReader = new InputStreamReader(
                    minecraft.getResourceManager().getResource(transformLocation).orElseThrow().open(),
                    StandardCharsets.UTF_8)) {
                JsonObject geometryRoot = JsonParser.parseReader(geometryReader).getAsJsonObject();
                JsonObject animationRoot = JsonParser.parseReader(animationReader).getAsJsonObject();
                JsonObject transformRoot = JsonParser.parseReader(transformReader).getAsJsonObject();
                VISUALS.put(id, WeaponVisual.parse(id, geometryRoot, animationRoot, transformRoot));
            } catch (Exception ex) {
                LOGGER.error("Failed to build native Destiny visual {}", id, ex);
            }
        }
        LOGGER.info("Loaded {} native Destiny weapon visuals", VISUALS.size());
    }

    public static final class WeaponVisual {
        private final String id;
        private final ModelPart root;
        private final Map<String, ModelPart> parts;
        private final Map<String, BasePose> basePoses;
        private final Map<String, AnimationClip> animations;
        private final ResourceLocation texture;
        private final DisplayTransform displayTransform;

        private WeaponVisual(String id, ModelPart root, Map<String, ModelPart> parts,
                             Map<String, BasePose> basePoses,
                             Map<String, AnimationClip> animations, ResourceLocation texture,
                             DisplayTransform displayTransform) {
            this.id = id;
            this.root = root;
            this.parts = parts;
            this.basePoses = basePoses;
            this.animations = animations;
            this.texture = texture;
            this.displayTransform = displayTransform;
        }

        public ModelPart root() { return root; }
        public ResourceLocation texture() { return texture; }
        public DisplayTransform displayTransform() { return displayTransform; }

        public void apply(ItemStack stack, float partialTick) {
            reset();
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) return;

            float nowTicks = minecraft.level.getGameTime() + partialTick;
            String activeName = NativeDestinyWeaponItem.activeAnimation(stack);
            long startTick = NativeDestinyWeaponItem.animationStart(stack);
            AnimationClip active = findAnimation(activeName);
            if (active != null && startTick > 0L) {
                float seconds = Math.max(0.0F, (nowTicks - startTick) / 20.0F);
                if (seconds <= active.length + 0.05F) {
                    applyClip(active, seconds);
                    return;
                }
            }

            AnimationClip idle = findAnimation("animation.model.idle");
            if (idle != null && idle.length > 0.0F) {
                float seconds = (nowTicks / 20.0F) % idle.length;
                applyClip(idle, seconds);
            }
        }

        /**
         * Accept both the already-normalised Matter Overdrive animation names and the
         * native GunPack names. The source pack calls the firing clip {@code shoot},
         * the idle clip {@code static_idle}, and uses separate tactical/empty reload
         * clips, so resolving aliases here keeps the weapon item independent of the
         * source pack's naming conventions.
         */
        private AnimationClip findAnimation(String requested) {
            AnimationClip direct = animations.get(requested);
            if (direct != null) return direct;
            return switch (requested) {
                case "animation.model.idle" -> firstAnimation("static_idle", "idle");
                case "animation.model.draw" -> firstAnimation("draw");
                case "animation.model.reload" -> firstAnimation(
                        "reload_tactical", "reload", "reload_loop", "reload_intro", "reload_empty");
                case "animation.model.fire", "animation.model.fire2" -> firstAnimation(
                        "shoot", "fire", "static_semi");
                default -> null;
            };
        }

        private AnimationClip firstAnimation(String... names) {
            for (String name : names) {
                AnimationClip clip = animations.get(name);
                if (clip != null) return clip;
            }
            return null;
        }

        private void reset() {
            for (Map.Entry<String, BasePose> entry : basePoses.entrySet()) {
                ModelPart part = parts.get(entry.getKey());
                if (part == null) continue;
                BasePose base = entry.getValue();
                part.x = base.x;
                part.y = base.y;
                part.z = base.z;
                part.xRot = base.xRot;
                part.yRot = base.yRot;
                part.zRot = base.zRot;
                part.xScale = 1.0F;
                part.yScale = 1.0F;
                part.zScale = 1.0F;
                part.visible = true;
            }
        }

        private void applyClip(AnimationClip clip, float seconds) {
            float time = clip.loop && clip.length > 0.0F ? seconds % clip.length : Mth.clamp(seconds, 0.0F, clip.length);
            for (Map.Entry<String, BoneAnimation> entry : clip.bones.entrySet()) {
                ModelPart part = parts.get(entry.getKey());
                BasePose base = basePoses.get(entry.getKey());
                if (part == null || base == null) continue;
                BoneAnimation animation = entry.getValue();
                Vec3 position = animation.position == null ? null : animation.position.sample(time);
                Vec3 rotation = animation.rotation == null ? null : animation.rotation.sample(time);
                Vec3 scale = animation.scale == null ? null : animation.scale.sample(time);
                if (position != null) {
                    part.x = base.x + position.x;
                    part.y = base.y - position.y;
                    part.z = base.z + position.z;
                }
                if (rotation != null) {
                    part.xRot = base.xRot + radians(-rotation.x);
                    part.yRot = base.yRot + radians(rotation.y);
                    part.zRot = base.zRot + radians(-rotation.z);
                }
                if (scale != null) {
                    part.xScale = scale.x;
                    part.yScale = scale.y;
                    part.zScale = scale.z;
                }
            }
        }

        private static WeaponVisual parse(String id, JsonObject geometryRoot, JsonObject animationRoot,
                                          JsonObject transformRoot) {
            JsonArray geometries = geometryRoot.getAsJsonArray("minecraft:geometry");
            if (geometries == null || geometries.size() == 0) {
                throw new IllegalArgumentException("geometry has no minecraft:geometry entries");
            }
            JsonObject geometry = geometries.get(0).getAsJsonObject();
            JsonObject description = geometry.getAsJsonObject("description");
            int textureWidth = description.get("texture_width").getAsInt();
            int textureHeight = description.get("texture_height").getAsInt();
            JsonArray sourceBones = geometry.getAsJsonArray("bones");

            Map<String, BoneSpec> specs = new LinkedHashMap<>();
            for (JsonElement element : sourceBones) {
                BoneSpec spec = BoneSpec.parse(element.getAsJsonObject());
                specs.put(spec.name, spec);
            }
            for (BoneSpec spec : specs.values()) {
                if (spec.parent != null && specs.containsKey(spec.parent)) {
                    specs.get(spec.parent).children.add(spec);
                }
            }

            MeshDefinition mesh = new MeshDefinition();
            PartDefinition rootDef = mesh.getRoot();
            for (BoneSpec spec : specs.values()) {
                if (spec.parent == null || !specs.containsKey(spec.parent)) {
                    buildPart(rootDef, spec, Vec3.ZERO);
                }
            }

            ModelPart root = LayerDefinition.create(mesh, textureWidth, textureHeight).bakeRoot();
            Map<String, ModelPart> parts = new HashMap<>();
            Map<String, BasePose> basePoses = new HashMap<>();
            for (BoneSpec spec : specs.values()) {
                if (spec.parent == null || !specs.containsKey(spec.parent)) {
                    collectParts(root, spec, parts, basePoses);
                }
            }

            Map<String, AnimationClip> animations = new HashMap<>();
            JsonObject sourceAnimations = animationRoot.getAsJsonObject("animations");
            if (sourceAnimations != null) {
                for (Map.Entry<String, JsonElement> animation : sourceAnimations.entrySet()) {
                    animations.put(animation.getKey(), AnimationClip.parse(animation.getValue().getAsJsonObject()));
                }
            }
            return new WeaponVisual(id, root, parts, basePoses, animations,
                    new ResourceLocation(MatterOverdrive.MOD_ID, "textures/native_destiny/" + id + ".png"),
                    DisplayTransform.parse(transformRoot));
        }

        private static PartDefinition buildPart(PartDefinition parentDef, BoneSpec spec, Vec3 parentPivot) {
            Vec3 local = spec.pivot.subtract(parentPivot);
            PartDefinition boneDef = parentDef.addOrReplaceChild(spec.name, CubeListBuilder.create(),
                    PartPose.offsetAndRotation(local.x, -local.y, local.z,
                            radians(-spec.rotation.x), radians(spec.rotation.y), radians(-spec.rotation.z)));

            int cubeIndex = 0;
            for (CubeSpec cube : spec.cubes) {
                Vec3 cubePivot = cube.pivot == null ? spec.pivot : cube.pivot;
                Vec3 cubeLocalPivot = cubePivot.subtract(spec.pivot);
                Vec3 localOrigin = new Vec3(
                        cube.origin.x - cubePivot.x,
                        cubePivot.y - cube.origin.y - cube.size.y,
                        cube.origin.z - cubePivot.z);
                CubeListBuilder builder = CubeListBuilder.create().texOffs(Math.round(cube.u), Math.round(cube.v));
                if (cube.mirror) builder.mirror();
                builder.addBox(localOrigin.x, localOrigin.y, localOrigin.z,
                        cube.size.x, cube.size.y, cube.size.z, new CubeDeformation(cube.inflate));
                boneDef.addOrReplaceChild("__cube_" + cubeIndex++, builder,
                        PartPose.offsetAndRotation(cubeLocalPivot.x, -cubeLocalPivot.y, cubeLocalPivot.z,
                                radians(-cube.rotation.x), radians(cube.rotation.y), radians(-cube.rotation.z)));
            }
            for (BoneSpec child : spec.children) {
                buildPart(boneDef, child, spec.pivot);
            }
            return boneDef;
        }

        private static void collectParts(ModelPart parentPart, BoneSpec spec,
                                         Map<String, ModelPart> parts,
                                         Map<String, BasePose> bases) {
            ModelPart part = parentPart.getChild(spec.name);
            parts.put(spec.name, part);
            bases.put(spec.name, new BasePose(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot));
            for (BoneSpec child : spec.children) {
                collectParts(part, child, parts, bases);
            }
        }
    }

    /** Display transforms copied from each source model's separate-transforms base model. */
    public static final class DisplayTransform {
        private final Entry firstPersonRight;
        private final Entry firstPersonLeft;
        private final Entry thirdPersonRight;
        private final Entry thirdPersonLeft;
        private final Entry ground;
        private final Entry gui;
        private final Entry fixed;

        private DisplayTransform(Entry firstPersonRight, Entry firstPersonLeft,
                                 Entry thirdPersonRight, Entry thirdPersonLeft,
                                 Entry ground, Entry gui, Entry fixed) {
            this.firstPersonRight = firstPersonRight;
            this.firstPersonLeft = firstPersonLeft;
            this.thirdPersonRight = thirdPersonRight;
            this.thirdPersonLeft = thirdPersonLeft;
            this.ground = ground;
            this.gui = gui;
            this.fixed = fixed;
        }

        public Entry forContext(ItemDisplayContext context) {
            return switch (context) {
                case FIRST_PERSON_RIGHT_HAND -> firstPersonRight;
                case FIRST_PERSON_LEFT_HAND -> firstPersonLeft;
                case THIRD_PERSON_RIGHT_HAND -> thirdPersonRight;
                case THIRD_PERSON_LEFT_HAND -> thirdPersonLeft;
                case GROUND -> ground;
                case GUI -> gui;
                case FIXED -> fixed;
                default -> Entry.IDENTITY;
            };
        }

        private static DisplayTransform parse(JsonObject root) {
            JsonObject base = root.getAsJsonObject("base");
            JsonObject display = base == null ? null : base.getAsJsonObject("display");
            return new DisplayTransform(
                    Entry.parse(display, "firstperson_righthand", Entry.IDENTITY),
                    Entry.parse(display, "firstperson_lefthand", Entry.IDENTITY),
                    Entry.parse(display, "thirdperson_righthand", Entry.IDENTITY),
                    Entry.parse(display, "thirdperson_lefthand", Entry.IDENTITY),
                    Entry.parse(display, "ground", Entry.IDENTITY),
                    Entry.parse(display, "gui", Entry.IDENTITY),
                    Entry.parse(display, "fixed", Entry.IDENTITY));
        }

        public record Entry(float x, float y, float z, float xRot, float yRot, float zRot, float scale) {
            private static final Entry IDENTITY = new Entry(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);

            private static Entry parse(JsonObject display, String name, Entry fallback) {
                if (display == null || !display.has(name)) return fallback;
                JsonObject object = display.getAsJsonObject(name);
                Vec3 translation = vector(object, "translation", Vec3.ZERO);
                Vec3 rotation = vector(object, "rotation", Vec3.ZERO);
                Vec3 scale = vector(object, "scale", new Vec3(1.0F, 1.0F, 1.0F));
                return new Entry(translation.x, translation.y, translation.z,
                        rotation.x, rotation.y, rotation.z, scale.x);
            }

            private static Vec3 vector(JsonObject object, String name, Vec3 fallback) {
                return object.has(name) && object.get(name).isJsonArray()
                        ? Vec3.parse(object.getAsJsonArray(name)) : fallback;
            }
        }
    }

    private static final class BoneSpec {
        final String name;
        final String parent;
        final Vec3 pivot;
        final Vec3 rotation;
        final List<CubeSpec> cubes;
        final List<BoneSpec> children = new ArrayList<>();

        private BoneSpec(String name, String parent, Vec3 pivot, Vec3 rotation, List<CubeSpec> cubes) {
            this.name = name;
            this.parent = parent;
            this.pivot = pivot;
            this.rotation = rotation;
            this.cubes = cubes;
        }

        static BoneSpec parse(JsonObject object) {
            String name = object.get("name").getAsString();
            String parent = object.has("parent") ? object.get("parent").getAsString() : null;
            Vec3 pivot = object.has("pivot") ? Vec3.parse(object.getAsJsonArray("pivot")) : Vec3.ZERO;
            Vec3 rotation = object.has("rotation") ? Vec3.parse(object.getAsJsonArray("rotation")) : Vec3.ZERO;
            List<CubeSpec> cubes = new ArrayList<>();
            if (object.has("cubes")) {
                for (JsonElement cube : object.getAsJsonArray("cubes")) cubes.add(CubeSpec.parse(cube.getAsJsonObject()));
            }
            return new BoneSpec(name, parent, pivot, rotation, cubes);
        }
    }

    private static final class CubeSpec {
        final Vec3 origin;
        final Vec3 size;
        final Vec3 pivot;
        final Vec3 rotation;
        final float inflate;
        final float u;
        final float v;
        final boolean mirror;

        private CubeSpec(Vec3 origin, Vec3 size, Vec3 pivot, Vec3 rotation,
                         float inflate, float u, float v, boolean mirror) {
            this.origin = origin;
            this.size = size;
            this.pivot = pivot;
            this.rotation = rotation;
            this.inflate = inflate;
            this.u = u;
            this.v = v;
            this.mirror = mirror;
        }

        static CubeSpec parse(JsonObject object) {
            JsonElement uvElement = object.get("uv");
            float[] uv = parseUv(uvElement);
            return new CubeSpec(
                    Vec3.parse(object.getAsJsonArray("origin")),
                    Vec3.parse(object.getAsJsonArray("size")),
                    object.has("pivot") ? Vec3.parse(object.getAsJsonArray("pivot")) : null,
                    object.has("rotation") ? Vec3.parse(object.getAsJsonArray("rotation")) : Vec3.ZERO,
                    object.has("inflate") ? object.get("inflate").getAsFloat() : 0.0F,
                    uv[0], uv[1],
                    object.has("mirror") && object.get("mirror").getAsBoolean());
        }

        /**
         * Bedrock geometry may use either Blockbench's compact [u, v] form or a
         * per-face UV object. Vanilla's CubeListBuilder accepts one anchor for its
         * standard box atlas layout, so use the source north face as that anchor when
         * importing the richer form. This preserves the source texture placement for
         * the visible face while keeping the loader compatible with both source packs.
         */
        private static float[] parseUv(JsonElement element) {
            if (element == null || element.isJsonNull()) return new float[] {0.0F, 0.0F};
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                return new float[] {array.get(0).getAsFloat(), array.get(1).getAsFloat()};
            }
            if (element.isJsonObject()) {
                JsonObject faces = element.getAsJsonObject();
                for (String face : List.of("north", "south", "east", "west", "up", "down")) {
                    JsonElement candidate = faces.get(face);
                    if (candidate == null || !candidate.isJsonObject()) continue;
                    JsonElement faceUv = candidate.getAsJsonObject().get("uv");
                    if (faceUv != null && faceUv.isJsonArray()) {
                        JsonArray array = faceUv.getAsJsonArray();
                        return new float[] {array.get(0).getAsFloat(), array.get(1).getAsFloat()};
                    }
                }
            }
            return new float[] {0.0F, 0.0F};
        }
    }

    private static final class AnimationClip {
        final float length;
        final boolean loop;
        final Map<String, BoneAnimation> bones;

        private AnimationClip(float length, boolean loop, Map<String, BoneAnimation> bones) {
            this.length = Math.max(0.001F, length);
            this.loop = loop;
            this.bones = bones;
        }

        static AnimationClip parse(JsonObject object) {
            float length = object.has("animation_length") ? object.get("animation_length").getAsFloat() : 0.1F;
            boolean loop = object.has("loop") && isLooping(object.get("loop"));
            Map<String, BoneAnimation> bones = new HashMap<>();
            if (object.has("bones")) {
                for (Map.Entry<String, JsonElement> bone : object.getAsJsonObject("bones").entrySet()) {
                    JsonObject channels = bone.getValue().getAsJsonObject();
                    bones.put(bone.getKey(), new BoneAnimation(
                            Channel.parse(channels.get("position")),
                            Channel.parse(channels.get("rotation")),
                            Channel.parse(channels.get("scale"))));
                }
            }
            return new AnimationClip(length, loop, bones);
        }

        private static boolean isLooping(JsonElement value) {
            if (!value.isJsonPrimitive()) return false;
            if (value.getAsJsonPrimitive().isBoolean()) return value.getAsBoolean();
            return !"hold_on_last_frame".equals(value.getAsString());
        }
    }

    private record BoneAnimation(Channel position, Channel rotation, Channel scale) {}

    private static final class Channel {
        final List<Keyframe> frames;

        private Channel(List<Keyframe> frames) {
            this.frames = frames;
        }

        static Channel parse(JsonElement element) {
            if (element == null || element.isJsonNull()) return null;
            List<Keyframe> frames = new ArrayList<>();
            if (element.isJsonArray()) {
                frames.add(new Keyframe(0.0F, Vec3.parse(element.getAsJsonArray()), "linear"));
                return new Channel(frames);
            }
            JsonObject object = element.getAsJsonObject();
            if (object.has("vector")) {
                frames.add(new Keyframe(0.0F, Vec3.parse(object.getAsJsonArray("vector")), "linear"));
            } else {
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    try {
                        float time = Float.parseFloat(entry.getKey());
                        JsonElement rawFrame = entry.getValue();
                        JsonObject frame = rawFrame.isJsonObject() ? rawFrame.getAsJsonObject() : null;
                        JsonArray vector = rawFrame.isJsonArray()
                                ? rawFrame.getAsJsonArray()
                                : frame == null ? null : frame.getAsJsonArray("vector");
                        // GunPack/Blockbench keyframes commonly store the actual
                        // value in pre/post fields rather than a direct vector.
                        // Prefer post (the value held at the keyframe), then fall
                        // back to pre so those source clips do not lose their
                        // reload, draw, or recoil poses during import.
                        if (vector == null && frame != null) {
                            vector = frame.has("post") ? frame.getAsJsonArray("post")
                                    : frame.has("pre") ? frame.getAsJsonArray("pre") : null;
                        }
                        if (vector == null) continue;
                        String easing = "linear";
                        if (frame != null) {
                            if (frame.has("easing")) easing = frame.get("easing").getAsString();
                            else if (frame.has("lerp_mode") && "catmullrom".equals(frame.get("lerp_mode").getAsString())) {
                                easing = "linear";
                            }
                        }
                        frames.add(new Keyframe(time, Vec3.parse(vector),
                                easing));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            frames.sort(Comparator.comparingDouble(Keyframe::time));
            return frames.isEmpty() ? null : new Channel(frames);
        }

        Vec3 sample(float time) {
            if (frames.size() == 1 || time <= frames.get(0).time) return frames.get(0).value;
            Keyframe last = frames.get(frames.size() - 1);
            if (time >= last.time) return last.value;
            for (int i = 1; i < frames.size(); i++) {
                Keyframe next = frames.get(i);
                if (time <= next.time) {
                    Keyframe prev = frames.get(i - 1);
                    float span = Math.max(0.0001F, next.time - prev.time);
                    float alpha = Mth.clamp((time - prev.time) / span, 0.0F, 1.0F);
                    alpha = ease(next.easing, alpha);
                    return Vec3.lerp(prev.value, next.value, alpha);
                }
            }
            return last.value;
        }
    }

    private record Keyframe(float time, Vec3 value, String easing) {}
    private record BasePose(float x, float y, float z, float xRot, float yRot, float zRot) {}

    private static final class Vec3 {
        static final Vec3 ZERO = new Vec3(0.0F, 0.0F, 0.0F);
        final float x;
        final float y;
        final float z;

        Vec3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        static Vec3 parse(JsonArray array) {
            return new Vec3(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
        }

        Vec3 subtract(Vec3 other) {
            return new Vec3(x - other.x, y - other.y, z - other.z);
        }

        static Vec3 lerp(Vec3 a, Vec3 b, float t) {
            return new Vec3(Mth.lerp(t, a.x, b.x), Mth.lerp(t, a.y, b.y), Mth.lerp(t, a.z, b.z));
        }
    }

    private static float radians(float degrees) {
        return degrees * ((float)Math.PI / 180.0F);
    }

    private static float ease(String easing, float t) {
        if (easing == null || easing.equals("linear")) return t;
        return switch (easing) {
            case "easeInSine" -> 1.0F - (float)Math.cos((t * Math.PI) / 2.0D);
            case "easeOutSine" -> (float)Math.sin((t * Math.PI) / 2.0D);
            case "easeInOutSine" -> -(float)(Math.cos(Math.PI * t) - 1.0D) / 2.0F;
            case "easeInQuad" -> t * t;
            case "easeOutQuad" -> 1.0F - (1.0F - t) * (1.0F - t);
            case "easeInOutQuad" -> t < 0.5F ? 2.0F * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 2.0D) / 2.0F;
            case "easeInCubic" -> t * t * t;
            case "easeInOutCubic" -> t < 0.5F ? 4.0F * t * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 3.0D) / 2.0F;
            case "easeInExpo" -> t <= 0.0F ? 0.0F : (float)Math.pow(2.0D, 10.0F * t - 10.0F);
            case "easeOutExpo" -> t >= 1.0F ? 1.0F : 1.0F - (float)Math.pow(2.0D, -10.0F * t);
            default -> t;
        };
    }
}
