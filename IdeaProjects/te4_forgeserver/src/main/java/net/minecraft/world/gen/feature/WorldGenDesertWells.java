package net.minecraft.world.gen.feature;

import com.google.common.base.Predicates;
import java.util.Random;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStoneSlab.EnumType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenDesertWells extends WorldGenerator {
    private static final BlockStateMatcher IS_SAND;
    private final IBlockState sandSlab;
    private final IBlockState sandstone;
    private final IBlockState water;

    public WorldGenDesertWells() {
        this.sandSlab = Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, EnumType.SAND).withProperty(BlockSlab.HALF, EnumBlockHalf.BOTTOM);
        this.sandstone = Blocks.SANDSTONE.getDefaultState();
        this.water = Blocks.FLOWING_WATER.getDefaultState();
    }


    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        ChunkSelector selector = new ChunkSelector(world);
        while(selector.isAirBlock(pos) && pos.getY() > 2) {
            pos = pos.down();
        }

        if (!IS_SAND.apply(selector.getBlockState(pos))) {
            return false;
        } else {
            int n1;
            int n2;

            final int px = pos.getX(), py = pos.getY(), pz = pos.getZ();
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

            for(n1 = -2; n1 <= 2; ++n1) {
                for(n2 = -2; n2 <= 2; ++n2) {
                    if (selector.isAirBlock(pos.add(n1, -1, n2)) && selector.isAirBlock(pos.add(n1, -2, n2))) {
                        return false;
                    }
                }
            }

            for(n1 = -1; n1 <= 0; ++n1) {
                for(n2 = -2; n2 <= 2; ++n2) {
                    for(int n3 = -2; n3 <= 2; ++n3) {
                        selector.setBlockState(mut.setPos(px+n2, py+n1, pz+n3), this.sandstone, 2);
                    }
                }
            }

            selector.setBlockState(pos, this.water, 2);

            for (EnumFacing facing : Plane.HORIZONTAL.facings()) {
                selector.setBlockState(pos.offset(facing), this.water, 2);
            }

            for(n1 = -2; n1 <= 2; ++n1) {
                for(n2 = -2; n2 <= 2; ++n2) {
                    if (n1 == -2 || n1 == 2 || n2 == -2 || n2 == 2) {
                        selector.setBlockState(mut.setPos(px+n1, py+1, pz+n2), this.sandstone, 2);
                    }
                }
            }

            selector.setBlockState(mut.setPos(px+2, py+1, pz), this.sandSlab, 2);
            selector.setBlockState(mut.setPos(px-2, py+1, pz), this.sandSlab, 2);
            selector.setBlockState(mut.setPos(px, py+1, pz+2), this.sandSlab, 2);
            selector.setBlockState(mut.setPos(px, py+1, pz-2), this.sandSlab, 2);

            for(n1 = -1; n1 <= 1; ++n1) {
                for(n2 = -1; n2 <= 1; ++n2) {
                    mut.setPosB(px+n1, py+4, pz+n2);
                    if (n1 == 0 && n2 == 0) {
                        selector.setBlockState(mut, this.sandstone, 2);
                    } else {
                        selector.setBlockState(mut, this.sandSlab, 2);
                    }
                }
            }

            for(n1 = 1; n1 <= 3; ++n1) {
                selector.setBlockState(mut.setPos(px-1, py+n1, pz-1), this.sandstone, 2);
                selector.setBlockState(mut.setPos(px-1, py+n1, pz+1), this.sandstone, 2);
                selector.setBlockState(mut.setPos(px+1, py+n1, pz-1), this.sandstone, 2);
                selector.setBlockState(mut.setPos(px+1, py+n1, pz+1), this.sandstone, 2);
            }
            return true;
        }
    }

    static {
        IS_SAND = BlockStateMatcher.forBlock(Blocks.SAND).where(BlockSand.VARIANT, Predicates.equalTo(net.minecraft.block.BlockSand.EnumType.SAND));
    }
}