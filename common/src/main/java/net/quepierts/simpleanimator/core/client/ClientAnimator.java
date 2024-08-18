package net.quepierts.simpleanimator.core.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.animation.AnimationState;
import net.quepierts.simpleanimator.api.animation.ModelBone;
import net.quepierts.simpleanimator.api.event.client.ClientAnimatorStateEvent;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.Animator;
import net.quepierts.simpleanimator.core.client.state.IAnimationState;
import net.quepierts.simpleanimator.core.network.packet.AnimatorDataPacket;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
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
                        SimpleAnimator.EVENT_BUS.post(new ClientAnimatorStateEvent.Enter(this.uuid, this.animationLocation, this.animation, this.curState, this.nextState));
                        this.update(new AnimatorDataPacket(this, false));
                    }
                    break;
                case PROCESS:
                    if (this.timer > this.animation.get(this.curState).getLength()) {
                        this.timer = 0;

                        IAnimationState impl = IAnimationState.Impl.get(this.curState);
                        if (impl.shouldEnd(this)) {
                            this.nextState = impl.getNext(this);
                            impl.exit(this);
                            SimpleAnimator.EVENT_BUS.post(new ClientAnimatorStateEvent.Exit(this.uuid, this.animationLocation, this.animation, this.curState, this.nextState));
                            this.procState = ProcessState.TRANSFER;
                            this.shouldUpdate = false;
                        } else {
                            SimpleAnimator.EVENT_BUS.post(new ClientAnimatorStateEvent.Loop(this.uuid, this.animationLocation, this.animation, this.curState, this.nextState));
                        }

                        this.update(new AnimatorDataPacket(this, false));
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

        if (this.animation.isModifiedRig()) {
            Matrix4f parent = processModifiedBody(model.body);

            processModified(ModelBone.HEAD, model.head, parent);
            processModified(ModelBone.LEFT_ARM, model.leftArm, parent);
            processModified(ModelBone.RIGHT_ARM, model.rightArm, parent);
        } else {
            process(ModelBone.HEAD, model.head);
            process(ModelBone.BODY, model.body);
            process(ModelBone.LEFT_ARM, model.leftArm);
            process(ModelBone.RIGHT_ARM, model.rightArm);
        }

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
        PartPose pose = animation.isOverride(bone) ? part.getInitialPose() : part.storePose();

        part.x = pose.x + position.x;
        part.y = pose.y - position.y;
        part.z = pose.z + position.z;

        Vector3f rotation = cache.rotation;

        part.xRot = pose.xRot + rotation.x;
        part.yRot = pose.yRot + rotation.y;
        part.zRot = pose.zRot + rotation.z;
    }

    private Matrix4f processModifiedBody(ModelPart body) {
        Cache cache = this.cache.get(ModelBone.BODY);
        Vector3f rotation = cache.rotation;

        PartPose pose = animation.isOverride(ModelBone.BODY) ? body.getInitialPose() : body.storePose();

        Quaternionf rot = new Quaternionf().rotateXYZ(rotation.x, rotation.y, rotation.z);
        Vector3f position = rot.transform(new Vector3f(cache.position).sub(0, 12, 0)).add(0, 12, 0);

        body.x = pose.x + position.x;
        body.y = pose.y - position.y;
        body.z = pose.z + position.z;

        body.xRot = pose.xRot + rotation.x;
        body.yRot = pose.yRot + rotation.y;
        body.zRot = pose.zRot + rotation.z;

        return new Matrix4f()
                .translate(body.x, body.y, body.z)
                .rotateXYZ(body.xRot, body.yRot, body.zRot);
    }

    private void processModified(ModelBone bone, ModelPart part, Matrix4f parent) {
        Cache cache = this.cache.get(bone);
        Vector3f position;
        PartPose pose = animation.isOverride(bone) ? part.getInitialPose() : part.storePose();

        position = parent.transformPosition(new Vector3f(pose.x, pose.y, pose.z).add(cache.position.x, -cache.position.y, cache.position.z));

        part.x = position.x;
        part.y = position.y;
        part.z = position.z;

        Vector3f rotation = parent.transformDirection(cache.rotation, new Vector3f());

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

    public Vector3f getCameraPosition() {
        Cache head = cache.get(ModelBone.HEAD);
        Cache root = cache.get(ModelBone.ROOT);

        Matrix4f mat = new Matrix4f()
                .translate(root.position())
                .rotateXYZ(root.rotation())
                .translate(0, 12, 0);

        if (this.isRunning() && this.animation.isModifiedRig()) {
            Cache body = cache.get(ModelBone.BODY);
            mat.translate(body.position())
                    .rotateXYZ(body.rotation());
        }

        return mat
                .translate(0, 12, 0)
                .translate(head.position())
                .invert()
                .transformPosition(new Vector3f(0, 0, 0))
                .add(0, 24, 0)
                .div(16.0f, -16.0f, 16.0f);
    }

    public Vector3f getCameraRotation() {
        if (this.isRunning() && this.animation.isModifiedRig()){
            return new Vector3f(cache.get(ModelBone.HEAD).rotation()).add(cache.get(ModelBone.BODY).rotation()).add(cache.get(ModelBone.ROOT).rotation());
        }

        return new Vector3f(cache.get(ModelBone.HEAD).rotation()).add(cache.get(ModelBone.ROOT).rotation());
    }

    public record Cache(Vector3f position, Vector3f rotation) {}

}
