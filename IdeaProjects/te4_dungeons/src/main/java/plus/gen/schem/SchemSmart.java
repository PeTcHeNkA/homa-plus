package plus.gen.schem;

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
import plus.gen.settings.SettingsFolder;
import plus.tson.utl.Tuple;
import java.util.Random;


public class SchemSmart extends SchemNormal{
    SchemSmart(Tuple<Stolb[], EntityInfo[]> tuple) {
        super(tuple);
    }


    public static Schem read(NBTTagCompound nbt) {
        return new SchemReader().read(nbt).toSchem(SchemSmart::new, SchemNormal::valid);
    }


    @Override
    public void build(World world, int x, int y, int z, SettingsFolder settingsFolder, Random rand) {
        PlacementSettings settings = settingsFolder.get(rand);
        if(stolbs.length>0){
            Rotation rotation = settings.getRotation();
            Mirror mirror = settings.getMirror();
            int prewReqChunkX = Integer.MIN_VALUE;
            int prewReqChunkZ = Integer.MIN_VALUE;

            Chunk chunk = null;

            for(Stolb stolb:stolbs){
                int reqX = (stolb.x+x)>>4;
                int reqZ = (stolb.z+z)>>4;
                if(reqX != prewReqChunkX || reqZ != prewReqChunkZ){
                    prewReqChunkX = reqX;
                    prewReqChunkZ = reqZ;
                    chunk = world.getChunk(reqX, reqZ);
                }
                for(StolbInfo inf:stolb.blocks){
                    BlockPos blockpos = transformedBlockPos(
                            stolb.x, inf.y, stolb.z, mirror, rotation, x, y, z);

                    int by = blockpos.getY();

                    if(by<255 && by>0){
                        IBlockState state = inf.state.withMirror(mirror).withRotation(rotation);
                        chunk.setBlockState(blockpos, state);

                        if(inf.y == 0){
                            int curY = y-1;
                            int bx = blockpos.getX();
                            int bz = blockpos.getZ();
                            while (curY>0 && mayBeReplaced(chunk.getBlockState(bx,curY,bz).getBlock())){
                                BlockPos pos = new BlockPos(bx,curY,bz);
                                chunk.setBlockState(pos,state);
                                --curY;
                            }
                        }

                        if(inf.tileEntityData != null){
                            TileEntity tileEntity = world.getTileEntity(blockpos);
                            if (tileEntity != null) {
                                inf.tileEntityData.setInteger("x", blockpos.getX());
                                inf.tileEntityData.setInteger("y", by);
                                inf.tileEntityData.setInteger("z", blockpos.getZ());
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


    protected boolean mayBeReplaced(Block b){
        return b == Blocks.AIR || b == Blocks.GRASS || b == Blocks.TALLGRASS || b == Blocks.FLOWING_WATER ||
                b == Blocks.FLOWING_LAVA || b == Blocks.WATER || b == Blocks.SNOW_LAYER ||
                b == Blocks.RED_FLOWER || b == Blocks.YELLOW_FLOWER || b == Blocks.SNOW ||
                b == Blocks.SAND || b == Blocks.DIRT || b == Blocks.GRASS_PATH || b == Blocks.LAVA;
    }
}