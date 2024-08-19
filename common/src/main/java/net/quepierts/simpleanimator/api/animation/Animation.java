package net.quepierts.simpleanimator.api.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.FriendlyByteBuf;
import net.quepierts.simpleanimator.api.animation.keyframe.VariableHolder;
import net.quepierts.simpleanimator.core.JsonUtils;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.client.ClientAnimator;

import java.io.Reader;
import java.util.Optional;
import java.util.Set;

public class Animation {
    private static final String KEY_REQUEST = "invite";
    private static final String KEY_WAITING = "waiting";
    private static final String KEY_CANCEL = "cancel";
    private static final String KEY_ENTER = "enter";
    private static final String KEY_LOOP = "main";
    private static final String KEY_EXIT = "exit";

    private final AnimationSection enter;
    private final AnimationSection loop;
    private final AnimationSection exit;

    private final boolean movable;
    private final boolean abortable;

    private final boolean useVanillaRig;

    private final Type type;
    private final byte unlockFlag;

    public static Animation[] fromStream(Reader reader) {
        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
        return serialize(object);
    }

    public static Animation[] serialize(JsonObject json) {
        JsonObject object = json.getAsJsonObject("animations");

        if (!object.has(KEY_LOOP))
            throw new RuntimeException("Cannot accept animation without \"main\"!");

        boolean movable = JsonUtils.getBoolean("movable", json, false);
        boolean abortable = JsonUtils.getBoolean("abortable", json, true);

        boolean rig = JsonUtils.getBoolean("useVanillaRig", json, true);

        byte unlock = getUnlocks(json);

        Animation[] animations;
        if (isInteractiveAnimation(object)) {
            animations = new Animation[3];

            final AnimationSection request = AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_REQUEST), Type.INVITE);
            if (request == null)
                throw new RuntimeException("Required animation: \"request\"!");

            final AnimationSection waiting = AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_WAITING), Type.INVITE);
            if (waiting == null)
                throw new RuntimeException("Required animation: \"waiting\"!");

            if (!waiting.repeatable())
                throw new RuntimeException("\"waiting\" should be looped!");
            final AnimationSection cancel = AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_CANCEL), Type.INVITE);

            animations[0] = new Animation(
                    request, waiting, cancel,
                    movable, true, rig, unlock, Type.INVITE
            );
            animations[1] = new Animation(
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_ENTER), Type.REQUESTER),
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_LOOP), Type.REQUESTER),
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_EXIT), Type.REQUESTER),
                    movable, false, rig, unlock, Type.REQUESTER
            );
            animations[2] = new Animation(
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_ENTER), Type.RECEIVER),
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_LOOP), Type.RECEIVER),
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_EXIT), Type.RECEIVER),
                    movable, false, rig, unlock, Type.RECEIVER

            );
        } else {
            animations = new Animation[] {
                    new Animation(
                            AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_ENTER)),
                            AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_LOOP)),
                            AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_EXIT)),
                            movable, abortable, rig, unlock, Type.SIMPLE
                    )
            };
        }

        return animations;
    }

    private static byte getUnlocks(JsonObject object) {
        JsonElement element = object.get("unlock");

        if (element == null || !element.isJsonArray())
            return (byte) 0xFF;

        byte flag = (byte) 0xFF;
        JsonArray array = element.getAsJsonArray();
        for (JsonElement ele : array) {
            if (!ele.isJsonPrimitive())
                continue;

            ModelBone bone = ModelBone.fromString(ele.getAsString());

            if (bone == null)
                continue;

            flag = bone.remove(flag);
        }
        return flag;
    }

    private Animation(
            AnimationSection enter,
            AnimationSection loop,
            AnimationSection exit,
            boolean movable,
            boolean abortable,
            boolean useVanillaRig,
            byte lock, Type type) {
        this.enter = enter;
        this.loop = loop;
        this.exit = exit;
        this.movable = movable;
        this.abortable = abortable;
        this.useVanillaRig = useVanillaRig;
        this.unlockFlag = lock;
        this.type = type;
    }

    private static boolean isInteractiveAnimation(JsonObject json) {
        return json.has(KEY_REQUEST);
    }

    private static boolean getBoolean(JsonObject json, String key, boolean def) {
        return json.has(key) ? json.get(key).getAsBoolean() : def;
    }

    public AnimationSection get(AnimationState state) {
        return switch (state) {
            case ENTER -> enter != null ? enter : loop;
            case EXIT -> exit != null ? exit : loop;
            default -> loop;
        };
    }

    public float getFadeIn(ClientAnimator animator) {
        AnimationSection animation = get(animator.getCurState());

        if (animator.getNextState() == AnimationState.IDLE) {
            return animation.getFadeOut();
        } else if (animator.getCurState() == AnimationState.IDLE) {
            return get(animator.getNextState()).getFadeIn();
        } else {
            return animation.getFadeIn();
        }
    }

    public boolean repeatable() {
        return loop.repeatable();
    }

    public boolean hasEnterAnimation() {
        return enter != null;
    }

    public boolean hasExitAnimation() {
        return exit != null;
    }

    public void update(ModelBone bone, ModelPart part, ClientAnimator animator) {
        final AnimationSection animation = animator.isTransferring() ? this.get(animator.getNextState()) : this.get(animator.getCurState());
        part.xRot = PlayerUtils.normalizeRadians(part.xRot);
        part.yRot = PlayerUtils.normalizeRadians(part.yRot);
        animation.update(bone, part, animator, getFadeIn(animator));
    }

    public void update(String variable, VariableHolder holder, ClientAnimator animator) {
        final AnimationSection animation = animator.isTransferring() ? this.get(animator.getNextState()) : this.get(animator.getCurState());
        animation.update(variable, holder, animator, getFadeIn(animator));
    }

    public boolean isOverride(ModelBone bone) {
        return bone.in(this.unlockFlag);
    }

    public boolean isOverrideHead() {
        return ModelBone.HEAD.in(this.unlockFlag);
    }

    public boolean isOverrideHands() {
        return ModelBone.LEFT_ARM.in(this.unlockFlag) || ModelBone.RIGHT_ARM.in(this.unlockFlag);
    }

    public boolean isMovable() {
        return movable;
    }

    public boolean isAbortable() {
        return abortable;
    }

    public Type getType() {
        return type;
    }

    public Set<String> getVariables() {
        Set<String> set = new ObjectOpenHashSet<>();

        if (this.enter != null)
            this.enter.getVariables(set);

        if (this.loop != null)
            this.loop.getVariables(set);

        if (this.exit != null)
            this.exit.getVariables(set);

        return set;
    }

    public static void toNetwork(FriendlyByteBuf byteBuf, Animation group) {
        byteBuf.writeBoolean(group.movable);
        byteBuf.writeBoolean(group.abortable);
        byteBuf.writeBoolean(group.useVanillaRig);
        byteBuf.writeByte(group.unlockFlag);
        byteBuf.writeEnum(group.type);
        byteBuf.writeOptional(Optional.ofNullable(group.enter), AnimationSection::toNetwork);
        byteBuf.writeOptional(Optional.ofNullable(group.loop), AnimationSection::toNetwork);
        byteBuf.writeOptional(Optional.ofNullable(group.exit), AnimationSection::toNetwork);
    }

    public static Animation fromNetwork(FriendlyByteBuf byteBuf) {
        final boolean movable = byteBuf.readBoolean();
        final boolean abortable = byteBuf.readBoolean();
        final boolean rigModified = byteBuf.readBoolean();
        final byte unlock = byteBuf.readByte();
        final Type type = byteBuf.readEnum(Type.class);
        final Optional<AnimationSection> enter = byteBuf.readOptional(AnimationSection::fromNetwork);
        final Optional<AnimationSection> loop = byteBuf.readOptional(AnimationSection::fromNetwork);
        final Optional<AnimationSection> exit = byteBuf.readOptional(AnimationSection::fromNetwork);
        return new Animation(
                enter.orElse(null),
                loop.orElse(null),
                exit.orElse(null),
                movable, abortable, rigModified, unlock, type);
    }

    public boolean isModifiedRig() {
        return !useVanillaRig;
    }

    public boolean isVanillaRig() {
        return useVanillaRig;
    }

    public enum Type {
        SIMPLE("", ""),
        INVITE("invite/", "requester"),
        REQUESTER("requester/", "requester"),
        RECEIVER("receiver/", "receiver");

        public final String path;
        public final String prefix;

        Type(String path, String prefix) {
            this.path = path;
            this.prefix = prefix;
        }
    }
}
