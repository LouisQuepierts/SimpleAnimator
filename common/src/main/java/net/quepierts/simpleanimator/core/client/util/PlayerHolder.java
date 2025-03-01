package net.quepierts.simpleanimator.core.client.util;

import net.minecraft.client.player.AbstractClientPlayer;

public interface PlayerHolder {
    AbstractClientPlayer getPlayer();

    void setPlayer(AbstractClientPlayer player);
}
