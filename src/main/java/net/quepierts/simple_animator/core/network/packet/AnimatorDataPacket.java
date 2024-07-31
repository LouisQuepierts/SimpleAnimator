package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.common.animation.Animator;
import net.quepierts.simple_animator.core.common.animation.AnimationState;
import net.quepierts.simple_animator.core.network.ModNetwork;

import java.util.UUID;

public class AnimatorDataPacket extends UserPacket {
    public final ResourceLocation animationLocation;
    public final AnimationState curState;
    public final AnimationState nextState;
    public final Animator.ProcessState procState;
    public final float timer;

    public final boolean publish;

    public AnimatorDataPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
        this.animationLocation = byteBuf.readResourceLocation();
        this.curState = byteBuf.readEnum(AnimationState.class);
        this.nextState = byteBuf.readEnum(AnimationState.class);
        this.procState = byteBuf.readEnum(Animator.ProcessState.class);
        this.timer = byteBuf.readFloat();
        this.publish = byteBuf.readBoolean();
    }

    public AnimatorDataPacket(UUID uuid, ResourceLocation animation, AnimationState curState, AnimationState nextState, Animator.ProcessState proState, float timer, boolean publish) {
        super(uuid);
        this.animationLocation = animation;
        this.curState = curState;
        this.nextState = nextState;
        this.procState = proState;
        this.timer = timer;
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
        buffer.writeBoolean(this.publish);
    }

    @Override
    public void update(NetworkEvent.Context context, ServerPlayer sender) {
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(this.owner).sync(this);
        if (publish)
            ModNetwork.sendToPlayers(this, context.getSender());
    }

    @Override
    public void sync(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(owner).sync(this);
    }

    @Override
    public String toString() {
        return "AnimatorPacket{" +
                "uuid=" + owner +
                ", animationLocation=" + animationLocation +
                ", curState=" + curState +
                ", nextState=" + nextState +
                ", procState=" + procState +
                ", timer=" + timer +
                ", publish=" + publish +
                '}';
    }
}
