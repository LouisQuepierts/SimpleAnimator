package net.quepierts.simpleanimator.core.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.AnimationState;
import net.quepierts.simpleanimator.core.animation.ModelBone;
import net.quepierts.simpleanimator.core.client.state.IAnimationState;
import net.quepierts.simpleanimator.core.network.packet.AnimatorDataPacket;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.EnumMap;
import java.util.UUID;

@SuppressWarnings("unchecked")
@Environment(EnvType.CLIENT)
public class ClientAnimator extends Animator {
    private static final ModelPart ROOT = new ModelPart(Collections.EMPTY_LIST, Collections.EMPTY_MAP);
    //private Animation animation;
    private final EnumMap<ModelBone, Cache> cache;
    private boolean processed = false;
    private boolean shouldUpdate = false;

    public ClientAnimator(UUID uuid) {
        super(uuid);

        this.cache = new EnumMap<>(ModelBone.class);
        for (ModelBone value : ModelBone.values()) {
            cache.put(value, new Cache(new Vector3f(), new Vector3f()));
        }
    }

    public void update(AnimatorDataPacket packet) {
        if (!isLocalPlayer())
            return;

        SimpleAnimator.getNetwork().update(packet);
    }

    @Override
    public boolean play(ResourceLocation location) {
        if (this.animation == null)
            processed = false;

        if (!super.play(location))
            return false;

        if (this.animation == null)
            return false;

        this.nextState = this.animation.hasEnterAnimation() ? AnimationState.ENTER : AnimationState.LOOP;
        this.procState = ProcessState.TRANSFER;
        this.shouldUpdate = true;
        return true;
    }

    public boolean stop() {
        if (this.animation == null || this.canStop() && !super.stop())
            return false;
        this.timer = 0;
        this.nextState = this.animation.hasExitAnimation() ? AnimationState.EXIT : AnimationState.IDLE;
        this.procState = ProcessState.TRANSFER;
        this.processed = true;
        return true;
    }

    public void tick(float time) {
        if (this.animation != null) {
            this.shouldUpdate = true;
            this.timer += time * speed;

            switch (this.procState) {
                case TRANSFER:
                    if (this.timer > this.animation.getFadeIn(this)) {
                        this.timer = 0;
                        this.curState = this.nextState;
                        IAnimationState.Impl.get(this.curState).enter(this);
                        this.procState = ProcessState.PROCESS;
                    }
                    break;
                case PROCESS:
                    if (this.timer > this.animation.get(this.curState).getLength()) {
                        this.timer = 0;

                        IAnimationState impl = IAnimationState.Impl.get(this.curState);
                        if (impl.shouldEnd(this)) {
                            this.nextState = impl.getNext(this);
                            impl.exit(this);
                            this.procState = ProcessState.TRANSFER;
                            this.shouldUpdate = false;
                            this.update(new AnimatorDataPacket(this, false));
                        }
                    }
                    break;
            }
        }
    }

    public void update(PlayerModel<AbstractClientPlayer> model, Player player) {
        if (this.animation == null)
            return;

        if (shouldUpdate) {
            if (!PlayerUtils.isRiding(player)) {
                animation.update(ModelBone.ROOT, ROOT, this);
                animation.update(ModelBone.LEFT_LEG, model.leftLeg, this);
                animation.update(ModelBone.RIGHT_LEG, model.rightLeg, this);
            }

            animation.update(ModelBone.BODY, model.body, this);
            animation.update(ModelBone.HEAD, model.head, this);
            animation.update(ModelBone.LEFT_ARM, model.leftArm, this);
            animation.update(ModelBone.RIGHT_ARM, model.rightArm, this);
            shouldUpdate = false;
            processed = true;
        }
    }

    public void process(PlayerModel<AbstractClientPlayer> model, Player player) {
        this.update(model, player);
        process(ModelBone.HEAD, model.head);
        process(ModelBone.BODY, model.body);
        process(ModelBone.LEFT_ARM, model.leftArm);
        process(ModelBone.RIGHT_ARM, model.rightArm);

        if (!PlayerUtils.isRiding(player)) {
            process(ModelBone.LEFT_LEG, model.leftLeg);
            process(ModelBone.RIGHT_LEG, model.rightLeg);
        }
    }

    public Cache getCache(ModelBone bone) {
        return this.cache.get(bone);
    }

    private void process(ModelBone bone, ModelPart part) {
        Cache cache = this.cache.get(bone);
        Vector3f position = cache.position;
        PartPose pose = animation.isOverride() ? part.getInitialPose() : part.storePose();

        part.x = pose.x + position.x;
        part.y = pose.y - position.y;
        part.z = pose.z + position.z;

        Vector3f rotation = cache.rotation;

        part.xRot = pose.xRot + rotation.x;
        part.yRot = pose.yRot + rotation.y;
        part.zRot = pose.zRot + rotation.z;
    }

    public boolean canStop() {
        return curState == AnimationState.LOOP && nextState == AnimationState.LOOP;
    }

    public boolean isTransferring() {
        return procState == ProcessState.TRANSFER;
    }


    // prevent problem when first tick process camera rotation
    public boolean isProcessed() {
        return processed;
    }

    public boolean isLocalPlayer() {
        return this.uuid.equals(Minecraft.getInstance().player.getUUID());
    }

    public void reset(boolean update) {
        super.reset(update);
        this.processed = false;

        this.cache.forEach((bone, cache) -> {
            cache.position.set(0);
            cache.rotation.set(0);
        });

        if (update) {
            this.update(new AnimatorDataPacket(this, false));
        }
    }

    public Vector3f getCameraPosition(Entity entity) {
        Cache head = cache.get(ModelBone.HEAD);
        Cache root = cache.get(ModelBone.ROOT);

        Vector3f position;
        if (root.rotation().x == 0 && root.rotation().y == 0 && root.rotation().z == 0) {
            position = new Vector3f(head.position());
        } else {
            float eyeHeight = entity.getEyeHeight() * 16.0f;
            position = new Matrix4f()
                    .rotateXYZ(root.rotation()).invert()
                    .transformPosition(new Vector3f(head.position()).add(0, eyeHeight, 0))
                    .sub(0, eyeHeight, 0);
        }

        position.add(root.position())
                .div(-16.0f, 16.0f, -16.0f);

        return position;
    }

    public Vector3f getCameraRotation() {
        return new Vector3f(cache.get(ModelBone.HEAD).rotation()).add(cache.get(ModelBone.ROOT).rotation());
    }

    public record Cache(Vector3f position, Vector3f rotation) {}

}
