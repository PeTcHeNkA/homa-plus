package plus.fserver.mixins.pf;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import plus.fserver.world.SpawnEntityFactory;
import java.util.function.Function;


@Mixin(value = EntityEntry.class, remap = false)
public class EntityEntryPatch {
    @Shadow private Class<? extends Entity> cls;
    @Shadow Function<World, ? extends Entity> factory;

    /**
     * @author Te4hno
     * @reason faster spawn
     */
    @Overwrite
    protected void init(){
        factory = SpawnEntityFactory.make(cls);
    }
}