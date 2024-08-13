package net.quepierts.simpleanimator.core.animation;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.api.INavigatable;
import net.quepierts.simpleanimator.api.animation.Interaction;
import net.quepierts.simpleanimator.api.animation.RequestHolder;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InteractionManager {
    protected final Map<UUID, RequestHolder> requests;

    public InteractionManager() {
        requests = new HashMap<>();
    }

    public void reset() {
        for (RequestHolder holder : this.requests.values()) {
            holder.reset();
        }
    }

    @Nullable
    public RequestHolder get(UUID requester) {
        return this.requests.get(requester);
    }

    public boolean exist(UUID requester) {
        return this.requests.containsKey(requester);
    }

    public boolean invite(Player inviter, Player target, ResourceLocation interaction) {
        UUID inviterUUID = inviter.getUUID();
        if (inviterUUID.equals(target.getUUID()))
            return false;

        Animator animator = SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(inviterUUID);
        if (animator.isRunning() && !animator.getAnimation().isAbortable()) {
            return false;
        }

        RequestHolder holder = this.createIfAbsent(inviterUUID);

        if (!PlayerUtils.inSameDimension(inviter, target) || inviter.distanceToSqr(target) > SimpleAnimator.getProxy().getConfig().interactInviteDistanceSquare) {
            return false;
        }

        Vec3 position = PlayerUtils.getRelativePositionWorldSpace(inviter, 1, 0);
        if (!PlayerUtils.isPositionSave(position, inviter.level())) {
            return false;
        }

        float rotY = PlayerUtils.getLookAtRotY(inviter, target.position());
        inviter.setYRot(rotY);
        inviter.yRotO = inviter.getYRot();
        inviter.yHeadRot = rotY;
        inviter.yHeadRotO = rotY;
        inviter.yBodyRot = rotY;
        inviter.yBodyRotO = rotY;

        this.cancel(inviterUUID);

        holder.invite(target.getUUID(), interaction);

        Interaction pInteraction = SimpleAnimator.getProxy().getAnimationManager().getInteraction(interaction);
        if (pInteraction != null) {
            animator.play(pInteraction.invite());
        }
        return true;
    }

    public boolean accept(Player requester, Player acceptor, boolean forced) {
        UUID acceptorUUID = acceptor.getUUID();
        UUID requesterUUID = requester.getUUID();
        RequestHolder holder = this.createIfAbsent(requesterUUID);

        if (!holder.hasRequest() || !acceptorUUID.equals(holder.getTarget()))
            return false;

        if (!PlayerUtils.inSameDimension(requester, requester))
            return false;


        this.cancel(acceptorUUID);

        Vec3 position = PlayerUtils.getRelativePositionWorldSpace(requester, 1, 0);

        if (!forced && acceptor.distanceToSqr(position) > 0.1f) {
            ((INavigatable) acceptor).simpleanimator$navigate(requester);
            return false;
        }
        Interaction interaction = SimpleAnimator.getProxy().getAnimationManager().getInteraction(holder.getInteraction());
        holder.success();
        //this.simpleanimator$request.accept(requester.getUUID(), req.simpleanimator$request.getInteraction());

        acceptor.setPos(position);
        acceptor.lookAt(EntityAnchorArgument.Anchor.EYES, requester.getEyePosition());

        if (interaction != null) {
            SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(requesterUUID).play(interaction.requester());
            SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(acceptorUUID).play(interaction.receiver());
        }

        return true;
    }

    public void cancel(UUID requester) {
        RequestHolder request = get(requester);

        if (request != null) {
            request.reset();
            SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(requester).stop();
        }
    }

    public RequestHolder createIfAbsent(UUID uuid) {
        return this.requests.computeIfAbsent(uuid, RequestHolder::new);
    }

    public void remove(UUID uuid) {
        this.requests.remove(uuid);
    }

    public record Request(
            UUID target,
            ResourceLocation interaction
    ) {}
}