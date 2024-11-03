package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.loot.LootTableList;
import plus.fserver.server.ElcaThread;
import plus.fserver.server.elca.ChunkSelector;


public class StructureNetherBridgePieces {
    private static final PieceWeight[] PRIMARY_COMPONENTS = new PieceWeight[]{new PieceWeight(Straight.class, 30, 0, true), new PieceWeight(Crossing3.class, 10, 4), new PieceWeight(Crossing.class, 10, 4), new PieceWeight(Stairs.class, 10, 3), new PieceWeight(Throne.class, 5, 2), new PieceWeight(Entrance.class, 5, 1)};
    private static final PieceWeight[] SECONDARY_COMPONENTS = new PieceWeight[]{new PieceWeight(Corridor5.class, 25, 0, true), new PieceWeight(Crossing2.class, 15, 5), new PieceWeight(Corridor2.class, 5, 10), new PieceWeight(Corridor.class, 5, 10), new PieceWeight(Corridor3.class, 10, 3, true), new PieceWeight(Corridor4.class, 7, 2), new PieceWeight(NetherStalkRoom.class, 5, 2)};

    public static void registerNetherFortressPieces() {
        MapGenStructureIO.registerStructureComponent(Crossing3.class, "NeBCr");
        MapGenStructureIO.registerStructureComponent(End.class, "NeBEF");
        MapGenStructureIO.registerStructureComponent(Straight.class, "NeBS");
        MapGenStructureIO.registerStructureComponent(Corridor3.class, "NeCCS");
        MapGenStructureIO.registerStructureComponent(Corridor4.class, "NeCTB");
        MapGenStructureIO.registerStructureComponent(Entrance.class, "NeCE");
        MapGenStructureIO.registerStructureComponent(Crossing2.class, "NeSCSC");
        MapGenStructureIO.registerStructureComponent(Corridor.class, "NeSCLT");
        MapGenStructureIO.registerStructureComponent(Corridor5.class, "NeSC");
        MapGenStructureIO.registerStructureComponent(Corridor2.class, "NeSCRT");
        MapGenStructureIO.registerStructureComponent(NetherStalkRoom.class, "NeCSR");
        MapGenStructureIO.registerStructureComponent(Throne.class, "NeMT");
        MapGenStructureIO.registerStructureComponent(Crossing.class, "NeRC");
        MapGenStructureIO.registerStructureComponent(Stairs.class, "NeSR");
        MapGenStructureIO.registerStructureComponent(Start.class, "NeStart");
    }


    private static Piece findAndCreateBridgePieceFactory(PieceWeight weight, List<StructureComponent> list, Random rand, int p_175887_3_, int p_175887_4_, int p_175887_5_, EnumFacing facing, int p_175887_7_) {
        Class<? extends Piece> clazz = weight.weightClass;
        Piece piece = null;
        if (clazz == Straight.class) {
            piece = StructureNetherBridgePieces.Straight.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        } else if (clazz == Crossing3.class) {
            piece = StructureNetherBridgePieces.Crossing3.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        } else if (clazz == Crossing.class) {
            piece = StructureNetherBridgePieces.Crossing.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        } else if (clazz == Stairs.class) {
            piece = StructureNetherBridgePieces.Stairs.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, p_175887_7_, facing);
        } else if (clazz == Throne.class) {
            piece = StructureNetherBridgePieces.Throne.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, p_175887_7_, facing);
        } else if (clazz == Entrance.class) {
            piece = StructureNetherBridgePieces.Entrance.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        } else if (clazz == Corridor5.class) {
            piece = StructureNetherBridgePieces.Corridor5.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        } else if (clazz == Corridor2.class) {
            piece = StructureNetherBridgePieces.Corridor2.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        } else if (clazz == Corridor.class) {
            piece = StructureNetherBridgePieces.Corridor.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        } else if (clazz == Corridor3.class) {
            piece = StructureNetherBridgePieces.Corridor3.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        } else if (clazz == Corridor4.class) {
            piece = StructureNetherBridgePieces.Corridor4.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        } else if (clazz == Crossing2.class) {
            piece = StructureNetherBridgePieces.Crossing2.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        } else if (clazz == NetherStalkRoom.class) {
            piece = StructureNetherBridgePieces.NetherStalkRoom.createPiece(list, rand, p_175887_3_, p_175887_4_, p_175887_5_, facing, p_175887_7_);
        }

        return (Piece)piece;
    }


    public static class Corridor4 extends Piece {
        public Corridor4() {}

        public Corridor4(int p_i45618_1_, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(p_i45618_1_);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            int num = 1;
            EnumFacing facing = this.getCoordBaseMode();
            if (facing == EnumFacing.WEST || facing == EnumFacing.NORTH) {
                num = 5;
            }

            this.getNextComponentX((Start)comp, list, rand, 0, num, rand.nextInt(8) > 0);
            this.getNextComponentZ((Start)comp, list, rand, 0, num, rand.nextInt(8) > 0);
        }


        public static Corridor4 createPiece(List<StructureComponent> list, Random rand, int p_175880_2_, int p_175880_3_, int p_175880_4_, EnumFacing p_175880_5_, int p_175880_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175880_2_, p_175880_3_, p_175880_4_, -3, 0, 0, 9, 7, 9, p_175880_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(list, lvt_7_1_) == null ? new Corridor4(p_175880_6_, rand, lvt_7_1_, p_175880_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ElcaThread.Cache selector = ElcaThread.cache(world);
            final IBlockState bricks = Blocks.NETHER_BRICK.getDefaultState();
            final IBlockState fence  = Blocks.NETHER_BRICK_FENCE.getDefaultState();
            final IBlockState air    = BlockIDIndexes.AIR_STATE;
            BlockPos.MutableBlockPos mut = selector.pos3;

            this.fillWithBlocks(mut, selector, box, 0, 0, 0, 8, 1, 8, bricks        , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 8, 5, 8, air           , false);
            this.fillWithBlocks(mut, selector, box, 0, 6, 0, 8, 6, 5, bricks        , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 2, 5, 0, bricks        , false);
            this.fillWithBlocks(mut, selector, box, 6, 2, 0, 8, 5, 0, bricks        , false);
            this.fillWithBlocks(mut, selector, box, 1, 3, 0, 1, 4, 0, fence         , false);
            this.fillWithBlocks(mut, selector, box, 7, 3, 0, 7, 4, 0, fence         , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 4, 8, 2, 8, bricks        , false);
            this.fillWithBlocks(mut, selector, box, 1, 1, 4, 2, 2, 4, air           , false);
            this.fillWithBlocks(mut, selector, box, 6, 1, 4, 7, 2, 4, air           , false);
            this.fillWithBlocks(mut, selector, box, 0, 3, 8, 8, 3, 8, fence         , false);
            this.fillWithBlocks(mut, selector, box, 0, 3, 6, 0, 3, 7, fence         , false);
            this.fillWithBlocks(mut, selector, box, 8, 3, 6, 8, 3, 7, fence         , false);
            this.fillWithBlocks(mut, selector, box, 0, 3, 4, 0, 5, 5, bricks        , false);
            this.fillWithBlocks(mut, selector, box, 8, 3, 4, 8, 5, 5, bricks        , false);
            this.fillWithBlocks(mut, selector, box, 1, 3, 5, 2, 5, 5, bricks        , false);
            this.fillWithBlocks(mut, selector, box, 6, 3, 5, 7, 5, 5, bricks        , false);
            this.fillWithBlocks(mut, selector, box, 1, 4, 5, 1, 5, 5, fence         , false);
            this.fillWithBlocks(mut, selector, box, 7, 4, 5, 7, 5, 5, fence         , false);

            for(int z = 0; z <= 5; ++z) {
                for(int x = 0; x <= 8; ++x) {
                    this.replaceAirAndLiquidDownwards(selector, bricks, x, -1, z, box);
                }
            }
            return true;
        }
    }

    public static class Corridor3 extends Piece {
        public Corridor3() {}

        public Corridor3(int id, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(id);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random p_74861_3_) {
            this.getNextComponentNormal((Start)comp, list, p_74861_3_, 1, 0, true);
        }


        public static Corridor3 createPiece(List<StructureComponent> p_175883_0_, Random p_175883_1_, int p_175883_2_, int p_175883_3_, int p_175883_4_, EnumFacing p_175883_5_, int p_175883_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175883_2_, p_175883_3_, p_175883_4_, -1, -7, 0, 5, 14, 10, p_175883_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(p_175883_0_, lvt_7_1_) == null ? new Corridor3(p_175883_6_, p_175883_1_, lvt_7_1_, p_175883_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ElcaThread.Cache selector = ElcaThread.cache(world);
            BlockPos.MutableBlockPos mut = selector.pos3;

            final IBlockState stairs = Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH);
            final IBlockState bricks = Blocks.NETHER_BRICK.getDefaultState();
            final IBlockState fence  = Blocks.NETHER_BRICK_FENCE.getDefaultState();
            final IBlockState air    = BlockIDIndexes.AIR_STATE;

            for(int zPos = 0; zPos <= 9; ++zPos) {
                int yMin = Math.max(1, 7 - zPos);
                int yMax = Math.min(Math.max(yMin + 5, 14 - zPos), 13);

                this.fillWithBlocks(mut, selector, box, 0, 0, zPos, 4, yMin, zPos, bricks, false);
                this.fillWithBlocks(mut, selector, box, 1, yMin + 1, zPos, 3, yMax - 1, zPos, air, false);
                if (zPos <= 6) {
                    this.setBlockStateM(mut, selector, stairs, 1, yMin + 1, zPos, box);
                    this.setBlockStateM(mut, selector, stairs, 2, yMin + 1, zPos, box);
                    this.setBlockStateM(mut, selector, stairs, 3, yMin + 1, zPos, box);
                }

                this.fillWithBlocks(mut, selector, box, 0, yMax, zPos, 4, yMax, zPos, bricks, false);
                this.fillWithBlocks(mut, selector, box, 0, yMin + 1, zPos, 0, yMax - 1, zPos, bricks, false);
                this.fillWithBlocks(mut, selector, box, 4, yMin + 1, zPos, 4, yMax - 1, zPos, bricks, false);

                if ((zPos & 1) == 0) {
                    this.fillWithBlocks(mut, selector, box, 0, yMin + 2, zPos, 0, yMin + 3, zPos, fence, false);
                    this.fillWithBlocks(mut, selector, box, 4, yMin + 2, zPos, 4, yMin + 3, zPos, fence, false);
                }

                for(int x = 0; x <= 4; ++x) {
                    this.replaceAirAndLiquidDownwards(selector, bricks, x, -1, zPos, box);
                }
            }
            return true;
        }
    }


    public static class Corridor extends Piece {
        private boolean chest;

        public Corridor() {}


        public Corridor(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
            this.chest = rand.nextInt(3) == 0;
        }


        @Override
        protected void readStructureFromNBT(NBTTagCompound nbt, TemplateManager manager) {
            super.readStructureFromNBT(nbt, manager);
            this.chest = nbt.getBoolean("Chest");
        }


        @Override
        protected void writeStructureToNBT(NBTTagCompound nbt) {
            super.writeStructureToNBT(nbt);
            nbt.setBoolean("Chest", this.chest);
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            this.getNextComponentX((Start)comp, list, rand, 0, 1, true);
        }


        public static Corridor createPiece(List<StructureComponent> list, Random rand, int p_175879_2_, int p_175879_3_, int p_175879_4_, EnumFacing p_175879_5_, int p_175879_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175879_2_, p_175879_3_, p_175879_4_, -1, 0, 0, 5, 7, 5, p_175879_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(list, lvt_7_1_) == null ? new Corridor(p_175879_6_, rand, lvt_7_1_, p_175879_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICK.getDefaultState()                                             , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 4, 5, 4, BlockIDIndexes.AIR_STATE                                                          , false);
            this.fillWithBlocks(mut, selector, box, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICK.getDefaultState()                                             , false);
            this.fillWithBlocks(mut, selector, box, 4, 3, 1, 4, 4, 1, Blocks.NETHER_BRICK_FENCE.getDefaultState()                                       , false);
            this.fillWithBlocks(mut, selector, box, 4, 3, 3, 4, 4, 3, Blocks.NETHER_BRICK_FENCE.getDefaultState()                                       , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICK.getDefaultState()                                             , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICK.getDefaultState()                                             , false);
            this.fillWithBlocks(mut, selector, box, 1, 3, 4, 1, 4, 4, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 3, 3, 4, 3, 4, 4, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);

            if (this.chest && box.isVecInside(new BlockPos(this.getXWithOffset(3, 3), this.getYWithOffset(2), this.getZWithOffset(3, 3)))) {
                this.chest = false;
                this.generateChest(world, box, rand, 3, 2, 3, LootTableList.CHESTS_NETHER_BRIDGE);
            }

            this.fillWithBlocks(mut, selector, box, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICK.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);

            for(int x = 0; x <= 4; ++x) {
                for(int z = 0; z <= 4; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                }
            }
            return true;
        }
    }


    public static class Corridor2 extends Piece {
        private boolean chest;
        public Corridor2() {}

        public Corridor2(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
            this.chest = rand.nextInt(3) == 0;
        }


        @Override
        protected void readStructureFromNBT(NBTTagCompound nbt, TemplateManager manager) {
            super.readStructureFromNBT(nbt, manager);
            this.chest = nbt.getBoolean("Chest");
        }


        @Override
        protected void writeStructureToNBT(NBTTagCompound nbt) {
            super.writeStructureToNBT(nbt);
            nbt.setBoolean("Chest", this.chest);
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            this.getNextComponentZ((Start)comp, list, rand, 0, 1, true);
        }


        public static Corridor2 createPiece(List<StructureComponent> list, Random rand, int p_175876_2_, int p_175876_3_, int p_175876_4_, EnumFacing p_175876_5_, int p_175876_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175876_2_, p_175876_3_, p_175876_4_, -1, 0, 0, 5, 7, 5, p_175876_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(list, lvt_7_1_) == null ? new Corridor2(p_175876_6_, rand, lvt_7_1_, p_175876_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICK.getDefaultState()                                             , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 4, 5, 4, Blocks.AIR.getDefaultState()                                                      , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICK.getDefaultState()                                             , false);
            this.fillWithBlocks(mut, selector, box, 0, 3, 1, 0, 4, 1, Blocks.NETHER_BRICK_FENCE.getDefaultState()                                       , false);
            this.fillWithBlocks(mut, selector, box, 0, 3, 3, 0, 4, 3, Blocks.NETHER_BRICK_FENCE.getDefaultState()                                       , false);
            this.fillWithBlocks(mut, selector, box, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICK.getDefaultState()                                             , false);
            this.fillWithBlocks(mut, selector, box, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICK.getDefaultState()                                             , false);
            this.fillWithBlocks(mut, selector, box, 1, 3, 4, 1, 4, 4, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 3, 3, 4, 3, 4, 4, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK.getDefaultState(), false);

            if (this.chest && box.isVecInside(new BlockPos(this.getXWithOffset(1, 3), this.getYWithOffset(2), this.getZWithOffset(1, 3)))) {
                this.chest = false;
                this.generateChest(world, box, rand, 1, 2, 3, LootTableList.CHESTS_NETHER_BRIDGE);
            }

            this.fillWithBlocks(mut, selector, box, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICK.getDefaultState(), false);

            for(int x = 0; x <= 4; ++x) {
                for(int z = 0; z <= 4; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                }
            }
            return true;
        }
    }


    public static class Crossing2 extends Piece {
        public Crossing2() {}

        public Crossing2(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            this.getNextComponentNormal((Start)comp, list, rand, 1, 0, true);
            this.getNextComponentX((Start)comp, list, rand, 0, 1, true);
            this.getNextComponentZ((Start)comp, list, rand, 0, 1, true);
        }


        public static Crossing2 createPiece(List<StructureComponent> list, Random rand, int p_175878_2_, int p_175878_3_, int p_175878_4_, EnumFacing p_175878_5_, int p_175878_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175878_2_, p_175878_3_, p_175878_4_, -1, 0, 0, 5, 7, 5, p_175878_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(list, lvt_7_1_) == null ? new Crossing2(p_175878_6_, rand, lvt_7_1_, p_175878_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 4, 5, 4, BlockIDIndexes.AIR_STATE             , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICK.getDefaultState(), false);

            for(int x = 0; x <= 4; ++x) {
                for(int z = 0; z <= 4; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                }
            }
            return true;
        }
    }


    public static class Corridor5 extends Piece {
        public Corridor5() {}

        public Corridor5(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            this.getNextComponentNormal((Start)comp, list, rand, 1, 0, true);
        }


        public static Corridor5 createPiece(List<StructureComponent> list, Random rand, int p_175877_2_, int p_175877_3_, int p_175877_4_, EnumFacing p_175877_5_, int p_175877_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175877_2_, p_175877_3_, p_175877_4_, -1, 0, 0, 5, 7, 5, p_175877_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(list, lvt_7_1_) == null ? new Corridor5(p_175877_6_, rand, lvt_7_1_, p_175877_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 4, 5, 4, BlockIDIndexes.AIR_STATE                   , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0, 3, 1, 0, 4, 1, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 3, 3, 0, 4, 3, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 3, 1, 4, 4, 1, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 3, 3, 4, 4, 3, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICK.getDefaultState()      , false);

            for(int x = 0; x <= 4; ++x) {
                for(int z = 0; z <= 4; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                }
            }
            return true;
        }
    }

    public static class NetherStalkRoom extends Piece {
        public NetherStalkRoom() {}

        public NetherStalkRoom(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            this.getNextComponentNormal((Start)comp, list, rand, 5, 3, true);
            this.getNextComponentNormal((Start)comp, list, rand, 5, 11, true);
        }


        public static NetherStalkRoom createPiece(List<StructureComponent> list, Random rand, int p_175875_2_, int p_175875_3_, int p_175875_4_, EnumFacing p_175875_5_, int p_175875_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175875_2_, p_175875_3_, p_175875_4_, -5, -3, 0, 13, 14, 13, p_175875_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(list, lvt_7_1_) == null ? new NetherStalkRoom(p_175875_6_, rand, lvt_7_1_, p_175875_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 0 , 3 , 0 , 12, 4 , 12, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0 , 5 , 0 , 12, 13, 12, BlockIDIndexes.AIR_STATE             , false);
            this.fillWithBlocks(mut, selector, box, 0 , 5 , 0 , 1 , 12, 12, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 11, 5 , 0 , 12, 12, 12, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 2 , 5 , 11, 4 , 12, 12, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 8 , 5 , 11, 10, 12, 12, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 5 , 9 , 11, 7 , 12, 12, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 2 , 5 , 0 , 4 , 12, 1 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 8 , 5 , 0 , 10, 12, 1 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 5 , 9 , 0 , 7 , 12, 1 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 2 , 11, 2 , 10, 12, 10, Blocks.NETHER_BRICK.getDefaultState(), false);

            int xMin;
            for(xMin = 1; xMin <= 11; xMin += 2) {
                this.fillWithBlocks(mut, selector, box,     xMin, 10, 0 ,     xMin, 11, 0 , Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
                this.fillWithBlocks(mut, selector, box,     xMin, 10, 12,     xMin, 11, 12, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
                this.fillWithBlocks(mut, selector, box, 0 , 10 ,    xMin, 0 , 11,     xMin, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
                this.fillWithBlocks(mut, selector, box, 12, 10 ,    xMin, 12, 11,     xMin, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);

                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK.getDefaultState(), xMin, 13, 0, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK.getDefaultState(), xMin, 13, 12, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK.getDefaultState(), 0, 13, xMin, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK.getDefaultState(), 12, 13, xMin, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), xMin + 1, 13, 0, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), xMin + 1, 13, 12, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 0, 13, xMin + 1, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 12, 13, xMin + 1, box);
            }

            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 0, 13, 0, box);
            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 0, 13, 12, box);
            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 0, 13, 0, box);
            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 12, 13, 0, box);

            for(xMin = 3; xMin <= 9; xMin += 2) {
                this.fillWithBlocks(mut, selector, box, 1, 7, xMin, 1, 8, xMin, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
                this.fillWithBlocks(mut, selector, box, 11, 7, xMin, 11, 8, xMin, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            }

            IBlockState stairs = Blocks.NETHER_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH);

            int y;
            int x;
            for(y = 0; y <= 6; ++y) {
                int z = y + 4;

                for(x = 5; x <= 7; ++x) {
                    this.setBlockStateM(mut, selector, stairs, x, 5 + y, z, box);
                }

                if (z >= 5 && z <= 8) {
                    this.fillWithBlocks(mut, selector, box, 5, 5, z, 7, y + 4, z, Blocks.NETHER_BRICK.getDefaultState(), false);
                } else if (z >= 9 && z <= 10) {
                    this.fillWithBlocks(mut, selector, box, 5, 8, z, 7, y + 4, z, Blocks.NETHER_BRICK.getDefaultState(), false);
                }

                if (y >= 1) {
                    this.fillWithBlocks(mut, selector, box, 5, 6 + y, z, 7, 9 + y, z, BlockIDIndexes.AIR_STATE, false);
                }
            }

            for(y = 5; y <= 7; ++y) {
                this.setBlockStateM(mut, selector, stairs, y, 12, 11, box);
            }

            this.fillWithBlocks(mut, selector, box, 5 , 6 , 7 , 5 , 7 , 7 , Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 7 , 6 , 7 , 7 , 7 , 7 , Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 5 , 13, 12, 7 , 13, 12, BlockIDIndexes.AIR_STATE                   , false);
            this.fillWithBlocks(mut, selector, box, 2 , 5 , 2 , 3 , 5 , 3 , Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 2 , 5 , 9 , 3 , 5 , 10, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 2 , 5 , 4 , 2 , 5 , 8 , Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 9 , 5 , 2 , 10, 5 , 3 , Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 9 , 5 , 9 , 10, 5 , 10, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 10, 5 , 4 , 10, 5 , 8 , Blocks.NETHER_BRICK.getDefaultState()      , false);

            IBlockState stairs2 = stairs.withProperty(BlockStairs.FACING, EnumFacing.EAST);
            IBlockState stairs3 = stairs.withProperty(BlockStairs.FACING, EnumFacing.WEST);

            this.setBlockStateM(mut, selector, stairs3, 4, 5, 2, box);
            this.setBlockStateM(mut, selector, stairs3, 4, 5, 3, box);
            this.setBlockStateM(mut, selector, stairs3, 4, 5, 9, box);
            this.setBlockStateM(mut, selector, stairs3, 4, 5, 10, box);
            this.setBlockStateM(mut, selector, stairs2, 8, 5, 2, box);
            this.setBlockStateM(mut, selector, stairs2, 8, 5, 3, box);
            this.setBlockStateM(mut, selector, stairs2, 8, 5, 9, box);
            this.setBlockStateM(mut, selector, stairs2, 8, 5, 10, box);

            this.fillWithBlocks(mut, selector, box, 3, 4, 4, 4 , 4, 8 , Blocks.SOUL_SAND.getDefaultState()   , false);
            this.fillWithBlocks(mut, selector, box, 8, 4, 4, 9 , 4, 8 , Blocks.SOUL_SAND.getDefaultState()   , false);
            this.fillWithBlocks(mut, selector, box, 3, 5, 4, 4 , 5, 8 , Blocks.NETHER_WART.getDefaultState() , false);
            this.fillWithBlocks(mut, selector, box, 8, 5, 4, 9 , 5, 8 , Blocks.NETHER_WART.getDefaultState() , false);
            this.fillWithBlocks(mut, selector, box, 4, 2, 0, 8 , 2, 12, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 4, 12, 2, 8 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 0, 0, 8 , 1, 3 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 0, 9, 8 , 1, 12, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 0, 4, 3 , 1, 8 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 9, 0, 4, 12, 1, 8 , Blocks.NETHER_BRICK.getDefaultState(), false);

            int z;
            for(x = 4; x <= 8; ++x) {
                for(z = 0; z <= 2; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, 12 - z, box);
                }
            }

            for(x = 0; x <= 2; ++x) {
                for(z = 4; z <= 8; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), 12 - x, -1, z, box);
                }
            }
            return true;
        }
    }


    public static class Entrance extends Piece {
        public Entrance() {}

        public Entrance(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            this.getNextComponentNormal((Start)comp, list, rand, 5, 3, true);
        }


        public static Entrance createPiece(List<StructureComponent> p_175881_0_, Random p_175881_1_, int p_175881_2_, int p_175881_3_, int p_175881_4_, EnumFacing p_175881_5_, int p_175881_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175881_2_, p_175881_3_, p_175881_4_, -5, -3, 0, 13, 14, 13, p_175881_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(p_175881_0_, lvt_7_1_) == null ? new Entrance(p_175881_6_, p_175881_1_, lvt_7_1_, p_175881_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 0 , 3 , 0 , 12, 4 , 12, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0 , 5 , 0 , 12, 13, 12, BlockIDIndexes.AIR_STATE                   , false);
            this.fillWithBlocks(mut, selector, box, 0 , 5 , 0 , 1 , 12, 12, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 11, 5 , 0 , 12, 12, 12, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 2 , 5 , 11, 4 , 12, 12, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 8 , 5 , 11, 10, 12, 12, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 5 , 9 , 11, 7 , 12, 12, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 2 , 5 , 0 , 4 , 12, 1 , Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 8 , 5 , 0 , 10, 12, 1 , Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 5 , 9 , 0 , 7 , 12, 1 , Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 2 , 11, 2 , 10, 12, 10, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 5 , 8 , 0 , 7 , 8 , 0 , Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);

            int x;
            for(x = 1; x <= 11; x += 2) {
                this.fillWithBlocks(mut, selector, box, x, 10, 0, x, 11, 0, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
                this.fillWithBlocks(mut, selector, box, x, 10, 12, x, 11, 12, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
                this.fillWithBlocks(mut, selector, box, 0, 10, x, 0, 11, x, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
                this.fillWithBlocks(mut, selector, box, 12, 10, x, 12, 11, x, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);

                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK.getDefaultState(), x, 13, 0, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK.getDefaultState(), x, 13, 12, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK.getDefaultState(), 0, 13, x, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK.getDefaultState(), 12, 13, x, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), x + 1, 13, 0, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), x + 1, 13, 12, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 0, 13, x + 1, box);
                this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 12, 13, x + 1, box);
            }

            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 0, 13, 0, box);
            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 0, 13, 12, box);
            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 0, 13, 0, box);
            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 12, 13, 0, box);

            for(x = 3; x <= 9; x += 2) {
                this.fillWithBlocks(mut, selector, box, 1, 7, x, 1, 8, x, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
                this.fillWithBlocks(mut, selector, box, 11, 7, x, 11, 8, x, Blocks.NETHER_BRICK_FENCE.getDefaultState(), Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            }

            this.fillWithBlocks(mut, selector, box, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICK.getDefaultState() , false);
            this.fillWithBlocks(mut, selector, box, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICK.getDefaultState() , false);
            this.fillWithBlocks(mut, selector, box, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICK.getDefaultState(), false);

            int z;
            for(x = 4; x <= 8; ++x) {
                for(z = 0; z <= 2; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, 12 - z, box);
                }
            }

            for(x = 0; x <= 2; ++x) {
                for(z = 4; z <= 8; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), 12 - x, -1, z, box);
                }
            }

            this.fillWithBlocks(mut, selector, box, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 6, 1, 6, 6, 4, 6, BlockIDIndexes.AIR_STATE, false);
            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK.getDefaultState(), 6, 0, 6, box);

            IBlockState lava = Blocks.FLOWING_LAVA.getDefaultState();
            this.setBlockStateM(mut, selector, lava, 6, 5, 6, box);

            BlockPos lavaPos = new BlockPos(this.getXWithOffset(6, 6), this.getYWithOffset(5), this.getZWithOffset(6, 6));
            if (box.isVecInside(lavaPos)) {
                world.immediateBlockTick(lavaPos, lava, rand);
            }
            return true;
        }
    }


    public static class Throne extends Piece {
        private boolean hasSpawner;
        public Throne() {}

        public Throne(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        protected void readStructureFromNBT(NBTTagCompound nbt, TemplateManager manager) {
            super.readStructureFromNBT(nbt, manager);
            this.hasSpawner = nbt.getBoolean("Mob");
        }


        protected void writeStructureToNBT(NBTTagCompound nbt) {
            super.writeStructureToNBT(nbt);
            nbt.setBoolean("Mob", this.hasSpawner);
        }


        public static Throne createPiece(List<StructureComponent> list, Random rand, int p_175874_2_, int p_175874_3_, int p_175874_4_, int p_175874_5_, EnumFacing p_175874_6_) {
            StructureBoundingBox box = StructureBoundingBox.getComponentToAddBoundingBox(p_175874_2_, p_175874_3_, p_175874_4_, -2, 0, 0, 7, 8, 9, p_175874_6_);
            return isAboveGround(box) && StructureComponent.findIntersecting(list, box) == null ? new Throne(p_175874_5_, rand, box, p_175874_6_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 6, 7, 7, BlockIDIndexes.AIR_STATE             , false);
            this.fillWithBlocks(mut, selector, box, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICK.getDefaultState(), false);

            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 1, 6, 3, box);
            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK_FENCE.getDefaultState(), 5, 6, 3, box);

            this.fillWithBlocks(mut, selector, box, 0, 6, 3, 0, 6, 8, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 6, 6, 3, 6, 6, 8, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 1, 6, 8, 5, 7, 8, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 2, 8, 8, 4, 8, 8, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);

            if (!this.hasSpawner) {
                BlockPos pos = new BlockPos(this.getXWithOffset(3, 5), this.getYWithOffset(5), this.getZWithOffset(3, 5));
                if (box.isVecInside(pos)) {
                    this.hasSpawner = true;
                    world.setBlockState(pos, Blocks.MOB_SPAWNER.getDefaultState(), 2);
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof TileEntityMobSpawner) {
                        ((TileEntityMobSpawner)tile).getSpawnerBaseLogic().setEntityId(EntityList.getKey(EntityBlaze.class));
                    }
                }
            }

            for(int x = 0; x <= 6; ++x) {
                for(int z = 0; z <= 6; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                }
            }
            return true;
        }
    }

    public static class Stairs extends Piece {
        public Stairs() {}

        public Stairs(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            this.getNextComponentZ((Start)comp, list, rand, 6, 2, false);
        }


        public static Stairs createPiece(List<StructureComponent> list, Random rand, int p_175872_2_, int p_175872_3_, int p_175872_4_, int p_175872_5_, EnumFacing p_175872_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175872_2_, p_175872_3_, p_175872_4_, -2, 0, 0, 7, 11, 7, p_175872_6_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(list, lvt_7_1_) == null ? new Stairs(p_175872_5_, rand, lvt_7_1_, p_175872_6_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 6, 10, 6, BlockIDIndexes.AIR_STATE                  , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0, 3, 2, 0, 5, 4, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 6, 3, 2, 6, 5, 2, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 6, 3, 4, 6, 5, 4, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);

            this.setBlockStateM(mut, selector, Blocks.NETHER_BRICK.getDefaultState(), 5, 2, 5, box);

            this.fillWithBlocks(mut, selector, box, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 6, 8, 2, 6, 8, 4, BlockIDIndexes.AIR_STATE             , false);
            this.fillWithBlocks(mut, selector, box, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 2, 5, 0, 4, 5, 0, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);

            for(int x = 0; x <= 6; ++x) {
                for(int z = 0; z <= 6; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                }
            }
            return true;
        }
    }


    public static class Crossing extends Piece {
        public Crossing() {}

        public Crossing(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            this.getNextComponentNormal((Start)comp, list, rand, 2, 0, false);
            this.getNextComponentX((Start)comp, list, rand, 0, 2, false);
            this.getNextComponentZ((Start)comp, list, rand, 0, 2, false);
        }


        public static Crossing createPiece(List<StructureComponent> p_175873_0_, Random p_175873_1_, int p_175873_2_, int p_175873_3_, int p_175873_4_, EnumFacing p_175873_5_, int p_175873_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175873_2_, p_175873_3_, p_175873_4_, -2, 0, 0, 7, 9, 7, p_175873_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(p_175873_0_, lvt_7_1_) == null ? new Crossing(p_175873_6_, p_175873_1_, lvt_7_1_, p_175873_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 6, 7, 6, BlockIDIndexes.AIR_STATE                   , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 2, 5, 0, 4, 5, 0, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 2, 5, 6, 4, 5, 6, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 0, 5, 2, 0, 5, 4, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICK.getDefaultState()      , false);
            this.fillWithBlocks(mut, selector, box, 6, 5, 2, 6, 5, 4, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);

            for(int x = 0; x <= 6; ++x) {
                for(int z = 0; z <= 6; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                }
            }
            return true;
        }
    }


    public static class Crossing3 extends Piece {
        public Crossing3() {}

        public Crossing3(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }

        protected Crossing3(Random p_i2042_1_, int p_i2042_2_, int p_i2042_3_) {
            super(0);
            this.setCoordBaseMode(Plane.HORIZONTAL.random(p_i2042_1_));
            if (this.getCoordBaseMode().getAxis() == Axis.Z) {
                this.boundingBox = new StructureBoundingBox(p_i2042_2_, 64, p_i2042_3_, p_i2042_2_ + 19 - 1, 73, p_i2042_3_ + 19 - 1);
            } else {
                this.boundingBox = new StructureBoundingBox(p_i2042_2_, 64, p_i2042_3_, p_i2042_2_ + 19 - 1, 73, p_i2042_3_ + 19 - 1);
            }
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            this.getNextComponentNormal((Start)comp, list, rand, 8, 3, false);
            this.getNextComponentX((Start)comp, list, rand, 3, 8, false);
            this.getNextComponentZ((Start)comp, list, rand, 3, 8, false);
        }


        public static Crossing3 createPiece(List<StructureComponent> list, Random rand, int p_175885_2_, int p_175885_3_, int p_175885_4_, EnumFacing p_175885_5_, int p_175885_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175885_2_, p_175885_3_, p_175885_4_, -8, -3, 0, 19, 10, 19, p_175885_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(list, lvt_7_1_) == null ? new Crossing3(p_175885_6_, rand, lvt_7_1_, p_175885_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 7 , 3, 0 , 11, 4, 18, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0 , 3, 7 , 18, 4, 11, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 8 , 5, 0 , 10, 7, 18, BlockIDIndexes.AIR_STATE             , false);
            this.fillWithBlocks(mut, selector, box, 0 , 5, 8 , 18, 7, 10, BlockIDIndexes.AIR_STATE             , false);
            this.fillWithBlocks(mut, selector, box, 7 , 5, 0 , 7 , 5, 7 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 7 , 5, 11, 7 , 5, 18, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 11, 5, 0 , 11, 5, 7 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0 , 5, 7 , 7 , 5, 7 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 11, 5, 7 , 18, 5, 7 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0 , 5, 11, 7 , 5, 11, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 7 , 2, 0 , 11, 2, 5 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 7 , 2, 13, 11, 2, 18, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 7 , 0, 0 , 11, 1, 3 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 7 , 0, 15, 11, 1, 18, Blocks.NETHER_BRICK.getDefaultState(), false);

            int x;
            int z;
            for(x = 7; x <= 11; ++x) {
                for(z = 0; z <= 2; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, 18 - z, box);
                }
            }

            this.fillWithBlocks(mut, selector, box, 0 , 2, 7, 5 , 2, 11, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0 , 0, 7, 3 , 1, 11, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICK.getDefaultState(), false);

            for(x = 0; x <= 2; ++x) {
                for(z = 7; z <= 11; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), 18 - x, -1, z, box);
                }
            }
            return true;
        }
    }


    public static class End extends Piece {
        private int fillSeed;
        public End() {}

        public End(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
            this.fillSeed = rand.nextInt();
        }


        public static End createPiece(List<StructureComponent> list, Random rand, int p_175884_2_, int p_175884_3_, int p_175884_4_, EnumFacing p_175884_5_, int p_175884_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175884_2_, p_175884_3_, p_175884_4_, -1, -3, 0, 5, 10, 8, p_175884_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(list, lvt_7_1_) == null ? new End(p_175884_6_, rand, lvt_7_1_, p_175884_5_) : null;
        }


        @Override
        protected void readStructureFromNBT(NBTTagCompound nbt, TemplateManager manager) {
            super.readStructureFromNBT(nbt, manager);
            this.fillSeed = nbt.getInteger("Seed");
        }


        @Override
        protected void writeStructureToNBT(NBTTagCompound nbt) {
            super.writeStructureToNBT(nbt);
            nbt.setInteger("Seed", this.fillSeed);
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
            Random cRand = new Random(this.fillSeed);

            int xPos;
            int yPos;
            int zPos;
            for(xPos = 0; xPos <= 4; ++xPos) {
                for(yPos = 3; yPos <= 4; ++yPos) {
                    zPos = cRand.nextInt(8);
                    this.fillWithBlocks(mut, selector, box, xPos, yPos, 0, xPos, yPos, zPos, Blocks.NETHER_BRICK.getDefaultState(), false);
                }
            }

            xPos = cRand.nextInt(8);
            this.fillWithBlocks(mut, selector, box, 0, 5, 0, 0, 5, xPos, Blocks.NETHER_BRICK.getDefaultState(), false);
            xPos = cRand.nextInt(8);
            this.fillWithBlocks(mut, selector, box, 4, 5, 0, 4, 5, xPos, Blocks.NETHER_BRICK.getDefaultState(), false);

            for(xPos = 0; xPos <= 4; ++xPos) {
                yPos = cRand.nextInt(5);
                this.fillWithBlocks(mut, selector, box, xPos, 2, 0, xPos, 2, yPos, Blocks.NETHER_BRICK.getDefaultState(), false);
            }

            for(xPos = 0; xPos <= 4; ++xPos) {
                for(yPos = 0; yPos <= 1; ++yPos) {
                    zPos = cRand.nextInt(3);
                    this.fillWithBlocks(mut, selector, box, xPos, yPos, 0, xPos, yPos, zPos, Blocks.NETHER_BRICK.getDefaultState(), false);
                }
            }
            return true;
        }
    }

    public static class Straight extends Piece {
        public Straight() {}

        public Straight(int type, Random rand, StructureBoundingBox box, EnumFacing facing) {
            super(type);
            this.setCoordBaseMode(facing);
            this.boundingBox = box;
        }


        @Override
        public void buildComponent(StructureComponent comp, List<StructureComponent> list, Random rand) {
            this.getNextComponentNormal((Start)comp, list, rand, 1, 3, false);
        }


        public static Straight createPiece(List<StructureComponent> p_175882_0_, Random p_175882_1_, int p_175882_2_, int p_175882_3_, int p_175882_4_, EnumFacing p_175882_5_, int p_175882_6_) {
            StructureBoundingBox lvt_7_1_ = StructureBoundingBox.getComponentToAddBoundingBox(p_175882_2_, p_175882_3_, p_175882_4_, -1, -3, 0, 5, 10, 19, p_175882_5_);
            return isAboveGround(lvt_7_1_) && StructureComponent.findIntersecting(p_175882_0_, lvt_7_1_) == null ? new Straight(p_175882_6_, p_175882_1_, lvt_7_1_, p_175882_5_) : null;
        }


        @Override
        public boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {
            ChunkSelector selector = new ChunkSelector(world);
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            this.fillWithBlocks(mut, selector, box, 0, 3, 0 , 4, 4, 18, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 1, 5, 0 , 3, 7, 18, BlockIDIndexes.AIR_STATE             , false);
            this.fillWithBlocks(mut, selector, box, 0, 5, 0 , 0, 5, 18, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 5, 0 , 4, 5, 18, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 0 , 4, 2, 5 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 0, 0 , 4, 1, 3 , Blocks.NETHER_BRICK.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICK.getDefaultState(), false);

            for(int x = 0; x <= 4; ++x) {
                for(int z = 0; z <= 2; ++z) {
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, z, box);
                    this.replaceAirAndLiquidDownwards(selector, Blocks.NETHER_BRICK.getDefaultState(), x, -1, 18 - z, box);
                }
            }

            this.fillWithBlocks(mut, selector, box, 0, 1, 1 , 0, 4, 1 , Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 3, 4 , 0, 4, 4 , Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 3, 14, 0, 4, 14, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 0, 1, 17, 0, 4, 17, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 1, 1 , 4, 4, 1 , Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 3, 4 , 4, 4, 4 , Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 3, 14, 4, 4, 14, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            this.fillWithBlocks(mut, selector, box, 4, 1, 17, 4, 4, 17, Blocks.NETHER_BRICK_FENCE.getDefaultState(), false);
            return true;
        }
    }

    public static class Start extends Crossing3 {
        public PieceWeight lastPlaced;
        public List<PieceWeight> primaryWeights;
        public List<PieceWeight> secondaryWeights;
        public List<StructureComponent> pendingChildren = Lists.newArrayList();

        public Start() {}

        public Start(Random rand, int p_i2059_2_, int p_i2059_3_) {
            super(rand, p_i2059_2_, p_i2059_3_);
            this.primaryWeights = Lists.newArrayList();
            PieceWeight[] var4 = StructureNetherBridgePieces.PRIMARY_COMPONENTS;
            int var5 = var4.length;

            int var6;
            PieceWeight weight;
            for(var6 = 0; var6 < var5; ++var6) {
                weight = var4[var6];
                weight.placeCount = 0;
                this.primaryWeights.add(weight);
            }

            this.secondaryWeights = Lists.newArrayList();
            var4 = StructureNetherBridgePieces.SECONDARY_COMPONENTS;
            var5 = var4.length;

            for(var6 = 0; var6 < var5; ++var6) {
                weight = var4[var6];
                weight.placeCount = 0;
                this.secondaryWeights.add(weight);
            }
        }
    }

    abstract static class Piece extends StructureComponent {
        public Piece() {}

        protected Piece(int type) {
            super(type);
        }

        protected void readStructureFromNBT(NBTTagCompound nbt, TemplateManager manager) {}

        protected void writeStructureToNBT(NBTTagCompound nbt) {}


        private int getTotalWeight(List<PieceWeight> weights) {
            boolean flag = false;
            int total = 0;

            PieceWeight weight;
            for(Iterator var4 = weights.iterator(); var4.hasNext(); total += weight.weight) {
                weight = (PieceWeight)var4.next();
                if (weight.maxPlaceCount > 0 && weight.placeCount < weight.maxPlaceCount) {
                    flag = true;
                }
            }
            return flag ? total : -1;
        }


        private Piece generatePiece(Start start, List<PieceWeight> weights, List<StructureComponent> list, Random p_175871_4_, int p_175871_5_, int p_175871_6_, int p_175871_7_, EnumFacing p_175871_8_, int p_175871_9_) {
            int lvt_10_1_ = this.getTotalWeight(weights);
            boolean lvt_11_1_ = lvt_10_1_ > 0 && p_175871_9_ <= 30;
            int lvt_12_1_ = 0;

            while(lvt_12_1_ < 5 && lvt_11_1_) {
                ++lvt_12_1_;
                int lvt_13_1_ = p_175871_4_.nextInt(lvt_10_1_);
                Iterator<PieceWeight> var14 = weights.iterator();

                while(var14.hasNext()) {
                    PieceWeight lvt_15_1_ = var14.next();
                    lvt_13_1_ -= lvt_15_1_.weight;
                    if (lvt_13_1_ < 0) {
                        if (!lvt_15_1_.doPlace(p_175871_9_) || lvt_15_1_ == start.lastPlaced && !lvt_15_1_.allowInRow) {
                            break;
                        }

                        Piece lvt_16_1_ = StructureNetherBridgePieces.findAndCreateBridgePieceFactory(lvt_15_1_, list, p_175871_4_, p_175871_5_, p_175871_6_, p_175871_7_, p_175871_8_, p_175871_9_);
                        if (lvt_16_1_ != null) {
                            ++lvt_15_1_.placeCount;
                            start.lastPlaced = lvt_15_1_;
                            if (!lvt_15_1_.isValid()) {
                                weights.remove(lvt_15_1_);
                            }

                            return lvt_16_1_;
                        }
                    }
                }
            }

            return StructureNetherBridgePieces.End.createPiece(list, p_175871_4_, p_175871_5_, p_175871_6_, p_175871_7_, p_175871_8_, p_175871_9_);
        }


        private StructureComponent generateAndAddPiece(Start start, List<StructureComponent> list, Random rand, int p_175870_4_, int p_175870_5_, int p_175870_6_, @Nullable EnumFacing p_175870_7_, int p_175870_8_, boolean p_175870_9_) {
            if (Math.abs(p_175870_4_ - start.getBoundingBox().minX) <= 112 && Math.abs(p_175870_6_ - start.getBoundingBox().minZ) <= 112) {
                List<PieceWeight> lvt_10_1_ = start.primaryWeights;
                if (p_175870_9_) {
                    lvt_10_1_ = start.secondaryWeights;
                }

                StructureComponent lvt_11_1_ = this.generatePiece(start, lvt_10_1_, list, rand, p_175870_4_, p_175870_5_, p_175870_6_, p_175870_7_, p_175870_8_ + 1);
                if (lvt_11_1_ != null) {
                    list.add(lvt_11_1_);
                    start.pendingChildren.add(lvt_11_1_);
                }

                return lvt_11_1_;
            } else {
                return StructureNetherBridgePieces.End.createPiece(list, rand, p_175870_4_, p_175870_5_, p_175870_6_, p_175870_7_, p_175870_8_);
            }
        }


        @Nullable
        protected StructureComponent getNextComponentNormal(Start start, List<StructureComponent> list, Random rand, int p_74963_4_, int p_74963_5_, boolean p_74963_6_) {
            EnumFacing facing = this.getCoordBaseMode();
            if (facing != null) {
                switch (facing) {
                    case NORTH:
                        return this.generateAndAddPiece(start, list, rand, this.boundingBox.minX + p_74963_4_, this.boundingBox.minY + p_74963_5_, this.boundingBox.minZ - 1, facing, this.getComponentType(), p_74963_6_);
                    case SOUTH:
                        return this.generateAndAddPiece(start, list, rand, this.boundingBox.minX + p_74963_4_, this.boundingBox.minY + p_74963_5_, this.boundingBox.maxZ + 1, facing, this.getComponentType(), p_74963_6_);
                    case WEST:
                        return this.generateAndAddPiece(start, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY + p_74963_5_, this.boundingBox.minZ + p_74963_4_, facing, this.getComponentType(), p_74963_6_);
                    case EAST:
                        return this.generateAndAddPiece(start, list, rand, this.boundingBox.maxX + 1, this.boundingBox.minY + p_74963_5_, this.boundingBox.minZ + p_74963_4_, facing, this.getComponentType(), p_74963_6_);
                }
            }
            return null;
        }


        @Nullable
        protected StructureComponent getNextComponentX(Start start, List<StructureComponent> list, Random rand, int p_74961_4_, int p_74961_5_, boolean flag) {
            EnumFacing facing = this.getCoordBaseMode();
            if (facing != null) {
                switch (facing) {
                    case NORTH:
                        return this.generateAndAddPiece(start, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY + p_74961_4_, this.boundingBox.minZ + p_74961_5_, EnumFacing.WEST, this.getComponentType(), flag);
                    case SOUTH:
                        return this.generateAndAddPiece(start, list, rand, this.boundingBox.minX - 1, this.boundingBox.minY + p_74961_4_, this.boundingBox.minZ + p_74961_5_, EnumFacing.WEST, this.getComponentType(), flag);
                    case WEST:
                        return this.generateAndAddPiece(start, list, rand, this.boundingBox.minX + p_74961_5_, this.boundingBox.minY + p_74961_4_, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType(), flag);
                    case EAST:
                        return this.generateAndAddPiece(start, list, rand, this.boundingBox.minX + p_74961_5_, this.boundingBox.minY + p_74961_4_, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType(), flag);
                }
            }
            return null;
        }


        @Nullable
        protected StructureComponent getNextComponentZ(Start start, List<StructureComponent> p_74965_2_, Random p_74965_3_, int p_74965_4_, int p_74965_5_, boolean p_74965_6_) {
            EnumFacing facing = this.getCoordBaseMode();
            if (facing != null) {
                switch (facing) {
                    case NORTH:
                        return this.generateAndAddPiece(start, p_74965_2_, p_74965_3_, this.boundingBox.maxX + 1, this.boundingBox.minY + p_74965_4_, this.boundingBox.minZ + p_74965_5_, EnumFacing.EAST, this.getComponentType(), p_74965_6_);
                    case SOUTH:
                        return this.generateAndAddPiece(start, p_74965_2_, p_74965_3_, this.boundingBox.maxX + 1, this.boundingBox.minY + p_74965_4_, this.boundingBox.minZ + p_74965_5_, EnumFacing.EAST, this.getComponentType(), p_74965_6_);
                    case WEST:
                        return this.generateAndAddPiece(start, p_74965_2_, p_74965_3_, this.boundingBox.minX + p_74965_5_, this.boundingBox.minY + p_74965_4_, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType(), p_74965_6_);
                    case EAST:
                        return this.generateAndAddPiece(start, p_74965_2_, p_74965_3_, this.boundingBox.minX + p_74965_5_, this.boundingBox.minY + p_74965_4_, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType(), p_74965_6_);
                }
            }
            return null;
        }


        protected static boolean isAboveGround(StructureBoundingBox box) {
            return box != null && box.minY > 10;
        }
    }


    static class PieceWeight {
        public Class<? extends Piece> weightClass;
        public final int weight;
        public int placeCount;
        public int maxPlaceCount;
        public boolean allowInRow;

        public PieceWeight(Class<? extends Piece> clazz, int weight, int max, boolean allowInRow) {
            this.weightClass = clazz;
            this.weight = weight;
            this.maxPlaceCount = max;
            this.allowInRow = allowInRow;
        }


        public PieceWeight(Class<? extends Piece> clazz, int weight, int max) {
            this(clazz, weight, max, false);
        }


        public boolean doPlace(int ignored) {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }


        public boolean isValid() {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }
    }
}
