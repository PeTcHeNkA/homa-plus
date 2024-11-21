package plus.fserver.server.elca;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.IBlockEAccess;
import plus.fserver.utls.EmptyChunk;
import plus.fserver.utls.math.MathUtls;


public class ChunkLCache implements IBlockEAccess {
    ChunkLCache xAdd;
    ChunkLCache xMin;
    ChunkLCache zAdd;
    ChunkLCache zMin;
    Chunk chunk;
    int x, z;
    final World world;

    public ChunkLCache(Entity entity){
        this.world = entity.world;
        this.x = ((int)(entity.posX))>>4;
        this.z = ((int)(entity.posZ))>>4;
    }


    public ChunkLCache(Chunk chunk){
        this.world = chunk.getWorld();
        this.chunk = chunk;
        this.x = chunk.x;
        this.z = chunk.z;
    }


    public void init(Entity entity){
        init(((int)(entity.posX))>>4, ((int)(entity.posZ))>>4);
    }


    public void init(int x, int z){
        if(x != this.x || z != this.z){
            this.chunk = null;
            this.x = x;
            this.z = z;

            if (xAdd != null) {
                xAdd.x = x + 1;
                xAdd.z = z;
                xAdd.cleanAll();
            }
            if (xMin != null) {
                xMin.x = x - 1;
                xMin.z = z;
                xMin.cleanAll();
            }
            if (zAdd != null) {
                zAdd.x = x;
                zAdd.z = z + 1;
                zAdd.cleanAll();
            }
            if (zMin != null) {
                zMin.x = x;
                zMin.z = z - 1;
                zMin.cleanAll();
            }
        }
    }


    public void init(Chunk chunk){
        init(chunk.x, chunk.z);
        this.chunk = chunk;
    }


    public void cleanAll(){
        xAdd = null;
        zAdd = null;
        xMin = null;
        zMin = null;
        chunk = null;
    }


    public ChunkLCache(World world, int x, int z) {
        this.x = x;
        this.z = z;
        this.world = world;
    }


    @Override
    public World getWorld() {
        return world;
    }


    //
//    protected ChunkLCache(World world, int x, int z,
//                          ChunkLCache xAdd, ChunkLCache xMin, ChunkLCache zAdd, ChunkLCache zMin) {
//        this.x = x;
//        this.z = z;
//        this.world = world;
//        this.xAdd = xAdd;this.xMin = xMin;this.zAdd = zAdd;this.zMin = zMin;
//    }


    public ChunkLCache move(int x, int z){
        ChunkLCache executor = getExecutor(this.x+x, this.z+z);

        ChunkLCache prev = this;
        while (x > 0){
            ChunkLCache next = prev.getExecutor(prev.x+1, prev.z);
            next.xMin = prev;
            prev = next;
            --x;
        }
        while (x < 0){
            ChunkLCache next = prev.getExecutor(prev.x-1, prev.z);
            next.xAdd = prev;
            prev = next;
            ++x;
        }
        ChunkLCache shifted = prev;
        while (z > 0){
            ChunkLCache next = prev.getExecutor(prev.x, prev.z+1);
            next.zMin = prev;
            prev = next;
            --z;
        }
        prev = shifted;
        while (z < 0){
            ChunkLCache next = prev.getExecutor(prev.x, prev.z-1);
            next.zMin = prev;
            prev = next;
            ++z;
        }
        return executor;
    }


    public final ChunkLCache getOrInitExecutor(int x, int z, int lim){
        int r1 = MathUtls.range(this.x, x);
        if(r1 > lim || r1 < -lim){
            init(x, z);
            return this;
        }
        r1 = MathUtls.range(this.z, z);
        if(r1 > lim || r1 < -lim){
            init(x, z);
            return this;
        }
        return getExecutor(x, z);
    }


    public final ChunkLCache getExecutor(int rx, int rz){
        if(rx == x){
            if(rz == z)return this;
            return rz > z ?
                    (zAdd == null?zAdd = new ChunkLCache(world, x, z+1):zAdd).getExecutor(rx, rz):
                    (zMin == null?zMin = new ChunkLCache(world, x, z-1):zMin).getExecutor(rx, rz);
        }
        return rx > x ?
                (xAdd == null?xAdd = new ChunkLCache(world, x+1, z):xAdd).getExecutor(rx, rz):
                (xMin == null?xMin = new ChunkLCache(world, x-1, z):xMin).getExecutor(rx, rz);
    }


    public void clear(){
        chunk = null;
        if(xAdd != null)xAdd.clear();
        if(xMin != null)xMin.clear();
        if(zAdd != null)zAdd.clear();
        if(zMin != null)zMin.clear();
    }


    public final Chunk getChunk(){
        Chunk chunk;
        if((chunk = this.chunk) == null) {
            chunk = world.getChunk(x, z);
            if(chunk == null)chunk = this.chunk = EmptyChunk.INSTANCE;
        }
        return chunk;
    }


    public final Chunk getLoadedChunk(){
        Chunk chunk;
        if((chunk = this.chunk) == null) {
            chunk = this.chunk = world.getChunkProvider().getLoadedChunk(x, z);
            return chunk;
        }
        return chunk;
    }


    @Nullable
    @Override
    public TileEntity getTileEntity(@NotNull BlockPos pos) {
        return getExecutor(pos.getX()>>4, pos.getZ()>>4).getChunk().getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
    }


    @SideOnly(Side.CLIENT)
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 15;
    }


    @Override
    public IBlockState getBlockState(BlockPos pos) {
        if(pos.getY()>255 || pos.getY()<0)return BlockIDIndexes.AIR_STATE;
        return getExecutor(pos.getX()>>4, pos.getZ()>>4).getChunk().getBlockStateUnchecked(pos.getX(), pos.getY(), pos.getZ());
    }


    public IBlockState getBlockStateUnchecked(int x, int y, int z) {
        return getExecutor(x>>4, z>>4).getChunk().getBlockStateUnchecked(x, y, z);
    }


    @Override
    public boolean isAirBlock(BlockPos pos) {
        if(pos.getY()>255 || pos.getY()<0)return true;
        IBlockState state = getExecutor(pos.getX()>>4, pos.getZ()>>4).getChunk().getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }


    @SideOnly(Side.CLIENT)
    public Biome getBiome(@NotNull BlockPos pos) {
        return getExecutor(pos.getX()>>4, pos.getZ()>>4).getChunk().getBiome(pos, world.getBiomeProvider());
    }


    @Override
    public int getStrongPower(@NotNull BlockPos pos, @NotNull EnumFacing direction) {
        return 0;
    }


    @SideOnly(Side.CLIENT)
    public WorldType getWorldType() {
        return world.getWorldType();
    }


    @Override
    public boolean isSideSolid(BlockPos pos, @NotNull EnumFacing side, boolean _default) {
        IBlockState state = getExecutor(pos.getX()>>4, pos.getZ()>>4).getChunk().getBlockState(pos);
        return state.getBlock().isSideSolid(state, world, pos, side);
    }


    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        if(y>255 || y<0)return BlockIDIndexes.AIR_STATE;
        return getExecutor(x>>4, z>>4).getChunk().getBlockStateUnchecked(x, y, z);
    }


    public IBlockState getState(BlockPos pos){
        if(pos.getY()>255 || pos.getY()<0)return BlockIDIndexes.AIR_STATE;
        return getExecutor(pos.getX()>>4, pos.getZ()>>4).getChunk().getBlockStateUnchecked(pos.getX(), pos.getY(), pos.getZ());
    }


    public IBlockState getState(int x, int y, int z){
        return getExecutor(x>>4, z>>4).getChunk().getBlockState(x, y, z);
    }


    @Override
    public boolean setBlockState(BlockPos pos, IBlockState state) {
        Chunk chunk = getExecutor(pos.getX()>>4, pos.getZ()>>4).getChunk();
        if(chunk.isEmpty())return false;
        return state == chunk.setBlockState(pos, state);
    }


    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (pos.getY() > 255 || pos.getY() < 1) return false;
        Chunk chunk = getExecutor(pos.getX()>>4, pos.getZ()>>4).getChunk();

        IBlockState oldState = chunk.getBlockStateUnchecked(pos.getX(), pos.getY(), pos.getZ());

        int oldLight = oldState.getLightValue(this, pos);
        int oldOpacity = oldState.getLightOpacity(this, pos);

        if (newState.getLightOpacity(this, pos) != oldOpacity ||
                newState.getLightValue(this, pos) != oldLight) {
            chunk.setBlockState(pos, newState);
            world.checkLight(pos);
        } else {
            chunk.setBlockStateFast(pos.getX(), pos.getY(), pos.getZ(), oldState, newState);
        }

        world.markAndNotifyBlock(pos, chunk, oldState, newState, flags);
        return true;
    }


    @Override
    public String toString() {
        return "ChunkLCache{" +
                "x=" + x +
                ", z=" + z +
                ", world=" + world +
                '}';
    }
}
