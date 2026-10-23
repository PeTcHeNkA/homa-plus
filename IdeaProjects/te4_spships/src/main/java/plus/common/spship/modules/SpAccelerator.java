package plus.common.spship.modules;

import net.minecraft.item.ItemStack;
import plus.common.mashine.inv.IItemLink;
import plus.common.spship.BaseSystem;
import plus.common.spship.SpShip;
import plus.common.spship.item.ItemAccelerator;


public final class SpAccelerator{
    private final float add;
    public final int energyCast;
    public final float maxBoost;
    private final IItemLink[] links;

    public static final class Builder{
        private int count = 0;
        private int energyCast = 0;
        private float maxBoost = 0;
        private final IItemLink[] links;

        public Builder(){
            links = new IItemLink[SpShip.SLOTS.length];
        }

        public void feed(IItemLink link){
            feed0((links[count] = link).getItem());
        }


        public void feed(SpInvProxy proxy, int slot){
            feed0((links[count] = proxy.link(slot)).getItem());
        }


        private void feed0(ItemStack item){
            float pow = ItemAccelerator.getPower(item);
            maxBoost += pow;
            energyCast += ItemAccelerator.calcAccEnergyCast(pow);
            ++count;
        }


        public boolean canBuild(){
            return count>0;
        }


        public SpAccelerator complete(float add){
            float d = coefCount(count);
            maxBoost *= d;
            maxBoost = maxBoost + add;
            maxBoost = 0.01f * maxBoost;
            return new SpAccelerator(maxBoost/10, energyCast, maxBoost, trimTo(links, count));
        }
    }


    public static float coefCount(int count){
        switch (count){
            case 1: return 1f;
            case 2: return 0.75f;
            case 3: return 0.6f;
            default: return 0.5f;
        }
    }


    public static IItemLink[] trimTo(IItemLink[] links, int count){
        if(count != links.length) {
            IItemLink[] links0 = new IItemLink[count];
            System.arraycopy(links, 0, links0, 0, count);
            return links0;
        }
        return links;
    }


    public SpAccelerator(float add, int energyCast, float maxBoost, IItemLink[] links) {
        this.add = add;
        this.energyCast = energyCast;
        this.maxBoost = maxBoost;
        this.links = links;
    }


    public float onBoost(SpShip sp, boolean damageC, float curSpeed) {
        if(sp.takeEnergy(energyCast)) {
            if(damageC) sp.damageLinkedItems(links, 1);
            if ((curSpeed += add) > maxBoost) return maxBoost;
        }
        else if ((curSpeed -= add) < 1)return 1;

        return curSpeed;
    }
}