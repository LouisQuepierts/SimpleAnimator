package net.quepierts.simpleanimator.core.mixin.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.quepierts.simpleanimator.core.client.util.IModifiedModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Environment(EnvType.CLIENT)
@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements IModifiedModelPart {
    @Shadow public boolean visible;
    @Shadow private boolean skipDraw;
    @Shadow @Final private List<ModelPart.Cube> cubes;
    @Shadow @Final private Map<String, ModelPart> children;

    @Unique private final Set<IModifiedModelPart> simpleanimator$children = new ObjectOpenHashSet<>();

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void simpleanimator$init(List<ModelPart.Cube> list, Map<String, ModelPart> map, CallbackInfo ci) {
        this.children.values().stream()
                .map(IModifiedModelPart.class::cast)
                .forEach(simpleanimator$children::add);
    }

    @Unique
    @Override
    public void simpleanimator$render(PoseStack poseStack, VertexConsumer consumer, int i, int j, int k) {
        if (this.visible) {
            if (!this.cubes.isEmpty() || !this.simpleanimator$children.isEmpty()) {
                if (!this.skipDraw) {
                    this.compile(poseStack.last(), consumer, i, j, k);
                }
            }
        }
    }

    @Unique
    @Override
    public void simpleanimator$addChildren(ModelPart... children) {
        for (ModelPart part : children) {
            this.simpleanimator$children.add((IModifiedModelPart) (Object) part);
        }
    }

    @Unique
    @Override
    public boolean simpleanimator$maches(ModelPart part) {
        return this.simpleanimator$children.contains(part);
    }

    @Shadow
    abstract void compile(PoseStack.Pose last, VertexConsumer consumer, int i, int j, int k);

    @Shadow
    abstract void translateAndRotate(PoseStack poseStack);
}
