package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class SPacketChunkData implements Packet<INetHandlerPlayClient> {
    private int chunkX;
    private int chunkZ;
    private int availableSections;
    private byte[] buffer;
    private List<NBTTagCompound> tileEntityTags;
    private boolean fullChunk;

    public SPacketChunkData() {}


    public SPacketChunkData(Chunk chunk, int mask) {
        this.chunkX = chunk.x;
        this.chunkZ = chunk.z;
        boolean hasSkyLight = chunk.getWorld().provider.hasSkyLight();

        this.availableSections = extractChunkData(
            Unpooled.wrappedBuffer(this.buffer = new byte[
                calculateChunkSize(chunk, hasSkyLight, mask, fullChunk = mask == 65535)
            ]).writerIndex(0),
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


    @Override
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeInt(this.chunkX);
        buf.writeInt(this.chunkZ);
        buf.writeBoolean(this.fullChunk);
        buf.writeVarInt(this.availableSections);
        buf.writeVarInt(this.buffer.length);
        buf.writeBytes(this.buffer);
        buf.writeVarInt(this.tileEntityTags.size());

        Iterator iter = this.tileEntityTags.iterator();

        while(iter.hasNext()) {
            NBTTagCompound tag = (NBTTagCompound)iter.next();
            buf.writeCompoundTag(tag);
        }
    }


    @Override
    public void processPacket(INetHandlerPlayClient pCli) {
        pCli.handleChunkData(this);
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


    public int extractChunkData(PacketBuffer buf, Chunk chunk, boolean hasSkyLight, int mask) {
        int resultMask = 0;
        ExtendedBlockStorage[] arrays = chunk.getBlockStorageArray();

        for(int index = 0, len = arrays.length; index < len; ++index) {
            ExtendedBlockStorage storage = arrays[index];
            if (storage != Chunk.NULL_BLOCK_STORAGE && (!this.isFullChunk() || !storage.isEmpty()) && (mask & 1 << index) != 0) {
                resultMask |= 1 << index;
                storage.getData().write(buf);
                buf.writeBytes(storage.getBlockLight().getData());
                if (hasSkyLight) {
                    buf.writeBytes(storage.getSkyLight().getData());
                }
            }
        }

        if (this.isFullChunk()) {
            buf.writeBytes(chunk.getBiomeArray());
        }
        return resultMask;
    }


    protected int calculateChunkSize(Chunk chunk, boolean hasSkyLight, int mask) {
        int size = 0;
        ExtendedBlockStorage[] arrays = chunk.getBlockStorageArray();

        for(int index = 0, len = arrays.length; index < len; ++index) {
            ExtendedBlockStorage array = arrays[index];
            if (array != Chunk.NULL_BLOCK_STORAGE && (!this.isFullChunk() || !array.isEmpty()) && (mask & 1 << index) != 0) {
                size += array.getData().getSerializedSize();
                size += array.getBlockLight().getData().length;
                if (hasSkyLight) {
                    size += array.getSkyLight().getData().length;
                }
            }
        }

        if (this.isFullChunk()) {
            size += chunk.getBiomeArray().length;
        }
        return size;
    }


    private static int extractChunkData(ByteBuf buf, Chunk chunk, boolean hasSkyLight, int mask, boolean fullChunk) {
        int resultMask = 0;
        ExtendedBlockStorage[] arrays = chunk.getBlockStorageArray();

        for(int index = 0, len = arrays.length; index < len; ++index) {
            ExtendedBlockStorage storage = arrays[index];
            if (storage != Chunk.NULL_BLOCK_STORAGE && (!fullChunk || !storage.isEmpty()) && (mask & 1 << index) != 0) {
                resultMask |= 1 << index;
                storage.getData().write(buf);
                buf.writeBytes(storage.getBlockLight().getData());
                if (hasSkyLight) {
                    buf.writeBytes(storage.getSkyLight().getData());
                }
            }
        }

        if (fullChunk) {
            buf.writeBytes(chunk.getBiomeArray());
        }
        return resultMask;
    }


    private static int calculateChunkSize(Chunk chunk, boolean hasSkyLight, int mask, boolean isFull) {
        int size = 0;
        ExtendedBlockStorage[] arrays = chunk.getBlockStorageArray();

        for(int index = 0, len = arrays.length; index < len; ++index) {
            ExtendedBlockStorage array = arrays[index];
            if (array != Chunk.NULL_BLOCK_STORAGE && (!isFull || !array.isEmpty()) && (mask & 1 << index) != 0) {
                size += array.getData().getSerializedSize();
                size += array.getBlockLight().getData().length;
                if (hasSkyLight) {
                    size += array.getSkyLight().getData().length;
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
