package matteroverdrive.client.debug;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Reads the active held item's JSON display transform directly from the resource pack stack.
 * This is diagnostic-only and never changes the transform used by Minecraft's renderer.
 */
public final class DeveloperModelTransformInspector {
    private static final Map<String, Optional<TransformData>> CACHE = new HashMap<>();

    private DeveloperModelTransformInspector() {}

    public static void clearCache() {
        CACHE.clear();
    }

    public static Inspection inspectHeldItem(Minecraft minecraft) {
        if (minecraft.player == null) return Inspection.empty("no player");

        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack stack = minecraft.player.getMainHandItem();
        if (stack.isEmpty()) {
            hand = InteractionHand.OFF_HAND;
            stack = minecraft.player.getOffhandItem();
        }
        if (stack.isEmpty()) return Inspection.empty("no held item");

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String context = displayContext(minecraft, hand);
        ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "models/item/" + itemId.getPath() + ".json");
        String cacheKey = modelFile + "#" + context;
        Optional<TransformData> transform = CACHE.computeIfAbsent(cacheKey,
                ignored -> readTransform(minecraft, modelFile, context));

        if (transform.isEmpty()) {
            return new Inspection(itemId.toString(), context, null,
                    "no direct display transform in " + modelFile);
        }

        TransformData value = transform.get();
        DeveloperVisualDebug.recordModelTransform(itemId.toString(), context,
                value.translation[0], value.translation[1], value.translation[2],
                value.rotation[0], value.rotation[1], value.rotation[2],
                value.scale[0], value.scale[1], value.scale[2]);
        return new Inspection(itemId.toString(), context, value, modelFile.toString());
    }

    private static String displayContext(Minecraft minecraft, InteractionHand hand) {
        HumanoidArm arm = minecraft.player.getMainArm();
        if (hand == InteractionHand.OFF_HAND) arm = arm.getOpposite();
        boolean right = arm == HumanoidArm.RIGHT;
        if (minecraft.options.getCameraType().isFirstPerson()) {
            return right ? "firstperson_righthand" : "firstperson_lefthand";
        }
        return right ? "thirdperson_righthand" : "thirdperson_lefthand";
    }

    private static Optional<TransformData> readTransform(Minecraft minecraft, ResourceLocation modelFile, String context) {
        Optional<Resource> resource = minecraft.getResourceManager().getResource(modelFile);
        if (resource.isEmpty()) return Optional.empty();

        try (Reader reader = resource.get().openAsReader()) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (!rootElement.isJsonObject()) return Optional.empty();
            JsonObject root = rootElement.getAsJsonObject();
            if (!root.has("display") || !root.get("display").isJsonObject()) return Optional.empty();
            JsonObject display = root.getAsJsonObject("display");
            if (!display.has(context) || !display.get(context).isJsonObject()) return Optional.empty();
            JsonObject entry = display.getAsJsonObject(context);
            return Optional.of(new TransformData(
                    vector(entry.get("rotation"), 0.0F),
                    vector(entry.get("translation"), 0.0F),
                    vector(entry.get("scale"), 1.0F)
            ));
        } catch (IOException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static float[] vector(JsonElement element, float defaultValue) {
        float[] result = new float[]{defaultValue, defaultValue, defaultValue};
        if (element == null || !element.isJsonArray()) return result;
        JsonArray array = element.getAsJsonArray();
        for (int i = 0; i < Math.min(3, array.size()); i++) {
            try {
                result[i] = array.get(i).getAsFloat();
            } catch (RuntimeException ignored) {
                result[i] = defaultValue;
            }
        }
        return result;
    }

    public record TransformData(float[] rotation, float[] translation, float[] scale) {}

    public record Inspection(String itemId, String context, TransformData transform, String source) {
        static Inspection empty(String reason) {
            return new Inspection("-", "-", null, reason);
        }
    }
}
