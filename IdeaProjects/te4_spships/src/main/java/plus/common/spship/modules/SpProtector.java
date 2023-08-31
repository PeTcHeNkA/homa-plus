package plus.common.spship.modules;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import plus.common.amm.SpShootDamage;
import plus.common.mashine.inv.IItemLink;
import plus.common.spship.SPM;
import plus.common.spship.SpShip;
import static plus.common.spship.modules.SpAccelerator.*;


public final class SpProtector {//TODO ссылки на предметы и урон предметам при попадании
    private final float physicDmgCoef, energyDmgCoef;
    private final int physicEnrgCoef, energyEnrgCoef;
    private final IItemLink[] links;

    public SpProtector(float physicDmgCoef, float energyDmgCoef, int physicEnrgCoef, int energyEnrgCoef, IItemLink[] links) {
        this.physicDmgCoef = physicDmgCoef;
        this.energyDmgCoef = energyDmgCoef;
        this.physicEnrgCoef = physicEnrgCoef;
        this.energyEnrgCoef = energyEnrgCoef;
        this.links = links;
    }


    public static class Builder{
        private float physicDmgCoef = 0, energyDmgCoef = 0;
        private int physicEnrgCoef = 0, energyEnrgCoef = 0, count = 0;
        private final IItemLink[] links;

        public Builder(){
            links = new IItemLink[SpShip.SLOTS.length];
        }

        public void feed(SpInvProxy proxy, int slot){
            IItemLink link = links[count] = proxy.link(slot);

            NBTTagCompound tag = link.getItem().getTagCompound();
            if (tag == null) return;
            int[] ints = tag.getIntArray("spg");
            if (ints.length == 0) return;

            physicEnrgCoef += SPM.getIOf(ints, 1);
            energyEnrgCoef += SPM.getIOf(ints, 2);
            physicDmgCoef += SPM.getIOf(ints, 2);
            energyDmgCoef += SPM.getIOf(ints, 3);

            ++count;
        }


        public SpProtector complete(){
            float c = coefCount(count);

            return new SpProtector(
                    (1-physicDmgCoef)*c, (1-energyDmgCoef)*c,
                    physicEnrgCoef, energyEnrgCoef,
                    trimTo(links, count));
        }
    }


    public float onDamage(SpShip sp, DamageSource src, float amount){
        if(src instanceof SpShootDamage){
            return onDamage(sp, (SpShootDamage) src);
        } else {
            if(sp.takeEnergy((int) (amount * physicEnrgCoef))){
                return amount*physicDmgCoef;
            }
            return amount;
        }
    }


    @SideOnly(Side.CLIENT)
    public boolean isActive(){
        return links.length>0;
    }


    @SideOnly(Side.CLIENT)
    public float[] getParams(){
        return new float[]{physicDmgCoef, physicEnrgCoef, energyDmgCoef, energyEnrgCoef};
    }


    public float onDamage(SpShip sp, SpShootDamage src){
        if(links.length>0) {
            int cast = (int) ((src.enrgDmg * energyEnrgCoef) + (src.physDmg * physicEnrgCoef));
            if (sp.takeEnergy(cast)) {
                damage(links, sp, 1 + ((cast / links.length) / 4));
                return (src.enrgDmg * energyDmgCoef) + (src.physDmg * physicDmgCoef);
            }
        }
        return src.enrgDmg + src.physDmg;
    }
}