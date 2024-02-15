package plus.fserver.utls;

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


public class ChunkLCache implements IBlockEAccess {
    ChunkLCache xAdd;
    ChunkLCache xMin;
    ChunkLCache zAdd;
    ChunkLCache zMin;
    Chunk chunk;
    final int x, z;
    final World world;

    public ChunkLCache(Entity entity){
        this.world = entity.world;
        this.x = ((int)(entity.posX))>>4;
        this.z = ((int)(entity.posZ))>>4;
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
        if(chunk == null) {
            chunk = world.getChunk(x, z);
            if(chunk == null)chunk = EmptyChunk.INSTANCE;
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
        return getExecutor(pos.getX()>>4, pos.getZ()>>4).getChunk().getBlockState(pos);
    }


    @Override
    public boolean isAirBlock(BlockPos pos) {
        if(pos.getY()>255 || pos.getY()<0)return true;
        IBlockState state = getExecutor(pos.getX()>>4, pos.getZ()>>4).getChunk().getBlockState(pos);
        return state.getBlock().isAir(state, world, pos);
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
        pos = pos.toImmutable();

        IBlockState oldState = chunk.getBlockState(pos);
        int oldLight = oldState.getLightValue(world, pos);
        int oldOpacity = oldState.getLightOpacity(world, pos);

        if (newState.getLightOpacity(world, pos) != oldOpacity ||
                newState.getLightValue(world, pos) != oldLight) {
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
