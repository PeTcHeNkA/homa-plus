package plus.fserver.mixins.md;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import plus.fserver.api.ICanElytraFly;


@Mixin(EntityPlayer.class)
public abstract class EntityPlayerPatch extends EntityLivingBase implements ICanElytraFly {
    public EntityPlayerPatch(World worldIn) {
        super(worldIn);
    }


    @Override
    public boolean isElytraFlying() {
        return this.getFlag(7);
    }
}