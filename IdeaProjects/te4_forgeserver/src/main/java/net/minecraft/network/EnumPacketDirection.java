package net.minecraft.network;

public enum EnumPacketDirection {
    SERVERBOUND(0),
    CLIENTBOUND(1);

    public final int id;

    EnumPacketDirection(int id) {
        this.id = id;
    }
}