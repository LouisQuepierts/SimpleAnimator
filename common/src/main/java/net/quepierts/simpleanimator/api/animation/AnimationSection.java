package net.quepierts.simpleanimator.api.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.quepierts.simpleanimator.api.animation.keyframe.*;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import net.quepierts.simpleanimator.core.client.state.IAnimationState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;

public class AnimationSection {
    private static final Vector3f ZERO_POS = new Vector3f(0);
    private static final Quaternionf ZERO_ROT = new Quaternionf();

    private static final String PREFIX_VARIABLE = "var_";
    private static final Object2ByteMap<String> KEYWORDS;

    private static final float DEFAULT_FADE_IN = 0.05f;
    private static final float DEFAULT_FADE_OUT = 0.05f;

    private final boolean repeat;

    private final float length;
    private final float fadeIn;
    private final float fadeOut;

    private final EnumMap<ModelBone, BoneData> keyFrames;
    private final Object2ObjectMap<String, VariableKeyFrame.Group> varFrames;

    public static AnimationSection fromJsonObject(JsonObject json, Animation.Type type) {
        if (json == null) {
            return null;
        }

        final boolean repeat = json.has("loop") && json.get("loop").getAsBoolean();
        final float length = json.get("animation_length").getAsFloat();

        final String var = json.has("anim_time_update") ? json.get("anim_time_update").getAsString() : "";
        final Map<String, String> variables = getVariables(var);

        float fadeIn = tryParse(variables.get("fade_in"), DEFAULT_FADE_IN);
        float fadeOut = tryParse(variables.get("fade_out"), DEFAULT_FADE_OUT);

        EnumMap<ModelBone, BoneData> keyFrames = new EnumMap<>(ModelBone.class);
        Object2ObjectMap<String, VariableKeyFrame.Group> varFrames = new Object2ObjectOpenHashMap<>();
        JsonObject bones = json.getAsJsonObject("bones");

        for (Map.Entry<String, JsonElement> entry : bones.entrySet()) {
            if (!entry.getKey().startsWith(type.prefix)) {
                continue;
            }
            final String key = entry.getKey().substring(type.prefix.length() + 1);

            if (key.startsWith(PREFIX_VARIABLE)) {
                getVarFramesFromBones(key.substring(4), entry.getValue().getAsJsonObject(), length, varFrames);
                continue;
            }

            ModelBone bone = ModelBone.fromString(key);

            if (bone != null) {
                VectorKeyFrame[] rotation = getRotation(entry.getValue().getAsJsonObject().get("rotation"), bone, length);
                VectorKeyFrame[] position = getPosition(entry.getValue().getAsJsonObject().get("position"), bone, length);
                keyFrames.put(bone, new BoneData(rotation, position));
            }
        }

        return new AnimationSection(repeat, length, fadeIn, fadeOut, keyFrames, varFrames);
    }

    public static AnimationSection fromJsonObject(JsonObject json) {
        if (json == null) {
            return null;
        }

        final boolean repeat = json.has("loop") && json.get("loop").getAsBoolean();
        final float length = json.get("animation_length").getAsFloat();

        final String var = json.has("anim_time_update") ? json.get("anim_time_update").getAsString() : "";
        final Map<String, String> variables = getVariables(var);

        float fadeIn = tryParse(variables.get("fade_in"), DEFAULT_FADE_IN);
        float fadeOut = tryParse(variables.get("fade_out"), DEFAULT_FADE_OUT);

        EnumMap<ModelBone, BoneData> keyFrames = new EnumMap<>(ModelBone.class);
        Object2ObjectMap<String, VariableKeyFrame.Group> varFrames = new Object2ObjectOpenHashMap<>();
        JsonObject bones = json.getAsJsonObject("bones");

        for (Map.Entry<String, JsonElement> entry : bones.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(PREFIX_VARIABLE)) {
                getVarFramesFromBones(key.substring(4), entry.getValue().getAsJsonObject(), length, varFrames);
                continue;
            }

            ModelBone bone = ModelBone.fromString(key);

            if (bone != null) {
                VectorKeyFrame[] rotation = getRotation(entry.getValue().getAsJsonObject().get("rotation"), bone, length);
                VectorKeyFrame[] position = getPosition(entry.getValue().getAsJsonObject().get("position"), bone, length);
                keyFrames.put(bone, new BoneData(rotation, position));
            }
        }

        return new AnimationSection(repeat, length, fadeIn, fadeOut, keyFrames, varFrames);
    }

    public void getVariables(Object2IntMap<String> set) {
        for (Map.Entry<String, VariableKeyFrame.Group> entry : this.varFrames.entrySet()) {
            set.put(entry.getKey(), entry.getValue().variableSize());
        }
    }

    private static float tryParse(String str, float def) {
        if (StringUtil.isNullOrEmpty(str)) {
            return def;
        }
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static void getVarFramesFromBones(String key, JsonObject object, float length, Object2ObjectMap<String, VariableKeyFrame.Group> map) {
        String[] split = key.split("_+");
        int i = 0;
        int[] sizes = {1, 1, 1};
        String[] keys = new String[3];

        for (String string : split) {
            byte b = KEYWORDS.getOrDefault(string, (byte) 0);

            if (b != 0) {
                sizes[i] = b;
                continue;
            }

            keys[i++] = string;
            if (i == 3) {
                break;
            }
        }

        if (i > 0) {
            VariableKeyFrame.Group array = getVarsFromBone(object.get("rotation"), sizes[0], length);
            if (array != null) {
                map.put(keys[0], array);
            }
        }

        if (i > 1) {
            VariableKeyFrame.Group array = getVarsFromBone(object.get("position"), sizes[1], length);
            if (array != null) {
                map.put(keys[1], array);
            }
        }

        if (i > 2) {
            VariableKeyFrame.Group array = getVarsFromBone(object.get("scale"), sizes[2], length);
            if (array != null) {
                map.put(keys[2], array);
            }
        }
    }

    private static VariableKeyFrame.Group getVarsFromBone(JsonElement element, int size, float length) {
        if (element == null) {
            return null;
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            VariableHolder pre = VariableHolder.fromJsonArray(array, size);
            return new VariableKeyFrame.Group(
                    new VariableKeyFrame[] {
                    new VariableKeyFrame(
                            0,
                            pre, pre,
                            LerpMode.LINEAR
                    ),
                    new VariableKeyFrame(
                            length,
                            pre, pre,
                            LerpMode.LINEAR
                    )}
                    , size
            );
        }

        JsonObject object = element.getAsJsonObject();
        VariableKeyFrame[] array = object.entrySet().stream()
                .map(entry -> {
                    JsonArray pre;
                    JsonArray post;
                    float time = Float.parseFloat(entry.getKey());
                    LerpMode mode = LerpMode.LINEAR;
                    if (entry.getValue().isJsonArray()) {
                        pre = entry.getValue().getAsJsonArray();
                        post = null;
                    } else {
                        JsonObject obj = entry.getValue().getAsJsonObject();
                        pre = obj.getAsJsonArray("pre");
                        post = obj.getAsJsonArray("post");

                        if (pre == null)
                            pre = post;

                        JsonElement lerpMode = obj.get("lerp_mode");
                        mode = lerpMode == null ? LerpMode.LINEAR : LerpMode.valueOf(lerpMode.getAsString().toUpperCase());
                    }
                    VariableHolder preHolder = VariableHolder.fromJsonArray(pre, size);
                    VariableHolder postHolder = post == null ? preHolder : VariableHolder.fromJsonArray(post, size);
                    return new VariableKeyFrame(time, preHolder, postHolder, mode);
                }).toArray(VariableKeyFrame[]::new);
        return new VariableKeyFrame.Group(array, size);
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

    private static VectorKeyFrame[] getRotation(JsonElement element, ModelBone bone, float length) {
        if (element == null) {
            return new VectorKeyFrame[0];
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

            Vector3f vector3f = new Vector3f(
                    (float) Math.toRadians(array.get(0).getAsFloat()),
                    (float) Math.toRadians(array.get(1).getAsFloat()),
                    (float) Math.toRadians(array.get(2).getAsFloat())
            );
            return new VectorKeyFrame[] {
                    new VectorKeyFrame(
                            0,
                            vector3f, vector3f,
                            LerpMode.LINEAR
                    ),
                    new VectorKeyFrame(
                            length,
                            vector3f, vector3f,
                            LerpMode.LINEAR
                    )
            };
        }

        JsonObject object = element.getAsJsonObject();
        return object.entrySet().stream()
                .map(entry -> {
                    JsonArray pre;
                    JsonArray post;
                    float time = Float.parseFloat(entry.getKey());
                    LerpMode mode = LerpMode.LINEAR;
                    if (entry.getValue().isJsonArray()) {
                        pre = entry.getValue().getAsJsonArray();
                        post = null;
                    } else {
                        JsonObject obj = entry.getValue().getAsJsonObject();
                        pre = obj.getAsJsonArray("pre");
                        post = obj.getAsJsonArray("post");

                        if (pre == null)
                            pre = post;
                        JsonElement lerpMode = obj.get("lerp_mode");
                        mode = lerpMode == null ? LerpMode.LINEAR : LerpMode.valueOf(lerpMode.getAsString().toUpperCase());
                    }
                    Vector3f preRotation = new Vector3f(
                            (float) Math.toRadians(pre.get(0).getAsFloat()),
                            (float) Math.toRadians(pre.get(1).getAsFloat()),
                            (float) Math.toRadians(pre.get(2).getAsFloat())
                    );

                    Vector3f postRotation = post == null ? preRotation : new Vector3f(
                            (float) Math.toRadians(post.get(0).getAsFloat()),
                            (float) Math.toRadians(post.get(1).getAsFloat()),
                            (float) Math.toRadians(post.get(2).getAsFloat())
                    );
                    /*Quaternionf quaternionf = new Quaternionf().rotateXYZ(
                            (float) Math.toRadians(post.get(0).getAsFloat()),
                            (float) Math.toRadians(post.get(1).getAsFloat()),
                            (float) Math.toRadians(post.get(2).getAsFloat())
                    );
                    return new VectorKeyFrame(time, quaternionf, mode);*/
                    return new VectorKeyFrame(time, preRotation, postRotation, mode);
                }).toArray(VectorKeyFrame[]::new);
    }

    private static VectorKeyFrame[] getPosition(JsonElement element, ModelBone bone, float length) {
        if (element == null) {
            return new VectorKeyFrame[0];
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            Vector3f vector3f = new Vector3f(
                    array.get(0).getAsFloat(),
                    array.get(1).getAsFloat(),
                    array.get(2).getAsFloat()
            );
            return new VectorKeyFrame[] {
                    new VectorKeyFrame(
                            0,
                            vector3f, vector3f, LerpMode.LINEAR
                    ),
                    new VectorKeyFrame(
                            length,
                            vector3f, vector3f, LerpMode.LINEAR
                    )
            };
        }

        JsonObject object = element.getAsJsonObject();
        return object.entrySet().stream()
                .map(entry -> {
                    JsonArray pre;
                    JsonArray post;
                    float time = Float.parseFloat(entry.getKey());
                    LerpMode mode = LerpMode.LINEAR;
                    if (entry.getValue().isJsonArray()) {
                        pre = entry.getValue().getAsJsonArray();
                        post = null;
                    } else {
                        JsonObject obj = entry.getValue().getAsJsonObject();
                        pre = obj.getAsJsonArray("pre");
                        post = obj.getAsJsonArray("post");

                        if (pre == null)
                            pre = post;

                        JsonElement lerpMode = obj.get("lerp_mode");
                        mode = lerpMode == null ? LerpMode.LINEAR : LerpMode.valueOf(lerpMode.getAsString().toUpperCase());
                    }
                    Vector3f prePosition = new Vector3f(
                            pre.get(0).getAsFloat(),
                            pre.get(1).getAsFloat(),
                            pre.get(2).getAsFloat()
                    );
                    Vector3f postPosition = post == null ? prePosition : new Vector3f(
                            post.get(0).getAsFloat(),
                            post.get(1).getAsFloat(),
                            post.get(2).getAsFloat()
                    );

                    // Fix offset for arms
                    if (bone == ModelBone.LEFT_ARM || bone == ModelBone.RIGHT_ARM) {
                        prePosition.y += 0.5f;
                    }

                    return new VectorKeyFrame(time, prePosition, postPosition, mode);
                }).toArray(VectorKeyFrame[]::new);
    }

    public AnimationSection(boolean repeat, float length, float fadeIn, float fadeOut, EnumMap<ModelBone, BoneData> keyFrames, Object2ObjectMap<String, VariableKeyFrame.Group> varFrames) {
        this.repeat = repeat;
        this.length = length;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.keyFrames = keyFrames;
        this.varFrames = varFrames;
    }

    public void update(ModelBone bone, ModelPart part, ClientAnimator animator, float fadeIn) {
        final ClientAnimator.Cache cache = animator.getCache(bone);
        final BoneData data = this.keyFrames.get(bone);

        Vector3f position = ZERO_POS;
        Vector3f rotation = ZERO_POS;
//        Quaternionf rotation = ZERO_ROT;
//        Vector3f eular = ZERO_POS;

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
//            rotation = new Quaternionf().rotateXYZ(pose.xRot, pose.yRot, pose.zRot);
//            eular = new Vector3f(pose.xRot, pose.yRot, pose.zRot);
            rotation = new Vector3f(pose.xRot, pose.yRot, pose.zRot);
        }

        if (data == null) {
            cache.position().set(position);
            cache.rotation().set(rotation);
            return;
        }

        final VectorKeyFrame[] posFrames = data.position();
        final VectorKeyFrame[] rotFrames = data.rotation();

//        Vector3f eulerAnglesXYZ = cache.rotation().getEulerAnglesXYZ(new Vector3f());
        if (animator.isTransferring()) {
            final float time = fadeIn == 0 ? 1.0f : Mth.clamp(animator.getTimer() / fadeIn, 0.0f, 1.0f);

            if (posFrames.length != 0) {
                cache.position().set(Interpolation.linerInterpolation(
                        IAnimationState.Impl.get(animator.getCurState()).getSrc(cache.position(), position),
                        IAnimationState.Impl.get(animator.getNextState()).getDest(posFrames[0].getPre(), position),
                        time
                ));
            } else {
                cache.position().set(Interpolation.linerInterpolation(
                        IAnimationState.Impl.get(animator.getCurState()).getSrc(cache.position(), position),
                        IAnimationState.Impl.get(animator.getNextState()).getDest(position, position),
                        time
                ));
            }

            if (rotFrames.length != 0) {
                /*Vector3f vector3f = Interpolation.linerInterpolation(
                        IAnimationState.Impl.get(animator.getCurState()).getSrc(eulerAnglesXYZ, eular),
                        IAnimationState.Impl.get(animator.getNextState()).getDest(rotFrames[0].getValue(), eular),
                        time
                );
                cache.rotation().set(0, 0, 0, 1).rotateXYZ(vector3f.x, vector3f.y, vector3f.z);*/
                cache.rotation().set(
                        Interpolation.linerInterpolation(
                                IAnimationState.Impl.get(animator.getCurState()).getSrc(cache.rotation(), rotation),
                                IAnimationState.Impl.get(animator.getNextState()).getDest(rotFrames[0].getPre(), rotation),
                                time
                        )
                );
            } else {
                /*Vector3f vector3f = Interpolation.linerInterpolation(
                        IAnimationState.Impl.get(animator.getCurState()).getSrc(eulerAnglesXYZ, eular),
                        IAnimationState.Impl.get(animator.getNextState()).getDest(eular, eular),
                        time
                );
                cache.rotation().set(0, 0, 0, 1).rotateXYZ(vector3f.x, vector3f.y, vector3f.z);*/
                cache.rotation().set(
                        Interpolation.linerInterpolation(
                                IAnimationState.Impl.get(animator.getCurState()).getSrc(cache.rotation(), rotation),
                                IAnimationState.Impl.get(animator.getNextState()).getDest(rotation, rotation),
                                time
                        )
                );
            }

        } else {
            final float time = Mth.clamp(animator.getTimer(), 0.0f, length);
            cache.position().set(Interpolation.interpolation(posFrames, cache.position(), time));
            cache.rotation().set(Interpolation.interpolation(rotFrames, cache.rotation(), time));
//            Vector3f interpolationed = Interpolation.interpolation(rotFrames, eulerAnglesXYZ, time);
//            cache.rotation().set(0, 0, 0, 1).rotateXYZ(interpolationed.x, interpolationed.y, interpolationed.z);
        }
    }

    public void update(String variable, VariableHolder holder, ClientAnimator animator, float fadeIn) {
        VariableKeyFrame.Group group = this.varFrames.get(variable);
        if (group == null) {
            holder.setValue(0.0f);
            return;
        }
        final VariableKeyFrame[] frames = group.keyFrames();

        VariableHolder target = new VariableHolder(0);

        if (animator.isTransferring()) {
            final float time = fadeIn == 0 ? 1.0f : Mth.clamp(animator.getTimer() / fadeIn, 0.0f, 1.0f);

            if (frames.length != 0) {
                holder.setValue(
                        Interpolation.linerInterpolation(
                                IAnimationState.Impl.get(animator.getCurState()).getSrc(holder, target),
                                IAnimationState.Impl.get(animator.getNextState()).getDest(frames[0].getPre(), target),
                                time
                        )
                );
            } else {
                holder.setValue(
                        Interpolation.linerInterpolation(
                                IAnimationState.Impl.get(animator.getCurState()).getSrc(holder, target),
                                IAnimationState.Impl.get(animator.getNextState()).getDest(target, target),
                                time
                        )
                );
            }

        } else {
            final float time = Mth.clamp(animator.getTimer(), 0.0f, length);
            holder.setValue(Interpolation.interpolation(frames, holder, time));
        }
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

        byteBuf.writeMap(animation.varFrames, FriendlyByteBuf::writeUtf, VariableKeyFrame.Group::toNetwork);
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
        final Object2ObjectMap<String, VariableKeyFrame.Group> varFrames = byteBuf.readMap(
                Object2ObjectOpenHashMap::new,
                FriendlyByteBuf::readUtf,
                VariableKeyFrame.Group::fromNetwork
        );
        return new AnimationSection(repeat, length, fadeIn, fadeOut, map, varFrames);
    }

    private static void writeVariableKeyFrames(FriendlyByteBuf byteBuf, VariableKeyFrame[] frames) {
        BoneData.writeKeyFrames(byteBuf, frames);
    }

    private static VariableKeyFrame[] readVariableKeyFrames(FriendlyByteBuf byteBuf) {
        return BoneData.readKeyFrames(byteBuf, VariableKeyFrame[]::new, VariableKeyFrame.class);
    }

    /*public record KeyFrame(float time, Vector3f vec3, LerpMode mode) {
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
    }*/

    public record BoneData(VectorKeyFrame[] rotation, VectorKeyFrame[] position) {
        public static void toNetwork(FriendlyByteBuf byteBuf, BoneData data) {
            writeKeyFrames(byteBuf, data.rotation);
            writeKeyFrames(byteBuf, data.position);
        }

        public static BoneData fromNetwork(FriendlyByteBuf byteBuf) {
            VectorKeyFrame[] rotate = readKeyFrames(byteBuf, VectorKeyFrame[]::new, VectorKeyFrame.class);
            VectorKeyFrame[] position = readKeyFrames(byteBuf, VectorKeyFrame[]::new, VectorKeyFrame.class);
            return new BoneData(rotate, position);
        }

        public static void writeKeyFrames(FriendlyByteBuf byteBuf, KeyFrame<?>[] arr) {
            byteBuf.writeVarInt(arr.length);
            for (KeyFrame<?> keyFrame : arr) {
                keyFrame.toNetwork(byteBuf);
            }
        }

        public static <T extends KeyFrame<?>> T[] readKeyFrames(FriendlyByteBuf byteBuf, IntFunction<T[]> function, Class<T> clazz) {
            int i = byteBuf.readVarInt();
            T[] arr = function.apply(i);
            for (int i1 = 0; i1 < i; i1++) {
                arr[i1] = KeyFrame.Decoder.decode(byteBuf, clazz);
            }
            return arr;
        }
    }

    static {
        Object2ByteOpenHashMap<String> keywords = new Object2ByteOpenHashMap<>(6);
        keywords.put("vec2", (byte) 2);
        keywords.put("vec3", (byte) 3);
        keywords.put("float",(byte) 1);
        keywords.put("float2", (byte) 2);
        keywords.put("float3", (byte) 3);
        keywords.put("int", (byte) 1);
        keywords.put("int2", (byte) 2);
        keywords.put("int3", (byte) 3);
        keywords.put("bool", (byte) 1);
        keywords.put("bool2", (byte) 2);
        keywords.put("bool3", (byte) 3);
        KEYWORDS = Object2ByteMaps.unmodifiable(keywords);
    }
}
