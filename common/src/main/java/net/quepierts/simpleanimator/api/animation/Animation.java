package net.quepierts.simpleanimator.api.animation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.FriendlyByteBuf;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.animation.AnimationState;
import net.quepierts.simpleanimator.core.animation.ModelBone;
import net.quepierts.simpleanimator.core.client.ClientAnimator;

import java.io.Reader;
import java.util.Optional;

public class Animation {
    private static final String KEY_REQUEST = "request";
    private static final String KEY_WAITING = "waiting";
    private static final String KEY_ENTER = "enter";
    private static final String KEY_LOOP = "main";
    private static final String KEY_EXIT = "exit";

    private final AnimationSection enter;
    private final AnimationSection loop;
    private final AnimationSection exit;

    private final boolean override;
    private final boolean movable;
    private final boolean abortable;

    private final Type type;

    public static Animation[] fromStream(Reader reader) {
        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
        return serialize(object);
    }

    public static Animation[] serialize(JsonObject json) {
        JsonObject object = json.getAsJsonObject("animations");

        if (!object.has(KEY_LOOP))
            throw new RuntimeException("Cannot accept animation without \"main\"!");

        boolean override = getBoolean(json, "override", true);
        boolean movable = getBoolean(json, "movable", false);
        boolean abortable = getBoolean(json, "abortable", true);

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

            animations[0] = new Animation(
                    request, waiting, null,
                    override, movable, true, Type.INVITE
            );
            animations[1] = new Animation(
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_ENTER), Type.REQUESTER),
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_LOOP), Type.REQUESTER),
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_EXIT), Type.REQUESTER),
                    override, movable, false, Type.REQUESTER
            );
            animations[2] = new Animation(
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_ENTER), Type.RECEIVER),
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_LOOP), Type.RECEIVER),
                    AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_EXIT), Type.RECEIVER),
                    override, movable, false, Type.RECEIVER

            );
        } else {
            animations = new Animation[] {
                    new Animation(
                            AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_ENTER)),
                            AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_LOOP)),
                            AnimationSection.fromJsonObject(object.getAsJsonObject(KEY_EXIT)),
                            override, movable, abortable, Type.SIMPLE
                    )
            };
        }

        return animations;
    }

    private Animation(
            AnimationSection enter,
            AnimationSection loop,
            AnimationSection exit,
            boolean override,
            boolean movable,
            boolean abortable,
            Type type) {
        this.enter = enter;
        this.loop = loop;
        this.exit = exit;
        this.override = override;
        this.movable = movable;
        this.abortable = abortable;
        this.type = type;
    }

    private static boolean isInteractiveAnimation(JsonObject json) {
        return json.has("request");
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
        AnimationState nextState = animator.getNextState();
        AnimationSection animation = get(animator.getCurState());

        if (nextState == AnimationState.IDLE) {
            return animation.getFadeOut();
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

    public boolean isOverride() {
        return override;
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

    public static void toNetwork(FriendlyByteBuf byteBuf, Animation group) {
        byteBuf.writeBoolean(group.override);
        byteBuf.writeBoolean(group.movable);
        byteBuf.writeBoolean(group.abortable);
        byteBuf.writeEnum(group.type);
        byteBuf.writeOptional(Optional.ofNullable(group.enter), AnimationSection::toNetwork);
        byteBuf.writeOptional(Optional.ofNullable(group.loop), AnimationSection::toNetwork);
        byteBuf.writeOptional(Optional.ofNullable(group.exit), AnimationSection::toNetwork);
    }

    public static Animation fromNetwork(FriendlyByteBuf byteBuf) {
        final boolean override = byteBuf.readBoolean();
        final boolean movable = byteBuf.readBoolean();
        final boolean abortable = byteBuf.readBoolean();
        final Type type = byteBuf.readEnum(Type.class);
        final Optional<AnimationSection> enter = byteBuf.readOptional(AnimationSection::fromNetwork);
        final Optional<AnimationSection> loop = byteBuf.readOptional(AnimationSection::fromNetwork);
        final Optional<AnimationSection> exit = byteBuf.readOptional(AnimationSection::fromNetwork);
        return new Animation(
                enter.orElse(null),
                loop.orElse(null),
                exit.orElse(null),
                override, movable, abortable, type);
    }

    public enum Type {
        SIMPLE("simple/", ""),
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
