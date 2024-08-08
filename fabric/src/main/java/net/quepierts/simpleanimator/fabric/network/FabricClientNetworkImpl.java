package net.quepierts.simpleanimator.fabric.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.quepierts.simpleanimator.core.network.IPacket;
import net.quepierts.simpleanimator.core.network.NetworkContext;
import net.quepierts.simpleanimator.core.network.NetworkDirection;
import net.quepierts.simpleanimator.core.network.NetworkPackets;

@Environment(EnvType.CLIENT)
public class FabricClientNetworkImpl extends FabricNetworkImpl {
    public FabricClientNetworkImpl() {
        super();
    }

    @Override
    public void update(IPacket packet) {
        ClientPlayNetworking.send(packet);
    }

    @SuppressWarnings("all")
    @Override
    public <T extends IPacket> void register(NetworkPackets.PacketType<T> packet) {
        super.register(packet);
        if (packet.direction != NetworkDirection.PLAY_TO_SERVER) {
            ClientPlayNetworking.registerGlobalReceiver(
                    packet.type,
                    ((payload, context) -> payload.handle(new NetworkContext(NetworkDirection.PLAY_TO_CLIENT, null)))
            );
        }
    }
}

