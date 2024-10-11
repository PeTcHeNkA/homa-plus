package net.minecraft.world.gen.structure.template;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.tileentity.TileEntityStructure.Mode;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.Rotation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.NbtUtils;


public class Template {
    private final List<BlockInfo>  blocks   = Lists.newArrayList();
    private final List<EntityInfo> entities = Lists.newArrayList();
    private BlockPos size;
    private String author;

    public Template() {
        this.size = BlockPos.ORIGIN;
        this.author = "?";
    }


    public BlockPos getSize() {
        return this.size;
    }


    public void setAuthor(String author) {
        this.author = author;
    }


    public String getAuthor() {
        return this.author;
    }


    public void takeBlocksFromWorld(World world, BlockPos pos, BlockPos size, boolean entities, @Nullable Block block) {
        if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
            BlockPos blockpos = pos.add(size).add(-1, -1, -1);
            List<BlockInfo> list = Lists.newArrayList();
            List<BlockInfo> list1 = Lists.newArrayList();
            List<BlockInfo> list2 = Lists.newArrayList();
            BlockPos blockpos1 = new BlockPos(Math.min(pos.getX(), blockpos.getX()), Math.min(pos.getY(), blockpos.getY()), Math.min(pos.getZ(), blockpos.getZ()));
            BlockPos blockpos2 = new BlockPos(Math.max(pos.getX(), blockpos.getX()), Math.max(pos.getY(), blockpos.getY()), Math.max(pos.getZ(), blockpos.getZ()));
            this.size = size;
            Iterator var12 = BlockPos.getAllInBoxMutable(blockpos1, blockpos2).iterator();

            while(true) {
                while(true) {
                    BlockPos.MutableBlockPos blockpos$mutableblockpos;
                    BlockPos blockpos3;
                    IBlockState iblockstate;
                    do {
                        if (!var12.hasNext()) {
                            this.blocks.clear();
                            this.blocks.addAll(list);
                            this.blocks.addAll(list1);
                            this.blocks.addAll(list2);
                            if (entities) {
                                this.takeEntitiesFromWorld(world, blockpos1, blockpos2.add(1, 1, 1));
                            } else {
                                this.entities.clear();
                            }

                            return;
                        }

                        blockpos$mutableblockpos = (BlockPos.MutableBlockPos)var12.next();
                        blockpos3 = blockpos$mutableblockpos.subtract(blockpos1);
                        iblockstate = world.getBlockState(blockpos$mutableblockpos);
                    } while(block != null && block == iblockstate.getBlock());

                    TileEntity tileentity = world.getTileEntity(blockpos$mutableblockpos);
                    if (tileentity != null) {
                        NBTTagCompound nbttagcompound = tileentity.writeToNBT(new NBTTagCompound());
                        nbttagcompound.removeTag("x");
                        nbttagcompound.removeTag("y");
                        nbttagcompound.removeTag("z");
                        list1.add(new BlockInfo(blockpos3, iblockstate, nbttagcompound));
                    } else if (!iblockstate.isFullBlock() && !iblockstate.isFullCube()) {
                        list2.add(new BlockInfo(blockpos3, iblockstate, (NBTTagCompound)null));
                    } else {
                        list.add(new BlockInfo(blockpos3, iblockstate, (NBTTagCompound)null));
                    }
                }
            }
        }
        blocks.sort(BlockInfo::compareTo);
    }


    private void takeEntitiesFromWorld(World world, BlockPos pos, BlockPos pos2) {
        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos, pos2), new Predicate<Entity>() {
            public boolean apply(@Nullable Entity p_apply_1_) {
                return !(p_apply_1_ instanceof EntityPlayer);
            }
        });
        this.entities.clear();

        Vec3d vec3d;
        NBTTagCompound nbttagcompound;
        BlockPos blockpos;
        for(Iterator var5 = list.iterator(); var5.hasNext(); this.entities.add(new EntityInfo(vec3d, blockpos, nbttagcompound))) {
            Entity entity = (Entity)var5.next();
            vec3d = new Vec3d(entity.posX - (double)pos.getX(), entity.posY - (double)pos.getY(), entity.posZ - (double)pos.getZ());
            nbttagcompound = new NBTTagCompound();
            entity.writeToNBTOptional(nbttagcompound);
            if (entity instanceof EntityPainting) {
                blockpos = ((EntityPainting)entity).getHangingPosition().subtract(pos);
            } else {
                blockpos = new BlockPos(vec3d);
            }
        }

    }


    public Map<BlockPos, String> getDataBlocks(BlockPos pos, PlacementSettings stn) {
        Map<BlockPos, String> map = Maps.newHashMap();
        StructureBoundingBox structureboundingbox = stn.getBoundingBox();
        Iterator var5 = this.blocks.iterator();

        while(true) {
            BlockInfo info;
            BlockPos blockpos;
            do {
                if (!var5.hasNext()) {
                    return map;
                }

                info = (BlockInfo)var5.next();
                blockpos = transformedBlockPos(stn, info.pos).add(pos);
            } while(structureboundingbox != null && !structureboundingbox.isVecInside(blockpos));

            IBlockState iblockstate = info.blockState;
            if (iblockstate.getBlock() == Blocks.STRUCTURE_BLOCK && info.tileentityData != null) {
                TileEntityStructure.Mode tileentitystructure$mode = Mode.valueOf(info.tileentityData.getString("mode"));
                if (tileentitystructure$mode == Mode.DATA) {
                    map.put(blockpos, info.tileentityData.getString("metadata"));
                }
            }
        }
    }


    public BlockPos calculateConnectedPos(PlacementSettings stn, BlockPos pos, PlacementSettings stn2, BlockPos pos2) {
        BlockPos blockpos = transformedBlockPos(stn, pos);
        BlockPos blockpos1 = transformedBlockPos(stn2, pos2);
        return blockpos.subtract(blockpos1);
    }


    public static BlockPos transformedBlockPos(PlacementSettings stn, BlockPos pos) {
        return transformedBlockPos(pos, stn.getMirror(), stn.getRotation());
    }


    public void addBlocksToWorldChunk(World world, BlockPos pos, PlacementSettings stn) {
        stn.setBoundingBoxFromChunk();
        this.addBlocksToWorld(world, pos, stn);
    }


    public void addBlocksToWorld(World world, BlockPos pos, PlacementSettings stn) {
        this.addBlocksToWorld(world, pos, new BlockRotationProcessor(pos, stn), stn, 2);
    }


    public void addBlocksToWorld(World world, BlockPos pos, PlacementSettings stn, int num) {
        this.addBlocksToWorld(world, pos, new BlockRotationProcessor(pos, stn), stn, num);
    }


    public void addBlocksToWorld(World world, BlockPos startPos, @Nullable ITemplateProcessor prc, PlacementSettings stn, int num) {
        if ((!this.blocks.isEmpty() || !stn.getIgnoreEntities() && !this.entities.isEmpty()) && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
            final ChunkSelector access = new ChunkSelector(world);
            final Block rep = stn.getReplacedBlock();
            final StructureBoundingBox strBox = stn.getBoundingBox();
            Iterator<BlockInfo> bIter = this.blocks.iterator();

            final int startX = startPos.getX(),
                      startY = startPos.getY(),
                      startZ = startPos.getZ();

            final Mirror   mirror   = stn.getMirror();
            final Rotation rotation = stn.getRotation();

            while(true) {
                BlockPos pos;
                BlockInfo info;
                Block block1;
                do {
                    do {
                        do {
                            do {
                                BlockInfo info2;
                                do {
                                    if (!bIter.hasNext()) {
                                        bIter = this.blocks.iterator();

                                        while(true) {
                                            do {
                                                do {
                                                    if (!bIter.hasNext()) {
                                                        if (!stn.getIgnoreEntities()) {
                                                            this.addEntitiesToWorld(world, startPos, mirror, rotation, strBox);
                                                        }
                                                        return;
                                                    }

                                                    info2 = bIter.next();
                                                } while(rep != null && rep == info2.blockState.getBlock());

                                                pos = transformPos(startX, startY, startZ, info2.pos, mirror, rotation);

                                            } while(strBox != null && !strBox.isVecInside(pos));

                                            world.notifyNeighborsRespectDebug(pos, info2.blockState.getBlock(), false);

                                            if (info2.tileentityData != null) {
                                                TileEntity tile = access.getTileEntity(pos);
                                                if (tile != null) tile.markDirty();
                                            }
                                        }
                                    }

                                    info2 = bIter.next();
                                    pos = transformPos(startX, startY, startZ, info2.pos, mirror, rotation);

                                } while(strBox != null && !strBox.isVecInside(pos));

                                info = prc != null ? prc.processBlock(world, pos, info2) : info2;
                            } while(info == null);

                            block1 = info.blockState.getBlock();
                        } while(rep != null && rep == block1);
                    } while(stn.getIgnoreStructureBlock() && block1 == Blocks.STRUCTURE_BLOCK);
                } while(strBox != null && !strBox.isVecInside(pos));

                IBlockState state = info.blockState.withMirror(mirror).withRotation(rotation);

                TileEntity tile;

                if (info.tileentityData != null) {
                    tile = access.getTileEntity(pos);
                    if (tile != null) {
                        if (tile instanceof IInventory) ((IInventory)tile).clear();
                        access.setBlockState(pos, Blocks.BARRIER.getDefaultState(), 4);
                    }
                }

                if (access.setBlockState(pos, state, num) && info.tileentityData != null) {
                    tile = access.getTileEntity(pos);
                    if (tile != null) {
                        info.tileentityData.setIntegerU("x", pos.getX());
                        info.tileentityData.setIntegerU("y", pos.getY());
                        info.tileentityData.setIntegerU("z", pos.getZ());
                        tile.readFromNBT(info.tileentityData);
                        tile.mirror(stn.getMirror());
                        tile.rotate(stn.getRotation());
                    }
                }
            }
        }
    }


    private void addEntitiesToWorld(World world, BlockPos pos, Mirror mirror, Rotation rotation, @Nullable StructureBoundingBox p_186263_5_) {
        Iterator var6 = this.entities.iterator();

        while(true) {
            EntityInfo template$entityinfo;
            BlockPos blockpos;
            do {
                if (!var6.hasNext()) {
                    return;
                }

                template$entityinfo = (EntityInfo)var6.next();
                blockpos = transformedBlockPos(template$entityinfo.blockPos, mirror, rotation).add(pos);
            } while(p_186263_5_ != null && !p_186263_5_.isVecInside(blockpos));

            NBTTagCompound nbttagcompound = template$entityinfo.entityData;
            Vec3d vec3d = transformedVec3d(template$entityinfo.pos, mirror, rotation);
            Vec3d vec3d1 = vec3d.add((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            NBTTagList nbttaglist = new NBTTagList();
            nbttaglist.appendTag(new NBTTagDouble(vec3d1.x));
            nbttaglist.appendTag(new NBTTagDouble(vec3d1.y));
            nbttaglist.appendTag(new NBTTagDouble(vec3d1.z));
            nbttagcompound.setTag("Pos", nbttaglist);
            nbttagcompound.setUniqueId("UUID", UUID.randomUUID());

            Entity entity;
            try {
                entity = EntityList.createEntityFromNBT(nbttagcompound, world);
            } catch (Exception var15) {
                entity = null;
            }

            if (entity != null) {
                float f = entity.getMirroredYaw(mirror);
                f += entity.rotationYaw - entity.getRotatedYaw(rotation);
                entity.setLocationAndAngles(vec3d1.x, vec3d1.y, vec3d1.z, f, entity.rotationPitch);
                world.spawnEntity(entity);
            }
        }
    }


    public BlockPos transformedSize(Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                return new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
            default:
                return this.size;
        }
    }


    private static BlockPos transformedBlockPos(BlockPos pos, Mirror mirror, Rotation rotation) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        boolean flag = true;
        switch (mirror) {
            case LEFT_RIGHT:
                k = -k;
                break;
            case FRONT_BACK:
                i = -i;
                break;
            default:
                flag = false;
        }

        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                return new BlockPos(k, j, -i);
            case CLOCKWISE_90:
                return new BlockPos(-k, j, i);
            case CLOCKWISE_180:
                return new BlockPos(-i, j, -k);
            default:
                return flag ? new BlockPos(i, j, k) : pos;
        }
    }


    private static BlockPos transformPos(int startX, int startY, int startZ, BlockPos pos, Mirror mirror, Rotation rotation) {
        int cx = pos.getX();
        int cz = pos.getZ();
        switch (mirror) {
            case LEFT_RIGHT:
                cz = -cz;
                break;
            case FRONT_BACK:
                cx = -cx;
                break;
            default:
        }

        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                return new BlockPos(cz+startX, pos.getY()+startY, -cx+startZ);
            case CLOCKWISE_90:
                return new BlockPos(-cz+startX, pos.getY()+startY, cx+startZ);
            case CLOCKWISE_180:
                return new BlockPos(-cx+startX, pos.getY()+startY, -cz+startZ);
            default:
                return new BlockPos(cx+startX, pos.getY()+startY, cz+startZ);
        }
    }


    private static Vec3d transformedVec3d(Vec3d vec, Mirror mirror, Rotation rotation) {
        double d0 = vec.x;
        double d1 = vec.y;
        double d2 = vec.z;
        boolean flag = true;
        switch (mirror) {
            case LEFT_RIGHT:
                d2 = 1.0 - d2;
                break;
            case FRONT_BACK:
                d0 = 1.0 - d0;
                break;
            default:
                flag = false;
        }

        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                return new Vec3d(d2, d1, 1.0 - d0);
            case CLOCKWISE_90:
                return new Vec3d(1.0 - d2, d1, d0);
            case CLOCKWISE_180:
                return new Vec3d(1.0 - d0, d1, 1.0 - d2);
            default:
                return flag ? new Vec3d(d0, d1, d2) : vec;
        }
    }


    public BlockPos getZeroPositionWithTransform(BlockPos pos, Mirror mirror, Rotation rotation) {
        return getZeroPositionWithTransform(pos, mirror, rotation, this.getSize().getX(), this.getSize().getZ());
    }


    public static BlockPos getZeroPositionWithTransform(BlockPos pos, Mirror mirror, Rotation rotation, int num1, int num2) {
        --num1;
        --num2;
        int i = mirror == Mirror.FRONT_BACK ? num1 : 0;
        int j = mirror == Mirror.LEFT_RIGHT ? num2 : 0;
        BlockPos blockpos = pos;
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                blockpos = pos.add(j, 0, num1 - i);
                break;
            case CLOCKWISE_90:
                blockpos = pos.add(num2 - j, 0, i);
                break;
            case CLOCKWISE_180:
                blockpos = pos.add(num1 - i, 0, num2 - j);
                break;
            case NONE:
                blockpos = pos.add(i, 0, j);
        }

        return blockpos;
    }


    public static void registerFixes(DataFixer fixer) {
        fixer.registerWalker(FixTypes.STRUCTURE, new IDataWalker() {
            public NBTTagCompound process(IDataFixer p_188266_1_, NBTTagCompound p_188266_2_, int p_188266_3_) {
                NBTTagList nbttaglist1;
                int j;
                NBTTagCompound nbttagcompound1;
                if (p_188266_2_.hasKey("entities", 9)) {
                    nbttaglist1 = p_188266_2_.getTagList("entities", 10);

                    for(j = 0; j < nbttaglist1.tagCount(); ++j) {
                        nbttagcompound1 = (NBTTagCompound)nbttaglist1.get(j);
                        if (nbttagcompound1.hasKey("nbt", 10)) {
                            nbttagcompound1.setTag("nbt", p_188266_1_.process(FixTypes.ENTITY, nbttagcompound1.getCompoundTag("nbt"), p_188266_3_));
                        }
                    }
                }

                if (p_188266_2_.hasKey("blocks", 9)) {
                    nbttaglist1 = p_188266_2_.getTagList("blocks", 10);

                    for(j = 0; j < nbttaglist1.tagCount(); ++j) {
                        nbttagcompound1 = (NBTTagCompound)nbttaglist1.get(j);
                        if (nbttagcompound1.hasKey("nbt", 10)) {
                            nbttagcompound1.setTag("nbt", p_188266_1_.process(FixTypes.BLOCK_ENTITY, nbttagcompound1.getCompoundTag("nbt"), p_188266_3_));
                        }
                    }
                }

                return p_188266_2_;
            }
        });
    }


    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        BasicPalette palette = new BasicPalette();
        NBTTagList list1 = new NBTTagList();

        NBTTagCompound nbttagcompound;
        for(Iterator var4 = this.blocks.iterator(); var4.hasNext(); list1.appendTag(nbttagcompound)) {
            BlockInfo template$blockinfo = (BlockInfo)var4.next();
            nbttagcompound = new NBTTagCompound(4, 1);
            nbttagcompound.setTag("pos", this.writeInts(template$blockinfo.pos.getX(), template$blockinfo.pos.getY(), template$blockinfo.pos.getZ()));
            nbttagcompound.setIntegerU("state", palette.idFor(template$blockinfo.blockState));
            if (template$blockinfo.tileentityData != null) {
                nbttagcompound.setTag("nbt", template$blockinfo.tileentityData);
            }
        }

        NBTTagList list2 = new NBTTagList();

        NBTTagCompound nbttagcompound1;
        for(Iterator var9 = this.entities.iterator(); var9.hasNext(); list2.appendTag(nbttagcompound1)) {
            EntityInfo template$entityinfo = (EntityInfo)var9.next();
            nbttagcompound1 = new NBTTagCompound(4, 1);
            nbttagcompound1.setTag("pos", this.writeDoubles(template$entityinfo.pos.x, template$entityinfo.pos.y, template$entityinfo.pos.z));
            nbttagcompound1.setTag("blockPos", this.writeInts(template$entityinfo.blockPos.getX(), template$entityinfo.blockPos.getY(), template$entityinfo.blockPos.getZ()));
            if (template$entityinfo.entityData != null) {
                nbttagcompound1.setTag("nbt", template$entityinfo.entityData);
            }
        }

        NBTTagList nbttaglist2 = new NBTTagList();
        Iterator var12 = palette.iterator();

        while(var12.hasNext()) {
            IBlockState iblockstate = (IBlockState)var12.next();
            nbttaglist2.appendTag(NBTUtil.writeBlockState(new NBTTagCompound(4,1), iblockstate));
        }

        FMLCommonHandler.instance().getDataFixer().writeVersionData(nbt);
        nbt.setTag("palette", nbttaglist2);
        nbt.setTag("blocks", list1);
        nbt.setTag("entities", list2);
        nbt.setTag("size", this.writeInts(this.size.getX(), this.size.getY(), this.size.getZ()));
        nbt.setString("author", this.author);
        nbt.setTag("DataVersion", NbtUtils.DATA_VERSION);
        return nbt;
    }


    public void read(NBTTagCompound nbt) {
        this.blocks.clear();
        this.entities.clear();
        NBTTagList nbttaglist = nbt.getTagList("size", 3);
        this.size = new BlockPos(nbttaglist.getIntAt(0), nbttaglist.getIntAt(1), nbttaglist.getIntAt(2));
        this.author = nbt.getString("author");
        BasicPalette palette = new BasicPalette();
        NBTTagList nbttaglist1 = nbt.getTagList("palette", 10);

        for(int i = 0; i < nbttaglist1.tagCount(); ++i) {
            palette.addMapping(NBTUtil.readBlockState(nbttaglist1.getCompoundTagAt(i)), i);
        }

        NBTTagList nbttaglist3 = nbt.getTagList("blocks", 10);

        for(int j = 0; j < nbttaglist3.tagCount(); ++j) {
            NBTTagCompound nBlock = nbttaglist3.getCompoundTagAt(j);
            NBTTagList nPos = nBlock.getTagList("pos", 3);
            BlockPos pos = new BlockPos(nPos.getIntAt(0), nPos.getIntAt(1), nPos.getIntAt(2));
            IBlockState state = palette.stateFor(nBlock.getInteger("state"));
            NBTTagCompound nNbt;
            if (nBlock.hasKey("nbt")) {
                nNbt = nBlock.getCompoundTag("nbt");
            } else {
                nNbt = null;
            }

            this.blocks.add(new BlockInfo(pos, state, nNbt));
        }

        blocks.sort(BlockInfo::compareTo);

        NBTTagList nEntities = nbt.getTagList("entities", 10);

        for(int k = 0; k < nEntities.tagCount(); ++k) {
            NBTTagCompound nEntity = nEntities.getCompoundTagAt(k);
            NBTTagList nVec = nEntity.getTagList("pos", 6);
            Vec3d vec3d = new Vec3d(nVec.getDoubleAt(0), nVec.getDoubleAt(1), nVec.getDoubleAt(2));
            NBTTagList nPos = nEntity.getTagList("blockPos", 3);
            BlockPos pos = new BlockPos(nPos.getIntAt(0), nPos.getIntAt(1), nPos.getIntAt(2));
            if (nEntity.hasKey("nbt")) {
                NBTTagCompound nNbt = nEntity.getCompoundTag("nbt");
                this.entities.add(new EntityInfo(vec3d, pos, nNbt));
            }
        }
    }


    private NBTTagList writeInts(int... ints) {
        NBTTagList nbttaglist = new NBTTagList();
        int var4 = ints.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            int i = ints[var5];
            nbttaglist.appendTag(new NBTTagInt(i));
        }

        return nbttaglist;
    }


    private NBTTagList writeDoubles(double... doubles) {
        NBTTagList nbttaglist = new NBTTagList();
        int var4 = doubles.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            double d0 = doubles[var5];
            nbttaglist.appendTag(new NBTTagDouble(d0));
        }

        return nbttaglist;
    }


    public static class EntityInfo {
        public final Vec3d pos;
        public final BlockPos blockPos;
        public final NBTTagCompound entityData;

        public EntityInfo(Vec3d vec, BlockPos pos, NBTTagCompound data) {
            this.pos = vec;
            this.blockPos = pos;
            this.entityData = data;
        }
    }


    public static class BlockInfo implements Comparable<BlockInfo>{
        public final BlockPos pos;
        public final IBlockState blockState;
        public final NBTTagCompound tileentityData;

        public BlockInfo(BlockPos pos, IBlockState state, @Nullable NBTTagCompound data) {
            this.pos = pos;
            this.blockState = state;
            this.tileentityData = data;
        }


        @Override
        public int compareTo(@NotNull BlockInfo o) {
            BlockPos opos = o.pos;
            BlockPos pos = this.pos;

            if(pos.getX() > opos.getX())return 1;
            if(pos.getX() < opos.getX())return -1;
            if(pos.getZ() > opos.getZ())return 1;
            if(pos.getZ() < opos.getZ())return -1;

            if(pos.getY() > opos.getY())return 1;
            if(pos.getY() < opos.getY())return -1;

            return 0;
        }
    }


    static class BasicPalette implements Iterable<IBlockState> {
        public static final IBlockState DEFAULT_BLOCK_STATE;
        final ObjectIntIdentityMap<IBlockState> ids;
        private int lastId;

        private BasicPalette() {
            this.ids = new ObjectIntIdentityMap(16);
        }


        public int idFor(IBlockState p_189954_1_) {
            int i = this.ids.get(p_189954_1_);
            if (i == -1) {
                i = this.lastId++;
                this.ids.put(p_189954_1_, i);
            }

            return i;
        }


        @Nullable
        public IBlockState stateFor(int id) {
            IBlockState state = this.ids.getByValue(id);
            if(state == null)return DEFAULT_BLOCK_STATE;
            return state;
        }


        @Override
        public Iterator<IBlockState> iterator() {
            return this.ids.iterator();
        }


        public void addMapping(IBlockState state, int id) {
            this.ids.put(state, id);
        }


        static {
            DEFAULT_BLOCK_STATE = Blocks.AIR.getDefaultState();
        }
    }
}
