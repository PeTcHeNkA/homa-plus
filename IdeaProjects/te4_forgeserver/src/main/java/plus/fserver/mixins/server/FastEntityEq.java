package plus.fserver.mixins.server;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public class FastEntityEq {
    @Shadow private int entityId;
    @Override
    public boolean equals(Object obj) {
        return ((FastEntityEq)obj).entityId == this.entityId;
    }
}