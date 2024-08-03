package net.quepierts.simpleanimator.core.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.quepierts.simpleanimator.core.animation.AnimatorManager;
import net.quepierts.simpleanimator.core.network.packet.AnimatorDataPacket;
import net.quepierts.simpleanimator.core.network.packet.batch.ClientUpdateAnimatorPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ClientAnimatorManager extends AnimatorManager<ClientAnimator> {
    @Environment(EnvType.CLIENT)
    @NotNull
    public ClientAnimator getLocalAnimator() {
        return get(Minecraft.getInstance().player.getUUID());
    }

    public ClientAnimator get(UUID player) {
        return animators.computeIfAbsent(player, ClientAnimator::new);
    }

    public void tick(float delta) {
        for (ClientAnimator animator : animators.values()) {
            animator.tick(delta);
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
