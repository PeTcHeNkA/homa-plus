package plus.gen.schem;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import plus.gen.settings.SettingsFolder;
import plus.tson.TsonMap;
import plus.tson.utl.Tuple;
import java.util.Random;


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


    public static Schem read(NBTTagCompound nbt, TsonMap map) {
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

                        if(inf.y == ySize){
                            ++by;
                            int bx = blockpos.getX();
                            int bz = blockpos.getZ();
                            int maxY=0;
                            while (maxY<limit && by<255 && mayBeReplaced(chunk.getBlockState(bx,by,bz).getBlock())){
                                BlockPos pos = new BlockPos(bx,by,bz);
                                chunk.setBlockState(pos,state);
                                ++by;
                                ++maxY;
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
}