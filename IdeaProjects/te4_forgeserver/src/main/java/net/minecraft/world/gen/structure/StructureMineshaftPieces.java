package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.loot.LootTableList;
import plus.fserver.server.elca.ChunkSelector;


public class StructureMineshaftPieces {
    public static void registerStructurePieces() {
        MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Corridor.class, "MSCorridor");
        MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Cross.class, "MSCrossing");
        MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Room.class, "MSRoom");
        MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Stairs.class, "MSStairs");
    }


    private static StructureMineshaftPieces.Peice createRandomShaftPiece(List<StructureComponent> list, Random rand, int x, int y, int z, @Nullable EnumFacing facing, int num, MapGenMineshaft.Type type) {
        int i = rand.nextInt(100);

        if (i >= 80) {
            StructureBoundingBox structureboundingbox = StructureMineshaftPieces.Cross.findCrossing(list, rand, x, y, z, facing);

            if (structureboundingbox != null) {
                return new StructureMineshaftPieces.Cross(num, rand, structureboundingbox, facing, type);
            }
        } else if (i >= 70) {
            StructureBoundingBox box = StructureMineshaftPieces.Stairs.findStairs(list, rand, x, y, z, facing);

            if (box != null) {
                return new StructureMineshaftPieces.Stairs(num, rand, box, facing, type);
            }
        } else {
            StructureBoundingBox structureboundingbox2 = StructureMineshaftPieces.Corridor.findCorridorSize(list, rand, x, y, z, facing);

            if (structureboundingbox2 != null) {
                return new StructureMineshaftPieces.Corridor(num, rand, structureboundingbox2, facing, type);
            }
        }
        return null;
    }


    private static StructureMineshaftPieces.Peice generateAndAddPiece(StructureComponent comp, List<StructureComponent> list, Random rand, int x, int y, int z, EnumFacing facing, int num) {
        if (num > 8) {
            return null;
        } else if (Math.abs(x - comp.getBoundingBox().minX) <= 80 && Math.abs(z - comp.getBoundingBox().minZ) <= 80) {
            MapGenMineshaft.Type mapgenmineshaft$type = ((StructureMineshaftPieces.Peice)comp).mineShaftType;
            StructureMineshaftPieces.Peice piece = createRandomShaftPiece(list, rand, x, y, z, facing, num + 1, mapgenmineshaft$type);

            if (piece != null) {
                list.add(piece);
                piece.buildComponent(comp, list, rand);
            }
            return piece;
        } else {
            return null;
        }
    }


    public static class Corridor extends StructureMineshaftPieces.Peice {
        private boolean hasRails;
        private boolean hasSpiders;
        private boolean spawnerPlaced;
        private int sectionCount;

        public Corridor() {}


        @Override
        protected void writeStructureToNBT(NBTTagCompound tag) {
            super.writeStructureToNBT(tag);
            tag.setBoolean("hr", this.hasRails);
            tag.setBoolean("sc", this.hasSpiders);
            tag.setBoolean("hps", this.spawnerPlaced);
            tag.setInteger("Num", this.sectionCount);
        }


        @Override
        protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
            super.readStructureFromNBT(tag, manager);
            this.hasRails = tag.getBoolean("hr");
            this.hasSpiders = tag.getBoolean("sc");
            this.spawnerPlaced = tag.getBoolean("hps");
            this.sectionCount = tag.getInteger("Num");
        }


        public Corridor(int num, Random rand, StructureBoundingBox box, EnumFacing facing, MapGenMineshaft.Type type) {
            super(num, type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
            this.hasRails = rand.nextInt(3) == 0;
            this.hasSpiders = !this.hasRails && rand.nextInt(23) == 0;

            if (this.getCoordBaseMode().getAxis() == EnumFacing.Axis.Z) {
                this.sectionCount = box.getZSize() / 5;
            } else {
                this.sectionCount = box.getXSize() / 5;
            }
        }


        public static StructureBoundingBox findCorridorSize(List<StructureComponent> list, Random rand, int x, int y, int z, EnumFacing facing) {
            StructureBoundingBox structureboundingbox = new StructureBoundingBox(x, y, z, x, y + 2, z);
            int i;

            for (i = rand.nextInt(3) + 2; i > 0; --i) {
                int j = i * 5;

                switch (facing) {
                    case NORTH:
                    default:
                        structureboundingbox.maxX = x + 2;
                        structureboundingbox.minZ = z - (j - 1);
                        break;
                    case SOUTH:
                        structureboundingbox.maxX = x + 2;
                        structureboundingbox.maxZ = z + (j - 1);
                        break;
                    case WEST:
                        structureboundingbox.minX = x - (j - 1);
                        structureboundingbox.maxZ = z + 2;
                        break;
                    case EAST:
                        structureboundingbox.maxX = x + (j - 1);
                        structureboundingbox.maxZ = z + 2;
                }

                if (StructureComponent.findIntersecting(list, structureboundingbox) == null) {
                    break;
                }
            }
            return i > 0 ? structureboundingbox : null;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            int i = this.getComponentType();
            int j = rand.nextInt(4);
            EnumFacing enumfacing = this.getCoordBaseMode();

            if (enumfacing != null) {
                switch (enumfacing) {
                    case NORTH:
                    default:
                        if (j <= 1) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ - 1, enumfacing, i);
                        }
                        else if (j == 2) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, EnumFacing.WEST, i);
                        } else {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, EnumFacing.EAST, i);
                        }

                        break;
                    case SOUTH:
                        if (j <= 1) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ + 1, enumfacing, i);
                        }
                        else if (j == 2) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ - 3, EnumFacing.WEST, i);
                        } else {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ - 3, EnumFacing.EAST, i);
                        }
                        break;
                    case WEST:
                        if (j <= 1) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, enumfacing, i);
                        } else if (j == 2) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                        } else {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                        }
                        break;
                    case EAST:
                        if (j <= 1) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ, enumfacing, i);
                        }
                        else if (j == 2) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.maxX - 3, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                        } else {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.maxX - 3, this.boundingBox.minY - 1 + rand.nextInt(3), this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                        }
                }
            }

            if (i < 8) {
                if (enumfacing != EnumFacing.NORTH && enumfacing != EnumFacing.SOUTH) {
                    for (int i1 = this.boundingBox.minX + 3; i1 + 3 <= this.boundingBox.maxX; i1 += 5) {
                        int j1 = rand.nextInt(5);

                        if (j1 == 0) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, i1, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, i + 1);
                        } else if (j1 == 1) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, i1, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i + 1);
                        }
                    }
                } else {
                    for (int k = this.boundingBox.minZ + 3; k + 3 <= this.boundingBox.maxZ; k += 5) {
                        int l = rand.nextInt(5);

                        if (l == 0) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY, k, EnumFacing.WEST, i + 1);
                        } else if (l == 1) {
                            StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, k, EnumFacing.EAST, i + 1);
                        }
                    }
                }
            }
        }


        @Override
        protected boolean generateChest(World worldIn, StructureBoundingBox box, Random randomIn, int x, int y, int z, ResourceLocation loot) {
            BlockPos pos = new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));

            if (box.isVecInside(pos) && worldIn.getBlockState(pos).getMaterial() == Material.AIR &&
                    worldIn.getBlockState(pos.down()).getMaterial() != Material.AIR) {
                IBlockState iblockstate = Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE,
                        randomIn.nextBoolean() ? BlockRailBase.EnumRailDirection.NORTH_SOUTH : BlockRailBase.EnumRailDirection.EAST_WEST);
                this.setBlockState(worldIn, iblockstate, x, y, z, box);
                EntityMinecartChest chest = new EntityMinecartChest(worldIn, (float)pos.getX() + 0.5F, (float)pos.getY() + 0.5F, (float)pos.getZ() + 0.5F);
                chest.setLootTable(loot, randomIn.nextLong());
                worldIn.spawnEntity(chest);
                return true;
            } else {
                return false;
            }
        }


        @Override
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(worldIn);
            if (this.isLiquidInStructureBoundingBox(selector, box)) {
                return false;
            } else {
                int i1 = this.sectionCount * 5 - 1;
                IBlockState iblockstate = this.getPlanksBlock();
                this.fillWithBlocks(selector, box, 0, 0, 0, 2, 1, i1, BlockIDIndexes.AIR_STATE, BlockIDIndexes.AIR_STATE, false);
                this.generateMaybeBox(selector, box, randomIn, 0.8F, 0, 2, 0, 2, 2, i1, BlockIDIndexes.AIR_STATE, BlockIDIndexes.AIR_STATE, false, 0);

                if (this.hasSpiders) {
                    this.generateMaybeBox(selector, box, randomIn, 0.6F, 0, 0, 0, 2, 1, i1, BlockIDIndexes.AIR_STATE, BlockIDIndexes.AIR_STATE, false, 8);
                }

                for (int j1 = 0; j1 < this.sectionCount; ++j1) {
                    int k1 = 2 + j1 * 5;
                    this.placeSupport(selector, box, 0, 0, k1, 2, 2, randomIn);
                    this.placeCobWeb(selector, box, randomIn, 0.1F, 0, 2, k1 - 1);
                    this.placeCobWeb(selector, box, randomIn, 0.1F, 2, 2, k1 - 1);
                    this.placeCobWeb(selector, box, randomIn, 0.1F, 0, 2, k1 + 1);
                    this.placeCobWeb(selector, box, randomIn, 0.1F, 2, 2, k1 + 1);
                    this.placeCobWeb(selector, box, randomIn, 0.05F, 0, 2, k1 - 2);
                    this.placeCobWeb(selector, box, randomIn, 0.05F, 2, 2, k1 - 2);
                    this.placeCobWeb(selector, box, randomIn, 0.05F, 0, 2, k1 + 2);
                    this.placeCobWeb(selector, box, randomIn, 0.05F, 2, 2, k1 + 2);

                    if (randomIn.nextInt(100) == 0) {
                        this.generateChest(worldIn, box, randomIn, 2, 0, k1 - 1, LootTableList.CHESTS_ABANDONED_MINESHAFT);
                    }

                    if (randomIn.nextInt(100) == 0) {
                        this.generateChest(worldIn, box, randomIn, 0, 0, k1 + 1, LootTableList.CHESTS_ABANDONED_MINESHAFT);
                    }

                    if (this.hasSpiders && !this.spawnerPlaced) {
                        int l1 = this.getYWithOffset(0);
                        int i2 = k1 - 1 + randomIn.nextInt(3);
                        int j2 = this.getXWithOffset(1, i2);
                        int k2 = this.getZWithOffset(1, i2);
                        BlockPos blockpos = new BlockPos(j2, l1, k2);

                        if (box.isVecInside(blockpos) && this.getSkyBrightness(selector, 1, 0, i2, box) < 8) {
                            this.spawnerPlaced = true;
                            selector.setBlockState(blockpos, Blocks.MOB_SPAWNER.getDefaultState(), 2);
                            TileEntity tileentity = worldIn.getTileEntity(blockpos);

                            if (tileentity instanceof TileEntityMobSpawner) {
                                ((TileEntityMobSpawner)tileentity).getSpawnerBaseLogic().setEntityId(EntityList.getKey(EntityCaveSpider.class));
                            }
                        }
                    }
                }

                for (int l2 = 0; l2 <= 2; ++l2) {
                    for (int i3 = 0; i3 <= i1; ++i3) {
                        IBlockState state = this.getBlockStateFromPos(selector, l2, -1, i3, box);

                        if (state.getMaterial() == Material.AIR && this.getSkyBrightness(selector, l2, -1, i3, box) < 8) {
                            this.setBlockState(selector, iblockstate, l2, -1, i3, box);
                        }
                    }
                }

                if (this.hasRails) {
                    IBlockState state = Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);

                    for (int j3 = 0; j3 <= i1; ++j3) {
                        IBlockState toReplace = this.getBlockStateFromPos(selector, 1, -1, j3, box);

                        if (toReplace.getMaterial() != Material.AIR && toReplace.isFullBlock()) {
                            float f = this.getSkyBrightness(selector, 1, 0, j3, box) > 8 ? 0.9F : 0.7F;
                            this.randomlyPlaceBlock(selector, box, randomIn, f, 1, 0, j3, state);
                        }
                    }
                }
                return true;
            }
        }


        private void placeSupport(World world, StructureBoundingBox box, int x, int y, int z, int maxY, int maxX, Random rand) {
            placeSupport(new ChunkSelector(world), box, x, y, z, maxY, maxX, rand);
        }


        private void placeSupport(ChunkSelector selector, StructureBoundingBox box, int x, int y, int z, int maxY, int maxX, Random rand) {
            if (this.isSupportingBox(selector, box, x, maxX, maxY, z)) {
                IBlockState planks = this.getPlanksBlock();
                IBlockState fence = this.getFenceBlock();
                IBlockState air = BlockIDIndexes.AIR_STATE;
                this.fillWithBlocks(selector, box, x, y, z, x, maxY - 1, z, fence, air, false);
                this.fillWithBlocks(selector, box, maxX, y, z, maxX, maxY - 1, z, fence, air, false);

                if (rand.nextInt(4) == 0) {
                    this.fillWithBlocks(selector, box, x, maxY, z, x, maxY, z, planks, air, false);
                    this.fillWithBlocks(selector, box, maxX, maxY, z, maxX, maxY, z, planks, air, false);
                } else {
                    this.fillWithBlocks(selector, box, x, maxY, z, maxX, maxY, z, planks, air, false);
                    this.randomlyPlaceBlock(selector, box, rand, 0.05F, x + 1, maxY, z - 1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH));
                    this.randomlyPlaceBlock(selector, box, rand, 0.05F, x + 1, maxY, z + 1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH));
                }
            }
        }


        private void placeCobWeb(World world, StructureBoundingBox box, Random random, float chance, int dx, int dy, int dz) {
            if (this.getSkyBrightness(world, dx, dy, dz, box) < 8) {
                this.randomlyPlaceBlock(world, box, random, chance, dx, dy, dz, Blocks.WEB.getDefaultState());
            }
        }


        private void placeCobWeb(ChunkSelector selector, StructureBoundingBox box, Random random, float chance, int dx, int dy, int dz) {
            if (this.getSkyBrightness(selector, dx, dy, dz, box) < 8) {
                this.randomlyPlaceBlock(selector, box, random, chance, dx, dy, dz, Blocks.WEB.getDefaultState());
            }
        }
    }


    public static class Cross extends StructureMineshaftPieces.Peice {
        private EnumFacing corridorDirection;
        private boolean isMultipleFloors;

        public Cross() {}


        @Override
        protected void writeStructureToNBT(NBTTagCompound tagCompound) {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setBoolean("tf", this.isMultipleFloors);
            tagCompound.setInteger("D", this.corridorDirection.getHorizontalIndex());
        }


        @Override
        protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
            super.readStructureFromNBT(tagCompound, p_143011_2_);
            this.isMultipleFloors = tagCompound.getBoolean("tf");
            this.corridorDirection = EnumFacing.byHorizontalIndex(tagCompound.getInteger("D"));
        }


        public Cross(int num, Random rand, StructureBoundingBox box, @Nullable EnumFacing facing, MapGenMineshaft.Type type) {
            super(num, type);
            this.corridorDirection = facing;
            this.boundingBox = box;
            this.isMultipleFloors = box.getYSize() > 3;
        }


        public static StructureBoundingBox findCrossing(List<StructureComponent> list, Random rand, int x, int y, int z, EnumFacing facing) {
            StructureBoundingBox structureboundingbox = new StructureBoundingBox(x, y, z, x, y + 2, z);

            if (rand.nextInt(4) == 0) {
                structureboundingbox.maxY += 4;
            }

            switch (facing) {
                case NORTH:
                default:
                    structureboundingbox.minX = x - 1;
                    structureboundingbox.maxX = x + 3;
                    structureboundingbox.minZ = z - 4;
                    break;
                case SOUTH:
                    structureboundingbox.minX = x - 1;
                    structureboundingbox.maxX = x + 3;
                    structureboundingbox.maxZ = z + 3 + 1;
                    break;
                case WEST:
                    structureboundingbox.minX = x - 4;
                    structureboundingbox.minZ = z - 1;
                    structureboundingbox.maxZ = z + 3;
                    break;
                case EAST:
                    structureboundingbox.maxX = x + 3 + 1;
                    structureboundingbox.minZ = z - 1;
                    structureboundingbox.maxZ = z + 3;
            }

            return StructureComponent.findIntersecting(list, structureboundingbox) != null ? null : structureboundingbox;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            int i = this.getComponentType();

            switch (this.corridorDirection) {
                case NORTH:
                default:
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.WEST, i);
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.EAST, i);
                    break;
                case SOUTH:
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.WEST, i);
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.EAST, i);
                    break;
                case WEST:
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.WEST, i);
                    break;
                case EAST:
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.EAST, i);
            }

            if (this.isMultipleFloors) {
                if (rand.nextBoolean()) {
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                }

                if (rand.nextBoolean()) {
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ + 1, EnumFacing.WEST, i);
                }

                if (rand.nextBoolean()) {
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ + 1, EnumFacing.EAST, i);
                }

                if (rand.nextBoolean()) {
                    StructureMineshaftPieces.generateAndAddPiece(comp, list, rand, this.boundingBox.minX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                }
            }
        }


        @Override
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            ChunkSelector selector = new ChunkSelector(worldIn);
            if (this.isLiquidInStructureBoundingBox(selector, structureBoundingBoxIn)) {
                return false;
            } else {
                IBlockState iblockstate = this.getPlanksBlock();

                if (this.isMultipleFloors) {
                    this.fillWithBlocks(selector, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.minY + 3 - 1, this.boundingBox.maxZ, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
                    this.fillWithBlocks(selector, structureBoundingBoxIn, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.minY + 3 - 1, this.boundingBox.maxZ - 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
                    this.fillWithBlocks(selector, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.maxY - 2, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
                    this.fillWithBlocks(selector, structureBoundingBoxIn, this.boundingBox.minX, this.boundingBox.maxY - 2, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ - 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
                    this.fillWithBlocks(selector, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.minY + 3, this.boundingBox.minZ + 1, this.boundingBox.maxX - 1, this.boundingBox.minY + 3, this.boundingBox.maxZ - 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
                } else {
                    this.fillWithBlocks(selector, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
                    this.fillWithBlocks(selector, structureBoundingBoxIn, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ - 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
                }

                this.placeSupportPillar(selector, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxY);
                this.placeSupportPillar(selector, structureBoundingBoxIn, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ - 1, this.boundingBox.maxY);
                this.placeSupportPillar(selector, structureBoundingBoxIn, this.boundingBox.maxX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxY);
                this.placeSupportPillar(selector, structureBoundingBoxIn, this.boundingBox.maxX - 1, this.boundingBox.minY, this.boundingBox.maxZ - 1, this.boundingBox.maxY);

                for (int i = this.boundingBox.minX; i <= this.boundingBox.maxX; ++i) {
                    for (int j = this.boundingBox.minZ; j <= this.boundingBox.maxZ; ++j) {
                        if (this.getBlockStateFromPos(selector, i, this.boundingBox.minY - 1, j,
                                structureBoundingBoxIn).getMaterial() == Material.AIR &&
                                this.getSkyBrightness(selector, i, this.boundingBox.minY - 1, j, structureBoundingBoxIn) < 8) {
                            this.setBlockState(selector, iblockstate, i, this.boundingBox.minY - 1, j, structureBoundingBoxIn);
                        }
                    }
                }
                return true;
            }
        }


        private void placeSupportPillar(ChunkSelector selector, StructureBoundingBox box, int x, int y, int z, int y2) {
            if (this.getBlockStateFromPos(selector, x, y2 + 1, z, box).getMaterial() != Material.AIR) {
                this.fillWithBlocks(selector, box, x, y, z, x, y2, z, this.getPlanksBlock(), BlockIDIndexes.AIR_STATE, false);
            }
        }


        private void placeSupportPillar(World world, StructureBoundingBox box, int x, int y, int z, int y2) {
            if (this.getBlockStateFromPos(world, x, y2 + 1, z, box).getMaterial() != Material.AIR) {
                this.fillWithBlocks(world, box, x, y, z, x, y2, z, this.getPlanksBlock(), BlockIDIndexes.AIR_STATE, false);
            }
        }
    }


    abstract static class Peice extends StructureComponent {
        protected MapGenMineshaft.Type mineShaftType;

        public Peice() {}


        public Peice(int num, MapGenMineshaft.Type type) {
            super(num);
            this.mineShaftType = type;
        }


        @Override
        protected void writeStructureToNBT(NBTTagCompound tagCompound) {
            tagCompound.setInteger("MST", this.mineShaftType.ordinal());
        }


        @Override
        protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager p_143011_2_) {
            this.mineShaftType = MapGenMineshaft.Type.byId(tagCompound.getInteger("MST"));
        }


        protected IBlockState getPlanksBlock() {
            switch (this.mineShaftType) {
                case NORMAL:
                default:
                    return Blocks.PLANKS.getDefaultState();
                case MESA:
                    return Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.DARK_OAK);
            }
        }


        protected IBlockState getFenceBlock() {
            switch (this.mineShaftType) {
                case NORMAL:
                default:
                    return Blocks.OAK_FENCE.getDefaultState();
                case MESA:
                    return Blocks.DARK_OAK_FENCE.getDefaultState();
            }
        }


        protected boolean isSupportingBox(ChunkSelector world, StructureBoundingBox box, int x, int maxX, int y, int z) {
            for (int i = x; i <= maxX; ++i) {
                if (this.getBlockStateFromPos(world, i, y + 1, z, box).getMaterial() == Material.AIR) {
                    return false;
                }
            }
            return true;
        }


        protected boolean isSupportingBox(World world, StructureBoundingBox box, int x, int maxX, int y, int z) {
            for (int i = x; i <= maxX; ++i) {
                if (this.getBlockStateFromPos(world, i, y + 1, z, box).getMaterial() == Material.AIR) {
                    return false;
                }
            }
            return true;
        }
    }


    public static class Room extends StructureMineshaftPieces.Peice {
        private final List<StructureBoundingBox> connectedRooms = Lists.newLinkedList();

        public Room() {}


        public Room(int num, Random rand, int x, int z, MapGenMineshaft.Type type) {
            super(num, type);
            this.mineShaftType = type;
            this.boundingBox = new StructureBoundingBox(x, 50, z, x + 7 + rand.nextInt(6), 54 + rand.nextInt(6), z + 7 + rand.nextInt(6));
        }


        @Override
        public void buildComponent(StructureComponent componentIn, List<StructureComponent> listIn, Random rand) {
            int i = this.getComponentType();
            int j = this.boundingBox.getYSize() - 3 - 1;

            if (j <= 0) {
                j = 1;
            }

            int k;

            for (k = 0; k < this.boundingBox.getXSize(); k = k + 4) {
                k = k + rand.nextInt(this.boundingBox.getXSize());

                if (k + 3 > this.boundingBox.getXSize()) {
                    break;
                }

                StructureMineshaftPieces.Peice comp = StructureMineshaftPieces.generateAndAddPiece(componentIn, listIn, rand, this.boundingBox.minX + k, this.boundingBox.minY + rand.nextInt(j) + 1, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);

                if (comp != null) {
                    StructureBoundingBox box = comp.getBoundingBox();
                    this.connectedRooms.add(new StructureBoundingBox(box.minX, box.minY, this.boundingBox.minZ, box.maxX, box.maxY, this.boundingBox.minZ + 1));
                }
            }


            for (k = 0; k < this.boundingBox.getXSize(); k = k + 4) {
                k = k + rand.nextInt(this.boundingBox.getXSize());

                if (k + 3 > this.boundingBox.getXSize()) {
                    break;
                }

                StructureMineshaftPieces.Peice comp = StructureMineshaftPieces.generateAndAddPiece(componentIn, listIn, rand, this.boundingBox.minX + k, this.boundingBox.minY + rand.nextInt(j) + 1, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);

                if (comp != null) {
                    StructureBoundingBox box = comp.getBoundingBox();
                    this.connectedRooms.add(new StructureBoundingBox(box.minX, box.minY, this.boundingBox.maxZ - 1, box.maxX, box.maxY, this.boundingBox.maxZ));
                }
            }


            for (k = 0; k < this.boundingBox.getZSize(); k = k + 4) {
                k = k + rand.nextInt(this.boundingBox.getZSize());

                if (k + 3 > this.boundingBox.getZSize()) {
                    break;
                }

                StructureMineshaftPieces.Peice comp = StructureMineshaftPieces.generateAndAddPiece(componentIn, listIn, rand, this.boundingBox.minX - 1, this.boundingBox.minY + rand.nextInt(j) + 1, this.boundingBox.minZ + k, EnumFacing.WEST, i);

                if (comp != null) {
                    StructureBoundingBox box = comp.getBoundingBox();
                    this.connectedRooms.add(new StructureBoundingBox(this.boundingBox.minX, box.minY, box.minZ, this.boundingBox.minX + 1, box.maxY, box.maxZ));
                }
            }


            for (k = 0; k < this.boundingBox.getZSize(); k = k + 4) {
                k = k + rand.nextInt(this.boundingBox.getZSize());

                if (k + 3 > this.boundingBox.getZSize()) {
                    break;
                }

                StructureComponent comp = StructureMineshaftPieces.generateAndAddPiece(componentIn, listIn, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + rand.nextInt(j) + 1, this.boundingBox.minZ + k, EnumFacing.EAST, i);

                if (comp != null) {
                    StructureBoundingBox box = comp.getBoundingBox();
                    this.connectedRooms.add(new StructureBoundingBox(this.boundingBox.maxX - 1, box.minY, box.minZ, this.boundingBox.maxX, box.maxY, box.maxZ));
                }
            }
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            if (this.isLiquidInStructureBoundingBox(selector, box)) {
                return false;
            } else {
                this.fillWithBlocks(selector, box, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX, this.boundingBox.minY, this.boundingBox.maxZ, Blocks.DIRT.getDefaultState(), Blocks.AIR.getDefaultState(), true);
                this.fillWithBlocks(selector, box, this.boundingBox.minX, this.boundingBox.minY + 1, this.boundingBox.minZ, this.boundingBox.maxX, Math.min(this.boundingBox.minY + 3, this.boundingBox.maxY), this.boundingBox.maxZ, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);

                for (StructureBoundingBox structureboundingbox : this.connectedRooms) {
                    this.fillWithBlocks(selector, box, structureboundingbox.minX, structureboundingbox.maxY - 2, structureboundingbox.minZ, structureboundingbox.maxX, structureboundingbox.maxY, structureboundingbox.maxZ, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
                }

                this.randomlyRareFillWithBlocks(selector, box, this.boundingBox.minX, this.boundingBox.minY + 4, this.boundingBox.minZ, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ, Blocks.AIR.getDefaultState(), false);
                return true;
            }
        }


        @Override
        public void offset(int x, int y, int z) {
            super.offset(x, y, z);

            for (StructureBoundingBox structureboundingbox : this.connectedRooms) {
                structureboundingbox.offset(x, y, z);
            }
        }


        @Override
        protected void writeStructureToNBT(NBTTagCompound tag) {
            super.writeStructureToNBT(tag);
            NBTTagList list = new NBTTagList();

            for (StructureBoundingBox box : this.connectedRooms) {
                list.appendTag(box.toNBTTagIntArray());
            }
            tag.setTag("Entrances", list);
        }


        @Override
        protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
            super.readStructureFromNBT(tag, manager);
            NBTTagList list = tag.getTagList("Entrances", 11);

            for (int i = 0; i < list.tagCount(); ++i) {
                this.connectedRooms.add(new StructureBoundingBox(list.getIntArrayAt(i)));
            }
        }
    }


    public static class Stairs extends StructureMineshaftPieces.Peice {
        public Stairs() {}


        public Stairs(int num, Random rand, StructureBoundingBox box, EnumFacing facing, MapGenMineshaft.Type type) {
            super(num, type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        public static StructureBoundingBox findStairs(List<StructureComponent> list, Random rand, int x, int y, int z, EnumFacing facing) {
            StructureBoundingBox box = new StructureBoundingBox(x, y - 5, z, x, y + 2, z);

            switch (facing) {
                case NORTH:
                default:
                    box.maxX = x + 2;
                    box.minZ = z - 8;
                    break;
                case SOUTH:
                    box.maxX = x + 2;
                    box.maxZ = z + 8;
                    break;
                case WEST:
                    box.minX = x - 8;
                    box.maxZ = z + 2;
                    break;
                case EAST:
                    box.maxX = x + 8;
                    box.maxZ = z + 2;
            }

            return StructureComponent.findIntersecting(list, box) != null ? null : box;
        }


        @Override
        public void buildComponent(StructureComponent box, List<StructureComponent> list, Random rand) {
            int i = this.getComponentType();
            EnumFacing facing = this.getCoordBaseMode();

            if (facing != null) {
                switch (facing) {
                    case NORTH:
                    default:
                        StructureMineshaftPieces.generateAndAddPiece(box, list, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, i);
                        break;
                    case SOUTH:
                        StructureMineshaftPieces.generateAndAddPiece(box, list, rand, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, i);
                        break;
                    case WEST:
                        StructureMineshaftPieces.generateAndAddPiece(box, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.WEST, i);
                        break;
                    case EAST:
                        StructureMineshaftPieces.generateAndAddPiece(box, list, rand, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.EAST, i);
                }
            }
        }


        @Override
        public boolean addComponentParts(World world, Random random, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            if (this.isLiquidInStructureBoundingBox(selector, box)) {
                return false;
            } else {
                this.fillWithBlocks(selector, box, 0, 5, 0, 2, 7, 1, BlockIDIndexes.AIR_STATE, BlockIDIndexes.AIR_STATE, false);
                this.fillWithBlocks(selector, box, 0, 0, 7, 2, 2, 8, BlockIDIndexes.AIR_STATE, BlockIDIndexes.AIR_STATE, false);

                for (int i = 0; i < 5; ++i) {
                    this.fillWithBlocks(selector, box, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, BlockIDIndexes.AIR_STATE, BlockIDIndexes.AIR_STATE, false);
                }

                return true;
            }
        }
    }
}