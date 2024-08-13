package net.quepierts.simpleanimator.core.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(EnvType.CLIENT)
public interface IModifiedModelPart {
    void simpleanimator$render(PoseStack poseStack, VertexConsumer consumer, int i, int j, int k);

    void simpleanimator$addChildren(ModelPart... part);

    boolean simpleanimator$maches(ModelPart part);
}
