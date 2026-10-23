package plus.common.mashine.lincker.craft;

import net.minecraft.item.ItemStack;
import plus.common.Strings;
import plus.common.mashine.item.recept.IItemValidator;
import plus.common.mashine.item.recept.ISimplePowerCraftable;
import plus.common.mashine.item.recept.ReseptKey;
import plus.common.spship.SPM;
import plus.common.spship.item.ItemGun;
import plus.common.spship.item.ItemModule;
import plus.utls.MathUtils;
import static plus.common.mashine.item.TehBasicItem.normal;
import static plus.utls.MathUtils.calcPowerComponent;


public class GunCraftProcessor extends DmgPowerProcessor{
    private static final float[] lvlCoefs = {1, 1.1f, 1.2f, 1.3f};
    private static final float[] lvlCoefs2 = {0.8f, 0.845f, 0.89f, 0.935f};
    protected final ISimplePowerCraftable result2;
    protected final int tehLimit, tehShift;

    public GunCraftProcessor(ISimplePowerCraftable result, ISimplePowerCraftable result2,
                             int tehLimit, int tehShift, IItemValidator... items) {
        super(result, items);
        this.result2 = result2;
        this.tehLimit = tehLimit;
        this.tehShift = tehShift;
    }


    @Override
    protected ItemStack onCalcResult(ReseptKey key, int shift, int tehProc, float power, float damage) {
        ItemStack result;
        if(tehProc<tehLimit){
            result = this.result.create(shift, power, damage, tehProc);
        } else {
            result = this.result2.create(shift, power, damage, tehProc);
        }

        int typeQuant = key.getItems()[2].getMetadata();
        float physic = normal(shift);
        float energy = 1f - physic;

        int lvl = ((ItemModule)super.result).lvl;

        float finPow = (float) (calcPowerComponent(power)* MathUtils.calcBigStructCoef(tehProc)/2);


        SPM.write(result, SPM.createRandomSPM(Math.abs(tehProc)));
        result.getTagCompound().setInteger(Strings.CMD, calcSkin(physic, energy, typeQuant, lvl));
        ItemGun.Info info = new ItemGun.Info();
        info.type = typeQuant;
        info.physDmg = (5.0f * lvlCoefs[lvl]) + (1.4f * physic * lvlCoefs2[lvl] * finPow);
        info.enrgDmg = (7.0f * lvlCoefs[lvl]) + (0.8f * energy * lvlCoefs2[lvl] * finPow);
        info.enrgCast = (int) energyCast(physic, energy);
        ItemGun.writeInfo(result, info);
        return result;
    }


    private int calcSkin(float phys, float enrg, int type, int lvl){
        if(type==3)return 3;
        if(MathUtils.isNear(phys, enrg, 0.2f)){
            return 2;
        }
        return phys>enrg?0:1;
    }


    private float energyCast(float phys, float enrg){
        float finPhys = phys;
        finPhys = finPhys*finPhys * 28;
        float finEnrg = enrg*enrg*220;
        return finEnrg + finPhys;
    }
}