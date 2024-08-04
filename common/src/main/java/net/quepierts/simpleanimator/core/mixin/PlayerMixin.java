package net.quepierts.simpleanimator.core.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.api.IInteractHandler;
import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.api.animation.Interaction;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.RequestHolder;
import net.quepierts.simpleanimator.core.network.packet.*;
import net.quepierts.simpleanimator.core.proxy.CommonProxy;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends Entity implements IAnimateHandler, IInteractHandler {
    public PlayerMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract boolean isLocalPlayer();

    @Unique
    private Animator simpleanimator$animator;
    
    @Unique
    private RequestHolder simpleanimator$request;
    
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    public void createAnimator(Level level, BlockPos blockPos, float f, GameProfile gameProfile, CallbackInfo ci) {
        CommonProxy proxy = SimpleAnimator.getProxy();
        simpleanimator$animator = proxy.getAnimatorManager().createIfAbsent(this.getUUID());
        simpleanimator$request = proxy.getInteractionManager().createIfAbsent(this.getUUID());
    }

    @Unique @Override
    public boolean simpleanimator$isRunning() {
        return this.simpleanimator$animator.isRunning();
    }

    @Unique @Override
    public void simpleanimator$playAnimate(@NotNull ResourceLocation animation, boolean update) {
        this.simpleanimator$animator.play(animation);

        if (update && this.isLocalPlayer()) {
            SimpleAnimator.getNetwork().update(new AnimatorPlayPacket(this.getUUID(), animation));
        }
    }

    @Unique @Override
    public void simpleanimator$stopAnimate(boolean update) {
        this.simpleanimator$animator.stop();

        if (update && this.isLocalPlayer()) {
            SimpleAnimator.getNetwork().update(new AnimatorStopPacket(this.getUUID()));
        }
    }

    @Unique @NotNull @Override
    public Animator simpleanimator$getAnimator() {
        return this.simpleanimator$animator;
    }

    @Unique @Override
    public boolean simpleanimator$invite(@NotNull Player target, @NotNull ResourceLocation interaction, boolean update) {
        if (this.getUUID().equals(target.getUUID()))
            return false;

        if (this.simpleanimator$animator.isRunning() && !this.simpleanimator$animator.getAnimation().isAbortable()) {
            return false;
        }

        if (!PlayerUtils.inSameDimension((Player) (Object) this, target) || this.distanceToSqr(target) > 1024) {
            return false;
        }

        this.simpleanimator$cancel(false);

        this.simpleanimator$request.set(target.getUUID(), interaction);
        if (update && this.isLocalPlayer()) {
            SimpleAnimator.getNetwork().update(new InteractInvitePacket(this.getUUID(), target.getUUID(), interaction));
        }

        Interaction pInteraction = SimpleAnimator.getProxy().getAnimationManager().getInteraction(interaction);
        if (pInteraction != null) {
            this.simpleanimator$animator.play(pInteraction.invite());
        }

        return true;
    }

    @Unique @Override
    public boolean simpleanimator$accept(@NotNull Player requester, boolean update) {
        PlayerMixin req = (PlayerMixin) (Object) requester;

        if (!req.simpleanimator$request.hasRequest() || !this.getUUID().equals(req.simpleanimator$request.getTarget()))
            return false;

        if (!PlayerUtils.inSameDimension((Player) (Object) this, requester))
            return false;

        this.simpleanimator$cancel(false);

        Vec3 position = PlayerUtils.getRelativePosition(requester, 1, 0);

        if (this.distanceToSqr(position) > 0.1f) {
            if (this.isLocalPlayer()) {
                SimpleAnimator.getClient()
                        .getNavigator()
                        .navigateTo(requester, 1, 0, () -> {
                            this.simpleanimator$accept(requester, true);
                            //SimpleAnimator.getNetwork().update(new InteractAcceptPacket(requester.getUUID(), this.getUUID()));
                        });
            }
            return false;
        }

        Interaction interaction = SimpleAnimator.getProxy().getAnimationManager().getInteraction(req.simpleanimator$request.getInteraction());
        req.simpleanimator$request.cancel();

        if (interaction == null)
            return true;

        req.simpleanimator$animator.play(interaction.requester());
        this.simpleanimator$animator.play(interaction.receiver());

        this.moveTo(position);
        this.lookAt(EntityAnchorArgument.Anchor.EYES, req.getEyePosition());

        if (update && this.isLocalPlayer()) {
            SimpleAnimator.getNetwork().update(new InteractAcceptPacket(requester.getUUID(), this.getUUID()));
        }
        return true;
    }

    @Unique @Override
    public void simpleanimator$cancel(boolean update) {
        if (!this.simpleanimator$request.hasRequest())
            return;

        this.simpleanimator$animator.stop();
        this.simpleanimator$request.cancel();

        if (update && this.isLocalPlayer()) {
            SimpleAnimator.getNetwork().update(new InteractCancelPacket(this.getUUID()));
        }
    }

    @Unique @Override
    public boolean simpleanimator$hasRequest() {
        return this.simpleanimator$request.hasRequest();
    }

    @Unique @Override @NotNull
    public RequestHolder simpleanimator$getRequest() {
        return this.simpleanimator$request;
    }
}