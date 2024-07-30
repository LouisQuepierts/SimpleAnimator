package net.quepierts.simple_animator.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.quepierts.simple_animator.SimpleAnimator;
import net.quepierts.simple_animator.common.AnimatorManager;

import javax.annotation.Nonnull;
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
        for (ClientAnimator animator : animators.values()) {
            animator.tick(renderTickTime / Minecraft.getInstance().getFps());
        }
    }
}
