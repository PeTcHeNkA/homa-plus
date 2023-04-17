package plus.mixins;

import com.google.common.collect.Sets;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Set;


@Mixin(EntityTracker.class)
public class MixinEntityTracker {
    @Final
    @Mutable
    @Shadow
    private Set<EntityTrackerEntry> entries;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(WorldServer theWorldIn, CallbackInfo ci){
        entries = new ConcurrentSet<>();
    }
}