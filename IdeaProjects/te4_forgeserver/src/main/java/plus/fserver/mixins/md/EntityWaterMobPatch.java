package plus.fserver.mixins.md;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(EntityWaterMob.class)
public class EntityWaterMobPatch extends EntityLiving {
    public EntityWaterMobPatch(World worldIn) {
        super(worldIn);
    }


    @Override
    public boolean isCreatureType(@NotNull EnumCreatureType type, boolean forSpawnCount) {
        if(forSpawnCount && isNoDespawnRequired())return false;
        return type == EnumCreatureType.WATER_CREATURE;
    }
}