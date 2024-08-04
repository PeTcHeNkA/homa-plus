package plus.fserver.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;


public class NetUtils {

    public static void writeByteArray(ByteBuf buf, final byte[] array) {
        writeVarInt(buf, array.length);
        buf.writeBytes(array);
    }


    public static byte[] readByteArray(ByteBuf buf) {
        return readByteArray(buf, buf.readableBytes());
    }


    public static byte[] readByteArray(ByteBuf buf, final int maxLength) {
        final int i = readVarInt(buf);
        if (i > maxLength) {
            throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + maxLength);
        }
        final byte[] abyte = new byte[i];
        buf.readBytes(abyte);
        return abyte;
    }


    public static void writeVarIntArray(ByteBuf buf, final int[] array) {
        writeVarInt(buf, array.length);
        for (final int i : array) {
            writeVarInt(buf, i);
        }
    }


    public static int[] readVarIntArray(ByteBuf buf) {
        return readVarIntArray(buf, buf.readableBytes());
    }


    public static int[] readVarIntArray(ByteBuf buf, final int maxLength) {
        final int i = readVarInt(buf);
        if (i > maxLength) {
            throw new DecoderException("VarIntArray with size " + i + " is bigger than allowed " + maxLength);
        }
        final int[] aint = new int[i];
        for (int j = 0; j < aint.length; ++j) {
            aint[j] = readVarInt(buf);
        }
        return aint;
    }


    public static void writeLongArray(ByteBuf buf, final long[] array) {
        writeVarInt(buf, array.length);
        for (final long i : array) buf.writeLong(i);
    }


    @SideOnly(Side.CLIENT)
    public long[] readLongArray(ByteBuf buf, @Nullable final long[] array) {
        return this.readLongArray(buf, array, buf.readableBytes() / 8);
    }


    @SideOnly(Side.CLIENT)
    public long[] readLongArray(ByteBuf buf, @Nullable long[] array, final int maxLength) {
        final int i = readVarInt(buf);
        if (array == null || array.length != i) {
            if (i > maxLength) {
                throw new DecoderException("LongArray with size " + i + " is bigger than allowed " + maxLength);
            }
            array = new long[i];
        }
        for (int j = 0; j < array.length; ++j) {
            array[j] = buf.readLong();
        }
        return array;
    }


    public static BlockPos readBlockPos(ByteBuf buf) {
        return BlockPos.fromLong(buf.readLong());
    }


    public static void writeBlockPos(ByteBuf buf, final BlockPos pos) {
        buf.writeLong(pos.toLong());
    }


    public ITextComponent readTextComponent(ByteBuf buf) throws IOException {
        return ITextComponent.Serializer.jsonToComponent(readString(buf, 32767));
    }


    public static void writeTextComponent(ByteBuf buf, final ITextComponent component) {
        writeString(buf, ITextComponent.Serializer.componentToJson(component));
    }


    public static <T extends Enum<T>> T readEnumValue(ByteBuf buf, final Class<T> enumClass) {
        return enumClass.getEnumConstants()[NetUtils.readVarInt(buf)];
    }


    public static <T extends Enum<T>> T readEnumValueFast(ByteBuf buf, final T[] values) {
        return values[NetUtils.readVarInt(buf)];
    }


    public static void writeEnumValue(ByteBuf buf, final Enum<?> value) {
        writeVarInt(buf, value.ordinal());
    }


    public static int readVarInt(ByteBuf buf) {
        int i = 0;
        int j = 0;
        while (true) {
            final byte b0 = buf.readByte();
            i |= (b0 & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((b0 & 0x80) != 0x80) {
                return i;
            }
        }
    }


    public static long readVarLong(ByteBuf buf) {
        long i = 0L;
        int j = 0;
        while (true) {
            final byte b0 = buf.readByte();
            i |= (long)(b0 & 0x7F) << j++ * 7;
            if (j > 10) {
                throw new RuntimeException("VarLong too big");
            }
            if ((b0 & 0x80) != 0x80) {
                return i;
            }
        }
    }


    public static void writeUniqueId(ByteBuf buf, final UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }


    public static UUID readUniqueId(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }


    public static void writeVarInt(ByteBuf buf, int input) {
        while ((input & 0xFFFFFF80) != 0x0) {
            buf.writeByte((input & 0x7F) | 0x80);
            input >>>= 7;
        }
        buf.writeByte(input);
    }


    public static void writeVarLong(ByteBuf buf, long value) {
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0x0L) {
            buf.writeByte((int)(value & 0x7FL) | 0x80);
            value >>>= 7;
        }
        buf.writeByte((int)value);
    }


    public static void writeCompoundTag(ByteBuf buf, @Nullable final NBTTagCompound nbt) {
        if (nbt == null) {
            buf.writeByte(0);
        } else {
            try {
                CompressedStreamTools.write(nbt, new ByteBufOutputStream(buf));
            } catch (IOException ioexception) {
                throw new EncoderException(ioexception);
            }
        }
    }


    @Nullable
    public static NBTTagCompound readCompoundTag(ByteBuf buf) {
        final int i = buf.readerIndex();
        final byte b0 = buf.readByte();
        if (b0 == 0) {
            return null;
        }
        buf.readerIndex(i);
        try {
            return CompressedStreamTools.read(new ByteBufInputStream(buf), new NBTSizeTracker(2097152L));
        } catch (IOException ioexception) {
            throw new EncoderException(ioexception);
        }
    }


    public static void writeItemStack(ByteBuf buf, final ItemStack stack) {
        if (stack.isEmpty()) {
            buf.writeShort(-1);
        }
        else {
            buf.writeShort(Item.getIdFromItem(stack.getItem()));
            buf.writeByte(stack.getCount());
            buf.writeShort(stack.getMetadata());
            NBTTagCompound nbttagcompound = null;
            if (stack.getItem().isDamageable() || stack.getItem().getShareTag()) {
                nbttagcompound = stack.getItem().getNBTShareTag(stack);
            }
            writeCompoundTag(buf, nbttagcompound);
        }
    }


    public static ItemStack readItemStack(ByteBuf buf) throws IOException {
        final int i = buf.readShort();
        if (i < 0) {
            return ItemStack.EMPTY;
        }
        final int j = buf.readByte();
        final int k = buf.readShort();
        final ItemStack itemstack = new ItemStack(Item.getItemById(i), j, k);
        itemstack.getItem().readNBTShareTag(itemstack, readCompoundTag(buf));
        return itemstack;
    }


    public static String readString(ByteBuf buf, final int maxLength) {
        final int i = readVarInt(buf);
        if (i > maxLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
        }
        if (i < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        final String s = buf.toString(buf.readerIndex(), i, StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + i);
        if (s.length() > maxLength) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")");
        }
        return s;
    }


    public static void writeString(ByteBuf buf, final String string) {
        final byte[] abyte = string.getBytes(StandardCharsets.UTF_8);
        if (abyte.length > 32767) {
            throw new EncoderException("String too big (was " + abyte.length + " bytes encoded, max " + 32767 + ")");
        }
        writeVarInt(buf, abyte.length);
        buf.writeBytes(abyte);
    }


    public static ResourceLocation readResourceLocation(ByteBuf buf) {
        return new ResourceLocation(readString(buf, 32767));
    }


    public static void writeResourceLocation(ByteBuf buf, final ResourceLocation resourceLocationIn) {
        writeString(buf, resourceLocationIn.toString());
    }


    public static Date readTime(ByteBuf buf) {
        return new Date(buf.readLong());
    }


    public static void writeTime(ByteBuf buf, final Date time) {
        buf.writeLong(time.getTime());
    }
}
