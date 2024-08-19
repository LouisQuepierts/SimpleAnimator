package net.quepierts.simpleanimator.core.network.packet.batch;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.api.animation.Animation;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.ISync;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ClientUpdateAnimationPacket implements ISync {
    public static final Type<ClientUpdateAnimationPacket> TYPE = NetworkPackets.createType(ClientUpdateAnimationPacket.class);
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
    public void sync() {
        SimpleAnimator.getClient().getAnimationManager().handleUpdateAnimations(this);
    }

    public Map<ResourceLocation, Animation> getAnimations() {
        return animations;
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
