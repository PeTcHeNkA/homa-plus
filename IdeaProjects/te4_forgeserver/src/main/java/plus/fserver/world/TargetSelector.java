package plus.fserver.world;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import javax.annotation.Nullable;


public final class TargetSelector<T extends EntityLivingBase> implements Predicate<T> {
    public static final TargetNullSelector NULL_TARGET_SELECTOR = new TargetNullSelector();
    private final Predicate<T> targetSelector;

    public TargetSelector(Predicate<T> targetSelector) {
        this.targetSelector = targetSelector;
    }


    @Override
    public boolean apply(@Nullable T entity) {
        if (entity == null || entity.isDead || !targetSelector.apply(entity)) return false;
        return !(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator();
    }


    public static final class TargetNullSelector implements Predicate<Entity>{
        @Override
        public boolean apply(@org.jetbrains.annotations.Nullable Entity entity) {
            if(entity == null || entity.isDead)return false;
            return !(entity instanceof EntityPlayer) || !((EntityPlayer)entity).isSpectator();
        }
    }
}