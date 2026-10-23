package plus.timidblocks.client;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;


/**
 * Аудио-пакет для отправки на сервер
 * @author HomaPlus
 */
public class AudioDataPayload implements CustomPayload {
    public static final Id<AudioDataPayload> ID = new Id<>(MainClient.TIMID_BLOCKS_NET_ID);
    public static final PacketCodec<RegistryByteBuf, AudioDataPayload> CODEC = new PacketCodec<>() {
        @Override
        public void encode(RegistryByteBuf buf, AudioDataPayload value) {
            buf.writeBytes(value.data, 0, value.data.length);
        }

        @Override
        public AudioDataPayload decode(RegistryByteBuf buf) {
            AudioDataPayload data = new AudioDataPayload(buf.readByteArray(), 0);
            data.count = data.data.length;
            return data;
        }
    };

    public static void init() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
    }

    public byte[] data;
    public int count;

    public AudioDataPayload(byte[] data, int count) {
        this.data = data;
        this.count = count;
    }


    @Override
    public Id<AudioDataPayload> getId() {
        return ID;
    }
}