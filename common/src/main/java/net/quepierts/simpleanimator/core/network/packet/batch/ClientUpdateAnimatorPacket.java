package net.quepierts.simpleanimator.core.network.packet.batch;

import net.minecraft.network.FriendlyByteBuf;
import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.AnimationState;
import net.quepierts.simpleanimator.core.network.ISync;
import net.quepierts.simpleanimator.core.network.packet.AnimatorDataPacket;

import java.util.ArrayList;
import java.util.List;

public class ClientUpdateAnimatorPacket implements ISync {
    private final List<AnimatorDataPacket> animators;

    public ClientUpdateAnimatorPacket(List<AnimatorDataPacket> animators) {
        this.animators = animators;
    }

    public ClientUpdateAnimatorPacket(FriendlyByteBuf byteBuf) {
        int capacity = byteBuf.readInt();
        this.animators = new ArrayList<>(capacity);

        for (int i = 0; i < capacity; i++) {
            this.animators.add(new AnimatorDataPacket(
                    byteBuf.readUUID(),
                    byteBuf.readResourceLocation(),
                    byteBuf.readEnum(AnimationState.class),
                    byteBuf.readEnum(AnimationState.class),
                    byteBuf.readEnum(Animator.ProcessState.class),
                    byteBuf.readFloat(),
                    byteBuf.readFloat(),
                    false
            ));
        }
    }

    @Override
    public void write(FriendlyByteBuf byteBuf) {
        byteBuf.writeInt(this.animators.size());
        for (AnimatorDataPacket packet : this.animators) {
            byteBuf.writeUUID(packet.getOwner());
            byteBuf.writeResourceLocation(packet.animationLocation);
            byteBuf.writeEnum(packet.curState);
            byteBuf.writeEnum(packet.nextState);
            byteBuf.writeEnum(packet.procState);
            byteBuf.writeFloat(packet.timer);
            byteBuf.writeFloat(packet.speed);
        }
    }

    @Override
    public void sync() {
        SimpleAnimator.getClient().getClientAnimatorManager().handleUpdateAnimator(this);
    }

    public List<AnimatorDataPacket> getAnimators() {
        return animators;
    }
}
