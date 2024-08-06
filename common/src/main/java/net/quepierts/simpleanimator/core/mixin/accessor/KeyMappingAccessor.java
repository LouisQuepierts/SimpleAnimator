package net.quepierts.simpleanimator.core.mixin.accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Environment(EnvType.CLIENT)
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    @Invoker("release")
    void simpleanimator$release();
}
