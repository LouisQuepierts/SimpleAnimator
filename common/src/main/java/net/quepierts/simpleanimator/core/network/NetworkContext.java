package net.quepierts.simpleanimator.core.network;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record NetworkContext(
        @NotNull NetworkDirection direction,
        @Nullable ServerPlayer sender
) {
}
