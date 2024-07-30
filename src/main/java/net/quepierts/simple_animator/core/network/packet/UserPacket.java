package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.quepierts.simple_animator.core.network.BiPacket;

import java.util.UUID;

public abstract class UserPacket extends BiPacket {
    protected final UUID uuid;

    public UserPacket(FriendlyByteBuf byteBuf) {
        this.uuid = byteBuf.readUUID();
    }

    public UserPacket(UUID uuid) {
        this.uuid = uuid;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.uuid);
    }

    public UUID getUuid() {
        return uuid;
    }
}
