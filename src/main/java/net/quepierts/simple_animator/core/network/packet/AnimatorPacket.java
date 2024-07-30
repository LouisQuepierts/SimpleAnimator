package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.animation.Animator;
import net.quepierts.simple_animator.core.animation.AnimationState;
import net.quepierts.simple_animator.core.network.BiPacket;
import net.quepierts.simple_animator.core.network.ModNetwork;

import java.util.UUID;

public class AnimatorPacket extends BiPacket {
    private final UUID uuid;
    public final ResourceLocation animationLocation;
    public final AnimationState curState;
    public final AnimationState nextState;
    public final Animator.ProcessState procState;
    public final float timer;

    public final boolean publish;

    public AnimatorPacket(FriendlyByteBuf byteBuf) {
        this.uuid = byteBuf.readUUID();
        this.animationLocation = byteBuf.readResourceLocation();
        this.curState = byteBuf.readEnum(AnimationState.class);
        this.nextState = byteBuf.readEnum(AnimationState.class);
        this.procState = byteBuf.readEnum(Animator.ProcessState.class);
        this.timer = byteBuf.readFloat();
        this.publish = byteBuf.readBoolean();
    }

    public AnimatorPacket(UUID uuid, ResourceLocation animation, AnimationState curState, AnimationState nextState, Animator.ProcessState proState, float timer, boolean publish) {
        this.uuid = uuid;
        this.animationLocation = animation;
        this.curState = curState;
        this.nextState = nextState;
        this.procState = proState;
        this.timer = timer;
        this.publish = publish;
    }

    public AnimatorPacket(Animator animator, boolean publish) {
        this.uuid = animator.getUuid();
        this.animationLocation = animator.getAnimationLocation();
        this.curState = animator.getCurState();
        this.nextState = animator.getNextState();
        this.procState = animator.getProcState();
        this.timer = animator.getTimer();
        this.publish = publish;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.uuid);
        buffer.writeResourceLocation(this.animationLocation);
        buffer.writeEnum(this.curState);
        buffer.writeEnum(this.nextState);
        buffer.writeEnum(this.procState);
        buffer.writeFloat(this.timer);
        buffer.writeBoolean(this.publish);
    }

    @Override
    public void update(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(this.uuid).sync(this);
        if (publish)
            ModNetwork.sendToPlayers(this, context.getSender());
    }

    @Override
    public void sync(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(uuid).sync(this);
    }

    @Override
    public String toString() {
        return "AnimatorPacket{" +
                "uuid=" + uuid +
                ", animationLocation=" + animationLocation +
                ", curState=" + curState +
                ", nextState=" + nextState +
                ", procState=" + procState +
                ", timer=" + timer +
                ", publish=" + publish +
                '}';
    }
}
