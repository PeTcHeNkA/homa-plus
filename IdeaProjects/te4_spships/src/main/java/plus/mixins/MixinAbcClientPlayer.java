package plus.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import plus.client.entity.ISpMemory;


@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbcClientPlayer extends EntityPlayer implements ISpMemory {
    private boolean active;
    private double prevXZRot, prevYRot, curXZRot, curYRot;
    private int freezeTimes;

    public MixinAbcClientPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean b) {
        active = false;
    }


    @Override
    public double getPrevXZRot() {
        return prevXZRot;
    }


    @Override
    public double getPrevYRot() {
        return prevYRot;
    }


    @Override
    public void setPrevXZRot(double v) {
        prevXZRot = v;
    }


    @Override
    public void setPrevYRot(double v) {
        prevYRot = v;
    }


    @Override
    public double getCurXZRot() {
        return curXZRot;
    }

    @Override
    public double getCurYRot() {
        return curYRot;
    }


    @Override
    public void setCurRot(double xz, double y) {
        prevXZRot = curXZRot;
        prevYRot = curYRot;
        curXZRot = xz;
        curYRot = y;
    }

    @Override
    public void setCurXZRot(double xz) {
        prevXZRot = curXZRot;
        curXZRot = xz;
    }

    @Override
    public void setCurYRot(double y) {
        prevYRot = curYRot;
        curYRot = y;
    }

    @Override
    public int getFreezeTimes() {
        return freezeTimes;
    }


    @Override
    public void setFreezeTimes(int t) {
        this.freezeTimes = t;
    }


//    @Override
//    public void onEntityUpdate() {
//        prevXZRot =
//        super.onEntityUpdate();
//    }
}
