package plus.fserver.mixins.pf;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import plus.fserver.patch.EmptyInvCraft;
import plus.fserver.patch.FastDyeBlend;


@Mixin(EntitySheep.class)
public abstract class EntitySheepPatch extends EntityAnimal{
    @Shadow @Final @Mutable
    private InventoryCrafting inventoryCrafting = EmptyInvCraft.INST;

    public EntitySheepPatch(World worldIn) {
        super(worldIn);
    }

    /**
     * @author Te4hno
     * @reason boost
     */
    @Overwrite
    private EnumDyeColor getDyeColorMixFromParents(EntityAnimal father, EntityAnimal mother) {
        EnumDyeColor i = ((EntitySheep)father).getFleeceColor();
        EnumDyeColor j = ((EntitySheep)mother).getFleeceColor();
        final EnumDyeColor blended = FastDyeBlend.getBlendedColor(i, j);

        if (blended != null) {
            return blended;
        } else {
            return father.world.rand.nextBoolean() ? i : j;
        }
    }
}