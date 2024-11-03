package net.minecraft.world.gen.feature;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.api.Keep;
import plus.fserver.server.elca.ChunkSelector;


public class WorldGenMinable extends CWorldGenerator {
    private static final Predicate<IBlockState> DEFAULT_PREDICATE = new StonePredicateV2();
    private final IBlockState oreBlock;
    private final int numberOfBlocks;
    private final Predicate<IBlockState> predicate;

    @Keep
    public WorldGenMinable(IBlockState state, int blockCount) {
        this(state, blockCount, DEFAULT_PREDICATE);
    }


    @Keep
    public WorldGenMinable(IBlockState state, int blockCount, Predicate<IBlockState> predicate) {
        this.oreBlock = state;
        this.numberOfBlocks = blockCount;
        this.predicate = predicate;
    }


    @Override
    public boolean generate(World world, ChunkSelector selector, Random rand, BlockPos position) {
        float fl1 = rand.nextFloat() * (float)Math.PI,
              d4 = MathHelper.sin(fl1), d5 = MathHelper.cos(fl1),
              d10 = this.numberOfBlocks >> 3;

        float d0 = (position.getX() + 8) + d4 * d10;
        float d1 = (position.getX() + 8) - d4 * d10;
        float d2 = (position.getZ() + 8) + d5 * d10;
        float d3 = (position.getZ() + 8) - d5 * d10;

        d4 = position.getY() + rand.nextInt(3) - 2;
        d5 = position.getY() + rand.nextInt(3) - 2;

        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        Predicate<IBlockState> predicate = this.predicate;

        for (int i = 0, num = this.numberOfBlocks; i < num; ++i) {
            float f1 = (float)i / (float)num;
            float d6 = d0 + (d1 - d0) * f1;
            float d7 = d4 + (d5 - d4) * f1;
            float d8 = d2 + (d3 - d2) * f1;
            float d9 = rand.nextFloat() * (num >> 4);

            d10 = (MathHelper.sin((float)Math.PI * f1) + 1.0F) * d9 + 1;

            int startX = MathHelper.floor(d6 - d10 * 0.5f);
            int startY = MathHelper.floor(d7 - d10 * 0.5f);
            if(startY<1)startY = 1;
            int l = MathHelper.floor(d8 - d10 * 0.5f);
            int i1 = MathHelper.floor(d6 + d10 * 0.5f);
            int lastY = MathHelper.floor(d7 + d10 * 0.5f);
            if(lastY>255)lastY = 255;
            int k1 = MathHelper.floor(d8 + d10 * 0.5f);
            float d10N2 = 1/(d10 * 0.5f);
            final IBlockState ore = this.oreBlock;

            for (int xPos = startX; xPos <= i1; ++xPos) {
                float d12 = (xPos + 0.5f - d6) * d10N2;

                if ((d12 *= d12) < 1) {
                    for (int yPos = startY; yPos <= lastY; ++yPos) {

                        float d13 = (yPos + 0.5f - d7) * d10N2;
                        d13 *= d13;

                        if ((fl1 = d12 + d13) < 1) {
                            for (int zPos = l; zPos <= k1; ++zPos) {
                                float d14 = (zPos + 0.5f - d8) * d10N2;

                                if (fl1 + d14 * d14 < 1) {
                                    Chunk chunk = selector.getChunkE(xPos>>4, zPos>>4);
                                    IBlockState state = chunk.getBlockStateUnchecked(xPos, yPos, zPos);
                                    mut.setPosB(xPos, yPos, zPos);
                                    if (state.getBlock().isReplaceableOreGen(state, selector, mut, predicate)) {
                                        chunk.setBlockStateFastNoTile(xPos, yPos, zPos, ore);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}