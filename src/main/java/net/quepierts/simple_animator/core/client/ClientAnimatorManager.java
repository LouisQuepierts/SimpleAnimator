package net.quepierts.simple_animator.core.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.quepierts.simple_animator.core.common.animation.AnimatorManager;
import net.quepierts.simple_animator.core.network.packet.AnimatorDataPacket;
import net.quepierts.simple_animator.core.network.packet.batch.ClientUpdateAnimatorPacket;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class ClientAnimatorManager extends AnimatorManager<ClientAnimator> {
    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public ClientAnimator getLocalAnimator() {
        return get(Minecraft.getInstance().player.getUUID());
    }

    public ClientAnimator get(UUID player) {
        return animators.computeIfAbsent(player, ClientAnimator::new);
    }

    public void tick(float renderTickTime) {
        float time = renderTickTime / Minecraft.getInstance().getFps();
        for (ClientAnimator animator : animators.values()) {
            animator.tick(time);
        }
    }

    public void handleUpdateAnimator(ClientUpdateAnimatorPacket packet) {
        this.clear();

        List<AnimatorDataPacket> list = packet.getAnimators();
        for (AnimatorDataPacket data : list) {
            this.get(data.getOwner()).sync(data);
        }

        this.getLocalAnimator();
    }
}
