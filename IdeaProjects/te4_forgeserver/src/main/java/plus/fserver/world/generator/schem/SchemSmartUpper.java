package plus.fserver.world.generator.schem;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import plus.fserver.world.generator.settings.SettingsFolder;
import plus.fserver.world.generator.vr.MultiChunkCache;
import plus.tson.TsonMap;
import plus.tson.utl.Tuple;


public class SchemSmartUpper extends SchemSmart{
    protected final int ySize;
    protected final int limit;
    SchemSmartUpper(Tuple<Stolb[], EntityInfo[]> tuple, TsonMap map) {
        super(tuple);
        int yMax = 0;
        for(Stolb s:stolbs){
            for(StolbInfo inf: s.blocks){
                if(inf.y>yMax){
                    yMax = inf.y;
                }
            }
        }
        ySize = yMax;
        final int[] lim = {10};
        map.ifContainsInt("limit", integer -> lim[0] = integer);
        limit = lim[0];
    }


    public static ITSchem read(NBTTagCompound nbt, TsonMap map) {
        class Helper{
            private final TsonMap map;
            Helper(TsonMap map) {
                this.map = map;
            }
            SchemSmartUpper create(Tuple<Stolb[], EntityInfo[]> tuple){
                return new SchemSmartUpper(tuple,map);
            }
        }
        return new SchemReader().read(nbt).toSchem(new Helper(map)::create, SchemNormal::valid);
    }


    @Override//UPDATED
    public void placeInGen(MultiChunkCache gen, int x, int y, int z, SettingsFolder settingsFolder) {
        PlacementSettings settings = settingsFolder.get();
        if(stolbs.length>0){
            Rotation rotation = settings.getRotation();
            Mirror mirror = settings.getMirror();

            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            for(Stolb stolb:stolbs){
                transformedBlockPos(stolb.x-senX, stolb.z-senZ, mirror, rotation, blockPos, x, y, z);

                for(StolbInfo inf:stolb.blocks){
                    int by = inf.y+y;

                    if(by<255 && by>0){
                        IBlockState state = inf.state.withMirror(mirror).withRotation(rotation);
                        gen.setBlockSafe(blockPos.getX(), by, blockPos.getZ(), state);
                        if(inf.y == ySize){
                            ++by;
                            int bx = blockPos.getX();
                            int bz = blockPos.getZ();
                            int maxY=0;
                            while (maxY<limit && by<255 && mayBeReplaced(gen.getBlock(bx,by,bz))){
                                gen.setBlockSafe(bx, by, bz, state);
                                ++by;
                                ++maxY;
                            }
                        }
//TODO
//                        if(inf.tileEntityData != null){
//                            TileEntity tileEntity = gen.world.getTileEntity(blockpos);
//                            if (tileEntity != null) {
//                                inf.tileEntityData.setInteger("x", blockpos.getX());
//                                inf.tileEntityData.setInteger("y", by);
//                                inf.tileEntityData.setInteger("z", blockpos.getZ());
//                                tileEntity.readFromNBT(inf.tileEntityData);
//                                tileEntity.mirror(mirror);
//                                tileEntity.rotate(rotation);
//                                tileEntity.markDirty();
//                            }
//                        }
                    }
                }
            }
        }
//        if (!settings.getIgnoreEntities() && this.entites != null) {
//            this.addEntitiesToWorld(gen.world, x,y,z, settings.getMirror(),
//                    settings.getRotation(), null);
//        }
    }


    @Override//UPDATED
    public void build(World world, int x, int y, int z, SettingsFolder settingsFolder) {
        PlacementSettings settings = settingsFolder.get(world.rand);
        if(stolbs.length>0){
            Rotation rotation = settings.getRotation();
            Mirror mirror = settings.getMirror();
            int prewReqChunkX = Integer.MIN_VALUE;
            int prewReqChunkZ = Integer.MIN_VALUE;

            Chunk chunk = null;

            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            for(Stolb stolb:stolbs){
                transformedBlockPos(stolb.x-senX, stolb.z-senZ, mirror, rotation, blockPos, x, y, z);

                int reqX = (blockPos.getX())>>4;
                int reqZ = (blockPos.getZ())>>4;

                if(reqX != prewReqChunkX || reqZ != prewReqChunkZ){
                    prewReqChunkX = reqX;
                    prewReqChunkZ = reqZ;
                    chunk = world.getChunk(reqX, reqZ);
                }
                for(StolbInfo inf:stolb.blocks){
                    int by = inf.y+y;
                    blockPos.setY(by);

                    if(by<255 && by>0){
                        IBlockState state = inf.state.withMirror(mirror).withRotation(rotation);
                        chunk.setBlockState(blockPos, state);

                        if(inf.y == ySize){
                            ++by;
                            blockPos.setY(by);
                            int maxY=0;
                            while (maxY<limit && by<255 && mayBeReplaced(chunk.getBlockState(blockPos))){
                                chunk.setBlockState(blockPos,state);
                                ++by;
                                blockPos.setY(by);
                                ++maxY;
                            }
                        }

                        if(inf.tileEntityData != null){
                            blockPos.setY(inf.y+y);
                            TileEntity tileEntity = world.getTileEntity(blockPos);
                            if (tileEntity != null) {
                                inf.tileEntityData.setInteger("x", blockPos.getX());
                                inf.tileEntityData.setInteger("y", blockPos.getY());
                                inf.tileEntityData.setInteger("z", blockPos.getZ());
                                tileEntity.readFromNBT(inf.tileEntityData);
                                tileEntity.mirror(mirror);
                                tileEntity.rotate(rotation);
                                tileEntity.markDirty();
                            }
                        }
                    }
                }
            }
        }

        if (!settings.getIgnoreEntities() && this.entites != null) {
            this.addEntitiesToWorld(world, x,y,z, settings.getMirror(),
                    settings.getRotation(), null);
        }
    }


    @Override
    public String toString() {
        return "-?"+super.toString();
    }
}