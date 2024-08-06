package net.quepierts.simpleanimator.core.client.util;

import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.core.Direction;

import java.util.Set;

public interface CubeListBuilderManipulator {
    CubeListBuilder simpleAnimator$addBox(float f, float g, float h, float i, float j, float k, CubeDeformation cubeDeformation, Set<Direction> faces);
}
