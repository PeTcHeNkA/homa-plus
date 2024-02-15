package plus.fserver.mixins.md;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(EntityMob.class)
public abstract class EntityMobPatch extends EntityLiving{
    public EntityMobPatch(World worldIn) {
        super(worldIn);
    }


    @Override
    public boolean isCreatureType(@NotNull EnumCreatureType type, boolean forSpawnCount) {
        if(forSpawnCount && isNoDespawnRequired())return false;
        return type == EnumCreatureType.MONSTER;
    }


    /**
     * @author Te4hno
     * @reason fix
     */
    @Overwrite
    protected boolean isValidLightLevel() {
        BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        if (this.world.getLightFor(EnumSkyBlock.SKY, blockpos) > this.rand.nextInt(32)) {
            return false;
        } else {
            int i;
            if (this.world.isThundering()) {
                int prev = this.world.getSkylightSubtracted();
                this.world.setSkylightSubtracted(10);
                i = this.world.getLightFromNeighbors(blockpos);
                this.world.setSkylightSubtracted(prev);
            } else {
                i = this.world.getLightFromNeighbors(blockpos);
            }
            return i <= this.rand.nextInt(8);
        }
    }
}