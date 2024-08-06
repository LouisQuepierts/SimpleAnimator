package net.quepierts.simpleanimator.core.mixin.accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public interface CameraAccessor {
    @Invoker("setRotation")
    void simpleanimator$setRotation(float yaw, float pitch);
}
