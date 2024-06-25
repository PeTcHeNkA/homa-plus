package todo;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.BlockRotationProcessor;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.fml.common.FMLCommonHandler;
import plus.fserver.server.elca.ChunkSelector;
import plus.fserver.utls.collections.ArrayListE6;


public class Template {
    private static final Line[] EMPTY = new Line[0];
    private Line[] lines = EMPTY;
    //private final List<BlockInfo> blocks = Lists.newArrayList();
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


    public void takeBlocksFromWorld(World world, BlockPos pos, BlockPos size, boolean entities, @Nullable Block p_186254_5_) {
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
//                            this.blocks.clear();
//                            this.blocks.addAll(list);
//                            this.blocks.addAll(list1);
//                            this.blocks.addAll(list2);
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
                    } while(p_186254_5_ != null && p_186254_5_ == iblockstate.getBlock());

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
    }


    private void takeEntitiesFromWorld(World p_186255_1_, BlockPos p_186255_2_, BlockPos p_186255_3_) {
        List<Entity> list = p_186255_1_.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(p_186255_2_, p_186255_3_), new Predicate<Entity>() {
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
            vec3d = new Vec3d(entity.posX - (double)p_186255_2_.getX(), entity.posY - (double)p_186255_2_.getY(), entity.posZ - (double)p_186255_2_.getZ());
            nbttagcompound = new NBTTagCompound();
            entity.writeToNBTOptional(nbttagcompound);
            if (entity instanceof EntityPainting) {
                blockpos = ((EntityPainting)entity).getHangingPosition().subtract(p_186255_2_);
            } else {
                blockpos = new BlockPos(vec3d);
            }
        }

    }


    public Map<BlockPos, String> getDataBlocks(BlockPos p_186258_1_, PlacementSettings p_186258_2_) {
        List<BlockInfo> blocks = Lists.newArrayList();
        Map<BlockPos, String> map = Maps.newHashMap();
        StructureBoundingBox structureboundingbox = p_186258_2_.getBoundingBox();
        Iterator var5 = blocks.iterator();

        while(true) {
            BlockInfo template$blockinfo;
            BlockPos blockpos;
            do {
                if (!var5.hasNext()) {
                    return map;
                }

                template$blockinfo = (BlockInfo)var5.next();
                blockpos = transformedBlockPos(p_186258_2_, template$blockinfo.pos).add(p_186258_1_);
            } while(structureboundingbox != null && !structureboundingbox.isVecInside(blockpos));

            IBlockState iblockstate = template$blockinfo.blockState;
            if (iblockstate.getBlock() == Blocks.STRUCTURE_BLOCK && template$blockinfo.tileentityData != null) {
                TileEntityStructure.Mode tileentitystructure$mode = Mode.valueOf(template$blockinfo.tileentityData.getString("mode"));
                if (tileentitystructure$mode == Mode.DATA) {
                    map.put(blockpos, template$blockinfo.tileentityData.getString("metadata"));
                }
            }
        }
    }


    public BlockPos calculateConnectedPos(PlacementSettings settings, BlockPos pos, PlacementSettings settings2, BlockPos pos2) {
        BlockPos blockpos = transformedBlockPos(settings, pos);
        BlockPos blockpos1 = transformedBlockPos(settings2, pos2);
        return blockpos.subtract(blockpos1);
    }


    public static BlockPos transformedBlockPos(PlacementSettings p_186266_0_, BlockPos p_186266_1_) {
        return transformedBlockPos(p_186266_1_, p_186266_0_.getMirror(), p_186266_0_.getRotation());
    }


    public void addBlocksToWorldChunk(World p_186260_1_, BlockPos p_186260_2_, PlacementSettings p_186260_3_) {
        //p_186260_3_.setBoundingBoxFromChunk();
        this.addBlocksToWorld(p_186260_1_, p_186260_2_, p_186260_3_);
    }


    public void addBlocksToWorld(World p_186253_1_, BlockPos p_186253_2_, PlacementSettings p_186253_3_) {
        this.addBlocksToWorld(p_186253_1_, p_186253_2_, new BlockRotationProcessor(p_186253_2_, p_186253_3_), p_186253_3_, 2);
    }


    public void addBlocksToWorld(World p_189962_1_, BlockPos p_189962_2_, PlacementSettings p_189962_3_, int p_189962_4_) {
        this.addBlocksToWorld(p_189962_1_, p_189962_2_, new BlockRotationProcessor(p_189962_2_, p_189962_3_), p_189962_3_, p_189962_4_);
    }


    //TODO
    public void addBlocksToWorld(World world, BlockPos startPos, @Nullable ITemplateProcessor proc, PlacementSettings stn, int num) {
        if (lines != EMPTY) {
            ChunkSelector selector = world.localSelector;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            final int startX = startPos.getX(), startY = startPos.getY(), startZ = startPos.getZ();
            Mirror mirror = stn.getMirror();
            Rotation rotation = stn.getRotation();

            BlockInfo inf = new BlockInfo(pos, null, null);

            for (Line line : lines) {
                transformPos(
                        startX, 0, startZ,
                        pos.setPos(line.x, 0, line.z),
                        mirror, rotation
                );

                Chunk chunk = selector.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
                for (int i = 0, s = line.sizeC(); i < s; i++) {
                    LItem item = line.getC(i);

                    pos.setY(item.y + startY);

                    inf.blockState = item.state;
                    inf.tileentityData = item.data;

                    //BlockInfo realInf = proc.processBlock(world, pos, inf);

                    //if (realInf == null) continue;
                    //chunk.setBlockState()


                }
            }
        }

    }


    public void addBlocksToWorld0(World world, BlockPos startPos, @Nullable ITemplateProcessor proc, PlacementSettings stn, int num) {
        List<BlockInfo> blocks = Lists.newArrayList();

        if ((!blocks.isEmpty() || !stn.getIgnoreEntities() && !this.entities.isEmpty()) && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
            Block block = stn.getReplacedBlock();
            StructureBoundingBox box = stn.getBoundingBox();
            Iterator var8 = blocks.iterator();

            while(true) {
                BlockPos blockpos;
                BlockInfo info0;
                Block block1;
                do {
                    do {
                        do {
                            do {
                                BlockInfo info;
                                do {
                                    if (!var8.hasNext()) {
                                        var8 = blocks.iterator();

                                        while(true) {
                                            do {
                                                do {
                                                    if (!var8.hasNext()) {
                                                        if (!stn.getIgnoreEntities()) {
                                                            this.addEntitiesToWorld(world, startPos, stn.getMirror(), stn.getRotation(), box);
                                                        }

                                                        return;
                                                    }

                                                    info = (BlockInfo)var8.next();
                                                } while(block != null && block == info.blockState.getBlock());

                                                blockpos = transformedBlockPos(stn, info.pos).add(startPos);
                                            } while(box != null && !box.isVecInside(blockpos));

                                            world.notifyNeighborsRespectDebug(blockpos, info.blockState.getBlock(), false);
                                            if (info.tileentityData != null) {
                                                TileEntity tileentity1 = world.getTileEntity(blockpos);
                                                if (tileentity1 != null) {
                                                    tileentity1.markDirty();
                                                }
                                            }
                                        }
                                    }

                                    info = (BlockInfo)var8.next();
                                    blockpos = transformedBlockPos(stn, info.pos).add(startPos);
                                } while(box != null && !box.isVecInside(blockpos));

                                info0 = null;
                                //info0 = proc != null ? proc.processBlock(world, blockpos, info) : info;
                            } while(info0 == null);

                            block1 = info0.blockState.getBlock();
                        } while(block != null && block == block1);
                    } while(stn.getIgnoreStructureBlock() && block1 == Blocks.STRUCTURE_BLOCK);
                } while(box != null && !box.isVecInside(blockpos));

                IBlockState iblockstate = info0.blockState.withMirror(stn.getMirror());
                IBlockState iblockstate1 = iblockstate.withRotation(stn.getRotation());
                TileEntity tileentity2;
                if (info0.tileentityData != null) {
                    tileentity2 = world.getTileEntity(blockpos);
                    if (tileentity2 != null) {
                        if (tileentity2 instanceof IInventory) {
                            ((IInventory)tileentity2).clear();
                        }

                        world.setBlockState(blockpos, Blocks.BARRIER.getDefaultState(), 4);
                    }
                }

                if (world.setBlockState(blockpos, iblockstate1, num) && info0.tileentityData != null) {
                    tileentity2 = world.getTileEntity(blockpos);
                    if (tileentity2 != null) {
                        info0.tileentityData.setInteger("x", blockpos.getX());
                        info0.tileentityData.setInteger("y", blockpos.getY());
                        info0.tileentityData.setInteger("z", blockpos.getZ());
                        tileentity2.readFromNBT(info0.tileentityData);
                        tileentity2.mirror(stn.getMirror());
                        tileentity2.rotate(stn.getRotation());
                    }
                }
            }
        }
    }


    private void addEntitiesToWorld(World world, BlockPos p_186263_2_, Mirror p_186263_3_, Rotation p_186263_4_, @Nullable StructureBoundingBox p_186263_5_) {
        Iterator var6 = this.entities.iterator();

        while(true) {
            EntityInfo template$entityinfo;
            BlockPos blockpos;
            do {
                if (!var6.hasNext()) {
                    return;
                }

                template$entityinfo = (EntityInfo)var6.next();
                blockpos = transformedBlockPos(template$entityinfo.blockPos, p_186263_3_, p_186263_4_).add(p_186263_2_);
            } while(p_186263_5_ != null && !p_186263_5_.isVecInside(blockpos));

            NBTTagCompound nbttagcompound = template$entityinfo.entityData;
            Vec3d vec3d = transformedVec3d(template$entityinfo.pos, p_186263_3_, p_186263_4_);
            Vec3d vec3d1 = vec3d.add((double)p_186263_2_.getX(), (double)p_186263_2_.getY(), (double)p_186263_2_.getZ());
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
                float f = entity.getMirroredYaw(p_186263_3_);
                f += entity.rotationYaw - entity.getRotatedYaw(p_186263_4_);
                entity.setLocationAndAngles(vec3d1.x, vec3d1.y, vec3d1.z, f, entity.rotationPitch);
                world.spawnEntity(entity);
            }
        }
    }


    public BlockPos transformedSize(Rotation p_186257_1_) {
        switch (p_186257_1_) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                return new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
            default:
                return this.size;
        }
    }


    private static void transformPos(int sX, int sY, int sZ, BlockPos.MutableBlockPos pos, Mirror mirror, Rotation rotation) {
        int cX = pos.getX();
        int cY = pos.getY();
        int cZ = pos.getZ();
        boolean flag = true;
        switch (mirror) {
            case LEFT_RIGHT:
                cZ = -cZ;
                break;
            case FRONT_BACK:
                cX = -cX;
                break;
            default:
                flag = false;
        }

        switch (rotation) {
            case COUNTERCLOCKWISE_90:
                pos.setPosB(cZ+sX, cY+sY, -cX+sZ);
                break;
            case CLOCKWISE_90:
                pos.setPosB(-cZ+sX, cY+sY, cX+sZ);
                break;
            case CLOCKWISE_180:
                pos.setPosB(-cX+sX, cY+sY, -cZ+sZ);
                break;
            default:
                if(flag){
                    pos.setPosB(cX+sX, cY+sY, cZ+sZ);
                }
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


    private static Vec3d transformedVec3d(Vec3d p_186269_0_, Mirror p_186269_1_, Rotation p_186269_2_) {
        double d0 = p_186269_0_.x;
        double d1 = p_186269_0_.y;
        double d2 = p_186269_0_.z;
        boolean flag = true;
        switch (p_186269_1_) {
            case LEFT_RIGHT:
                d2 = 1.0 - d2;
                break;
            case FRONT_BACK:
                d0 = 1.0 - d0;
                break;
            default:
                flag = false;
        }

        switch (p_186269_2_) {
            case COUNTERCLOCKWISE_90:
                return new Vec3d(d2, d1, 1.0 - d0);
            case CLOCKWISE_90:
                return new Vec3d(1.0 - d2, d1, d0);
            case CLOCKWISE_180:
                return new Vec3d(1.0 - d0, d1, 1.0 - d2);
            default:
                return flag ? new Vec3d(d0, d1, d2) : p_186269_0_;
        }
    }


    public BlockPos getZeroPositionWithTransform(BlockPos p_189961_1_, Mirror p_189961_2_, Rotation p_189961_3_) {
        return getZeroPositionWithTransform(p_189961_1_, p_189961_2_, p_189961_3_, this.getSize().getX(), this.getSize().getZ());
    }


    public static BlockPos getZeroPositionWithTransform(BlockPos p_191157_0_, Mirror p_191157_1_, Rotation p_191157_2_, int p_191157_3_, int p_191157_4_) {
        --p_191157_3_;
        --p_191157_4_;
        int i = p_191157_1_ == Mirror.FRONT_BACK ? p_191157_3_ : 0;
        int j = p_191157_1_ == Mirror.LEFT_RIGHT ? p_191157_4_ : 0;
        BlockPos blockpos = p_191157_0_;
        switch (p_191157_2_) {
            case COUNTERCLOCKWISE_90:
                blockpos = p_191157_0_.add(j, 0, p_191157_3_ - i);
                break;
            case CLOCKWISE_90:
                blockpos = p_191157_0_.add(p_191157_4_ - j, 0, i);
                break;
            case CLOCKWISE_180:
                blockpos = p_191157_0_.add(p_191157_3_ - i, 0, p_191157_4_ - j);
                break;
            case NONE:
                blockpos = p_191157_0_.add(i, 0, j);
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
        List<BlockInfo> blocks = Lists.newArrayList();

        BasicPalette palette = new BasicPalette();
        NBTTagList nbttaglist = new NBTTagList();

        NBTTagCompound nbttagcompound;
        for(Iterator var4 = blocks.iterator(); var4.hasNext(); nbttaglist.appendTag(nbttagcompound)) {
            BlockInfo template$blockinfo = (BlockInfo)var4.next();
            nbttagcompound = new NBTTagCompound();
            nbttagcompound.setTag("pos", this.writeInts(template$blockinfo.pos.getX(), template$blockinfo.pos.getY(), template$blockinfo.pos.getZ()));
            nbttagcompound.setInteger("state", palette.idFor(template$blockinfo.blockState));
            if (template$blockinfo.tileentityData != null) {
                nbttagcompound.setTag("nbt", template$blockinfo.tileentityData);
            }
        }

        NBTTagList nbttaglist1 = new NBTTagList();

        NBTTagCompound nbttagcompound1;
        for(Iterator var9 = this.entities.iterator(); var9.hasNext(); nbttaglist1.appendTag(nbttagcompound1)) {
            EntityInfo template$entityinfo = (EntityInfo)var9.next();
            nbttagcompound1 = new NBTTagCompound();
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
            nbttaglist2.appendTag(NBTUtil.writeBlockState(new NBTTagCompound(), iblockstate));
        }

        FMLCommonHandler.instance().getDataFixer().writeVersionData(nbt);
        nbt.setTag("palette", nbttaglist2);
        nbt.setTag("blocks", nbttaglist);
        nbt.setTag("entities", nbttaglist1);
        nbt.setTag("size", this.writeInts(this.size.getX(), this.size.getY(), this.size.getZ()));
        nbt.setString("author", this.author);
        nbt.setInteger("DataVersion", 1343);
        return nbt;
    }


    public void read(NBTTagCompound nbt) {
        lines = EMPTY;
        this.entities.clear();
        NBTTagList size = nbt.getTagList("size", 3);
        this.size = new BlockPos(size.getIntAt(0), size.getIntAt(1), size.getIntAt(2));
        this.author = nbt.getString("author");
        BasicPalette palette = new BasicPalette();
        NBTTagList cPalette = nbt.getTagList("palette", 10);

        for(int i = 0, s = cPalette.tagCount(); i < s; ++i) {
            palette.addMapping(NBTUtil.readBlockState(cPalette.getCompoundTagAt(i)), i);
        }

        NBTTagList blocks = nbt.getTagList("blocks", 10);

        Builder builder = new Builder();

        for(int j = 0; j < blocks.tagCount(); ++j) {
            NBTTagCompound nbttagcompound = blocks.getCompoundTagAt(j);
            NBTTagList pos = nbttagcompound.getTagList("pos", 3);

            int blockX = pos.getIntAt(0);
            int blockY = pos.getIntAt(1);
            int blockZ = pos.getIntAt(2);

            IBlockState state = palette.stateFor(nbttagcompound.getInteger("state"));
            NBTTagCompound tileNbt;
            if (nbttagcompound.hasKey("nbt")) {
                tileNbt = nbttagcompound.getCompoundTag("nbt");
            } else {
                tileNbt = null;
            }
            builder.add(blockX, blockY, blockZ, state, tileNbt);
        }
        lines = builder.build();

        NBTTagList cEntities = nbt.getTagList("entities", 10);

        for(int k = 0, s = cEntities.tagCount(); k < s; ++k) {
            NBTTagCompound entityTag = cEntities.getCompoundTagAt(k);
            NBTTagList cPos = entityTag.getTagList("pos", 6);
            Vec3d vec3d = new Vec3d(cPos.getDoubleAt(0), cPos.getDoubleAt(1), cPos.getDoubleAt(2));
            NBTTagList cBlockPos = entityTag.getTagList("blockPos", 3);
            BlockPos pos = new BlockPos(cBlockPos.getIntAt(0), cBlockPos.getIntAt(1), cBlockPos.getIntAt(2));
            if (entityTag.hasKey("nbt")) {
                NBTTagCompound cNbt = entityTag.getCompoundTag("nbt");
                this.entities.add(new EntityInfo(vec3d, pos, cNbt));
            }
        }
    }


    private NBTTagList writeInts(int... data) {
        NBTTagList nbttaglist = new NBTTagList();
        int[] var3 = data;
        int var4 = data.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            int i = var3[var5];
            nbttaglist.appendTag(new NBTTagInt(i));
        }

        return nbttaglist;
    }


    private NBTTagList writeDoubles(double... data) {
        NBTTagList nbttaglist = new NBTTagList();
        double[] var3 = data;
        int var4 = data.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            double d0 = var3[var5];
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


    private static class Builder extends Int2ObjectOpenHashMap<Line>{
        public void add(int x, int y, int z, IBlockState state, NBTTagCompound data){
            int key = pos2key(x, z);
            Line line = get(key);
            if(line == null){
                put(key, line = new Line(x, z));
            }
            line.add(y, state, data);
        }

        private int pos2key(int x, int z){
            return ((x & 0xFFFF) << 16) | (z & 0xFFFF);
        }


        public Line[] build(){
            Line[] lines = new Line[size()];
            int i = 0;
            for (Line entry:values()){
                (lines[i] = entry).trimToSize();
                ++i;
            }
            return lines;
        }
    }


    private static class Line extends ArrayListE6<LItem> {
        private final int x, z;
        private Line(int x, int z) {
            this.x = x;
            this.z = z;
        }
        public void add(int y, IBlockState state, NBTTagCompound data){
            add(new LItem(y, state, data));
        }

        @Override
        public int hashCode() {
            return x * 13 + z;
        }
    }


    private static class LItem{
        private final int y;
        private final IBlockState state;
        private final NBTTagCompound data;

        private LItem(int y, IBlockState state, NBTTagCompound data) {
            this.y = y;
            this.state = state;
            this.data = data;
        }

        @Override
        public int hashCode() {
            return (y * 13) + state.hashCode() * 13;
        }
    }


    public static class BlockInfo {
        public BlockPos pos;
        public IBlockState blockState;
        public NBTTagCompound tileentityData;

        public BlockInfo(BlockPos pos, IBlockState state, @Nullable NBTTagCompound nbt) {
            this.pos = pos;
            this.blockState = state;
            this.tileentityData = nbt;
        }
    }


    static class BasicPalette implements Iterable<IBlockState> {
        public static final IBlockState DEFAULT_BLOCK_STATE;
        final ObjectIntIdentityMap<IBlockState> ids;
        private int lastId;

        private BasicPalette() {
            this.ids = new ObjectIntIdentityMap(16);
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
        public IBlockState stateFor(int state) {
            IBlockState iblockstate = this.ids.getByValue(state);
            return iblockstate == null ? DEFAULT_BLOCK_STATE : iblockstate;
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
