package plus.fserver.mixins.pf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.fserver.world.SpawnEntityFactory;
import java.util.function.Function;


@Mixin(Biome.SpawnListEntry.class)
public class SpawnListEntryPatch {
    @Shadow public Class<? extends EntityLiving> entityClass;
//
//    @Inject(method = "<init>", at = @At("TAIL"))
//    public void onInit(Class entityclassIn, int weight, int groupCountMin, int groupCountMax, CallbackInfo ci){
//
//    }

    public EntityLiving newInstance(World world) throws Exception {
        int hs;
        Function<World, ? extends Entity> func = SpawnEntityFactory.cache.get(hs = entityClass.hashCode());
        if(func != null)return (EntityLiving) func.apply(world);
        EntityEntry entry = EntityRegistry.getEntry(this.entityClass);
        if (entry != null) {
            SpawnEntityFactory.cache.put(hs, entry::newInstance);
            return (EntityLiving) entry.newInstance(world);
        } else {
            func = SpawnEntityFactory.make(entityClass);
            return (EntityLiving) func.apply(world);
        }
    }
}