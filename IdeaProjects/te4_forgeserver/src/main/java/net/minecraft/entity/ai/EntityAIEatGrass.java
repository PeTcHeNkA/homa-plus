package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;


public class EntityAIEatGrass extends EntityAIBase {
    private static final Predicate<IBlockState> IS_TALL_GRASS = BlockStateMatcher.forBlock(Blocks.TALLGRASS).where(BlockTallGrass.TYPE, Predicates.equalTo(BlockTallGrass.EnumType.GRASS));
    private final EntityLiving grassEaterEntity;
    private final World entityWorld;
    int eatingGrassTimer;

    public EntityAIEatGrass(EntityLiving grassEaterEntityIn) {
        this.grassEaterEntity = grassEaterEntityIn;
        this.entityWorld = grassEaterEntityIn.world;
        this.setMutexBits(7);
    }


    @Override
    public boolean shouldExecute() {
        if (this.grassEaterEntity.getRNG().nextInt(this.grassEaterEntity.isChild() ? 50 : 1000) != 0) {
            return false;
        } else {
            BlockPos blockpos = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY, this.grassEaterEntity.posZ);
            Chunk chunk = this.entityWorld.getChunk(blockpos);
            if (IS_TALL_GRASS.apply(chunk.getBlockState(blockpos))) {
                return true;
            } else {
                return chunk.getBlockState(blockpos.down()).getBlock() == Blocks.GRASS;
            }
        }
    }


    @Override
    public void startExecuting() {
        this.eatingGrassTimer = 40;
        this.entityWorld.setEntityState(this.grassEaterEntity, (byte)10);
        this.grassEaterEntity.getNavigator().clearPath();
    }


    @Override
    public void resetTask() {
        this.eatingGrassTimer = 0;
    }


    @Override
    public boolean shouldContinueExecuting() {
        return this.eatingGrassTimer > 0;
    }


    public int getEatingGrassTimer() {
        return this.eatingGrassTimer;
    }


    @Override
    public void updateTask() {
        this.eatingGrassTimer = Math.max(0, this.eatingGrassTimer - 1);

        if (this.eatingGrassTimer == 4) {
            BlockPos blockpos = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY, this.grassEaterEntity.posZ);
            Chunk chunk = this.entityWorld.getChunk(blockpos);

            if (IS_TALL_GRASS.apply(chunk.getBlockState(blockpos))) {
                if (ForgeEventFactory.getMobGriefingEvent(this.entityWorld, this.grassEaterEntity)) {
                    this.entityWorld.destroyBlock(blockpos, false);
                }
                this.grassEaterEntity.eatGrassBonus();
            } else {
                BlockPos blockpos1 = blockpos.down();

                if (chunk.getBlockState(blockpos1).getBlock() == Blocks.GRASS) {
                    if (ForgeEventFactory.getMobGriefingEvent(this.entityWorld, this.grassEaterEntity)) {
                        this.entityWorld.playEvent(2001, blockpos1, Block.getIdFromBlock(Blocks.GRASS));
                        chunk.setBlockState(blockpos1, Blocks.DIRT.getDefaultState());
                    }
                    this.grassEaterEntity.eatGrassBonus();
                }
            }
        }
    }
}