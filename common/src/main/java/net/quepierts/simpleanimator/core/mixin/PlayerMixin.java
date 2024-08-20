package net.quepierts.simpleanimator.core.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.api.IInteractHandler;
import net.quepierts.simpleanimator.api.INavigatable;
import net.quepierts.simpleanimator.api.animation.RequestHolder;
import net.quepierts.simpleanimator.api.event.common.*;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.Animator;
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
public abstract class PlayerMixin extends LivingEntity implements IAnimateHandler, IInteractHandler, INavigatable {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
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
    public boolean simpleanimator$playAnimate(@NotNull ResourceLocation animation, boolean update) {
        if (SimpleAnimator.EVENT_BUS.post(new AnimatePlayEvent.Pre((Player) (Object) this, animation)).isCanceled())
            return false;

        if (!this.simpleanimator$animator.play(animation))
            return false;

        this.setYBodyRot(this.getYHeadRot());

        if (update && this.isLocalPlayer()) {
            SimpleAnimator.getNetwork().update(new AnimatorPlayPacket(this.getUUID(), animation));
        }

        SimpleAnimator.EVENT_BUS.post(new AnimatePlayEvent.Post((Player) (Object) this, animation));
        return true;
    }

    @Unique @Override
    public boolean simpleanimator$stopAnimate(boolean update) {
        ResourceLocation animationID = this.simpleanimator$animator.getAnimationLocation();
        if (SimpleAnimator.EVENT_BUS.post(new AnimateStopEvent.Pre((Player) (Object) this, animationID)).isCanceled())
            return false;

        if (!this.simpleanimator$animator.stop()) {
            return false;
        }

        if (update && this.isLocalPlayer()) {
            SimpleAnimator.getNetwork().update(new AnimatorStopPacket(this.getUUID()));
        }

        SimpleAnimator.EVENT_BUS.post(new AnimatePlayEvent.Post((Player) (Object) this, animationID));
        return true;
    }

    @Unique @NotNull @Override
    public Animator simpleanimator$getAnimator() {
        return this.simpleanimator$animator;
    }

    @Unique @Override
    public boolean simpleanimator$inviteInteract(@NotNull Player target, @NotNull ResourceLocation interaction, boolean update) {
        if (SimpleAnimator.EVENT_BUS.post(new InteractInviteEvent.Pre((Player) (Object) this, target, interaction)).isCanceled())
            return false;

        if (!SimpleAnimator.getProxy().getInteractionManager().invite((Player) (Object) this, target, interaction))
            return false;

        if (update && this.isLocalPlayer()) {
            SimpleAnimator.getNetwork().update(new InteractInvitePacket(this.getUUID(), target.getUUID(), interaction));
        }

        SimpleAnimator.EVENT_BUS.post(new InteractInviteEvent.Post((Player) (Object) this, target, interaction));
        return true;
    }

    @Unique @Override
    public boolean simpleanimator$acceptInteract(@NotNull Player requester, boolean update, boolean forced) {
        if (SimpleAnimator.EVENT_BUS.post(new InteractAcceptEvent.Pre(requester, (Player) (Object) this, forced)).isCanceled() && !forced)
            return false;

        if (!SimpleAnimator.getProxy().getInteractionManager().accept(requester, (Player) (Object) this, forced))
            return false;

        if (update && this.isLocalPlayer()) {
            SimpleAnimator.getNetwork().update(new InteractAcceptPacket(requester.getUUID(), this.getUUID(), forced));
        }

        SimpleAnimator.EVENT_BUS.post(new InteractAcceptEvent.Post(requester, (Player) (Object) this, forced, this.simpleanimator$request.getInteraction()));
        return true;
    }

    @Unique @Override
    public void simpleanimator$cancelInteract(boolean update) {
        if (!this.simpleanimator$request.hasRequest())
            return;

        SimpleAnimator.EVENT_BUS.post(new CancelInteractEvent((Player) (Object) this, this.simpleanimator$request.getTarget(), this.simpleanimator$request.getInteraction()));
        this.simpleanimator$animator.stop();
        this.simpleanimator$request.reset();

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

    @Unique
    public void simpleanimator$navigate(Player requester) {
        if (this.isLocalPlayer()) {
            this.simpleanimator$innerNavigate(requester);
        }
    }

    @Environment(EnvType.CLIENT)
    private void simpleanimator$innerNavigate(Player requester) {
        SimpleAnimator.getClient()
                .getNavigator()
                .navigateTo(requester, 1, 0, () -> {
                    this.simpleanimator$acceptInteract(requester, true, true);
                    //SimpleAnimator.getNetwork().update(new InteractAcceptPacket(requester.getUUID(), this.getUUID()));
                });
    }
}
