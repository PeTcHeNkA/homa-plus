package plus.gen.schem;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import plus.gen.settings.SettingsFolder;
import plus.tson.utl.Tuple;
import java.util.Random;
import java.util.UUID;


public class SchemNormal extends Schem implements ITSchem {
    protected final EntityInfo[] entites;
    SchemNormal(Tuple<Stolb[], EntityInfo[]> tuple) {
        super(tuple);
        if(tuple.B.length != 0){
            entites = tuple.B;
        } else {
            entites = null;
        }
    }


    public static Schem read(NBTTagCompound nbt) {
        return new SchemReader().read(nbt).toSchem(SchemNormal::new, SchemNormal::valid);
    }


    protected static boolean valid(BlockInfo info){
        Block b = info.blockState.getBlock();
        return b != Blocks.STRUCTURE_BLOCK && b != Blocks.BARRIER;
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
                        chunk.setBlockState(blockpos,
                                inf.state.withMirror(mirror).withRotation(rotation)
                        );
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


    protected void addEntitiesToWorld(World worldIn, int x, int y, int z, Mirror mirrorIn,
                                      Rotation rotationIn, StructureBoundingBox aabb) {
        for (EntityInfo entityInfo : this.entites) {
            BlockPos pos = entityInfo.blockPos;
            BlockPos blockpos = transformedBlockPos(
                    pos.getX(), pos.getY(), pos.getZ(), mirrorIn, rotationIn,x,y,z);

            if (aabb == null || aabb.isVecInside(blockpos)) {
                NBTTagCompound nbttagcompound = entityInfo.entityData;
                Vec3d vec = transformedVec3d(entityInfo.pos, mirrorIn, rotationIn).add(x, y, z);
                NBTTagList nbttaglist = new NBTTagList();
                nbttaglist.appendTag(new NBTTagDouble(vec.x));
                nbttaglist.appendTag(new NBTTagDouble(vec.y));
                nbttaglist.appendTag(new NBTTagDouble(vec.z));
                nbttagcompound.setTag("Pos", nbttaglist);
                nbttagcompound.setUniqueId("UUID", UUID.randomUUID());
                Entity entity;

                try {
                    entity = EntityList.createEntityFromNBT(nbttagcompound, worldIn);
                } catch (Exception e) {
                    entity = null;
                }

                if (entity != null) {
                    float f = entity.getMirroredYaw(mirrorIn);
                    f = f + (entity.rotationYaw - entity.getRotatedYaw(rotationIn));
                    entity.setLocationAndAngles(vec.x, vec.y, vec.z, f, entity.rotationPitch);
                    worldIn.spawnEntity(entity);
                }
            }
        }
    }


    protected static BlockPos transformedBlockPos(int x, int y, int z, Mirror mirrorIn, Rotation rotationIn, int xa, int ya, int za) {
        switch (mirrorIn) {
            case LEFT_RIGHT:
                z = -z;
                break;
            case FRONT_BACK:
                x = -x;
                break;
        }
        switch (rotationIn) {
            case COUNTERCLOCKWISE_90:
                return new BlockPos(xa+z, ya+y, za-x);
            case CLOCKWISE_90:
                return new BlockPos(xa-z, ya+y, za+x);
            case CLOCKWISE_180:
                return new BlockPos(za-x, ya+y, za-z);
            default:
                return new BlockPos(xa+x, ya+y, za+z);
        }
    }


    private static Vec3d transformedVec3d(Vec3d vec, Mirror mirrorIn, Rotation rotationIn) {
        double d0 = vec.x;
        double d1 = vec.y;
        double d2 = vec.z;
        boolean flag = true;

        switch (mirrorIn) {
            case LEFT_RIGHT:
                d2 = 1.0D - d2;
                break;
            case FRONT_BACK:
                d0 = 1.0D - d0;
                break;
            default:
                flag = false;
        }
        switch (rotationIn) {
            case COUNTERCLOCKWISE_90:
                return new Vec3d(d2, d1, 1.0D - d0);
            case CLOCKWISE_90:
                return new Vec3d(1.0D - d2, d1, d0);
            case CLOCKWISE_180:
                return new Vec3d(1.0D - d0, d1, 1.0D - d2);
            default:
                return flag ? new Vec3d(d0, d1, d2) : vec;
        }
    }
}