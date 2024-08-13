package net.quepierts.simpleanimator.core.network.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.api.animation.AnimationState;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.Animator;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AnimatorDataPacket extends UserPacket {
    public static final Type<AnimatorDataPacket> TYPE = NetworkPackets.createType(AnimatorDataPacket.class);
    public final ResourceLocation animationLocation;
    public final AnimationState curState;
    public final AnimationState nextState;
    public final Animator.ProcessState procState;
    public final float timer;
    public final float speed;

    public final boolean publish;

    public AnimatorDataPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
        this.animationLocation = byteBuf.readResourceLocation();
        this.curState = byteBuf.readEnum(AnimationState.class);
        this.nextState = byteBuf.readEnum(AnimationState.class);
        this.procState = byteBuf.readEnum(Animator.ProcessState.class);
        this.timer = byteBuf.readFloat();
        this.speed = byteBuf.readFloat();
        this.publish = byteBuf.readBoolean();
    }

    public AnimatorDataPacket(UUID uuid, ResourceLocation animation, AnimationState curState, AnimationState nextState, Animator.ProcessState proState, float timer, float speed, boolean publish) {
        super(uuid);
        this.animationLocation = animation;
        this.curState = curState;
        this.nextState = nextState;
        this.procState = proState;
        this.timer = timer;
        this.speed = speed;
        this.publish = publish;
    }

    public AnimatorDataPacket(Animator animator) {
        this(animator, false);
    }

    public AnimatorDataPacket(Animator animator, boolean publish) {
        super(animator.getUuid());
        this.animationLocation = animator.getAnimationLocation();
        this.curState = animator.getCurState();
        this.nextState = animator.getNextState();
        this.procState = animator.getProcState();
        this.timer = animator.getTimer();
        this.speed = animator.getSpeed();
        this.publish = publish;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.owner);
        buffer.writeResourceLocation(this.animationLocation);
        buffer.writeEnum(this.curState);
        buffer.writeEnum(this.nextState);
        buffer.writeEnum(this.procState);
        buffer.writeFloat(this.timer);
        buffer.writeFloat(this.speed);
        buffer.writeBoolean(this.publish);
    }

    @Override
    public void update(@NotNull ServerPlayer sender) {
        SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(this.owner).sync(this);
        if (publish)
            SimpleAnimator.getNetwork().sendToPlayers(this, sender);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void sync() {
        SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(owner).sync(this);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
