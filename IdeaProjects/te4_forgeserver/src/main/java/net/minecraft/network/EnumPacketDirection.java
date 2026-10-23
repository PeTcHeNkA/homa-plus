package net.minecraft.network;

import plus.fserver.api.Keep;


@Keep
public enum EnumPacketDirection {
    SERVERBOUND(0),
    CLIENTBOUND(1);

    public final int id;

    EnumPacketDirection(int id) {
        this.id = id;
    }
}