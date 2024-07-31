package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.quepierts.simple_animator.core.network.BiPacket;

import java.util.UUID;

public abstract class UserPacket extends BiPacket {
    protected final UUID owner;

    public UserPacket(FriendlyByteBuf byteBuf) {
        this.owner = byteBuf.readUUID();
    }

    public UserPacket(UUID uuid) {
        this.owner = uuid;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.owner);
    }

    public UUID getOwner() {
        return owner;
    }
}
