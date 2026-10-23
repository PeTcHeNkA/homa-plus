package plus.gen.legacy_templates;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;


public class VTemplate {
    //protected final List<VTemplate.BlockInfo> blocks = Lists.newArrayList();
    protected VTemplate.BlockInfo[] blocks;
    protected final List<VTemplate.EntityInfo> entities = Lists.newArrayList();
    protected BlockPos size = BlockPos.ORIGIN;


    public void takeBlocksFromWorld(World worldIn, BlockPos startPos, BlockPos size, boolean takeEntities, @Nullable Block toIgnore) {
        if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
            BlockPos blockpos = startPos.add(size).add(-1, -1, -1);
            List<VTemplate.BlockInfo> list = Lists.newArrayList();
            BlockPos minPos = new BlockPos(Math.min(startPos.getX(), blockpos.getX()), Math.min(startPos.getY(), blockpos.getY()), Math.min(startPos.getZ(), blockpos.getZ()));
            BlockPos maxPos = new BlockPos(Math.max(startPos.getX(), blockpos.getX()), Math.max(startPos.getY(), blockpos.getY()), Math.max(startPos.getZ(), blockpos.getZ()));
            this.size = size;

            for (BlockPos.MutableBlockPos mutableBlockPos : BlockPos.getAllInBoxMutable(minPos, maxPos)) {
                BlockPos pos = mutableBlockPos.subtract(minPos);
                IBlockState iblockstate = worldIn.getBlockState(mutableBlockPos);

                if (toIgnore == null || toIgnore != iblockstate.getBlock()) {
                    TileEntity tileentity = worldIn.getTileEntity(mutableBlockPos);

                    if (tileentity != null) {
                        NBTTagCompound nbttagcompound = tileentity.writeToNBT(new NBTTagCompound());
                        nbttagcompound.removeTag("x");
                        nbttagcompound.removeTag("y");
                        nbttagcompound.removeTag("z");
                        list.add(new VTemplate.BlockInfo(pos, iblockstate, nbttagcompound));
                    } else if (!iblockstate.isFullBlock() && !iblockstate.isFullCube()) {
                        list.add(new VTemplate.BlockInfo(pos, iblockstate, null));
                    } else {
                        list.add(new VTemplate.BlockInfo(pos, iblockstate, null));
                    }
                }
            }

            this.blocks = list.toArray(new BlockInfo[0]);

            if (takeEntities) {
                this.takeEntitiesFromWorld(worldIn, minPos, maxPos.add(1, 1, 1));
            } else {
                this.entities.clear();
            }
        }
    }


    private void takeEntitiesFromWorld(World worldIn, BlockPos startPos, BlockPos endPos) {
        List<Entity> list = worldIn.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(startPos, endPos), new Predicate<Entity>()
        {
            public boolean apply(@Nullable Entity p_apply_1_)
            {
                return !(p_apply_1_ instanceof EntityPlayer);
            }
        });
        this.entities.clear();

        for (Entity entity : list) {
            Vec3d vec3d = new Vec3d(entity.posX - (double)startPos.getX(), entity.posY - (double)startPos.getY(), entity.posZ - (double)startPos.getZ());
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            entity.writeToNBTOptional(nbttagcompound);
            BlockPos blockpos;

            if (entity instanceof EntityPainting) {
                blockpos = ((EntityPainting)entity).getHangingPosition().subtract(startPos);
            } else {
                blockpos = new BlockPos(vec3d);
            }

            this.entities.add(new VTemplate.EntityInfo(vec3d, blockpos, nbttagcompound));
        }
    }


    public Map<BlockPos, String> getDataBlocks(BlockPos pos, PlacementSettings settings) {
        Map<BlockPos, String> map = Maps.newHashMap();
        StructureBoundingBox structureboundingbox = settings.getBoundingBox();

        for (VTemplate.BlockInfo blockInfo : this.blocks) {
            BlockPos blockpos = transformedBlockPos(
                    blockInfo.x, blockInfo.y,blockInfo.z,settings.getMirror(),settings.getRotation(),
                    pos.getX(),pos.getY(),pos.getZ());

            if (structureboundingbox == null || structureboundingbox.isVecInside(blockpos)) {
                IBlockState iblockstate = blockInfo.blockState;

                if (iblockstate.getBlock() == Blocks.STRUCTURE_BLOCK && blockInfo.tileentitydata != null) {
                    TileEntityStructure.Mode tileentitystructure$mode = TileEntityStructure.Mode.valueOf(blockInfo.tileentitydata.getString("mode"));

                    if (tileentitystructure$mode == TileEntityStructure.Mode.DATA) {
                        map.put(blockpos, blockInfo.tileentitydata.getString("metadata"));
                    }
                }
            }
        }
        return map;
    }


    public void addBlocksToWorld(World worldIn, int x, int y, int z,
                                 PlacementSettings settings) {
        if (this.blocks.length>0) {
            Block replacedBlock = settings.getReplacedBlock();
            boolean needReplace = replacedBlock==null;

            Rotation rotation = settings.getRotation();
            Mirror mirror = settings.getMirror();

            for (VTemplate.BlockInfo blockInfo : this.blocks) {
                BlockPos blockpos = transformedBlockPos(
                        blockInfo.x, blockInfo.y,blockInfo.z, mirror, rotation, x,y,z);

                Block block = blockInfo.blockState.getBlock();

                if (needReplace || replacedBlock != block) {
                    if(block != Blocks.STRUCTURE_BLOCK){
                        int by = blockpos.getY();
                        if(by<255 && by>0){
                            worldIn.getChunk(blockpos).setBlockState(blockpos,
                                    blockInfo.blockState.withMirror(mirror).withRotation(rotation)
                            );
                            if(blockInfo.tileentitydata != null){
                                TileEntity tileEntity = worldIn.getTileEntity(blockpos);
                                if (tileEntity != null) {
                                    blockInfo.tileentitydata.setInteger("x", blockpos.getX());
                                    blockInfo.tileentitydata.setInteger("y", by);
                                    blockInfo.tileentitydata.setInteger("z", blockpos.getZ());
                                    tileEntity.readFromNBT(blockInfo.tileentitydata);
                                    tileEntity.mirror(mirror);
                                    tileEntity.rotate(rotation);
                                }
                            }
                        }
                    }

                    if (blockInfo.tileentitydata != null) {
                        TileEntity tileEntity = worldIn.getTileEntity(blockpos);

                        if (tileEntity != null) {
                            tileEntity.markDirty();
                        }
                    }
                }
            }
        }


        if (!settings.getIgnoreEntities() && !this.entities.isEmpty()) {
            this.addEntitiesToWorld(worldIn, x,y,z, settings.getMirror(),
                    settings.getRotation(), null);
        }
    }


    protected void addEntitiesToWorld(World worldIn, int x, int y, int z, Mirror mirrorIn,
                                    Rotation rotationIn, @Nullable StructureBoundingBox aabb) {
        for (VTemplate.EntityInfo entityInfo : this.entities) {
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


    public BlockPos transformedSize(Rotation rotationIn) {
        switch (rotationIn) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                return new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
            default:
                return this.size;
        }
    }


    public static BlockPos transformedBlockPos(int x, int y, int z, Mirror mirrorIn, Rotation rotationIn, int xa, int ya, int za) {
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


    public BlockPos getZeroPositionWithTransform(BlockPos pos, Mirror mirror, Rotation rotation) {
        return getZeroPositionWithTransform(pos, mirror, rotation, this.size.getX(), this.size.getZ());
    }


    public static BlockPos getZeroPositionWithTransform(BlockPos blockPos, Mirror mirror,
                                                        Rotation rotation, int i1, int i2) {
        --i1;
        --i2;
        int i = mirror == Mirror.FRONT_BACK ? i1 : 0;
        int j = mirror == Mirror.LEFT_RIGHT ? i2 : 0;
        BlockPos blockpos = blockPos;

        switch (rotation)
        {
            case COUNTERCLOCKWISE_90:
                blockpos = blockPos.add(j, 0, i1 - i);
                break;
            case CLOCKWISE_90:
                blockpos = blockPos.add(i2 - j, 0, i);
                break;
            case CLOCKWISE_180:
                blockpos = blockPos.add(i1 - i, 0, i2 - j);
                break;
            case NONE:
                blockpos = blockPos.add(i, 0, j);
        }

        return blockpos;
    }


    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        VTemplate.BasicPalette palette = new VTemplate.BasicPalette();
        NBTTagList nbttaglist = new NBTTagList();

        for (VTemplate.BlockInfo blockInfo : this.blocks) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setTag("pos", this.writeInts(blockInfo.x, blockInfo.y, blockInfo.z));
            nbttagcompound.setInteger("state", palette.idFor(blockInfo.blockState));

            if (blockInfo.tileentitydata != null) {
                nbttagcompound.setTag("nbt", blockInfo.tileentitydata);
            }

            nbttaglist.appendTag(nbttagcompound);
        }

        NBTTagList nbtTagList = new NBTTagList();

        for (VTemplate.EntityInfo entityInfo : this.entities) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            tagCompound.setTag("pos", this.writeDoubles(entityInfo.pos.x, entityInfo.pos.y, entityInfo.pos.z));
            tagCompound.setTag("blockPos", this.writeInts(entityInfo.blockPos.getX(), entityInfo.blockPos.getY(), entityInfo.blockPos.getZ()));

            if (entityInfo.entityData != null) {
                tagCompound.setTag("nbt", entityInfo.entityData);
            }

            nbtTagList.appendTag(tagCompound);
        }

        NBTTagList nbtTagList2 = new NBTTagList();

        for (IBlockState iblockstate : palette) {
            nbtTagList2.appendTag(NBTUtil.writeBlockState(new NBTTagCompound(), iblockstate));
        }

        net.minecraftforge.fml.common.FMLCommonHandler.instance().getDataFixer().writeVersionData(nbt); //Moved up for MC updating reasons.
        nbt.setTag("palette", nbtTagList2);
        nbt.setTag("blocks", nbttaglist);
        nbt.setTag("entities", nbtTagList);
        nbt.setTag("size", this.writeInts(this.size.getX(), this.size.getY(), this.size.getZ()));
        nbt.setString("author", "?");
        nbt.setInteger("DataVersion", 1343);
        return nbt;
    }


    public void read(NBTTagCompound compound) {
        //this.blocks.clear();
        this.entities.clear();
        NBTTagList nbtTagList = compound.getTagList("size", 3);
        this.size = new BlockPos(nbtTagList.getIntAt(0), nbtTagList.getIntAt(1), nbtTagList.getIntAt(2));
        VTemplate.BasicPalette palette = new VTemplate.BasicPalette();
        NBTTagList nbtTagList1 = compound.getTagList("palette", 10);

        for (int i = 0; i < nbtTagList1.tagCount(); ++i) {
            palette.addMapping(NBTUtil.readBlockState(nbtTagList1.getCompoundTagAt(i)), i);
        }

        NBTTagList nbtTagList2 = compound.getTagList("blocks", 10);

        List<BlockInfo> blocks = new ArrayList<>();
        for (int j = 0; j < nbtTagList2.tagCount(); ++j) {
            NBTTagCompound nbttagcompound = nbtTagList2.getCompoundTagAt(j);
            NBTTagList nbtTagList3 = nbttagcompound.getTagList("pos", 3);

            IBlockState iblockstate = palette.stateFor(nbttagcompound.getInteger("state"));

            NBTTagCompound nbttagcompound1;

            if (nbttagcompound.hasKey("nbt")) {
                nbttagcompound1 = nbttagcompound.getCompoundTag("nbt");
            } else {
                nbttagcompound1 = null;
            }

            blocks.add(
                    new VTemplate.BlockInfo(
                            nbtTagList3.getIntAt(0),
                            nbtTagList3.getIntAt(1),
                            nbtTagList3.getIntAt(2),
                            iblockstate, nbttagcompound1)
            );
        }
        this.blocks = blocks.toArray(new BlockInfo[0]);

        NBTTagList nbtTagList4 = compound.getTagList("entities", 10);

        for (int k = 0; k < nbtTagList4.tagCount(); ++k) {
            NBTTagCompound compoundTagAt = nbtTagList4.getCompoundTagAt(k);
            NBTTagList nbtTagList5 = compoundTagAt.getTagList("pos", 6);
            Vec3d vec3d = new Vec3d(nbtTagList5.getDoubleAt(0), nbtTagList5.getDoubleAt(1), nbtTagList5.getDoubleAt(2));
            NBTTagList tagListBlockPos = compoundTagAt.getTagList("blockPos", 3);
            BlockPos blockPos = new BlockPos(tagListBlockPos.getIntAt(0), tagListBlockPos.getIntAt(1), tagListBlockPos.getIntAt(2));

            if (compoundTagAt.hasKey("nbt")) {
                NBTTagCompound nbt = compoundTagAt.getCompoundTag("nbt");
                this.entities.add(new VTemplate.EntityInfo(vec3d, blockPos, nbt));
            }
        }
    }


    private NBTTagList writeInts(int... values) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i : values) {
            nbttaglist.appendTag(new NBTTagInt(i));
        }

        return nbttaglist;
    }


    private NBTTagList writeDoubles(double... values) {
        NBTTagList nbttaglist = new NBTTagList();

        for (double d0 : values) {
            nbttaglist.appendTag(new NBTTagDouble(d0));
        }

        return nbttaglist;
    }


    public static class BasicPalette implements Iterable<IBlockState> {
        public static final IBlockState DEFAULT_BLOCK_STATE = Blocks.AIR.getDefaultState();
        final ObjectIntIdentityMap<IBlockState> ids;
        private int lastId;

        public BasicPalette() {
            this.ids = new ObjectIntIdentityMap<>(16);
        }

        public int idFor(IBlockState state) {
            int i = this.ids.get(state);

            if (i == -1) {
                i = this.lastId++;
                this.ids.put(state, i);
            }
            return i;
        }

        @Nullable
        public IBlockState stateFor(int id) {
            IBlockState iblockstate = this.ids.getByValue(id);
            return iblockstate == null ? DEFAULT_BLOCK_STATE : iblockstate;
        }

        public Iterator<IBlockState> iterator() {
            return this.ids.iterator();
        }

        public void addMapping(IBlockState p_189956_1_, int p_189956_2_) {
            this.ids.put(p_189956_1_, p_189956_2_);
        }
    }


    public static class BlockInfo {
        public final IBlockState blockState;
        public final NBTTagCompound tileentitydata;
        public final int x;
        public final int y;
        public final int z;

        public BlockInfo(BlockPos pos, IBlockState stateIn, @Nullable NBTTagCompound compoundIn) {
            this(pos.getX(), pos.getY(), pos.getZ(), stateIn, compoundIn);
        }


        public BlockInfo(int x,int y, int z, IBlockState stateIn, @Nullable NBTTagCompound compoundIn) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockState = stateIn;
            this.tileentitydata = compoundIn;
        }
    }


    public static class EntityInfo {
        public final Vec3d pos;
        public final BlockPos blockPos;
        public final NBTTagCompound entityData;

        public EntityInfo(Vec3d vecIn, BlockPos posIn, NBTTagCompound compoundIn) {
            this.pos = vecIn;
            this.blockPos = posIn;
            this.entityData = compoundIn;
        }
    }
}