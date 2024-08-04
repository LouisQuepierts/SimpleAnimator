package net.quepierts.simpleanimator.core.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.InteractionManager;
import net.quepierts.simpleanimator.core.animation.RequestHolder;
import net.quepierts.simpleanimator.core.network.packet.InteractAcceptPacket;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ClientInteractionManager extends InteractionManager {
    @Deprecated
    @Override
    public boolean accept(Player requester, Player receiver) {
        if (receiver == Minecraft.getInstance().player) {
            return tryAccept(requester);
        }
        return super.accept(requester, receiver);
    }

    @Deprecated
    public boolean tryAccept(Player requester) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        UUID uuid = requester.getUUID();

        RequestHolder request = this.get(uuid);
        if (request == null)
            return false;

        Vec3 position = PlayerUtils.getRelativePosition(requester, 1, 0);
        if (player.distanceToSqr(position) > 0.01) {
            SimpleAnimator.getClient().getNavigator().navigateTo(
                    requester, 1, 0,
                    () -> SimpleAnimator.getNetwork().update(new InteractAcceptPacket(uuid, Minecraft.getInstance().player.getUUID()))
            );
            return false;
        }

        return super.accept(requester, player);
    }

    public boolean requesting() {
        return this.exist(Minecraft.getInstance().player.getUUID());
    }

    public void cancel() {
        this.cancel(Minecraft.getInstance().player.getUUID());
    }

    protected RequestHolder getLocalRequest() {
        return get(Minecraft.getInstance().player.getUUID());
    }
}
