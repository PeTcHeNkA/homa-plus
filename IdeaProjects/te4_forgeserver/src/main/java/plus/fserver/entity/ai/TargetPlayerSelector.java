package plus.fserver.entity.ai;

import com.google.common.base.Predicate;
import net.minecraft.entity.player.EntityPlayer;
import javax.annotation.Nullable;


public class TargetPlayerSelector implements Predicate<EntityPlayer>{
    public static final TargetNullPlayerSelector NULL_PLAYER_SELECTOR = new TargetNullPlayerSelector();
    private final Predicate<EntityPlayer> targetSelector;

    public TargetPlayerSelector(Predicate<EntityPlayer> targetSelector) {
        this.targetSelector = targetSelector;
    }


    @Override
    public boolean apply(@Nullable EntityPlayer entity) {
        if (entity == null || entity.isDead || !targetSelector.apply(entity)) return false;
        return !entity.isSpectator();
    }


    public static final class TargetNullPlayerSelector implements Predicate<EntityPlayer>{
        @Override
        public boolean apply(@Nullable EntityPlayer entity) {
            if(entity == null || entity.isDead)return false;
            return !entity.isSpectator();
        }
    }
}
