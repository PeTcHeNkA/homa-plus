package net.minecraft.world.chunk;

import io.netty.buffer.FastByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.fserver.ElcaCV;
import java.util.Arrays;


public class BlockStatePaletteHashMap extends BlockStatePaletteBase implements IBlockStatePalette {
    private IBlockState[] stateIndex;
    private int[] intKeys;
    private short nextFreeIndex;
    private int mapSize;
    private final byte bits;

    public BlockStatePaletteHashMap(int bitsIn) {
        int initialCapacity = 1 << bitsIn;
        initialCapacity = (int)((float)initialCapacity / 0.8F);
        this.stateIndex = new IBlockState[initialCapacity];
        this.intKeys = new int[initialCapacity];

        this.states = new IBlockState[initialCapacity];
        this.arraySize = initialCapacity;

        if(ElcaCV.FIX_NULL_HASH_PALETTE)
            Arrays.fill(states, BlockIDIndexes.AIR_STATE);
        this.bits = (byte) bitsIn;
    }


    @Override
    public int idFor(IBlockState state) {
        int i;
        if ((i = getId(state)) == -1) {
            i = add(state);
            if (i >= 1 << this.bits) return -1;
        }
        return i;
    }


    private int getId(IBlockState state) {
        return this.getValue(this.getIndex(state, this.hashObject(state)));
    }


    private int getValue(int num) {
        return num == -1 ? -1 : this.intKeys[num];
    }


    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer buf) {
        clear();
        int i = buf.readVarInt();
        for (int j = 0; j < i; ++j) {
            add(BlockIDIndexes.BLOCK_INDEX.get(buf.readVarInt()));
        }
    }


    @Override
    public void write(PacketBuffer buf) {
        int i = size();
        buf.writeVarInt(i);
        IBlockState state;
        for (int cursor = 0; cursor < i; ++cursor) {
            state = get(cursor);
            buf.writeVarInt(state.getId());
        }
    }


    @Override
    public void write(FastByteBuf buf) {
        final int i;
        buf.writeVarInt(i = size());
        final IBlockState[] byId = this.states;
        for (int cursor = 0; cursor < i; ++cursor)
            buf.writeVarInt(byId[cursor].getId());
    }


    @Override
    public int getSerializedSize() {
        int i = PacketBuffer.getVarIntSize(size());
        IBlockState state;
        for (int cursor = 0; cursor < size(); ++cursor) {
            state = get(cursor);
            i += PacketBuffer.getVarIntSize(state.getId());
        }
        return i;
    }


    private int add(IBlockState objectIn) {
        int i = this.nextId();
        this.put(objectIn, i);
        return i;
    }


    private int nextId() {
        if(ElcaCV.FIX_NULL_HASH_PALETTE){
            while (this.nextFreeIndex < this.states.length && this.states[this.nextFreeIndex] != BlockIDIndexes.AIR_STATE)
                ++this.nextFreeIndex;
            return this.nextFreeIndex;
        } else {
            while (this.nextFreeIndex < this.states.length && this.states[this.nextFreeIndex] != null)
                ++this.nextFreeIndex;
            return this.nextFreeIndex;
        }
    }


    private void grow(int capacity) {
        IBlockState[] ak = this.stateIndex;
        IBlockState[] byIdn = new IBlockState[capacity];
        if(ElcaCV.FIX_NULL_HASH_PALETTE)
            Arrays.fill(byIdn, BlockIDIndexes.AIR_STATE);
        this.states = byIdn;
        int[] aint = this.intKeys;
        this.stateIndex = new IBlockState[capacity];
        this.arraySize = capacity;
        this.intKeys = new int[capacity];

        this.nextFreeIndex = 0;
        this.mapSize = 0;

        for (int i = 0; i < ak.length; ++i) {
            if (ak[i] != null) {
                this.put(ak[i], aint[i]);
            }
        }
    }


    private void put(IBlockState objectIn, int intKey) {
        int i = Math.max(intKey, this.mapSize + 1);
        if ((float)i >= (float)this.stateIndex.length * 0.8F) {
            int j;
            for (j = this.stateIndex.length << 1; j < intKey; j <<= 1) {
                ;
            }
            this.grow(j);
        }

        int k = this.findEmpty(this.hashObject(objectIn));
        this.stateIndex[k] = objectIn;
        this.intKeys[k] = intKey;
        this.states[intKey] = objectIn;
        ++this.mapSize;
        if (intKey == this.nextFreeIndex) {
            ++this.nextFreeIndex;
        }
    }


    private int hashObject(IBlockState obectIn) {
        return (MathHelper.hash(obectIn.getId()) & Integer.MAX_VALUE) % this.arraySize;
    }


    private int getIndex(IBlockState objectIn, int num) {
        IBlockState[] states = this.stateIndex;
        for (int i = num; i < states.length; ++i) {
            if (states[i] == objectIn) return i;
            if (states[i] == null) return -1;
        }
        for (int j = 0; j < num; ++j) {
            if (states[j] == objectIn) return j;
            if (states[j] == null) return -1;
        }
        return -1;
    }


    private int findEmpty(int num) {
        IBlockState[] states = this.stateIndex;
        for (int i = num; i < states.length; ++i)
            if (states[i] == null) return i;

        for (int j = 0; j < num; ++j)
            if (states[j] == null) return j;

        throw new RuntimeException("Overflowed :(");
    }


    private void clear() {
        Arrays.fill(this.stateIndex, null);
        if(ElcaCV.FIX_NULL_HASH_PALETTE){
            this.nextFreeIndex = 0;
            this.mapSize = 0;
            Arrays.fill(this.states, BlockIDIndexes.AIR_STATE);
        } else {
            Arrays.fill(this.states, null);
            this.nextFreeIndex = 0;
            this.mapSize = 0;
        }
    }


    private int size() {
        return this.mapSize;
    }
}