package plus.fserver.mixins.md;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockIDIndexes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.fserver.utls.ChunkSelector;
import java.util.Random;


@Mixin(BlockLeaves.class)
public abstract class BlockLeavesPatch extends Block{
    @Shadow @Final public static PropertyBool CHECK_DECAY;
    @Shadow @Final public static PropertyBool DECAYABLE;
    @Shadow int[] surroundings;

    public BlockLeavesPatch(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    /**
     * @author Te4hno
     * @reason faster tick
     */
    @Overwrite
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            if (state.getValue(CHECK_DECAY) && state.getValue(DECAYABLE)) {
                if (this.surroundings == null)
                    this.surroundings = new int[32768];

                if (worldIn.isAreaLoaded(pos, 6)) {
                    int num1 = pos.getX(), num2 = pos.getY(), num3 = pos.getZ(), num4, num5, num6, temp;
                    BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos();
                    ChunkSelector selector = ((WorldServer)worldIn).localSelector;

                    for (num4 = -4; num4 <= 4; ++num4) {
                        for (num5 = -4; num5 <= 4; ++num5) {
                            if((temp = num2 + num5)<0 || temp>255)continue;
                            for (num6 = -4; num6 <= 4; ++num6) {

                                IBlockState iblockstate = selector.getState(pos1.setPos(num1 + num4, temp, num3 + num6));
                                Block block = iblockstate.getBlock();

                                if (!block.canSustainLeaves(iblockstate, worldIn, pos1)) {
                                    if (block.isLeaves(iblockstate, worldIn, pos1)) {
                                        this.surroundings[((num4 + 16) << 10) + ((num5 + 16) << 5) + num6 + 16] = -2;
                                    } else {
                                        this.surroundings[((num4 + 16) << 10) + ((num5 + 16) << 5) + num6 + 16] = -1;
                                    }
                                } else {
                                    this.surroundings[((num4 + 16) << 10) + ((num5 + 16) << 5) + num6 + 16] = 0;
                                }
                            }
                        }
                    }
                    for (num4 = 1; num4 <= 4; ++num4) {
                        for (num1 = -4; num1 <= 4; ++num1) {
                            for (num2 = -4; num2 <= 4; ++num2) {
                                for (num3 = -4; num3 <= 4; ++num3) {
                                    if (this.surroundings[((num1 + 16) << 10) + ((num2 + 16) << 5) + num3 + 16] == num4 - 1) {

                                        num5 = ((num2 + 16) << 5) + num3 + 16;
                                        num6 = ((num1 + 16 - 1) << 10) + num5;
                                        if (this.surroundings[num6] == -2)
                                            this.surroundings[num6] = num4;
                                        num6 = ((num1 + 16 + 1) << 10) + num5;
                                        if (this.surroundings[num6] == -2)
                                            this.surroundings[num6] = num4;


                                        num5 = ((num1 + 16) << 10) + num3 + 16;
                                        num6 = num5 + ((num2 + 16 - 1) << 5);
                                        if (this.surroundings[num6] == -2)
                                            this.surroundings[num6] = num4;
                                        num6 = num5 + ((num2 + 16 + 1) << 5);
                                        if (this.surroundings[num6] == -2)
                                            this.surroundings[num6] = num4;


                                        num5 = ((num1 + 16) << 10) + ((num2 + 16) << 5);
                                        num6 = num5 + (num3 + 16 - 1);
                                        if (this.surroundings[num6] == -2)
                                            this.surroundings[num6] = num4;
                                        num6 = num5 + num3 + 16 + 1;
                                        if (this.surroundings[num6] == -2) {
                                            this.surroundings[num6] = num4;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (!worldIn.isAreaLoaded(pos, 1)) return;

                if (this.surroundings[16912] >= 0) {
                    worldIn.getChunk(pos).setBlockStateFast(
                            pos.getX(), pos.getY(), pos.getZ(), state,
                            state.withProperty(CHECK_DECAY, Boolean.FALSE)
                    );
                } else {
                    Chunk chunk = worldIn.getChunk(pos);
                    this.dropBlockAsItem(worldIn, pos, chunk.getBlockState(pos), 0);
                    chunk.setBlockStateFastNoTile(pos.getX(), pos.getY(), pos.getZ(), BlockIDIndexes.AIR_STATE);
                    worldIn.checkLight(pos);
                    worldIn.markAndNotifyBlock(pos, chunk, state, BlockIDIndexes.AIR_STATE, 3);
                }
            }
        }
    }
}