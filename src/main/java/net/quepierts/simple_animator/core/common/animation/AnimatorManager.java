package net.quepierts.simple_animator.core.common.animation;

import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simple_animator.core.network.ModNetwork;
import net.quepierts.simple_animator.core.network.packet.AnimatorDataPacket;
import net.quepierts.simple_animator.core.network.packet.batch.ClientUpdateAnimatorPacket;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class AnimatorManager<T extends Animator> {
    protected final Map<UUID, T> animators;

    public AnimatorManager() {
        this.animators = new HashMap<>();
    }

    @Nullable
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
        /*for (T value : animators.values()) {
            if (value.getUuid().equals(player.getUUID()))
                continue;

            SimpleAnimator.LOGGER.info("Sync: {}", value.getUuid());
            ModNetwork.sendToPlayer(new AnimatorDataPacket(value, false), player);
        }*/

        ClientUpdateAnimatorPacket packet = new ClientUpdateAnimatorPacket(this.animators.keySet().stream()
                .filter(Predicate.not(player.getUUID()::equals))
                .map(this.animators::get)
                .map(AnimatorDataPacket::new)
                .toList());
        
        ModNetwork.sendToPlayer(packet, player);
    }
}
