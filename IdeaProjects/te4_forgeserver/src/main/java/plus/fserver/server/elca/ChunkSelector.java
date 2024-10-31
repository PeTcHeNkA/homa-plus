package plus.fserver.server.elca;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.jetbrains.annotations.Nullable;
import plus.fserver.api.IBlockEAccess;


public class ChunkSelector implements IBlockEAccess {
    private ChunkProviderServer provider;
    private Chunk chunk;
    private int curX, curZ;

    protected ChunkSelector(){}

    public ChunkSelector(Chunk chunk){
        this.provider = ((WorldServer)chunk.getWorld()).getChunkProvider();
        this.chunk = chunk;curX = chunk.x;curZ = chunk.z;
    }


    public void define(ChunkProviderServer provider){
        this.provider = provider;
        this.chunk = null;
        this.curX = this.curZ = Integer.MIN_VALUE;
    }


    public void release(){
        this.provider = null;
        this.chunk = null;
        this.curX = this.curZ = Integer.MIN_VALUE;
    }


    public ChunkProviderServer provider(){
        return provider;
    }


    public void setChunk(Chunk chunk){
        this.chunk = chunk;curX = chunk.x;curZ = chunk.z;
    }


    public void clear(){
        this.chunk = null;
        curX = curZ = Integer.MIN_VALUE;
    }


    @Override
    public WorldServer getWorld(){
        return provider.world;
    }


    public ChunkSelector(World world) {
        this.provider = ((WorldServer)world).getChunkProvider();
        curX = curZ = Integer.MIN_VALUE;
    }


    public ChunkSelector(WorldServer world) {
        this.provider = world.getChunkProvider();
        curX = curZ = Integer.MIN_VALUE;
    }


    public Chunk getChunk(final int x, final int z){
        return getChunkE(x>>4, z>>4);
    }


    public Chunk getChunk(final BlockPos pos){
        return getChunkE(pos.getX()>>4, pos.getZ()>>4);
    }


    public Chunk current(){
        return chunk;
    }


    public Chunk getChunkE(final int x, final int z){
        if(curX == x && curZ == z)return chunk;
        return chunk = provider.provideChunk(curX = x, curZ = z);
    }


    public Chunk getChunkI(final int x, final int z){
        return chunk = provider.provideChunk(curX = x, curZ = z);
    }


    public Chunk getLoadedE(final int x, final int z){
        if(curX == x && curZ == z)return chunk;
        Chunk temp = provider.getLoadedChunk(x, z);
        if(temp == null)return null;
        this.curX = x; this.curZ = z;
        return chunk = temp;
    }


    @Override
    public IBlockState getBlockState(int x, int y, int z){
        return getChunkE(x>>4, z>>4).getBlockState(x, y, z);
    }


    public int getPrecipitationHeight(int x, int z) {
        return getChunkE(x>>4, z>>4).getPrecipitationHeightY(x, z);
    }


    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return provider.world.getTileForAcces(this, pos);
    }


    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return provider.world.getCombinedLight(pos, lightValue);
    }


    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        if(pos.getY() < 0 || pos.getY() > 255)return type.defaultLightValue;
        Chunk chunk1 = getLoadedE(pos.getX()>>4, pos.getZ()>>4);
        return chunk1 == null?type.defaultLightValue:chunk1.getLightFor(type, pos);
    }


    public int getLightFor(EnumSkyBlock type, int x, int y, int z) {
        if(y < 0 || y > 255)return type.defaultLightValue;
        Chunk chunk1 = getLoadedE(x>>4, z>>4);
        return chunk1 == null?type.defaultLightValue:chunk1.getLightFor(type, x, y, z);
    }


    public IBlockState getState(BlockPos pos){
        return getChunkE(pos.getX()>>4, pos.getZ()>>4).getBlockState(pos);
    }


    public IBlockState getState(int x, int y, int z){
        return getChunkE(x>>4, z>>4).getBlockState(x, y, z);
    }


    public IBlockState getStateUnchecked(int x, int y, int z){
        return getChunkE(x>>4, z>>4).getBlockStateUnchecked(x, y, z);
    }


    @Override
    public IBlockState getBlockState(BlockPos pos) {
        //if(pos.getY()<0||pos.getY()>255)return BlockIDIndexes.AIR_STATE;
        return getChunkE(pos.getX()>>4, pos.getZ()>>4).getBlockState(pos);
    }


    public IBlockState getCurState(BlockPos pos){
        return chunk.getBlockState(pos);
    }


    public IBlockState getCurState(int x, int y, int z){
        return chunk.getBlockState(x, y, z);
    }


    public IBlockState getCurStateUnchecked(int x, int y, int z){
        return chunk.getBlockStateUnchecked(x, y, z);
    }


    @Override
    public boolean isAirBlock(BlockPos pos){
        IBlockState state = getChunkE(pos.getX()>>4, pos.getZ()>>4).getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }


    public boolean isCurAir(BlockPos pos){
        IBlockState state = chunk.getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }


    public TileEntity getTile(BlockPos pos){
        return getChunk(pos.getX()>>4, pos.getZ()>>4).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
    }


    public void setState(BlockPos pos, IBlockState state){
        getChunkE(pos.getX()>>4, pos.getZ()>>4).setBlockState(pos, state);
    }


    public void setCurState(BlockPos pos, IBlockState state){
        chunk.setBlockState(pos, state);
    }


    public void setCurState(BlockPos.MutableBlockPos pos, IBlockState state){
        chunk.setBlockState(pos, state);
    }


    public boolean setStateSafe(BlockPos pos, IBlockState state){
        if(pos.getY()>255 || pos.getY() <1)return false;
        getChunkE(pos.getX()>>4, pos.getZ()>>4).setBlockState(pos, state);
        return true;
    }


    public void setCurStateFast(int x, int y, int z, IBlockState current, IBlockState newState){
        chunk.setBlockStateFast(x, y, z, current, newState);
    }


    public void setStateFast(int x, int y, int z, IBlockState newState){
        getChunk(x>>4, z>>4).setBlockStateFastNoTile(x, y, z, newState);
    }


    public void setCurStateFast(int x, int y, int z, IBlockState newState){
        chunk.setBlockStateFastNoTile(x, y, z, newState);
    }


    public boolean isAreaLoaded3(int x, int z){
        ChunkProviderServer provider = this.provider;
        int tx, tz;
        if(curX != x || curZ != z) {
            Chunk chunk = provider.getLoadedChunk(curX = tx = x >> 4, curZ = tz = z >> 4);
            if (chunk == null) return false;
            this.chunk = chunk;
        } else {
            tx = curX;
            tz = curZ;
        }
        if((x + 3)>>4 != tx || ((z + 3)>>4) != tz){
            if(!provider.chunkExists((x + 3)>>4, (z + 3)>>4))return false;
        }
        if((x - 3)>>4 != tx || ((z - 3)>>4) != tz){
            if(!provider.chunkExists((x - 3)>>4, (z - 3)>>4))return false;
        }
        return true;
    }


    public boolean isAreaLoaded16_A(int x, int z){
        x = x>>4; z = z>>4;
        return
                provider.chunkExists(x+1, z+1) &&
                provider.chunkExists(x-1, z-1) &&
                provider.chunkExists(x+1, z-1) &&
                provider.chunkExists(x-1, z+1);
    }


    public boolean isAreaLoaded16_B(int x, int z){
        x = x>>4; z = z>>4;
        return
                provider.chunkExists(   x  ,    z  ) &&
                provider.chunkExists(x+1, z+1) &&
                provider.chunkExists(x-1, z-1) &&
                provider.chunkExists(x+1, z-1) &&
                provider.chunkExists(x-1, z+1);
    }


    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState) {
        return setBlockState(pos, newState, 2);
    }


    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        int px, py, pz;
        if ((py = pos.getY()) > 255 || py < 1) return false;
        Chunk chunk = getChunkE((px = pos.getX())>>4, (pz = pos.getZ())>>4);
        IBlockState oldState = chunk.getBlockStateUnchecked(px, py, pz);

        if (newState.getLightOpacity(this, pos) != oldState.getLightOpacity(this, pos) ||
                newState.getLightValue(this, pos) != oldState.getLightValue(this, pos)) {
            chunk.setBlockState(pos, newState);
            provider.world.checkLight(pos);
        } else {
            chunk.setBlockStateFast(px, py, pz, oldState, newState);
        }
        provider.world.markAndNotifyBlock(pos, chunk, oldState, newState, flags);
        return true;
    }


    public boolean setBlockState(BlockPos.MutableBlockPos pos, IBlockState newState, int flags) {
        int px, py, pz;
        if ((py = pos.getY()) > 255 || py < 1) return false;
        Chunk chunk = getChunkE((px = pos.getX())>>4, (pz = pos.getZ())>>4);
        IBlockState oldState = chunk.getBlockStateUnchecked(px, py, pz);

        if (newState.getLightOpacity(this, pos) != oldState.getLightOpacity(this, pos) ||
                newState.getLightValue(this, pos) != oldState.getLightValue(this, pos)) {
            chunk.setBlockState(pos, newState);
            provider.world.checkLight(pos);
        } else {
            chunk.setBlockStateFast(px, py, pz, oldState, newState);
        }
        provider.world.markAndNotifyBlock(pos, chunk, oldState, newState, flags);
        return true;
    }


    @Override
    public Biome getBiome(BlockPos pos) {
        Chunk chunk = getChunkE(pos.getX()>>4, pos.getZ()>>4);
        return chunk.getBiome(pos, provider.world.getBiomeProvider());
    }



    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return getState(pos).getStrongPower(this, pos, direction);
    }


    @Override
    public WorldType getWorldType() {
        return provider.world.getWorldType();
    }


    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        if (pos.getY() < 0 || pos.getY() >= 256) return _default;
        Chunk chunk = getChunk(pos);
        if (chunk.isEmpty()) return _default;
        return getCurState(pos).isSideSolid(this, pos, side);
    }


    public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
        Chunk chunk = getChunkE(pos.getX()>>4, pos.getZ()>>4);
        BlockPos pos1, pos2;
        IBlockState state;

        for (pos1 = new BlockPos(pos.getX(),
                chunk.getTopFilledSegment() + 16, pos.getZ()); pos1.getY() >= 0; pos1 = pos2) {

            if ((state = chunk.getBlockState(pos2 = pos1.down())).getMaterial().blocksMovement()
                    && !state.getBlock().isLeaves(state, this, pos2)
                    && !state.getBlock().isFoliage(this, pos2)) {
                break;
            }
        }
        return pos1;
    }


    public boolean isBlockFullCube(BlockPos pos) {
        if(pos.getY()<0||pos.getY()>255)return false;
        AxisAlignedBB axisalignedbb = getChunkE(pos.getX()>>4, pos.getZ()>>4).getBlockState(pos).getCollisionBoundingBox(this, pos);
        return axisalignedbb != Block.NULL_AABB && axisalignedbb.getAverageEdgeLength() >= 1.0D;
    }


    public int getHeight(int x, int z){
        return getChunkE(x >> 4, z >> 4).getHeightValue(x & 15, z & 15);
    }


    public int getHeightY(BlockPos pos) {
        return getHeight(pos.getX(), pos.getZ());
    }


    public BlockPos getHeight(BlockPos pos) {
        return new BlockPos(pos.getX(), getHeight(pos.getX(), pos.getZ()), pos.getZ());
    }


    public BlockPos getHeightMut(BlockPos pos) {
        BlockPos.unsafeEdit(pos, pos.getX(), getHeight(pos.getX(), pos.getZ()), pos.getZ());
        return pos;
    }
}