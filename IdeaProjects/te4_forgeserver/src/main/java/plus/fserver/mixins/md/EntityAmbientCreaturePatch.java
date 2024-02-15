package plus.fserver.mixins.md;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(EntityAmbientCreature.class)
public class EntityAmbientCreaturePatch extends EntityLiving{
    public EntityAmbientCreaturePatch(World worldIn) {
        super(worldIn);
    }


    @Override
    public boolean isCreatureType(@NotNull EnumCreatureType type, boolean forSpawnCount) {
        if(forSpawnCount && isNoDespawnRequired())return false;
        return type == EnumCreatureType.AMBIENT;
    }
}