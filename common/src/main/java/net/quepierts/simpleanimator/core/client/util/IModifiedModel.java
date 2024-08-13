package net.quepierts.simpleanimator.core.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.quepierts.simpleanimator.core.client.ClientAnimator;

@Environment(EnvType.CLIENT)
public interface IModifiedModel {
    void simpleAnimator$render(PoseStack poseStack, VertexConsumer consumer, ClientAnimator animator, boolean rigModified, int i, int j, int k);
}
