package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.FastByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import plus.fserver.ElcaConfig;
import plus.fserver.server.network.NetUtils;
import plus.fserver.server.network.packet.SNodePacketPlayBase;
import static io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess.UNSAFE;


public class SPacketChunkData extends SNodePacketPlayBase {
    private static final long tempShift;

    private final static Node[] temp;
    private static int cursor = 0;

    private static class Node{
        volatile byte[] temp;
        @Nullable
        byte[] tryGet(int minSize){
            byte[] temp = this.temp;
            if(temp == null || temp.length < minSize)return null;
            if(UNSAFE.compareAndSwapObject(this, tempShift, temp, null)){
                return temp;
            }
            return null;
        }
        boolean tryFree(byte[] src){
            byte[] temp = this.temp;
            if(temp == null || temp.length < src.length) {
                return UNSAFE.compareAndSwapObject(this, tempShift, temp, src);
            }
            return false;
        }
    }

    private int chunkX;
    private int chunkZ;
    private int availableSections;
    private byte[] buffer;
    private int size;
    private List<NBTTagCompound> tileEntityTags;
    private boolean fullChunk;

    static {
        temp = new Node[ElcaConfig.CACHE_CHUNK_DATA];
        for (int i = 0; i < temp.length; i++){
            temp[i] = new Node();
        }
        try {
            tempShift = UNSAFE.objectFieldOffset(SPacketChunkData.Node.class.getDeclaredField("temp"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    public static void freeMemory(){
        for (Node node:temp)node.temp = null;
    }


    private static byte[] alloc(int minSize) {
        Node[] nodes = temp;
        for (int i = cursor, s = nodes.length; i < s; i++){
            Node node = nodes[i];
            byte[] result;
            if((result = node.tryGet(minSize)) != null){
                cursor = i;
                return result;
            }
        }
        for (int i = 0, s = cursor; i < s; i++){
            Node node = nodes[i];
            byte[] result;
            if((result = node.tryGet(minSize)) != null){
                cursor = i;
                return result;
            }
        }
        return new byte[minSize];
    }


    private static void free(byte[] src){
        Node[] nodes = temp;
        for (int i = cursor, s = nodes.length; i < s; i++) {
            Node node = nodes[i];
            if(node.tryFree(src)){
                cursor = i;
                return;
            }
        }
        for (int i = 0, s = cursor; i < s; i++) {
            Node node = nodes[i];
            if(node.tryFree(src)){
                cursor = i;
                return;
            }
        }
    }


    public SPacketChunkData() {}


    public SPacketChunkData(Chunk chunk, int mask) {
        this(chunk, mask, false);
    }


    public SPacketChunkData(Chunk chunk, int mask, boolean pooled) {
        this.chunkX = chunk.x;
        this.chunkZ = chunk.z;
        boolean hasSkyLight = chunk.getWorld().provider.hasSkyLight();

        int size = calculateChunkSize(chunk, hasSkyLight, mask, fullChunk = mask == 65535);

        byte[] bytes;
        if(pooled){
            this.size = size;
            bytes = alloc(size);
        } else {
            bytes = new byte[size];
            this.size = -1;
        }

        this.availableSections = extractChunkData(new FastByteBuf(buffer = bytes),
            chunk, hasSkyLight, mask, fullChunk
        );

        this.tileEntityTags = Lists.newArrayList();

        Iterator var4 = chunk.getTileEntityMap().entrySet().iterator();

        while(true) {
            TileEntity tile;
            int section;
            do {
                if (!var4.hasNext()) {
                    return;
                }

                Map.Entry<BlockPos, TileEntity> entry = (Map.Entry)var4.next();
                BlockPos pos = entry.getKey();
                tile = entry.getValue();
                section = pos.getY() >> 4;
            } while(!this.isFullChunk() && (mask & 1 << section) == 0);

            NBTTagCompound tag = tile.getUpdateTag();
            this.tileEntityTags.add(tag);
        }
    }


    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.chunkX = buf.readInt();
        this.chunkZ = buf.readInt();
        this.fullChunk = buf.readBoolean();
        this.availableSections = buf.readVarInt();
        int len = buf.readVarInt();
        if (len > 2097152) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        } else {
            this.buffer = new byte[len];
            buf.readBytes(this.buffer);
            int lvt_3_1_ = buf.readVarInt();
            this.tileEntityTags = Lists.newArrayList();

            for(int i = 0; i < lvt_3_1_; ++i) {
                this.tileEntityTags.add(buf.readCompoundTag());
            }
        }
    }


    @Deprecated
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeInt(this.chunkX);
        buf.writeInt(this.chunkZ);
        buf.writeBoolean(this.fullChunk);
        buf.writeVarInt(this.availableSections);

        byte[] bytes = this.buffer;
        int size;
        if((size = this.size) == -1){
            buf.writeVarInt(bytes.length);
            buf.writeBytes(bytes,0, bytes.length);
        } else {
            buf.writeVarInt(size);
            buf.writeBytes(bytes,0, size);
            free(bytes);
        }

        buf.writeVarInt(this.tileEntityTags.size());

        Iterator iter = this.tileEntityTags.iterator();

        while(iter.hasNext()) {
            NBTTagCompound tag = (NBTTagCompound)iter.next();
            buf.writeCompoundTag(tag);
        }
    }


    @Override
    public void writePacketData(int id, ByteBuf buf) throws IOException {
        NetUtils.writeVarInt(buf, id);

        buf.writeInt(this.chunkX);
        buf.writeInt(this.chunkZ);

        buf.writeBoolean(this.fullChunk);
        NetUtils.writeVarInt(buf, this.availableSections);

        byte[] bytes = this.buffer;
        int size;
        if((size = this.size) == -1){
            NetUtils.writeVarInt(buf, bytes.length);
            buf.writeBytes(bytes,0, bytes.length);
        } else {
            NetUtils.writeVarInt(buf, size);
            buf.writeBytes(bytes,0, size);
            free(bytes);
        }

        NetUtils.writeVarInt(buf, this.tileEntityTags.size());

        Iterator iter = this.tileEntityTags.iterator();

        while(iter.hasNext()) {
            NBTTagCompound tag = (NBTTagCompound)iter.next();
            NetUtils.writeCompoundTag(buf, tag);
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient pCli) {
        pCli.handleChunkData(this);
        if(size == -1)return;
        free(buffer);
        buffer = null;
    }


    @SideOnly(Side.CLIENT)
    public PacketBuffer getReadBuffer() {
        return new PacketBuffer(Unpooled.wrappedBuffer(this.buffer));
    }


    private ByteBuf getWriteBuffer() {
        ByteBuf buf = Unpooled.wrappedBuffer(this.buffer);
        buf.writerIndex(0);
        return buf;
    }


    private static int extractChunkData(FastByteBuf buf, Chunk chunk, boolean hasSkyLight, int mask, boolean fullChunk) {
        int resultMask = 0;
        ExtendedBlockStorage[] arrays = chunk.getBlockStorageArray();

        for(int index = 0, len = arrays.length; index < len; ++index) {
            ExtendedBlockStorage storage = arrays[index];
            if (storage != Chunk.NULL_BLOCK_STORAGE && (!fullChunk || !storage.isEmpty()) && (mask & 1 << index) != 0) {
                resultMask |= 1 << index;
                storage.write(buf);
                storage.writeBlockLightTo(buf);
                //buf.writeBytes(storage.getBlockLight().getData());
                if (hasSkyLight) {
                    storage.writeSkyLightTo(buf);
                    //buf.writeBytes(storage.getSkyLight().getData());
                }
            }
        }

        if (fullChunk) {
            buf.writeBytes(chunk.getBiomeArray());
        }
        return resultMask;
    }


    private static int calculateChunkSize(Chunk chunk, boolean hasSkyLight, int mask, boolean isFull) {
        //light engine
        //chunk.getLightingEngine().processLightUpdates();

        int size = 0;
        ExtendedBlockStorage[] arrays = chunk.getBlockStorageArray();

        for(int index = 0, len = arrays.length; index < len; ++index) {
            ExtendedBlockStorage array = arrays[index];
            if (array != Chunk.NULL_BLOCK_STORAGE && (!isFull || !array.isEmpty()) && (mask & 1 << index) != 0) {
                size += array.getSerializedSize();
                size += 2048;
                if (hasSkyLight) {
                    size += 2048;
                }
            }
        }

        if (isFull) {
            size += chunk.getBiomeArray().length;
        }
        return size;
    }


    @SideOnly(Side.CLIENT)
    public int getChunkX() {
        return this.chunkX;
    }


    @SideOnly(Side.CLIENT)
    public int getChunkZ() {
        return this.chunkZ;
    }


    @SideOnly(Side.CLIENT)
    public int getExtractedSize() {
        return this.availableSections;
    }


    public boolean isFullChunk() {
        return this.fullChunk;
    }


    @SideOnly(Side.CLIENT)
    public List<NBTTagCompound> getTileEntityTags() {
        return this.tileEntityTags;
    }
}
