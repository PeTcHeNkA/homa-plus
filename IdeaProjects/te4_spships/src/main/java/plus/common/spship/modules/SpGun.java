package plus.common.spship.modules;

import net.minecraft.entity.EntityLivingBase;
import plus.common.amm.SpBullet;
import plus.common.mashine.inv.IItemLink;
import plus.common.spship.SpShip;
import plus.common.spship.item.ItemGun;
import java.util.Random;
import static plus.common.spship.modules.SpAccelerator.*;


public class SpGun implements ISpGun{
    private static final Random r = new Random();

    private final float physDmg, engrDmg, critDmg, critRate;
    private final int engCast;
    private final int mark, modelId;
    private final IItemLink[] links;

    private static final int[][] modelIdTable = {
            {20001,20001,20001,20001},
            {20001,20001,20001,20001},
            {20001,20001,20001,20001},
            {20001,20001,20001,20001},
    };
    public static class Builder{
        public float physDmg = 0, enrgDmg = 0;
        public int energyCast = 0, count = 0, summLvl = 0;
        private IItemLink[] links;
        private final int[] types = new int[4];

        public Builder(){
            links = new IItemLink[SpShip.SLOTS.length];
        }

        public void feed(SpInvProxy proxy, int slot){
            links[count] = proxy.link(slot);
            ItemGun.Info inf = ItemGun.scanInfo(links[count].getItem());
            physDmg += inf.physDmg;
            enrgDmg += inf.enrgDmg;
            energyCast += inf.enrgCast;
            types[inf.type] += 1;
            summLvl += inf.lvl;

            ++count;
        }


        public boolean canBuild(){
            return count>0;
        }


        public SpGun complete(float dmg, float dmgPercent, float critDmg, float critRate){
            int mark = 0, maxType = 0, maxI = 0;
            for (int i=types.length-1;i>=0;i--){
                int cur = types[i];
                if(cur>0){
                    mark = mark | 1;
                    if(cur>maxType){
                        maxType = cur;
                        maxI = i;
                    }
                }
                mark = mark<<1;
            }
            float c = coefCount(count);

            int lvl = summLvl/4;
            if(summLvl%4>0)lvl+=1;

            dmg = dmg/2;
            dmgPercent = dmgPercent/2;

            physDmg = physDmg+dmg;
            enrgDmg = enrgDmg+dmg;

            return new SpGun(
                    physDmg*c*dmgPercent, enrgDmg*c*dmgPercent,
                    critDmg/2, critRate, energyCast, mark,
                    modelIdTable[lvl][maxI], trimTo(links, count));

        }
    }


    public SpGun(float physDmg, float engrDmg, float critDmg, float critRate, int engCast, int mark, int modelId, IItemLink[] links) {
        this.physDmg = physDmg;
        this.engrDmg = engrDmg;
        this.critDmg = critDmg;
        this.critRate = critRate;
        this.engCast = engCast;
        this.mark = mark;
        this.modelId = modelId;
        this.links = links;
    }


    @Override
    public void shoot(SpShip sp, EntityLivingBase shooter, float yaw, float pith) {
        if(sp.takeEnergy(engCast)) {
            SpBullet bullet;
            if(r.nextFloat() < critRate){
                bullet = new SpBullet(shooter.world, modelId, physDmg, engrDmg, mark);
            } else {
                bullet = new SpBullet(shooter.world, modelId, physDmg*critDmg, engrDmg*critDmg, mark);
            }
            shooter.world.spawnEntity(bullet.shoot(shooter, yaw, pith,
                    physDmg>engrDmg?1.1f:1.35f, 0)
            );
            damage(links, sp, 1);
        }
    }
}