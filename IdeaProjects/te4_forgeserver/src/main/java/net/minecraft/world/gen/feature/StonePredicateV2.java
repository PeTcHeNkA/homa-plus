package net.minecraft.world.gen.feature;

import com.google.common.base.Predicate;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import org.jetbrains.annotations.Nullable;

final class StonePredicateV2 implements Predicate<IBlockState> {
    private static final IBlockState state1 = Blocks.STONE.getDefaultState()
            .withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE);
    private static final IBlockState state2 = Blocks.STONE.getDefaultState()
            .withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE);
    private static final IBlockState state3 = Blocks.STONE.getDefaultState()
            .withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE);
    private static final IBlockState state4 = Blocks.STONE.getDefaultState()
            .withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE);

    @Override
    public boolean apply(@Nullable IBlockState state) {
        return state == state1 || state == state2 || state == state3 || state == state4;
    }
}
