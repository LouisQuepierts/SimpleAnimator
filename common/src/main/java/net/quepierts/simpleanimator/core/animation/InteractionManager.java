package net.quepierts.simpleanimator.core.animation;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.api.animation.Interaction;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.proxy.CommonProxy;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InteractionManager {
    protected final Map<UUID, RequestHolder> requests;

    public InteractionManager() {
        requests = new HashMap<>();
    }

    @Deprecated
    public boolean invite(Player requester, Player receiver, ResourceLocation location) {
        if (requester == receiver)
            return false;

        CommonProxy proxy = SimpleAnimator.getProxy();
        Animator animator = proxy.getAnimatorManager().createIfAbsent(requester.getUUID());

        if (animator.getAnimation() != null && !animator.getAnimation().isAbortable())
            return false;

        Interaction interaction = proxy.getAnimationManager().getInteraction(location);

        if (interaction == null)
            return false;

        SimpleAnimator.LOGGER.info(interaction.toString());

        this.createIfAbsent(receiver.getUUID()).set(receiver.getUUID(), location);
        animator.play(interaction.invite());
        return true;
    }

    @Deprecated
    public boolean accept(Player requester, Player receiver) {
        RequestHolder request = this.requests.get(requester.getUUID());

        if (request.hasRequest() || !request.getTarget().equals(receiver.getUUID()))
            return false;

        Vec3 position = PlayerUtils.getRelativePosition(requester, 1, 0);
        if (!PlayerUtils.inSameDimension(receiver, receiver) || receiver.distanceToSqr(position) > 0.01)
            return false;

        CommonProxy proxy = SimpleAnimator.getProxy();
        Interaction interaction = proxy.getAnimationManager().getInteraction(request.getInteraction());

        if (interaction == null)
            return false;

        proxy.getAnimatorManager().createIfAbsent(requester.getUUID()).play(interaction.requester());
        proxy.getAnimatorManager().createIfAbsent(receiver.getUUID()).play(interaction.receiver());

        receiver.moveTo(position);
        receiver.lookAt(EntityAnchorArgument.Anchor.EYES, requester.getEyePosition());

        request.cancel();
        return true;
    }

    public void reset() {
        for (RequestHolder holder : this.requests.values()) {
            holder.cancel();
        }
    }

    @Nullable
    public RequestHolder get(UUID requester) {
        return this.requests.get(requester);
    }

    public boolean exist(UUID requester) {
        return this.requests.containsKey(requester);
    }

    public void cancel(UUID requester) {
        RequestHolder request = get(requester);

        if (request != null) {
            request.cancel();
            SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(requester).stop();
        }
    }

    public RequestHolder createIfAbsent(UUID uuid) {
        return this.requests.computeIfAbsent(uuid, RequestHolder::new);
    }

    public record Request(
            UUID target,
            ResourceLocation interaction
    ) {}
}