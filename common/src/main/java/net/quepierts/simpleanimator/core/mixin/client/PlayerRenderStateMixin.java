package net.quepierts.simpleanimator.core.mixin.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.quepierts.simpleanimator.core.client.util.PlayerHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements PlayerHolder {
    @Unique
    private AbstractClientPlayer player;

    @Override
    public AbstractClientPlayer getPlayer() {
        return this.player;
    }

    @Override
    public void setPlayer(AbstractClientPlayer player) {
        this.player = player;
    }
}
