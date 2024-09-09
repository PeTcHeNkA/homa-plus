package plus.fserver.mixins.server;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(Entity.class)
public abstract class FastEntityEq {
    @Shadow public abstract int getEntityId();

    @Override
    public boolean equals(Object obj) {
        return ((FastEntityEq)obj).getEntityId() == this.getEntityId();
    }
}