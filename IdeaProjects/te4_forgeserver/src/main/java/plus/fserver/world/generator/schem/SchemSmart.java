package plus.fserver.world.generator.schem;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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
import plus.tson.utl.Tuple;


public class SchemSmart extends SchemNormal{
    SchemSmart(Tuple<Stolb[], EntityInfo[]> tuple) {
        super(tuple);
    }


    public static ITSchem read(NBTTagCompound nbt) {
        return new SchemReader().read(nbt).toSchem(SchemSmart::new, SchemNormal::valid);
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
                    //blockPos.setY(by);

                    if(by<255 && by>0){
                        IBlockState state = inf.state.withMirror(mirror).withRotation(rotation);
                        gen.setBlockSafe(blockPos.getX(), by, blockPos.getZ(), state);

                        if(inf.y == 0){
                            int curY = y-1;
                            int bx = blockPos.getX();
                            int bz = blockPos.getZ();
                            while (curY>0 && mayBeReplaced(gen.getBlock(bx,curY,bz))){
                                gen.setBlockSafe(bx, curY, bz, state);
                                --curY;
                            }
                        }//TODO
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

                        if(inf.y == 0){
                            int curY = y-1;
                            blockPos.setY(curY);
                            while (curY>0 && mayBeReplaced(chunk.getBlockState(blockPos))){
                                chunk.setBlockState(blockPos,state);
                                --curY;
                                blockPos.setY(curY);
                            }
                        }

                        if(inf.tileEntityData != null){
                            blockPos.setY(by);
                            TileEntity tileEntity = world.getTileEntity(blockPos);
                            if (tileEntity != null) {
                                inf.tileEntityData.setIntegerU("x", blockPos.getX());
                                inf.tileEntityData.setIntegerU("y", by);
                                inf.tileEntityData.setIntegerU("z", blockPos.getZ());
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


    protected boolean mayBeReplaced(IBlockState state){
        Block b = state.getBlock();
        //return !state.isFullCube();
        return b == Blocks.AIR || b == Blocks.GRASS || b == Blocks.TALLGRASS || b == Blocks.FLOWING_WATER ||
                b == Blocks.FLOWING_LAVA || b == Blocks.WATER || b == Blocks.SNOW_LAYER ||
                b == Blocks.RED_FLOWER || b == Blocks.YELLOW_FLOWER || b == Blocks.SNOW ||
                b == Blocks.SAND || b == Blocks.DIRT || b == Blocks.GRASS_PATH || b == Blocks.LAVA;
    }


    @Override
    public String toString() {
        return "--"+super.toString();
    }
}