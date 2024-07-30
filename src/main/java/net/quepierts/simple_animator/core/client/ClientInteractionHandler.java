package net.quepierts.simple_animator.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simple_animator.core.animation.Interaction;
import net.quepierts.simple_animator.core.common.PlayerUtils;
import net.quepierts.simple_animator.core.network.ModNetwork;
import net.quepierts.simple_animator.core.network.packet.InteractAcceptPacket;
import net.quepierts.simple_animator.core.proxy.ClientProxy;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientInteractionHandler {
    private final ClientProxy client;
    private final Map<UUID, Interaction> received;

    @Nullable private UUID target;
    @Nullable private Interaction requested;

    public ClientInteractionHandler(ClientProxy clientProxy) {
        this.client = clientProxy;
        received = new HashMap<>();
    }

    public void receive(UUID uuid, ResourceLocation location) {
        assert Minecraft.getInstance().level != null;
        Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);

        if (player == null)
            return;

        Interaction interaction = client.getAnimationManager().getInteraction(location);
        if (interaction == null) {
            received.remove(uuid);
        } else {
            received.put(uuid, interaction);
        }
    }

    public void travel(Player player) {
        Vec3 position = PlayerUtils.getRelativePosition(player, 1, 0);
        client.getNavigator().navigateTo(position);
    }

    public void tryAccept(UUID uuid) {
        Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);

        if (player == null) {
            received.remove(uuid);
            return;
        }

        Vec3 position = PlayerUtils.getRelativePosition(player, 1, 0);
        if (player.distanceToSqr(position) > 0.01) {
            client.getNavigator().navigateTo(position);
            return;
        }

        accept(uuid);
    }

    public void accept(UUID uuid) {
        if (!received.containsKey(uuid))
            return;

        ClientAnimator localAnimator = client.getAnimatorManager().getLocalAnimator();
        if (localAnimator.isRunning())
            return;

        Interaction interaction = received.get(uuid);

        localAnimator.play(interaction.receiver());
        ModNetwork.update(new InteractAcceptPacket(uuid));
    }
}
