package net.quepierts.simple_animator.common;

import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simple_animator.SimpleAnimator;
import net.quepierts.simple_animator.animation.Animator;
import net.quepierts.simple_animator.network.ModNetwork;
import net.quepierts.simple_animator.network.packet.AnimatorPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnimatorManager<T extends Animator> {
    protected final Map<UUID, T> animators;

    public AnimatorManager() {
        this.animators = new HashMap<>();
    }

    public T getAnimator(UUID uuid) {
        return animators.get(uuid);
    }

    public T get(UUID uuid) {
        return animators.computeIfAbsent(uuid, (uid) -> (T) new Animator(uid));
    }

    public void clear() {
        animators.clear();
    }

    public  boolean exist(UUID player) {
        return animators.containsKey(player);
    }

    public void remove(UUID uuid) {
        animators.remove(uuid);
    }

    public void tick(float renderTickTime) {}

    public void sync(ServerPlayer player) {
        for (T value : animators.values()) {
            if (value.getUuid().equals(player.getUUID()))
                continue;

            SimpleAnimator.LOGGER.info("Sync: {}", value.getUuid());
            ModNetwork.sendToPlayer(new AnimatorPacket(value, false), player);
        }
    }
}
