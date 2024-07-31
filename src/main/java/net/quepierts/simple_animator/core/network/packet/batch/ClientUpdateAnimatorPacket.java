package net.quepierts.simple_animator.core.network.packet.batch;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.common.animation.AnimationState;
import net.quepierts.simple_animator.core.common.animation.Animator;
import net.quepierts.simple_animator.core.network.ISync;
import net.quepierts.simple_animator.core.network.packet.AnimatorDataPacket;

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
        }
    }

    @Override
    public void sync(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getClient().getClientAnimatorManager().handleUpdateAnimator(this);
    }

    public List<AnimatorDataPacket> getAnimators() {
        return animators;
    }
}
