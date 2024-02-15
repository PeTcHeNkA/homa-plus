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
    @SideOnly(Side.CLIENT)
    public double getSpXZRot() {
        return mem.curZX;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public double getPrevSpXZRot() {
        return mem.prevXZ;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public double getPrevSpYRot() {
        return mem.prevY;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public double getSpYRot() {
        return mem.curY;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void clearSpRot() {
        mem.prevXZ = mem.curZX = mem.cache[0] = mem.cache[1] = MathUtils.wrapDegrees360(180-rotationYaw);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public boolean isActive() {
        return mem.active;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void setActive(boolean active) {
        mem.active = active;
    }



    @Override
    @SideOnly(Side.CLIENT)
    public void setDCache(double[] cache) {
        mem.cache = cache;
    }
}