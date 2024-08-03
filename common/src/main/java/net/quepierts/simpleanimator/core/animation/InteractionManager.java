package net.quepierts.simpleanimator.core.animation;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.proxy.CommonProxy;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InteractionManager {
    protected final Map<UUID, Request> requests;

    public InteractionManager() {
        requests = new HashMap<>();
    }

    public boolean invite(Player requester, Player receiver, ResourceLocation location) {
        if (requester == receiver)
            return false;

        CommonProxy proxy = SimpleAnimator.getProxy();
        Animator animator = proxy.getAnimatorManager().get(requester.getUUID());

        if (animator.getAnimation() != null && !animator.getAnimation().isAbortable())
            return false;

        Interaction interaction = proxy.getAnimationManager().getInteraction(location);
        System.out.println(interaction);

        if (interaction == null)
            return false;

        this.requests.put(requester.getUUID(), new Request(receiver.getUUID(), location));
        animator.play(interaction.invite());
        return true;
    }

    public boolean accept(Player requester, Player receiver) {
        Request request = this.requests.get(requester.getUUID());

        if (request == null || !request.target.equals(receiver.getUUID()))
            return false;

        Vec3 position = PlayerUtils.getRelativePosition(requester, 1, 0);
        if (!PlayerUtils.inSameDimension(receiver, receiver) || receiver.distanceToSqr(position) > 0.01)
            return false;

        CommonProxy proxy = SimpleAnimator.getProxy();
        Interaction interaction = proxy.getAnimationManager().getInteraction(request.interaction);

        if (interaction == null)
            return false;

        proxy.getAnimatorManager().get(requester.getUUID()).play(interaction.requester());
        proxy.getAnimatorManager().get(receiver.getUUID()).play(interaction.receiver());

        receiver.moveTo(position);
        receiver.lookAt(EntityAnchorArgument.Anchor.EYES, requester.getEyePosition());

        this.requests.remove(requester.getUUID());
        return true;
    }

    public void clear() {
        this.requests.clear();
    }

    @Nullable
    public Request get(UUID requester) {
        return this.requests.get(requester);
    }

    public boolean exist(UUID requester) {
        return this.requests.containsKey(requester);
    }

    public void cancel(UUID requester) {
        Request request = get(requester);

        if (request != null) {
            this.requests.remove(requester);
            SimpleAnimator.getProxy().getAnimatorManager().get(requester).stop();
        }
    }

    public record Request(
            UUID target,
            ResourceLocation interaction
    ) {}
}