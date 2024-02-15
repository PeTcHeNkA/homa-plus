package net.minecraft.world.gen.feature;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import plus.fserver.utls.ChunkSelector;


public class WorldGenBigTree extends WorldGenAbstractTree {
    private final double heightAttenuation, branchSlope, scaleWidth, leafDensity;
    private final IBlockState defLogState;
    private final IBlockState defLeavesState;
    private final int heightLimitLimit, trunkSize;

    public WorldGenBigTree(boolean notify) {
        super(notify);
        defLogState    = Blocks.LOG.getDefaultState();
        defLeavesState = Blocks.LEAVES.getDefaultState();
        heightAttenuation = 0.618D;
        branchSlope = 0.381D;
        scaleWidth = 1.0D;
        leafDensity = 1.0D;
        heightLimitLimit = 12;
        trunkSize = 1;
    }

    public WorldGenBigTree(boolean notify, IBlockState log, IBlockState leaf,
                           double heightAttenuation, double branchSlope, double scaleWidth,
                           double leafDensity, int heightLimitLimit, int trunkSize
    ) {
        super(notify);
        this.defLogState       = log;
        this.defLeavesState    = leaf;
        this.heightAttenuation = heightAttenuation;
        this.branchSlope       = branchSlope;
        this.scaleWidth        = scaleWidth;
        this.leafDensity       = leafDensity;
        this.heightLimitLimit  = heightLimitLimit;
        this.trunkSize         = trunkSize;
    }


    void generateLeafNodeList(GenSession session, ArrayList<WorldGenBigTree.FoliageCoordinates> cords, BlockPos basePos) {
        session.height = (int)((double)session.heightLimit * this.heightAttenuation);
        if (session.height >= session.heightLimit) {
            session.height = session.heightLimit - 1;
        }

        int i = (int)(1.382D + Math.pow(this.leafDensity * (double)session.heightLimit / 13.0D, 2.0D));

        if (i < 1) {
            i = 1;
        }
        int j = basePos.getY() + session.height;
        int k = session.heightLimit - session.leafDistanceLimit;

        cords.add(new WorldGenBigTree.FoliageCoordinates(basePos.up(k), j));

        Random rand = session.selector.getWorld().rand;
        double d0, d1, d2, d3, d4;
        for (BlockPos pos1, pos2, pos3; k >= 0; --k) {
            float f = this.layerSize(session.heightLimit, k);

            if (f >= 0.0F) {
                for (int l = 0; l < i; ++l) {
                    d0 = this.scaleWidth * (double)f * ((double)rand.nextFloat() + 0.328D);
                    d1 = (double)(rand.nextFloat() * 2.0F) * Math.PI;
                    d2 = d0 * Math.sin(d1) + 0.5D;
                    d3 = d0 * Math.cos(d1) + 0.5D;
                    pos1 = basePos.add(d2, k - 1, d3);
                    pos2 = pos1.up(session.leafDistanceLimit);

                    if (this.checkBlockLine(session.selector, pos1, pos2) == -1) {
                        int i1 = basePos.getX() - pos1.getX();
                        int j1 = basePos.getZ() - pos1.getZ();
                        d4 = (double)pos1.getY() - Math.sqrt(i1 * i1 + j1 * j1) * this.branchSlope;
                        int k1 = d4 > (double)j ? j : (int)d4;
                        pos3 = new BlockPos(basePos.getX(), k1, basePos.getZ());

                        if (this.checkBlockLine(session.selector, pos3, pos1) == -1) {
                            cords.add(new WorldGenBigTree.FoliageCoordinates(pos1, pos3.getY()));
                        }
                    }
                }
            }
        }
    }


    void crosSection(ChunkSelector selector, BlockPos pos, float num, IBlockState state1) {
        int i = (int)((double)num + 0.618D);
        World world = selector.getWorld();
        IBlockState state;
        BlockPos blockpos;
        for (int j = -i; j <= i; ++j) {
            for (int k = -i; k <= i; ++k) {
                if (Math.pow((double)Math.abs(j) + 0.5D, 2.0D) +
                        Math.pow((double)Math.abs(k) + 0.5D, 2.0D) <= (double)(num * num)) {

                    state = selector.getState(blockpos = pos.add(j, 0, k));

                    if (state.getBlock().isAir(state, world, blockpos) ||
                            state.getBlock().isLeaves(state, world, blockpos)) {
                        this.setBlockAndNotifyAdequately(selector, blockpos, state1);
                    }
                }
            }
        }
    }


    float layerSize(int heightLimit, int y) {
        if ((float)y < (float)heightLimit * 0.3F) {
            return -1.0F;
        }
        else {
            float f = (float)heightLimit / 2.0F;
            float f1 = f - (float)y;
            float f2 = MathHelper.sqrt(f * f - f1 * f1);

            if (f1 == 0.0F) {
                f2 = f;
            }
            else if (Math.abs(f1) >= f) {
                return 0.0F;
            }
            return f2 * 0.5F;
        }
    }


    float leafSize(int leafDistanceLimit, int y) {
        if (y >= 0 && y < leafDistanceLimit) {
            return y != 0 && y != leafDistanceLimit - 1 ? 3.0F : 2.0F;
        } else {
            return -1.0F;
        }
    }


    void generateLeafNode(GenSession session, BlockPos pos) {
        IBlockState state = defLeavesState.withProperty(
                BlockLeaves.CHECK_DECAY, Boolean.FALSE
        );
        for (int i = 0; i < session.leafDistanceLimit; ++i)
            this.crosSection(
                    session.selector, pos.up(i),
                    this.leafSize(session.leafDistanceLimit, i), state
            );
    }


    void limb(ChunkSelector selector, BlockPos pod2, BlockPos pos2) {
        BlockPos blockpos = pos2.add(-pod2.getX(), -pod2.getY(), -pod2.getZ());
        int i = this.getGreatestDistance(blockpos);
        float f = (float)blockpos.getX() / (float)i;
        float f1 = (float)blockpos.getY() / (float)i;
        float f2 = (float)blockpos.getZ() / (float)i;

        for (int j = 0; j <= i; ++j) {
            blockpos = pod2.add(0.5F + (float)j * f, 0.5F + (float)j * f1, 0.5F + (float)j * f2);

            this.setBlockAndNotifyAdequately(
                    selector, blockpos,
                    defLogState.withProperty(BlockLog.LOG_AXIS, this.getLogAxis(pod2, blockpos))
            );
        }
    }


    private int getGreatestDistance(BlockPos posIn) {
        int i = MathHelper.abs(posIn.getX());
        int j = MathHelper.abs(posIn.getY());
        int k = MathHelper.abs(posIn.getZ());
        if (k > i && k > j) {
            return k;
        } else {
            return j > i ? j : i;
        }
    }


    private BlockLog.EnumAxis getLogAxis(BlockPos pos1, BlockPos pos) {
        BlockLog.EnumAxis axis = BlockLog.EnumAxis.Y;
        int i = Math.abs(pos.getX() - pos1.getX());
        int j = Math.abs(pos.getZ() - pos1.getZ());
        int k = Math.max(i, j);

        if (k > 0) {
            if (i == k) {
                axis = BlockLog.EnumAxis.X;
            } else {
                axis = BlockLog.EnumAxis.Z;
            }
        }
        return axis;
    }


    boolean leafNodeNeedsBase(int heightLimit, int num) {
        return (double)num >= (double)heightLimit * 0.2D;
    }


    int checkBlockLine(ChunkSelector selector, BlockPos posOne, BlockPos posTwo) {
        BlockPos blockpos = posTwo.add(-posOne.getX(), -posOne.getY(), -posOne.getZ());
        int i = this.getGreatestDistance(blockpos);

        if (i != 0) {
            float f = (float)blockpos.getX() / (float)i;
            float f1 = (float)blockpos.getY() / (float)i;
            float f2 = (float)blockpos.getZ() / (float)i;
            BlockPos blockpos1;
            for (int j = 0; j <= i; ++j) {
                blockpos1 = posOne.add(0.5F + (float) j * f, 0.5F + (float) j * f1, 0.5F + (float) j * f2);

                if (!this.isReplaceable(selector, selector.getWorld(), blockpos1)) {
                    return j;
                }
            }
        }
        return -1;
    }


    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        GenSession session = new GenSession(worldIn);
        if (session.heightLimit == 0) {
            session.heightLimit = 5 + worldIn.rand.nextInt(this.heightLimitLimit);
        }
        if (!this.validTreeLocation(session, position)) {
            return false;
        } else {
            final ArrayList<FoliageCoordinates> cords = new ArrayList<>();
            this.generateLeafNodeList(session, cords, position);
            BlockPos blockpos;

            for (WorldGenBigTree.FoliageCoordinates cord : cords) {
                this.generateLeafNode(session, cord);
            }

            blockpos = position.up(session.height);
            this.limb(session.selector, position, blockpos);

            if (this.trunkSize == 2) {
                this.limb(session.selector, position.east(), blockpos.east());
                this.limb(session.selector, position.add(1,0,1), blockpos.add(1,0,1));
                this.limb(session.selector, position.south(), blockpos.south());
            }

            for (WorldGenBigTree.FoliageCoordinates cord : cords) {
                blockpos = new BlockPos(position.getX(), cord.branchBase, position.getZ());
                if (!blockpos.equals(cord) && this.leafNodeNeedsBase(
                        session.heightLimit,
                        cord.branchBase - position.getY())) {
                    this.limb(session.selector, blockpos, cord);
                }
            }
            return true;
        }
    }



    private static class GenSession{
        final ChunkSelector selector;
        int heightLimit;
        int height;
        int leafDistanceLimit = 4;

        private GenSession(World world) {
            this.selector = new ChunkSelector(world);
        }
    }


    private boolean validTreeLocation(GenSession session, BlockPos basePos) {
        BlockPos down = basePos.down();
        IBlockState state = session.selector.getState(down);
        boolean isSoil = state.getBlock().canSustainPlant(state, session.selector.getWorld(), down,
                EnumFacing.UP, ((BlockSapling)Blocks.SAPLING));

        if (!isSoil) {
            return false;
        } else {
            int i = this.checkBlockLine(session.selector, basePos, basePos.up(session.heightLimit - 1));

            if (i == -1) {
                return true;
            }
            else if (i < 6) {
                return false;
            } else {
                session.heightLimit = i;
                return true;
            }
        }
    }


    static class FoliageCoordinates extends BlockPos {
        private final int branchBase;

        public FoliageCoordinates(BlockPos pos, int base) {
            super(pos.getX(), pos.getY(), pos.getZ());
            this.branchBase = base;
        }

        public int getBranchBase() {
            return this.branchBase;
        }
    }
}