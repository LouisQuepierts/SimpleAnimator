package net.quepierts.simpleanimator.core.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.Animator;
import net.quepierts.simpleanimator.core.animation.IAnimatorProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin implements IAnimatorProvider {
    @Unique
    private Animator animator;
    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    public void createAnimator(Level level, BlockPos blockPos, float f, GameProfile gameProfile, CallbackInfo ci) {
        animator = SimpleAnimator.getProxy().getAnimatorManager().get(((Player) (Object) this).getUUID());
    }

    @Unique
    @Override
    public Animator simpleanimator$getAnimator() {
        return animator;
    }
}
