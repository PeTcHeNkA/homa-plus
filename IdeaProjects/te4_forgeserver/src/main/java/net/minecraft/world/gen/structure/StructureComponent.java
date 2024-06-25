package net.minecraft.world.gen.structure;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.TemplateManager;
import plus.fserver.server.elca.ChunkSelector;


public abstract class StructureComponent {
    protected StructureBoundingBox boundingBox;
    @Nullable
    private EnumFacing coordBaseMode;
    private Mirror mirror;
    private Rotation rotation;
    protected int componentType;

    public StructureComponent() {}


    protected StructureComponent(int type) {
        this.componentType = type;
    }


    public final NBTTagCompound createStructureBaseNBT() {
        if (MapGenStructureIO.getStructureComponentName(this) == null)
            // Friendlier error then the Null String error below.
        {
            throw new RuntimeException("StructureComponent \"" + this.getClass().getName() + "\" missing ID Mapping, Modder see MapGenStructureIO");
        }
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setString("id", MapGenStructureIO.getStructureComponentName(this));
        nbttagcompound.setTag("BB", this.boundingBox.toNBTTagIntArray());
        EnumFacing enumfacing = this.getCoordBaseMode();
        nbttagcompound.setInteger("O", enumfacing == null ? -1 : enumfacing.getHorizontalIndex());
        nbttagcompound.setInteger("GD", this.componentType);
        this.writeStructureToNBT(nbttagcompound);
        return nbttagcompound;
    }


    protected abstract void writeStructureToNBT(NBTTagCompound tagCompound);


    public void readStructureBaseNBT(World worldIn, NBTTagCompound tagCompound) {
        if (tagCompound.hasKey("BB")) {
            this.boundingBox = new StructureBoundingBox(tagCompound.getIntArray("BB"));
        }
        int i = tagCompound.getInteger("O");
        this.setCoordBaseMode(i == -1 ? null : EnumFacing.byHorizontalIndex(i));
        this.componentType = tagCompound.getInteger("GD");
        this.readStructureFromNBT(tagCompound, worldIn.getSaveHandler().getStructureTemplateManager());
    }


    protected abstract void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_);


    public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand) {
    }


    public abstract boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn);


    public StructureBoundingBox getBoundingBox() {
        return this.boundingBox;
    }


    public int getComponentType() {
        return this.componentType;
    }


    public static StructureComponent findIntersecting(List<StructureComponent> list, StructureBoundingBox box) {
        for (StructureComponent comp : list) {
            if (comp.getBoundingBox() != null && comp.getBoundingBox().intersectsWith(box)) {
                return comp;
            }
        }
        return null;
    }


    protected boolean isLiquidInStructureBoundingBox(World worldIn, StructureBoundingBox box) {
        return isLiquidInStructureBoundingBox(new ChunkSelector(worldIn), box);
    }


    protected boolean isLiquidInStructureBoundingBox(ChunkSelector selector, StructureBoundingBox box) {
        int x1 = Math.max(this.boundingBox.minX - 1, box.minX);
        int y1 = Math.max(this.boundingBox.minY - 1, box.minY);
        int z1 = Math.max(this.boundingBox.minZ - 1, box.minZ);
        int x2 = Math.min(this.boundingBox.maxX + 1, box.maxX);
        int y2 = Math.min(this.boundingBox.maxY + 1, box.maxY);
        int z2 = Math.min(this.boundingBox.maxZ + 1, box.maxZ);

        for (int x = x1; x <= x2; ++x) {
            for (int z = z1; z <= z2; ++z) {
                if(y1 > 255 || y1 <1)continue;
                if (selector.getStateUnchecked(x, y1, z).getMaterial().isLiquid()) {
                    return true;
                }
                if(y2 > 255 || y2 <1)continue;

                if (selector.getCurStateUnchecked(x, y2, z).getMaterial().isLiquid()) {
                    return true;
                }
            }
        }
        for (int x = x1; x <= x2; ++x) {
            for (int y = y1; y <= y2; ++y) {
                if(y > 255 || y <1)continue;
                if (selector.getStateUnchecked(x, y, z1).getMaterial().isLiquid()) {
                    return true;
                }
                if (selector.getStateUnchecked(x, y, z2).getMaterial().isLiquid()) {
                    return true;
                }
            }
        }
        for (int z = z1; z <= z2; ++z) {
            for (int y = y1; y <= y2; ++y) {
                if(y > 255 || y <1)continue;
                if (selector.getStateUnchecked(x1, y, z).getMaterial().isLiquid()) {
                    return true;
                }
                if (selector.getStateUnchecked(x2, y, z).getMaterial().isLiquid()) {
                    return true;
                }
            }
        }
        return false;
    }


    protected int getXWithOffset(int x, int z) {
        EnumFacing enumfacing = this.getCoordBaseMode();
        if (enumfacing == null) {
            return x;
        } else {
            switch (enumfacing) {
                case NORTH:
                case SOUTH:
                    return this.boundingBox.minX + x;
                case WEST:
                    return this.boundingBox.maxX - z;
                case EAST:
                    return this.boundingBox.minX + z;
                default:
                    return x;
            }
        }
    }


    protected int getYWithOffset(int y) {
        return this.getCoordBaseMode() == null ? y : y + this.boundingBox.minY;
    }


    protected int getZWithOffset(int x, int z) {
        EnumFacing facing = this.getCoordBaseMode();
        if (facing == null) {
            return z;
        } else {
            switch (facing) {
                case NORTH:
                    return this.boundingBox.maxZ - z;
                case SOUTH:
                    return this.boundingBox.minZ + z;
                case WEST:
                case EAST:
                    return this.boundingBox.minZ + x;
                default:
                    return z;
            }
        }
    }


    protected void setBlockState(World worldIn, IBlockState state,
                                 int x, int y, int z, StructureBoundingBox box) {
        BlockPos blockpos = new BlockPos(
                this.getXWithOffset(x, z),
                this.getYWithOffset(y),
                this.getZWithOffset(x, z)
        );

        if (box.isVecInside(blockpos)) {
            if (this.mirror != Mirror.NONE) {
                state = state.withMirror(this.mirror);
            }
            if (this.rotation != Rotation.NONE) {
                state = state.withRotation(this.rotation);
            }
            worldIn.setBlockState(blockpos, state, 2);
        }
    }


    protected void setBlockState(ChunkSelector selector, IBlockState state,
                                 int x, int y, int z, StructureBoundingBox box) {
        BlockPos blockpos = new BlockPos(
                this.getXWithOffset(x, z),
                this.getYWithOffset(y),
                this.getZWithOffset(x, z)
        );

        if (box.isVecInside(blockpos)) {
            if (this.mirror != Mirror.NONE) {
                state = state.withMirror(this.mirror);
            }
            if (this.rotation != Rotation.NONE) {
                state = state.withRotation(this.rotation);
            }
            selector.setBlockState(blockpos, state);
        }
    }


    protected IBlockState getBlockStateFromPos(ChunkSelector selector, int x, int y, int z, StructureBoundingBox box) {
        BlockPos pos;
        return !box.isVecInside(pos = new BlockPos(
                this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z))
        ) ? BlockIDIndexes.AIR_STATE : selector.getState(pos);
    }


    protected IBlockState getBlockStateFromPos(World worldIn, int x, int y, int z, StructureBoundingBox box) {
        BlockPos pos;
        return !box.isVecInside(pos = new BlockPos(
                this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z))
        ) ? BlockIDIndexes.AIR_STATE : worldIn.getBlockState(pos);
    }


    protected int getSkyBrightness(World worldIn, int x, int y, int z, StructureBoundingBox box) {
        BlockPos pos;
        return !box.isVecInside(pos = new BlockPos(
                this.getXWithOffset(x, z), this.getYWithOffset(y + 1), this.getZWithOffset(x, z))
        ) ? EnumSkyBlock.SKY.defaultLightValue : worldIn.getLightFor(EnumSkyBlock.SKY, pos);
    }


    protected int getSkyBrightness(ChunkSelector selector, int x, int y, int z, StructureBoundingBox box) {
        BlockPos pos;
        return !box.isVecInside(pos = new BlockPos(
                this.getXWithOffset(x, z), this.getYWithOffset(y + 1), this.getZWithOffset(x, z))
        ) ? EnumSkyBlock.SKY.defaultLightValue : selector.getLightFor(EnumSkyBlock.SKY, pos);
    }


    protected void fillWithAir(World worldIn, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        ChunkSelector selector = new ChunkSelector(worldIn);
        for (int i = minY; i <= maxY; ++i) {
            for (int j = minX; j <= maxX; ++j) {
                for (int k = minZ; k <= maxZ; ++k) {
                    this.setBlockState(selector, BlockIDIndexes.AIR_STATE, j, i, k, box);
                }
            }
        }
    }


    protected void fillWithBlocks(World worldIn, StructureBoundingBox box,
                                  int xMin, int yMin, int zMin, int xMax, int yMax, int zMax,
                                  IBlockState boundaryBlockState, IBlockState insideBlockState,
                                  boolean existingOnly) {
        fillWithBlocks(new ChunkSelector(worldIn),
                box, xMin, yMin, zMin, xMax, yMax, zMax,
                boundaryBlockState, insideBlockState, existingOnly
        );
    }


    protected void fillWithBlocks(ChunkSelector selector, StructureBoundingBox box,
                                  int xMin, int yMin, int zMin, int xMax, int yMax, int zMax,
                                  IBlockState bound, IBlockState inside,
                                  boolean existingOnly) {

        for (int x = xMin; x <= xMax; ++x) {
            for (int z = zMin; z <= zMax; ++z) {
                for (int y = yMin; y <= yMax; ++y) {
                    if (!existingOnly || this.getBlockStateFromPos(selector, x, y, z, box)
                            .getMaterial() != Material.AIR) {
                        if (y != yMin && y != yMax && x != xMin && x != xMax && z != zMin && z != zMax) {
                            this.setBlockState(selector, inside, x, y, z, box);
                        } else {
                            this.setBlockState(selector, bound, x, y, z, box);
                        }
                    }
                }
            }
        }
    }


    protected void fillWithRandomizedBlocks(World worldIn, StructureBoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean alwaysReplace, Random rand, StructureComponent.BlockSelector blockselector) {
        for (int i = minY; i <= maxY; ++i) {
            for (int j = minX; j <= maxX; ++j) {
                for (int k = minZ; k <= maxZ; ++k) {
                    if (!alwaysReplace || this.getBlockStateFromPos(worldIn, j, i, k, box).getMaterial() != Material.AIR) {
                        blockselector.selectBlocks(rand, j, i, k, i == minY || i == maxY || j == minX || j == maxX || k == minZ || k == maxZ);
                        this.setBlockState(worldIn, blockselector.getBlockState(), j, i, k, box);
                    }
                }
            }
        }
    }


    protected void generateMaybeBox(ChunkSelector selector, StructureBoundingBox sbb, Random rand, float chance, int x1, int y1, int z1, int x2, int y2, int z2, IBlockState edgeState, IBlockState state, boolean requireNonAir, int requiredSkylight) {
        for (int i = y1; i <= y2; ++i) {
            for (int j = x1; j <= x2; ++j) {
                for (int k = z1; k <= z2; ++k) {
                    if (rand.nextFloat() <= chance && (!requireNonAir ||
                            this.getBlockStateFromPos(selector, j, i, k, sbb).getMaterial() != Material.AIR) && (requiredSkylight <= 0 ||
                            this.getSkyBrightness(selector, j, i, k, sbb) < requiredSkylight)) {
                        if (i != y1 && i != y2 && j != x1 && j != x2 && k != z1 && k != z2) {
                            this.setBlockState(selector, state, j, i, k, sbb);
                        } else {
                            this.setBlockState(selector, edgeState, j, i, k, sbb);
                        }
                    }
                }
            }
        }
    }


    protected void generateMaybeBox(World worldIn, StructureBoundingBox sbb, Random rand, float chance, int x1, int y1, int z1, int x2, int y2, int z2, IBlockState edgeState, IBlockState state, boolean requireNonAir, int requiredSkylight) {
        for (int i = y1; i <= y2; ++i) {
            for (int j = x1; j <= x2; ++j) {
                for (int k = z1; k <= z2; ++k) {
                    if (rand.nextFloat() <= chance && (!requireNonAir ||
                            this.getBlockStateFromPos(worldIn, j, i, k, sbb).getMaterial() != Material.AIR) && (requiredSkylight <= 0 ||
                            this.getSkyBrightness(worldIn, j, i, k, sbb) < requiredSkylight)) {
                        if (i != y1 && i != y2 && j != x1 && j != x2 && k != z1 && k != z2) {
                            this.setBlockState(worldIn, state, j, i, k, sbb);
                        } else {
                            this.setBlockState(worldIn, edgeState, j, i, k, sbb);
                        }
                    }
                }
            }
        }
    }


    protected void randomlyPlaceBlock(ChunkSelector selector, StructureBoundingBox box, Random rand, float chance, int x, int y, int z, IBlockState blockstateIn) {
        if (rand.nextFloat() < chance) {
            this.setBlockState(selector, blockstateIn, x, y, z, box);
        }
    }


    protected void randomlyPlaceBlock(World worldIn, StructureBoundingBox box, Random rand, float chance, int x, int y, int z, IBlockState blockstateIn) {
        if (rand.nextFloat() < chance) {
            this.setBlockState(worldIn, blockstateIn, x, y, z, box);
        }
    }


    protected void randomlyRareFillWithBlocks(World worldIn, StructureBoundingBox box,
                                              int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                                              IBlockState state, boolean excludeAir) {

        randomlyRareFillWithBlocks(new ChunkSelector(worldIn), box,
                minX, minY, minZ, maxX, maxY, maxZ, state, excludeAir
        );
    }


    protected void randomlyRareFillWithBlocks(ChunkSelector selector, StructureBoundingBox box,
                                              int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                                              IBlockState state, boolean excludeAir) {
        float f = (float)(maxX - minX + 1);
        float f1 = (float)(maxY - minY + 1);
        float f2 = (float)(maxZ - minZ + 1);
        float f3 = (float)minX + f / 2.0F;
        float f4 = (float)minZ + f2 / 2.0F;

        for (int i = minY; i <= maxY; ++i) {
            float f5 = (float)(i - minY) / f1;

            for (int j = minX; j <= maxX; ++j) {
                float f6 = ((float)j - f3) / (f * 0.5F);

                for (int k = minZ; k <= maxZ; ++k) {
                    float f7 = ((float)k - f4) / (f2 * 0.5F);

                    if (!excludeAir || this.getBlockStateFromPos(selector, j, i, k, box).getMaterial() != Material.AIR) {
                        float f8 = f6 * f6 + f5 * f5 + f7 * f7;

                        if (f8 <= 1.05F) {
                            this.setBlockState(selector, state, j, i, k, box);
                        }
                    }
                }
            }
        }
    }


    protected void clearCurrentPositionBlocksUpwards(ChunkSelector selector, int x, int y, int z, StructureBoundingBox structurebb) {
        BlockPos blockpos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));

        if (structurebb.isVecInside(blockpos)) {
            while (!selector.isAirBlock(blockpos) && blockpos.getY() < 255) {
                selector.setBlockState(blockpos, BlockIDIndexes.AIR_STATE);
                blockpos = blockpos.up();
            }
        }
    }


    protected void clearCurrentPositionBlocksUpwards(World worldIn, int x, int y, int z, StructureBoundingBox structurebb) {
        BlockPos blockpos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));

        if (structurebb.isVecInside(blockpos)) {
            ChunkSelector selector = new ChunkSelector(worldIn);
            while (!selector.isAirBlock(blockpos) && blockpos.getY() < 255) {
                selector.setBlockState(blockpos, BlockIDIndexes.AIR_STATE);
                blockpos = blockpos.up();
            }
        }
    }


    protected void replaceAirAndLiquidDownwards(ChunkSelector selector, IBlockState state, int x, int y, int z, StructureBoundingBox boundingboxIn) {
        int i = this.getXWithOffset(x, z);
        int j = this.getYWithOffset(y);
        int k = this.getZWithOffset(x, z);

        if (boundingboxIn.isVecInside(new BlockPos(i, j, k))) {
            BlockPos pos;
            while (j > 1 && (selector.isAirBlock(pos = new BlockPos(i, j, k)) ||
                    selector.getCurState(pos).getMaterial().isLiquid())) {

                selector.setBlockState(pos, state);
                --j;
            }
        }
    }


    protected void replaceAirAndLiquidDownwards(World worldIn, IBlockState state, int x, int y, int z, StructureBoundingBox boundingboxIn) {
        int i = this.getXWithOffset(x, z);
        int j = this.getYWithOffset(y);
        int k = this.getZWithOffset(x, z);

        if (boundingboxIn.isVecInside(new BlockPos(i, j, k))) {
            ChunkSelector selector = new ChunkSelector(worldIn);
            BlockPos pos;
            while (j > 1 && (selector.isAirBlock(pos = new BlockPos(i, j, k)) ||
                    selector.getCurState(pos).getMaterial().isLiquid())) {

                selector.setBlockState(pos, state);
                --j;
            }
        }
    }


    protected boolean generateChest(World worldIn, StructureBoundingBox structurebb, Random randomIn, int x, int y, int z, ResourceLocation loot) {
        BlockPos blockpos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
        return this.generateChest(worldIn, structurebb, randomIn, blockpos, loot, (IBlockState)null);
    }


    protected boolean generateChest(World world, StructureBoundingBox box, Random rand, BlockPos pos, ResourceLocation lootTable, @Nullable IBlockState state) {
        if (box.isVecInside(pos) && world.getBlockState(pos).getBlock() != Blocks.CHEST) {
            if (state == null) {
                state = Blocks.CHEST.correctFacing(world, pos, Blocks.CHEST.getDefaultState());
            }

            world.setBlockState(pos, state, 2);
            TileEntity tileentity = world.getTileEntity(pos);

            if (tileentity instanceof TileEntityChest) {
                ((TileEntityChest)tileentity).setLootTable(lootTable, rand.nextLong());
            }
            return true;
        } else {
            return false;
        }
    }


    protected boolean createDispenser(World worldIn, StructureBoundingBox sbb, Random rand, int x, int y, int z, EnumFacing facing, ResourceLocation lootTableIn) {
        BlockPos blockpos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));

        if (sbb.isVecInside(blockpos) && worldIn.getBlockState(blockpos).getBlock() != Blocks.DISPENSER) {
            this.setBlockState(worldIn, Blocks.DISPENSER.getDefaultState().withProperty(BlockDispenser.FACING, facing), x, y, z, sbb);
            TileEntity tile = worldIn.getTileEntity(blockpos);

            if (tile instanceof TileEntityDispenser) {
                ((TileEntityDispenser)tile).setLootTable(lootTableIn, rand.nextLong());
            }
            return true;
        } else {
            return false;
        }
    }


    protected void generateDoor(ChunkSelector selector, StructureBoundingBox sbb, Random rand, int x, int y, int z, EnumFacing facing, BlockDoor door) {
        this.setBlockState(selector, door.getDefaultState().withProperty(BlockDoor.FACING, facing), x, y, z, sbb);
        this.setBlockState(selector, door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), x, y + 1, z, sbb);
    }


    protected void generateDoor(World worldIn, StructureBoundingBox sbb, Random rand, int x, int y, int z, EnumFacing facing, BlockDoor door) {
        this.setBlockState(worldIn, door.getDefaultState().withProperty(BlockDoor.FACING, facing), x, y, z, sbb);
        this.setBlockState(worldIn, door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), x, y + 1, z, sbb);
    }


    public void offset(int x, int y, int z) {
        this.boundingBox.offset(x, y, z);
    }


    @Nullable
    public EnumFacing getCoordBaseMode()
    {
        return this.coordBaseMode;
    }


    public void setCoordBaseMode(@Nullable EnumFacing facing) {
        this.coordBaseMode = facing;

        if (facing == null) {
            this.rotation = Rotation.NONE;
            this.mirror = Mirror.NONE;
        } else {
            switch (facing) {
                case SOUTH:
                    this.mirror = Mirror.LEFT_RIGHT;
                    this.rotation = Rotation.NONE;
                    break;
                case WEST:
                    this.mirror = Mirror.LEFT_RIGHT;
                    this.rotation = Rotation.CLOCKWISE_90;
                    break;
                case EAST:
                    this.mirror = Mirror.NONE;
                    this.rotation = Rotation.CLOCKWISE_90;
                    break;
                default:
                    this.mirror = Mirror.NONE;
                    this.rotation = Rotation.NONE;
            }
        }
    }


    public abstract static class BlockSelector {
        protected IBlockState blockstate = BlockIDIndexes.AIR_STATE;

        public abstract void selectBlocks(Random rand, int x, int y, int z, boolean wall);


        public IBlockState getBlockState() {
            return this.blockstate;
        }
    }
}