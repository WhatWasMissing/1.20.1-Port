package matteroverdrive.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import matteroverdrive.MatterOverdrive;
import matteroverdrive.item.weapon.NativeDestinyWeaponItem;
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
    private static final ResourceLocation DATA =
            new ResourceLocation(MatterOverdrive.MOD_ID, "native_destiny/weapons.json");
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
        minecraft.getResourceManager().getResource(DATA).ifPresent(resource -> {
            try (var reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject weapons = root.getAsJsonObject("weapons");
                for (Map.Entry<String, JsonElement> entry : weapons.entrySet()) {
                    try {
                        VISUALS.put(entry.getKey(), WeaponVisual.parse(entry.getKey(), entry.getValue().getAsJsonObject()));
                    } catch (RuntimeException ex) {
                        LOGGER.error("Failed to build native Destiny visual {}", entry.getKey(), ex);
                    }
                }
                LOGGER.info("Loaded {} native Destiny weapon visuals", VISUALS.size());
            } catch (Exception ex) {
                LOGGER.error("Failed to load native Destiny weapon visual data", ex);
            }
        });
    }

    public static final class WeaponVisual {
        private final String id;
        private final ModelPart root;
        private final Map<String, ModelPart> parts;
        private final Map<String, BasePose> basePoses;
        private final Map<String, AnimationClip> animations;
        private final ResourceLocation texture;

        private WeaponVisual(String id, ModelPart root, Map<String, ModelPart> parts,
                             Map<String, BasePose> basePoses,
                             Map<String, AnimationClip> animations,
                             ResourceLocation texture) {
            this.id = id;
            this.root = root;
            this.parts = parts;
            this.basePoses = basePoses;
            this.animations = animations;
            this.texture = texture;
        }

        public ModelPart root() { return root; }
        public ResourceLocation texture() { return texture; }

        public void apply(ItemStack stack, float partialTick) {
            reset();
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) return;

            float nowTicks = minecraft.level.getGameTime() + partialTick;
            String activeName = NativeDestinyWeaponItem.activeAnimation(stack);
            long startTick = NativeDestinyWeaponItem.animationStart(stack);
            AnimationClip active = animations.get(activeName);
            if (active != null && startTick > 0L) {
                float seconds = Math.max(0.0F, (nowTicks - startTick) / 20.0F);
                if (seconds <= active.length + 0.05F) {
                    applyClip(active, seconds);
                    return;
                }
            }

            AnimationClip idle = animations.get("animation.model.idle");
            if (idle != null && idle.length > 0.0F) {
                float seconds = (nowTicks / 20.0F) % idle.length;
                applyClip(idle, seconds);
            }
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

        private static WeaponVisual parse(String id, JsonObject object) {
            JsonObject geometry = object.getAsJsonObject("geometry");
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
            JsonObject sourceAnimations = object.getAsJsonObject("animations");
            for (Map.Entry<String, JsonElement> animation : sourceAnimations.entrySet()) {
                animations.put(animation.getKey(), AnimationClip.parse(animation.getValue().getAsJsonObject()));
            }
            return new WeaponVisual(id, root, parts, basePoses, animations,
                    new ResourceLocation(MatterOverdrive.MOD_ID, "textures/native_destiny/" + id + ".png"));
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
            JsonArray uv = object.getAsJsonArray("uv");
            return new CubeSpec(
                    Vec3.parse(object.getAsJsonArray("origin")),
                    Vec3.parse(object.getAsJsonArray("size")),
                    object.has("pivot") ? Vec3.parse(object.getAsJsonArray("pivot")) : null,
                    object.has("rotation") ? Vec3.parse(object.getAsJsonArray("rotation")) : Vec3.ZERO,
                    object.has("inflate") ? object.get("inflate").getAsFloat() : 0.0F,
                    uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
                    object.has("mirror") && object.get("mirror").getAsBoolean());
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
            boolean loop = object.has("loop") && object.get("loop").getAsBoolean();
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
    }

    private record BoneAnimation(Channel position, Channel rotation, Channel scale) {}

    private static final class Channel {
        final List<Keyframe> frames;

        private Channel(List<Keyframe> frames) {
            this.frames = frames;
        }

        static Channel parse(JsonElement element) {
            if (element == null || element.isJsonNull()) return null;
            JsonObject object = element.getAsJsonObject();
            List<Keyframe> frames = new ArrayList<>();
            if (object.has("vector")) {
                frames.add(new Keyframe(0.0F, Vec3.parse(object.getAsJsonArray("vector")), "linear"));
            } else {
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    try {
                        float time = Float.parseFloat(entry.getKey());
                        JsonObject frame = entry.getValue().getAsJsonObject();
                        if (!frame.has("vector")) continue;
                        frames.add(new Keyframe(time, Vec3.parse(frame.getAsJsonArray("vector")),
                                frame.has("easing") ? frame.get("easing").getAsString() : "linear"));
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
