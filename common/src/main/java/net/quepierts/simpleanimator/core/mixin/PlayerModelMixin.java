package net.quepierts.simpleanimator.core.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {
    @Shadow @Final public ModelPart leftPants;

    @Shadow @Final public ModelPart rightPants;

    @Shadow @Final public ModelPart leftSleeve;

    @Shadow @Final public ModelPart rightSleeve;

    @Shadow @Final public ModelPart jacket;

    public PlayerModelMixin(ModelPart pRoot) {
        super(pRoot);
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(
                    value = "RETURN"
            )
    )
    public void process(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
        ClientAnimator animator = SimpleAnimator.getClient().getClientAnimatorManager().getAnimator(pEntity.getUUID());

        /*if (pEntity.getUUID().equals(Minecraft.getInstance().sender.getUUID())) {
            ClientHandler.clientPlayerModel = (PlayerModel<AbstractClientPlayer>) (Object) this;
        }*/

        /*if (Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
            float yaw = pEntity.yHeadRot - pEntity.yBodyRot;
            float pitch = pEntity.getXRot();

            this.head.yRot = yaw * ((float)Math.PI / 180F);

            boolean falling = pEntity.getFallFlyingTicks() > 4;
            boolean flag1 = pEntity.isVisuallySwimming();
            if (falling) {
                this.head.xRot = (-(float)Math.PI / 4F);
            } else if (this.swimAmount > 0.0F) {
                if (flag1) {
                    this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (-(float)Math.PI / 4F));
                } else {
                    this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, pitch * ((float)Math.PI / 180F));
                }
            } else {
                this.head.xRot = pitch * ((float)Math.PI / 180F);
            }
        }*/

        if (animator != null && animator.isRunning()) {
            // Prevent model parts will fly away
            this.head.x = 0;
            this.head.z = 0;
            this.head.zRot = 0;

            this.body.x = 0;
            this.body.z = 0;
            this.body.zRot = 0;

            animator.process((PlayerModel<AbstractClientPlayer>) (Object) this);

            this.hat.copyFrom(this.head);
            this.leftPants.copyFrom(this.leftLeg);
            this.rightPants.copyFrom(this.rightLeg);
            this.leftSleeve.copyFrom(this.leftArm);
            this.rightSleeve.copyFrom(this.rightArm);
            this.jacket.copyFrom(this.body);
        }
    }
}
