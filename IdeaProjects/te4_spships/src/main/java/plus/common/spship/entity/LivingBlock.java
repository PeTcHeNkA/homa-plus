package plus.common.spship.entity;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.utls.MathUtils;


public abstract class LivingBlock extends Entity implements ILivingBlock {
    private final Mem mem = newMem();

    public LivingBlock(World worldIn) {
        super(worldIn);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public Mem newMem() {
        return new Mem();
    }


    @SideOnly(Side.CLIENT)
    static class Mem{
        double curZX;
        double prevXZ;
        double curY = 0;
        double prevY = 0;
        boolean active = false;
        double[] cache = new double[3];
    }


    @Override
    public double[] getDCache() {
        return mem.cache;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void onClientInit() {
        clearSpRot();
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void onClientUpdate() {
        double dx = MathUtils.range(posX, prevPosX);
        double dz = MathUtils.range(posZ, prevPosZ);

        mem.prevXZ = mem.curZX;
        if ((0.06 < dx || -0.06 > dx) || (0.06 < dz || -0.06 > dz)) {
            mem.curZX = Math.toDegrees(Math.atan2(dx, dz));
        }
        mem.prevY = mem.curY;
        mem.curY = MathUtils.calcYrot(this);
    }


    @Override
    public double getSpXZRot() {
        return mem.curZX;
    }


    @Override
    public double getPrevSpXZRot() {
        return mem.prevXZ;
    }


    @Override
    public double getPrevSpYRot() {
        return mem.prevY;
    }


    @Override
    public double getSpYRot() {
        return mem.curY;
    }


    @Override
    public void clearSpRot() {
        mem.prevXZ = mem.curZX = mem.cache[0] = mem.cache[1] = 180-rotationYaw;
    }


    @Override
    public boolean isActive() {
        return mem.active;
    }


    @Override
    public void setActive(boolean active) {
        mem.active = active;
    }


    @Override
    public void setDCache(double[] cache) {
        mem.cache = cache;
    }
}