package net.minecraft.entity.ai;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import plus.fserver.utls.ChunkSelector;


public class EntityAIPanic extends EntityAIBase {
    protected final EntityCreature creature;
    protected double speed;
    protected double randPosX;
    protected double randPosY;
    protected double randPosZ;

    public EntityAIPanic(EntityCreature creature, double speedIn) {
        this.creature = creature;
        this.speed = speedIn;
        this.setMutexBits(1);
    }


    @Override
    public boolean shouldExecute() {
        if (this.creature.getRevengeTarget() == null && !this.creature.isBurning()) {
            return false;
        } else {
            if (this.creature.isBurning()) {
                BlockPos blockpos = this.getRandPos(this.creature.world, this.creature, 5, 4);

                if (blockpos != null) {
                    this.randPosX = blockpos.getX();
                    this.randPosY = blockpos.getY();
                    this.randPosZ = blockpos.getZ();
                    return true;
                }
            }
            return this.findRandomPosition();
        }
    }


    protected boolean findRandomPosition() {
        Vec3d vec3d = RandomPositionGenerator.findRandomTarget(this.creature, 5, 4);
        if (vec3d == null) {
            return false;
        } else {
            this.randPosX = vec3d.x;
            this.randPosY = vec3d.y;
            this.randPosZ = vec3d.z;
            return true;
        }
    }


    @Override
    public void startExecuting() {
        this.creature.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);
    }


    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.getNavigator().noPath();
    }


    @Nullable
    private BlockPos getRandPos(World worldIn, Entity entityIn, int horizontalRange, int verticalRange) {
        BlockPos blockpos = new BlockPos(entityIn);
        int cX = blockpos.getX();
        int cY = blockpos.getY();
        int cZ = blockpos.getZ();
        float factor = (float)(horizontalRange * horizontalRange * verticalRange * 2);
        BlockPos.MutableBlockPos pos = null;
        //todo check?
        ChunkSelector selector = ((WorldServer)worldIn).localSelector;

        for (int aX = cX - horizontalRange; aX <= cX + horizontalRange; ++aX) {
            for (int aZ = cZ - horizontalRange; aZ <= cZ + horizontalRange; ++aZ) {
                Chunk chunk = selector.getChunk(aX, aZ);
                for (int aY = cY - verticalRange; aY <= cY + verticalRange; ++aY) {
                    IBlockState iblockstate = chunk.getBlockState(aX, aY, aZ);

                    if (iblockstate.getMaterial() == Material.WATER) {
                        float factor2 = (float) ((aX - cX) * (aX - cX) + (aY - cY) * (aY - cY) + (aZ - cZ) * (aZ - cZ));

                        if (factor2 < factor) {
                            factor = factor2;
                            if(pos == null)pos = new BlockPos.MutableBlockPos(aX, aY, aZ);
                            else pos.setPos(aX, aY, aZ);
                        }
                    }
                }
            }
        }
        return pos;
    }
}