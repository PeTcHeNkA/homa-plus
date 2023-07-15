package plus.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import plus.client.ISpREntity;
import plus.common.player.PlayerSpship;
import plus.utls.MathUtils;


@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbcClientPlayer extends EntityPlayer implements ISpREntity, PlayerSpship {
    double curZX;
    double prevXZ;
    double curY = 0;
    double prevY = 0;
    boolean active = false;

    public MixinAbcClientPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
        prevXZ = 180-rotationYaw;
        curZX = prevXZ;
    }


    @Override
    public void onUpdate() {
        super.onUpdate();
        if(active) {
            double dx = MathUtils.range(posX, prevPosX);
            double dz = MathUtils.range(posZ, prevPosZ);

            prevXZ = curZX;
            if ((0.06 < dx || -0.06 > dx) || (0.06 < dz || -0.06 > dz)) {
                curZX = Math.toDegrees(Math.atan2(dx, dz));
            }
            prevY = curY;
            curY = MathUtils.calcYrot(this);
        }
    }


    @Override
    public double getSpXZRot() {
        return curZX;
    }


    @Override
    public double getPrevSpXZRot() {
        return prevXZ;
    }

    @Override
    public double getPrevSpYRot() {
        return prevY;
    }


    @Override
    public double getSpYRot() {
        return curY;
    }


    @Override
    public void clearSpRot() {
        prevXZ = 180-rotationYaw;
        curZX = prevXZ;
    }


    @Override
    public boolean isActive() {
        return active;
    }


    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
}