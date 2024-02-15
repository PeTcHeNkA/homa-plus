package plus.fserver.server.network;

import static com.google.common.base.Preconditions.checkArgument;
import io.netty.buffer.ByteBuf;
import java.util.UUID;


public class ProtocolUtils {

    /**
     * Reads a Minecraft-style VarInt from the specified {@code buf}.
     * @param buf the buffer to read from
     * @return the decoded VarInt
     */
    public static int readVarInt(ByteBuf buf) {
        int read = readVarIntSafely(buf);

        return read;
    }

    /**
     * Reads a Minecraft-style VarInt from the specified {@code buf}. The difference between this
     * method and {@link #readVarInt(ByteBuf)} is that this function returns a sentinel value if the
     * varint is invalid.
     * @param buf the buffer to read from
     * @return the decoded VarInt, or {@code Integer.MIN_VALUE} if the varint is invalid
     */
    public static int readVarIntSafely(ByteBuf buf) {
        int i = 0;
        int maxRead = Math.min(5, buf.readableBytes());
        for (int j = 0; j < maxRead; j++) {
            int k = buf.readByte();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) {
                return i;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Writes a Minecraft-style VarInt to the specified {@code buf}.
     * @param buf the buffer to read from
     * @param value the integer to write
     */
    public static void writeVarInt(ByteBuf buf, int value) {
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
                buf.writeByte(value);
                return;
            }

            buf.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
    }




    /**
     * Reads an VarInt-prefixed array of VarInt integers from the {@code buf}.
     * @param buf the buffer to read from
     * @return an array of integers
     */
    public static int[] readIntegerArray(ByteBuf buf) {
        int len = readVarInt(buf);
        checkArgument(len >= 0, "Got a negative-length integer array (%s)", len);
        int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            array[i] = readVarInt(buf);
        }
        return array;
    }

    /**
     * Reads an UUID from the {@code buf}.
     * @param buf the buffer to read from
     * @return the UUID from the buffer
     */
    public static UUID readUuid(ByteBuf buf) {
        long msb = buf.readLong();
        long lsb = buf.readLong();
        return new UUID(msb, lsb);
    }

    public static void writeUuid(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Reads an UUID stored as an Integer Array from the {@code buf}.
     * @param buf the buffer to read from
     * @return the UUID from the buffer
     */
    public static UUID readUuidIntArray(ByteBuf buf) {
        long msbHigh = (long) buf.readInt() << 32;
        long msbLow = (long) buf.readInt() & 0xFFFFFFFFL;
        long msb = msbHigh | msbLow;
        long lsbHigh = (long) buf.readInt() << 32;
        long lsbLow = (long) buf.readInt() & 0xFFFFFFFFL;
        long lsb = lsbHigh | lsbLow;
        return new UUID(msb, lsb);
    }

    /**
     * Writes an UUID as an Integer Array to the {@code buf}.
     * @param buf the buffer to write to
     * @param uuid the UUID to write
     */
    public static void writeUuidIntArray(ByteBuf buf, UUID uuid) {
        buf.writeInt((int) (uuid.getMostSignificantBits() >> 32));
        buf.writeInt((int) uuid.getMostSignificantBits());
        buf.writeInt((int) (uuid.getLeastSignificantBits() >> 32));
        buf.writeInt((int) uuid.getLeastSignificantBits());
    }


    private static final int FORGE_MAX_ARRAY_LENGTH = Integer.MAX_VALUE & 0x1FFF9A;

    /**
     * Reads an byte array for legacy version 1.7 from the specified {@code buf}
     *
     * @param buf the buffer to read from
     * @return the read byte array
     */
    public static byte[] readByteArray17(ByteBuf buf) {
        // Read in a 2 or 3 byte number that represents the length of the packet. (3 byte "shorts" for
        // Forge only)
        // No vanilla packet should give a 3 byte packet
        int len = readExtendedForgeShort(buf);

        checkArgument(len <= FORGE_MAX_ARRAY_LENGTH,
                "Cannot receive array longer than %s (got %s bytes)", FORGE_MAX_ARRAY_LENGTH, len);

        byte[] ret = new byte[len];
        buf.readBytes(ret);
        return ret;
    }


    /**
     * Reads a Minecraft-style extended short from the specified {@code buf}.
     *
     * @param buf buf to write
     * @return read extended short
     */
    public static int readExtendedForgeShort(ByteBuf buf) {
        int low = buf.readUnsignedShort();
        int high = 0;
        if ((low & 0x8000) != 0) {
            low = low & 0x7FFF;
            high = buf.readUnsignedByte();
        }
        return ((high & 0xFF) << 15) | low;
    }

    /**
     * Writes a Minecraft-style extended short to the specified {@code buf}.
     *
     * @param buf buf to write
     * @param toWrite the extended short to write
     */
    public static void writeExtendedForgeShort(ByteBuf buf, int toWrite) {
        int low = toWrite & 0x7FFF;
        int high = (toWrite & 0x7F8000) >> 15;
        if (high != 0) {
            low = low | 0x8000;
        }
        buf.writeShort(low);
        if (high != 0) {
            buf.writeByte(high);
        }
    }
}