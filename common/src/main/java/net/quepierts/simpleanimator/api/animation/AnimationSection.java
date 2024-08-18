package net.quepierts.simpleanimator.api.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import net.quepierts.simpleanimator.core.client.state.IAnimationState;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AnimationSection {
    public static final Vector3f ZERO = new Vector3f(0);

    private static final float DEFAULT_FADE_IN = 0.05f;
    private static final float DEFAULT_FADE_OUT = 0.05f;

    private final boolean repeat;

    private final float length;
    private final float fadeIn;
    private final float fadeOut;

    private final EnumMap<ModelBone, BoneData> keyFrames;

    public static AnimationSection fromJsonObject(JsonObject json, Animation.Type type) {
        if (json == null)
            return null;

        final boolean repeat = json.has("loop") && json.get("loop").getAsBoolean();
        final float length = json.get("animation_length").getAsFloat();

        final String var = json.has("anim_time_update") ? json.get("anim_time_update").getAsString() : "";
        final Map<String, String> variables = getVariables(var);

        float fadeIn = tryParse(variables.get("fade_in"), DEFAULT_FADE_IN);
        float fadeOut = tryParse(variables.get("fade_out"), DEFAULT_FADE_OUT);

        EnumMap<ModelBone, BoneData> keyFrames = new EnumMap<>(ModelBone.class);
        JsonObject bones = json.getAsJsonObject("bones");

        for (Map.Entry<String, JsonElement> entry : bones.entrySet()) {
            final String key = entry.getKey();
            if (!key.startsWith(type.prefix))
                continue;
            ModelBone bone = ModelBone.fromString(key.substring(type.prefix.length() + 1));

            if (bone != null) {
                KeyFrame[] rotation = getRotation(entry.getValue().getAsJsonObject().get("rotation"), bone, length);
                KeyFrame[] position = getPosition(entry.getValue().getAsJsonObject().get("position"), bone, length);
                keyFrames.put(bone, new BoneData(rotation, position));
            }
        }

        return new AnimationSection(repeat, length, fadeIn, fadeOut, keyFrames);
    }

    public static AnimationSection fromJsonObject(JsonObject json) {
        if (json == null)
            return null;

        final boolean repeat = json.has("loop") && json.get("loop").getAsBoolean();
        final float length = json.get("animation_length").getAsFloat();

        final String var = json.has("anim_time_update") ? json.get("anim_time_update").getAsString() : "";
        final Map<String, String> variables = getVariables(var);

        float fadeIn = tryParse(variables.get("fade_in"), DEFAULT_FADE_IN);
        float fadeOut = tryParse(variables.get("fade_out"), DEFAULT_FADE_OUT);

        EnumMap<ModelBone, BoneData> keyFrames = new EnumMap<>(ModelBone.class);
        JsonObject bones = json.getAsJsonObject("bones");

        for (Map.Entry<String, JsonElement> entry : bones.entrySet()) {
            ModelBone bone = ModelBone.fromString(entry.getKey());

            if (bone != null) {
                KeyFrame[] rotation = getRotation(entry.getValue().getAsJsonObject().getAsJsonObject("rotation"), bone, length);
                KeyFrame[] position = getPosition(entry.getValue().getAsJsonObject().getAsJsonObject("position"), bone, length);
                keyFrames.put(bone, new BoneData(rotation, position));
            }
        }

        return new AnimationSection(repeat, length, fadeIn, fadeOut, keyFrames);
    }

    private static float tryParse(String str, float def) {
        if (StringUtil.isNullOrEmpty(str))
            return def;
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static Map<String, String> getVariables(String input) {
        Map<String, String> result = new HashMap<>();

        if (StringUtil.isNullOrEmpty(input)) {
            return result;
        }

        String[] pairs = input.split(",\\s*");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=\\s*");

            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                result.put(key, keyValue[1].trim());
            }
        }

        return result;
    }

    private static KeyFrame[] getRotation(JsonElement element, ModelBone bone, float length) {
        if (element == null)
            return new KeyFrame[0];

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            return new KeyFrame[] {
                    new KeyFrame(
                            0,
                            new Vector3f(
                                    (float) Math.toRadians(array.get(0).getAsFloat()),
                                    (float) Math.toRadians(array.get(1).getAsFloat()),
                                    (float) Math.toRadians(array.get(2).getAsFloat())
                            ), LerpMode.LINEAR
                    ),
                    new KeyFrame(
                            length,
                            new Vector3f(
                                    (float) Math.toRadians(array.get(0).getAsFloat()),
                                    (float) Math.toRadians(array.get(1).getAsFloat()),
                                    (float) Math.toRadians(array.get(2).getAsFloat())
                            ), LerpMode.LINEAR
                    )
            };
        }

        JsonObject object = element.getAsJsonObject();
        return object.entrySet().stream()
                .map(entry -> {
                    JsonArray array;
                    float time = Float.parseFloat(entry.getKey());
                    LerpMode mode = LerpMode.LINEAR;
                    if (entry.getValue().isJsonArray()) {
                        array = entry.getValue().getAsJsonArray();
                    } else {
                        JsonObject obj = entry.getValue().getAsJsonObject();
                        array = obj.getAsJsonArray("post");
                        mode = LerpMode.valueOf(obj.get("lerp_mode").getAsString().toUpperCase());
                    }
                    Vector3f vec3 = new Vector3f(
                            (float) Math.toRadians(array.get(0).getAsFloat()),
                            (float) Math.toRadians(array.get(1).getAsFloat()),
                            (float) Math.toRadians(array.get(2).getAsFloat())
                    );
                    return new KeyFrame(time, vec3, mode);
                }).toArray(KeyFrame[]::new);
    }

    private static KeyFrame[] getPosition(JsonElement element, ModelBone bone, float length) {
        if (element == null)
            return new KeyFrame[0];

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            return new KeyFrame[] {
                    new KeyFrame(
                            0,
                            new Vector3f(
                                    array.get(0).getAsFloat(),
                                    array.get(1).getAsFloat(),
                                    array.get(2).getAsFloat()
                            ), LerpMode.LINEAR
                    ),
                    new KeyFrame(
                            length,
                            new Vector3f(
                                    array.get(0).getAsFloat(),
                                    array.get(1).getAsFloat(),
                                    array.get(2).getAsFloat()
                            ), LerpMode.LINEAR
                    )
            };
        }

        JsonObject object = element.getAsJsonObject();
        return object.entrySet().stream()
                .map(entry -> {
                    JsonArray array;
                    float time = Float.parseFloat(entry.getKey());
                    LerpMode mode = LerpMode.LINEAR;
                    if (entry.getValue().isJsonArray()) {
                        array = entry.getValue().getAsJsonArray();
                    } else {
                        JsonObject obj = entry.getValue().getAsJsonObject();
                        array = obj.getAsJsonArray("post");
                        mode = LerpMode.valueOf(obj.get("lerp_mode").getAsString().toUpperCase());
                    }
                    Vector3f vec3 = new Vector3f(
                            array.get(0).getAsFloat(),
                            array.get(1).getAsFloat(),
                            array.get(2).getAsFloat()
                    );

                    // Fix offset for arms
                    if (bone == ModelBone.LEFT_ARM || bone == ModelBone.RIGHT_ARM) {
                        vec3.y += 0.5f;
                    }

                    return new KeyFrame(time, vec3, mode);
                }).toArray(KeyFrame[]::new);
    }

    public AnimationSection(boolean repeat, float length, float fadeIn, float fadeOut, EnumMap<ModelBone, BoneData> keyFrames) {
        this.repeat = repeat;
        this.length = length;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.keyFrames = keyFrames;
    }

    public void update(ModelBone bone, ModelPart part, ClientAnimator animator, float fadeIn) {
        final ClientAnimator.Cache cache = animator.getCache(bone);
        final BoneData data = this.keyFrames.get(bone);

        Vector3f position = ZERO;
        Vector3f rotation = ZERO;

        if (animator.getAnimation().isOverride(bone)) {
            final PartPose initialPose = part.getInitialPose();
            final PartPose pose = PartPose.offsetAndRotation(
                    part.x - initialPose.x,
                    initialPose.y - part.y,     // Fix invert
                    part.z - initialPose.z,
                    part.xRot - initialPose.xRot,
                    part.yRot - initialPose.yRot,
                    part.zRot - initialPose.zRot
            );

            position = new Vector3f(pose.x, pose.y, pose.z);
            rotation = new Vector3f(pose.xRot, pose.yRot, pose.zRot);
        }

        if (data == null) {
            cache.position().set(position);
            cache.rotation().set(rotation);
            return;
        }

        final KeyFrame[] posFrames = data.position();
        final KeyFrame[] rotFrames = data.rotation();

        if (animator.isTransferring()) {
            final float time = fadeIn == 0 ? 1.0f : Mth.clamp(animator.getTimer() / fadeIn, 0.0f, 1.0f);

            if (posFrames.length != 0) {
                cache.position().set(linearLerp(
                        IAnimationState.Impl.get(animator.getCurState()).getSrc(cache.position(), position),
                        IAnimationState.Impl.get(animator.getNextState()).getDest(posFrames[0].vec3(), position),
                        time
                ));
            } else {
                cache.position().set(linearLerp(
                        IAnimationState.Impl.get(animator.getCurState()).getSrc(cache.position(), position),
                        IAnimationState.Impl.get(animator.getNextState()).getDest(position, position),
                        time
                ));
            }
            if (rotFrames.length != 0) {
                cache.rotation().set(linearLerp(
                        IAnimationState.Impl.get(animator.getCurState()).getSrc(cache.rotation(), rotation),
                        IAnimationState.Impl.get(animator.getNextState()).getDest(rotFrames[0].vec3(), rotation),
                        time
                ));
            } else {
                cache.rotation().set(linearLerp(
                        IAnimationState.Impl.get(animator.getCurState()).getSrc(cache.rotation(), rotation),
                        IAnimationState.Impl.get(animator.getNextState()).getDest(rotation, rotation),
                        time
                ));
            }

        } else {
            final float time = Mth.clamp(animator.getTimer(), 0.0f, length);
            interpolate(posFrames, cache.position(), time);
            interpolate(rotFrames, cache.rotation(), time);
        }
    }

    public static void interpolate(KeyFrame[] keyframes, Vector3f out, float time) {
        KeyFrame kf0 = null, kf1 = null, kf2 = null, kf3 = null;
        for (int i = 0; i < keyframes.length - 1; i++) {
            if (time >= keyframes[i].time() && time <= keyframes[i + 1].time()) {
                kf1 = keyframes[i];
                kf2 = keyframes[i + 1];
                kf0 = (i > 0) ? keyframes[i - 1] : kf1;
                kf3 = (i + 2 < keyframes.length) ? keyframes[i + 2] : kf2;
                break;
            }
        }

        if (kf1 == null || kf2 == null) {
            out.set(ZERO);
            return;
        }

        float localT = (time - kf1.time()) / (kf2.time() - kf1.time());

        switch (kf2.mode()) {
            case LINEAR -> out.set(linearLerp(kf1.vec3(), kf2.vec3(), localT));
            case CATMULLROM -> out.set(catmullRomLerp(kf0.vec3(), kf1.vec3(), kf2.vec3(), kf3.vec3(), localT));
            case STEP -> out.set(kf1.vec3());
        }
    }

    public static Vector3f interpolate(KeyFrame[] keyframes, float time) {
        KeyFrame kf0 = null, kf1 = null, kf2 = null, kf3 = null;
        for (int i = 0; i < keyframes.length - 1; i++) {
            if (time >= keyframes[i].time() && time <= keyframes[i + 1].time()) {
                kf1 = keyframes[i];
                kf2 = keyframes[i + 1];
                kf0 = (i > 0) ? keyframes[i - 1] : kf1;
                kf3 = (i + 2 < keyframes.length) ? keyframes[i + 2] : kf2;
                break;
            }
        }

        if (kf1 == null || kf2 == null)
            return ZERO;

        float localT = (time - kf1.time()) / (kf2.time() - kf1.time());

        return switch (kf2.mode()) {
            case LINEAR -> linearLerp(kf1.vec3(), kf2.vec3(), localT);
            case CATMULLROM -> catmullRomLerp(kf0.vec3(), kf1.vec3(), kf2.vec3(), kf3.vec3(), localT);
            case STEP -> kf1.vec3();
        };
    }

    public static Vector3f linearLerp(Vector3f p1, Vector3f p2, float time) {
        return new Vector3f(p1.x * (1.0F - time) + p2.x * time, p1.y * (1.0F - time) + p2.y * time, p1.z * (1.0F - time) + p2.z * time);
    }

    public static Vector3f catmullRomLerp(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, float time) {
        float t2 = time * time;
        float t3 = t2 * time;
        float x = 0.5F * (2.0F * p1.x + (-p0.x + p2.x) * time + (2.0F * p0.x - 5.0F * p1.x + 4.0F * p2.x - p3.x) * t2 + (-p0.x + 3.0F * p1.x - 3.0F * p2.x + p3.x) * t3);
        float y = 0.5F * (2.0F * p1.y + (-p0.y + p2.y) * time + (2.0F * p0.y - 5.0F * p1.y + 4.0F * p2.y - p3.y) * t2 + (-p0.y + 3.0F * p1.y - 3.0F * p2.y + p3.y) * t3);
        float z = 0.5F * (2.0F * p1.z + (-p0.z + p2.z) * time + (2.0F * p0.z - 5.0F * p1.z + 4.0F * p2.z - p3.z) * t2 + (-p0.z + 3.0F * p1.z - 3.0F * p2.z + p3.z) * t3);
        return new Vector3f(x, y, z);
    }

    public boolean repeatable() {
        return repeat;
    }

    public float getLength() {
        return length;
    }

    public float getFadeIn() {
        return fadeIn;
    }

    public float getFadeOut() {
        return fadeOut;
    }

    public static void toNetwork(FriendlyByteBuf byteBuf, AnimationSection animation) {
        byteBuf.writeBoolean(animation.repeat);
        byteBuf.writeFloat(animation.length);
        byteBuf.writeFloat(animation.fadeIn);
        byteBuf.writeFloat(animation.fadeOut);

        for (ModelBone value : ModelBone.values()) {
            byteBuf.writeOptional(Optional.ofNullable(animation.keyFrames.get(value)), BoneData::toNetwork);
        }
    }

    public static AnimationSection fromNetwork(FriendlyByteBuf byteBuf) {
        final boolean repeat = byteBuf.readBoolean();
        final float length = byteBuf.readFloat();
        final float fadeIn = byteBuf.readFloat();
        final float fadeOut = byteBuf.readFloat();
        final EnumMap<ModelBone, BoneData> map = new EnumMap<>(ModelBone.class);
        for (ModelBone value : ModelBone.values()) {
            Optional<BoneData> optional = byteBuf.readOptional(BoneData::fromNetwork);
            optional.ifPresent(boneData -> map.put(value, boneData));
        }
        return new AnimationSection(repeat, length, fadeIn, fadeOut, map);
    }

    public record KeyFrame(float time, Vector3f vec3, LerpMode mode) {
        public static void toNetwork(FriendlyByteBuf byteBuf, KeyFrame data) {
            byteBuf.writeFloat(data.time);
            byteBuf.writeVector3f(data.vec3);
            byteBuf.writeEnum(data.mode);
        }

        public static KeyFrame fromNetwork(FriendlyByteBuf byteBuf) {
            final float time = byteBuf.readFloat();
            final Vector3f vec3 = byteBuf.readVector3f();
            final LerpMode mode = byteBuf.readEnum(LerpMode.class);
            return new KeyFrame(time, vec3, mode);
        }
    }

    public record BoneData(KeyFrame[] rotation, KeyFrame[] position) {
        public static void toNetwork(FriendlyByteBuf byteBuf, BoneData data) {
            writeArray(byteBuf, data.rotation);
            writeArray(byteBuf, data.position);
        }

        public static BoneData fromNetwork(FriendlyByteBuf byteBuf) {
            KeyFrame[] rotate = readArray(byteBuf);
            KeyFrame[] position = readArray(byteBuf);
            return new BoneData(rotate, position);
        }

        private static void writeArray(FriendlyByteBuf byteBuf, KeyFrame[] arr) {
            byteBuf.writeVarInt(arr.length);
            for (KeyFrame keyFrame : arr) {
                KeyFrame.toNetwork(byteBuf, keyFrame);
            }
        }

        private static KeyFrame[] readArray(FriendlyByteBuf byteBuf) {
            int i = byteBuf.readVarInt();
            KeyFrame[] arr = new KeyFrame[i];
            for (int i1 = 0; i1 < i; i1++) {
                arr[i1] = KeyFrame.fromNetwork(byteBuf);
            }
            return arr;
        }
    }
}
