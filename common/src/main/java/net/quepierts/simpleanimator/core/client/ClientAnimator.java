package net.quepierts.simpleanimator.core.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.animation.AnimationState;
import net.quepierts.simpleanimator.api.animation.IKBone;
import net.quepierts.simpleanimator.api.animation.ModelBone;
import net.quepierts.simpleanimator.api.animation.keyframe.VariableHolder;
import net.quepierts.simpleanimator.api.event.client.ClientAnimatorStateEvent;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.Animator;
import net.quepierts.simpleanimator.core.client.state.IAnimationState;
import net.quepierts.simpleanimator.core.network.packet.AnimatorDataPacket;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

@SuppressWarnings("unchecked")
@Environment(EnvType.CLIENT)
public class ClientAnimator extends Animator {
    private static final Set<String> BUILTIN_VARIABLES;
    private static final ModelPart ROOT = new ModelPart(Collections.EMPTY_LIST, Collections.EMPTY_MAP);
    //private Animation animation;
    private final EnumMap<ModelBone, Cache> cache;
    private final EnumMap<IKBone, IKCache> ikCache;
    private final Object2ObjectMap<String, VariableHolder> variables;
    private boolean processed = false;
    private boolean shouldUpdate = false;

    public ClientAnimator(UUID uuid) {
        super(uuid);

        this.cache = new EnumMap<>(ModelBone.class);
        this.ikCache = new EnumMap<>(IKBone.class);
        this.variables = new Object2ObjectOpenHashMap<>();
        for (String variable : BUILTIN_VARIABLES) {
            this.variables.put(variable, new VariableHolder(0.0f));
        }

        for (ModelBone value : ModelBone.values()) {
            //cache.put(value, new Cache(new Vector3f(), new Quaternionf()));
            cache.put(value, new Cache(new Vector3f(), new Vector3f(), new Vector3f()));
        }

        for (IKBone value : IKBone.values()) {
            ikCache.put(value, new IKCache(new Vector3f(), new Vector3f()));
        }
    }

    public void update(AnimatorDataPacket packet) {
        if (!isLocalPlayer())
            return;

        SimpleAnimator.getNetwork().update(packet);
    }

    @Override
    public void sync(AnimatorDataPacket packet) {
        ResourceLocation location = this.animationLocation;
        super.sync(packet);
        if (!location.equals(this.animationLocation) || this.animation != null) {
            Set<String> animationVariables = this.animation.getVariables();
            for (String variable : animationVariables) {
                this.variables.computeIfAbsent(variable, VariableHolder::get);
            }
        }
    }

    @Override
    public boolean play(ResourceLocation location) {
        @Nullable Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);

        if (this.animation == null) {
            processed = false;
        }

        if (!super.play(location)) {
            return false;
        }

        if (this.animation == null) {
            return false;
        }

        for (String variable : this.animation.getVariables()) {
            this.variables.computeIfAbsent(variable, VariableHolder::get);
        }

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
            this.timer += time * speed;
            this.shouldUpdate = true;

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

    public void process(PlayerModel<AbstractClientPlayer> model, Player player) {
        this.update(model, player);

        if (this.animation.isModifiedRig()) {
            Matrix4f pos = processModifiedBody(model.body);
            Vector3f rotation = pos.getEulerAnglesXYZ(new Vector3f());

            processModified(ModelBone.HEAD, model.head, pos, rotation);
            processModified(ModelBone.LEFT_ARM, model.leftArm, pos, rotation);
            processModified(ModelBone.RIGHT_ARM, model.rightArm, pos, rotation);
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

    private void update(PlayerModel<AbstractClientPlayer> model, Player player) {
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

            this.variables.forEach(
                    (variable, holder) -> animation.update(variable, holder, ClientAnimator.this)
            );

            resolveIK(player);
            shouldUpdate = false;
            processed = true;
        }
    }

    private void resolveIK(Player player) {
        if (!this.isRunning())
            return;

        Cache root = cache.get(ModelBone.ROOT);
        Cache body = cache.get(ModelBone.BODY);
        Matrix4f mat = new Matrix4f()
                .rotateXYZ(root.rotation())
                .translate(body.position().x / 16.0f, body.position().y / 16.0f + 0.75f, body.position().z / 16.0f)
                .rotateXYZ(body.rotation())
                .translate(0, 0.75f, 0);

        resolveIK(IKBone.HEAD, mat, player, 0);
        resolveIK(IKBone.LEFT_ARM, mat, player, 0.3125f);
        resolveIK(IKBone.RIGHT_ARM, mat, player, -0.3125f);
    }

    private void resolveIK(IKBone bone, Matrix4f mat, Player player, float left) {
        final IKCache cache = this.ikCache.get(bone);
        final Matrix4f local = new Matrix4f(mat)
                .translate(left, (float) 0, (float) 0);

        Vector3f w2l = positionW2L(ikCache.get(bone).target, player).mul(-1, 1, 1);
        Vector3f current = new Vector3f();
        local.getTranslation(current);

        if (this.getIKWeight(bone) <= 0)
            return;

        Vector3f forward = new Vector3f(0, -1, 0);
        Vector3f dir = new Vector3f(w2l).sub(current);

        local.transformDirection(forward);
        Quaternionf quaternionf = new Quaternionf()
                .rotateTo(forward, dir);

        if (bone == IKBone.HEAD) {
            quaternionf.rotateX(Mth.HALF_PI);
        }

        Vector3f vector3f = new Vector3f();
        quaternionf.getEulerAnglesXYZ(vector3f);

        cache.rotation().set(
                Mth.clamp(vector3f.x, -Mth.PI, Mth.PI),
                Mth.clamp(vector3f.y, -Mth.PI, Mth.PI),
                Mth.clamp(vector3f.z, -Mth.PI, Mth.PI)
        );
    }

    private Vector3f positionW2L(Vector3f world, Player player) {
        Matrix4f mat = new Matrix4f()
                .rotateY(player.yBodyRot * Mth.DEG_TO_RAD);
        Vector3f sub = new Vector3f(world).sub(player.position().toVector3f());
        return mat.transformPosition(sub);
    }

    public Cache getCache(ModelBone bone) {
        return this.cache.get(bone);
    }

    public VariableHolder getVariable(String variable) {
        return this.variables.getOrDefault(variable, VariableHolder.Immutable.INSTANCE);
    }

    public float getIKWeight(IKBone bone) {
        return this.getVariable(bone.varName).getAsFloat();
    }

    public void setIkTarget(IKBone bone, Vector3f worldPosition) {
        this.ikCache.get(bone).target().set(worldPosition);
    }

    public Vector3f getIkTarget(IKBone bone) {
        return new Vector3f(this.ikCache.get(bone).target());
    }

    public void resetIK() {
        for (IKCache value : this.ikCache.values()) {
            value.reset();
        }
    }

    private void process(ModelBone bone, ModelPart part) {
        Cache cache = this.cache.get(bone);
        Vector3f position = cache.position;
        PartPose pose = animation.isOverride(bone) ? part.getInitialPose() : part.storePose();

        part.x = pose.x + position.x;
        part.y = pose.y - position.y;
        part.z = pose.z + position.z;

        //Vector3f rotation = cache.rotation.getEulerAnglesXYZ(new Vector3f());
        Vector3f rotation = cache.rotation();

        if (bone.getIk() != null) {
            rotation.lerp(this.ikCache.get(bone.getIk()).rotation(), this.getIKWeight(bone.getIk()));
            //rotation.add(this.ikCache.get(bone.getIk()).rotation);
        }

        part.xRot = pose.xRot + rotation.x;
        part.yRot = pose.yRot + rotation.y;
        part.zRot = pose.zRot + rotation.z;
    }

    // change pivot point from (0 24 0) -> (0 12 0)
    // then rotate
    private Matrix4f processModifiedBody(ModelPart body) {
        Cache cache = this.cache.get(ModelBone.BODY);
        //Vector3f rotation = cache.rotation.getEulerAnglesXYZ(new Vector3f());
        Vector3f rotation = cache.rotation();

        PartPose pose = animation.isOverride(ModelBone.BODY) ? body.getInitialPose() : body.storePose();

        Quaternionf rot = new Quaternionf().rotateXYZ(
                pose.xRot + rotation.x,
                pose.yRot + rotation.y,
                pose.zRot + rotation.z
        );
        Vector3f position = new Vector3f(pose.x, pose.y, pose.z)
                .sub(0, 12, 0)
                .rotate(rot)
                .add(0, 12, 0)
                .add(cache.position.x, -cache.position.y, cache.position.z);

        body.x = position.x;
        body.y = position.y;
        body.z = position.z;

        Vector3f anglesXYZ = rot.getEulerAnglesXYZ(new Vector3f());
        body.xRot = anglesXYZ.x;
        body.yRot = anglesXYZ.y;
        body.zRot = anglesXYZ.z;

        return new Matrix4f()
                .translate(body.x, body.y, body.z)
                .rotateXYZ(body.xRot, body.yRot, body.zRot);
    }

    private void processModified(ModelBone bone, ModelPart part, Matrix4f parentMat, Vector3f parentRot) {
        Cache cache = this.cache.get(bone);
        Vector3f position;
        PartPose pose = animation.isOverride(bone) ? part.getInitialPose() : part.storePose();

        position = parentMat.transformPosition(new Vector3f(pose.x, pose.y, pose.z).add(cache.position.x, -cache.position.y, cache.position.z));

        part.x = position.x;
        part.y = position.y;
        part.z = position.z;

        Vector3f rotation = new Vector3f(cache.rotation()).add(pose.xRot, pose.yRot, pose.zRot).add(parentRot);
        //Vector3f rotation = new Vector3f(cache.rotation.getEulerAnglesXYZ(new Vector3f())).add(pose.xRot, pose.yRot, pose.zRot).add(parentRot);

        if (bone.getIk() != null) {
            rotation.lerp(this.ikCache.get(bone.getIk()).rotation(), this.getIKWeight(bone.getIk()));
        }

        part.xRot = rotation.x;
        part.yRot = rotation.y;
        part.zRot = rotation.z;
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
            //cache.rotation.set(0, 0, 0, 1);
        });

        final Map<String, VariableHolder> temp = new Object2ObjectOpenHashMap<>(this.variables.size());
        for (String variable : BUILTIN_VARIABLES) {
            temp.put(variable, this.variables.get(variable));
        }
        this.variables.clear();
        this.variables.putAll(temp);
        this.resetIK();

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
                //.rotate(root.rotation())
                .translate(0, 12, 0);

        if (this.isRunning() && this.animation.isModifiedRig()) {
            Cache body = cache.get(ModelBone.BODY);
            mat.translate(body.position())
                    .rotateXYZ(body.rotation())
                    //.rotate(body.rotation())
            ;
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
            //return cache.get(ModelBone.HEAD).rotation().mul(cache.get(ModelBone.BODY).rotation()).mul(cache.get(ModelBone.ROOT).rotation()).getEulerAnglesXYZ(new Vector3f());
            return new Vector3f(cache.get(ModelBone.HEAD).rotation()).add(cache.get(ModelBone.BODY).rotation()).add(cache.get(ModelBone.ROOT).rotation());
        }

        //return cache.get(ModelBone.HEAD).rotation().mul(cache.get(ModelBone.ROOT).rotation()).getEulerAnglesXYZ(new Vector3f());
        return new Vector3f(cache.get(ModelBone.HEAD).rotation()).add(cache.get(ModelBone.ROOT).rotation());
    }

    public record Cache(Vector3f position, Vector3f rotation, Vector3f worldPosition) {}

    public record IKCache(Vector3f target, Vector3f rotation) {
        public void reset() {
            this.target.set(0);
            this.rotation.set(0);
        }
    }

    static {
        BUILTIN_VARIABLES = ObjectOpenHashSet.of(
                IKBone.HEAD.varName,
                IKBone.LEFT_ARM.varName,
                IKBone.RIGHT_ARM.varName
                //,
                //IKBone.LEFT_LEG.varName,
                //IKBone.RIGHT_LEG.varName
        );
    }
}
