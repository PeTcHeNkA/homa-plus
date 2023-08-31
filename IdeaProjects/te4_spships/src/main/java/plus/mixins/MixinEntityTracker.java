package plus.mixins;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import java.util.Set;


@Mixin(EntityTracker.class)
public class MixinEntityTracker {
    @Final
    @Mutable
    @Shadow
    private final Set<EntityTrackerEntry> entries = new ConcurrentSet<>();
//
//    @Inject(method = "<init>", at = @At("TAIL"))
//    public void init(WorldServer theWorldIn, CallbackInfo ci){
//        entries = new ConcurrentSet<>();
//    }
}