package plus.common.spship.modules;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import plus.common.amm.SpShootDamage;
import plus.common.spship.SPM;
import plus.common.spship.SpShip;


public final class SpProtector {//TODO ссылки на предметы и урон предметам при попадании
    private final float physicDmgCoef, energyDmgCoef;
    private final int physicEnrgCoef, energyEnrgCoef;

    public SpProtector(float physicDmgCoef, float energyDmgCoef, int physicEnrgCoef, int energyEnrgCoef) {
        this.physicDmgCoef = physicDmgCoef;
        this.energyDmgCoef = energyDmgCoef;
        this.physicEnrgCoef = physicEnrgCoef;
        this.energyEnrgCoef = energyEnrgCoef;
    }


    public static class Builder{
        private float physicDmgCoef = 0, energyDmgCoef = 0;
        private int physicEnrgCoef = 0, energyEnrgCoef = 0, count = 0;

        public void feed(SpInvProxy proxy, int slot){
            NBTTagCompound tag = proxy.getItem(slot).getTagCompound();
            if (tag == null) return;
            int[] ints = tag.getIntArray("spg");
            if (ints.length == 0) return;

            physicEnrgCoef += SPM.getIOf(ints, 1);
            energyEnrgCoef += SPM.getIOf(ints, 2);
            physicDmgCoef += SPM.getIOf(ints, 2);
            energyDmgCoef += SPM.getIOf(ints, 3);
        }


        public SpProtector complete(){
            float d;
            switch (count){
                case 0:
                    return new SpProtector(0,0,0,0);
                case 1:
                    d = 1f;break;
                case 2:
                    d = 0.75f;break;
                case 3:
                    d = 0.6f;break;
                default:
                    d = 0.5f;break;
            }
            return new SpProtector(
                    (1-physicDmgCoef)*d, (1-energyDmgCoef)*d, physicEnrgCoef, energyEnrgCoef
            );
        }
    }


//    public SpProtector(SpInvProxy proxy){
//        int physE = 0;
//        int engE = 0;
//        float physD = 0;
//        float engD = 0;
//        if(proxy != null) {
//            for (int slot = 0; slot < proxy.size(); slot++) {
//                NBTTagCompound tag = proxy.getItem(slot).getTagCompound();
//                if (tag == null) continue;
//                int[] ints = tag.getIntArray("spg");
//                if (ints.length == 0) continue;
//
//                physE += SPM.getIOf(ints, 1);
//                engE += SPM.getIOf(ints, 2);
//                physD += SPM.getIOf(ints, 2);
//                engD += SPM.getIOf(ints, 3);
//            }
//        }
//        physicDmgCoef = physD;
//        energyDmgCoef = engD;
//        physicEnrgCoef = physE;
//        energyEnrgCoef = engE;
//    }


    public final float onDamage(SpShip sp, DamageSource src, float amount){
        if(src instanceof SpShootDamage){
            return onDamage(sp, (SpShootDamage) src, amount);
        } else {
            if(1==1)return amount;
            return 0;
        }
    }


    public float onDamage(SpShip sp, SpShootDamage src, float damage){
        switch (src.getType()){
            case PHYSIC:
                if(sp.takeEnergy(physicEnrgCoef)){
                    return damage*physicDmgCoef;
                }
            case ENERGY:
                if(sp.takeEnergy(energyEnrgCoef)){
                    return damage*energyDmgCoef;
                }
            case COMBINED:
                if(sp.takeEnergy((energyEnrgCoef + physicEnrgCoef)>>1)){
                    return damage*((physicDmgCoef+energyDmgCoef)/2f);
                }
        }
        return damage;
    }
}