package net.quepierts.simple_animator.core.network.packet.data;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.animation.Animation;
import net.quepierts.simple_animator.core.network.ISync;

import java.util.Map;

public class ClientUpdateAnimationPacket implements ISync {
    private final Map<ResourceLocation, Animation> animations;
    public ClientUpdateAnimationPacket(Map<ResourceLocation, Animation> map) {
        this.animations = ImmutableMap.copyOf(map);
    }

    public ClientUpdateAnimationPacket(FriendlyByteBuf byteBuf) {
        this.animations = byteBuf.readMap(FriendlyByteBuf::readResourceLocation, Animation::fromNetwork);
    }

    @Override
    public void write(FriendlyByteBuf byteBuf) {
        byteBuf.writeMap(animations, FriendlyByteBuf::writeResourceLocation, Animation::toNetwork);
        SimpleAnimator.LOGGER.info("Buffer Capacity: {} / {}", byteBuf.writerIndex(), byteBuf.capacity());
    }

    @Override
    public void sync(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getProxy().getAnimationManager().handleUpdateAnimations(this);
    }

    public Map<ResourceLocation, Animation> getAnimations() {
        return animations;
    }
}
