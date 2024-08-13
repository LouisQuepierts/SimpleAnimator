package net.quepierts.simpleanimator.core.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public interface IModelUpdater {
    void simpleAnimator$update(@NotNull AbstractClientPlayer pPlayer);
}
