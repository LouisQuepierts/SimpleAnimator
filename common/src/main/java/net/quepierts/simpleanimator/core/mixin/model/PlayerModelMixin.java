package net.quepierts.simpleanimator.core.mixin.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Environment(EnvType.CLIENT)
@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T>  {
    @Unique private static final Set<Direction> UPPER_MODEL = Set.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
    @Unique private static final Set<Direction> LOWER_MODEL = Set.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);


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
            at = @At("HEAD")
    )
    public void resetModelParts(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        this.head.resetPose();
        this.body.resetPose();
        this.leftArm.resetPose();
        this.rightArm.resetPose();
        this.leftLeg.resetPose();
        this.rightLeg.resetPose();
    }

    @SuppressWarnings("unchecked")
    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(
                    value = "RETURN"
            )
    )
    public void process(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
        ClientAnimator animator = SimpleAnimator.getClient().getClientAnimatorManager().getAnimator(pEntity.getUUID());

        if (animator != null && animator.isRunning()) {
            animator.process((PlayerModel<AbstractClientPlayer>) (Object) this, (Player) pEntity);

            this.hat.copyFrom(this.head);
            this.leftPants.copyFrom(this.leftLeg);
            this.rightPants.copyFrom(this.rightLeg);
            this.leftSleeve.copyFrom(this.leftArm);
            this.rightSleeve.copyFrom(this.rightArm);
            this.jacket.copyFrom(this.body);
        }
    }

    /*@Inject(
            method = "createMesh",
            at = @At(
                    value = "RETURN",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void replaceMesh(CubeDeformation cubeDeformation, boolean bl, CallbackInfoReturnable<MeshDefinition> cir, MeshDefinition meshDefinition, PartDefinition partDefinition) {
        if (!SimpleAnimator.getClient().getClientConfiguration().s)
            return;

        if (bl) {
            PartDefinition left_arm = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 6.0F, 4.0F, cubeDeformation), PartPose.offset(5.0F, 2.5F, 0.0F));
            PartDefinition right_arm = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 6.0F, 4.0F, cubeDeformation), PartPose.offset(-5.0F, 2.5F, 0.0F));

            partDefinition.addOrReplaceChild("left_sleeve", ((CubeListBuilderManipulator) CubeListBuilder.create().texOffs(48, 48)).simpleAnimator$addBox(-1.0F, -2.0F, -2.0F, 3.0F, 6.0F, 4.0F, cubeDeformation.extend(0.25F), UPPER_MODEL), PartPose.offset(5.0F, 2.0F, 0.0F));
            partDefinition.addOrReplaceChild("right_sleeve", ((CubeListBuilderManipulator) CubeListBuilder.create().texOffs(40, 32)).simpleAnimator$addBox(-3.0F, -2.0F, -2.0F, 3.0F, 6.0F, 4.0F, cubeDeformation.extend(0.25F), UPPER_MODEL), PartPose.offset(-5.0F, 2.0F, 0.0F));

            left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(32, 54).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 4.0F, 0.0F));
            right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(40, 22).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 4.0F, 0.0F));

            left_arm.addOrReplaceChild("left_hand_sleeve", ((CubeListBuilderManipulator) CubeListBuilder.create().texOffs(48, 54)).simpleAnimator$addBox(-2.0F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F), LOWER_MODEL), PartPose.offset(1.0F, 4.0F, 0.0F));
            right_arm.addOrReplaceChild("right_hand_sleeve", ((CubeListBuilderManipulator) CubeListBuilder.create().texOffs(40, 38)).simpleAnimator$addBox(-2.0F, 0.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F), LOWER_MODEL), PartPose.offset(1.0F, 4.0F, 0.0F));
        } else {
            PartDefinition left_arm = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, cubeDeformation), PartPose.offset(5.0F, 2.0F, 0.0F));
            PartDefinition right_arm = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, cubeDeformation), PartPose.offset(-5.0F, 2.0F, 0.0F));

            partDefinition.addOrReplaceChild("left_sleeve", ((CubeListBuilderManipulator) CubeListBuilder.create().texOffs(48, 48)).simpleAnimator$addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, cubeDeformation.extend(0.25F), UPPER_MODEL), PartPose.offset(5.0F, 2.0F, 0.0F));
            partDefinition.addOrReplaceChild("right_sleeve", ((CubeListBuilderManipulator) CubeListBuilder.create().texOffs(40, 32)).simpleAnimator$addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, cubeDeformation.extend(0.25F), UPPER_MODEL), PartPose.offset(-5.0F, 2.0F, 0.0F));

            left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(32, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 4.0F, 0.0F));
            right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(40, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 4.0F, 0.0F));

            left_arm.addOrReplaceChild("left_hand_sleeve", ((CubeListBuilderManipulator) CubeListBuilder.create().texOffs(48, 54)).simpleAnimator$addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F), LOWER_MODEL), PartPose.offset(1.0F, 4.0F, 0.0F));
            right_arm.addOrReplaceChild("right_hand_sleeve", ((CubeListBuilderManipulator) CubeListBuilder.create().texOffs(40, 38)).simpleAnimator$addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F), LOWER_MODEL), PartPose.offset(1.0F, 4.0F, 0.0F));
        }
    }*/
}
