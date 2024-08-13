package net.quepierts.simpleanimator.core.animation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.packet.AnimatorDataPacket;
import net.quepierts.simpleanimator.core.network.packet.batch.ClientUpdateAnimatorPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class AnimatorManager<T extends Animator> {
    protected final Map<UUID, T> animators;

    public AnimatorManager() {
        this.animators = new Object2ObjectOpenHashMap<>();
    }

    @Nullable
    public T getAnimator(UUID uuid) {
        return animators.get(uuid);
    }

    @SuppressWarnings("unchecked")
    public T createIfAbsent(UUID uuid) {
        return animators.computeIfAbsent(uuid, (uid) -> (T) new Animator(uid));
    }

    public void clear() {
        animators.clear();
    }

    public void reset() {
        for (T value : animators.values()) {
            value.reset(false);
        }
    }

    public  boolean exist(UUID player) {
        return animators.containsKey(player);
    }

    public void remove(UUID uuid) {
        animators.remove(uuid);
    }

    public void tick(float delta) {}

    public void sync(ServerPlayer player) {
        /*for (T value : animators.values()) {
            if (value.getUuid().equals(sender.getUUID()))
                continue;

            SimpleAnimator.LOGGER.info("Sync: {}", value.getUuid());
            SimpleAnimator.getNetwork().sendToPlayer(new AnimatorDataPacket(value, false), sender);
        }*/

        ClientUpdateAnimatorPacket packet = new ClientUpdateAnimatorPacket(this.animators.keySet().stream()
                .filter(Predicate.not(player.getUUID()::equals))
                .map(this.animators::get)
                .map(AnimatorDataPacket::new)
                .toList());
        
        SimpleAnimator.getNetwork().sendToPlayer(packet, player);
    }
}
