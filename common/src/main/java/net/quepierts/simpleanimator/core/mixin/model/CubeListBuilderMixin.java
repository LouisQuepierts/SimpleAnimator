package net.quepierts.simpleanimator.core.mixin.model;

import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.core.Direction;
import net.quepierts.simpleanimator.core.client.CubeListBuilderManipulator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Set;

@Mixin(CubeListBuilder.class)
public abstract class CubeListBuilderMixin implements CubeListBuilderManipulator {

    @Shadow @Final private List<CubeDefinition> cubes;

    @Shadow private boolean mirror;

    @Shadow private int yTexOffs;

    @Shadow private int xTexOffs;

    @Unique
    public CubeListBuilder simpleAnimator$addBox(float f, float g, float h, float i, float j, float k, CubeDeformation cubeDeformation, Set<Direction> faces) {
        this.cubes.add(CubeDefinitionAccessor.getInstance(null, (float)this.xTexOffs, (float)this.yTexOffs, f, g, h, i, j, k, cubeDeformation, this.mirror, 1.0F, 1.0F, faces));
        return (CubeListBuilder) (Object) this;
    }
}
