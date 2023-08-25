package plus.common.spship.modules;

import net.minecraft.item.ItemStack;
import plus.common.mashine.inv.IItemLink;
import plus.common.spship.SpShip;
import plus.common.spship.item.ItemAccelerator;


public final class SpAccelerator{
    private final float add;
    private final int energyCast;
    private final float maxBoost;
    private final IItemLink[] links;

    public static final class Builder{
        private int count = 0;
        private int energyCast = 0;
        private float maxBoost = 0;
        private IItemLink[] links;

        public Builder(){
            links = new IItemLink[SpShip.SLOTS.length];
        }
        public Builder(int size){
            links = new IItemLink[size];
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


        public SpAccelerator complete(float add, float addPer){
            float d;
            switch (count){
                case 1:
                    d = 1f;break;
                case 2:
                    d = 0.75f;break;
                case 3:
                    d = 0.6f;break;
                default:
                    d = 0.5f;break;
            }
            maxBoost *= d;
            maxBoost = maxBoost * (1+addPer) + add;

            if(count != links.length) {
                IItemLink[] links0 = new IItemLink[count];
                System.arraycopy(links, 0, links0, 0, count);
                links = links0;
            }

            maxBoost = 0.01f * maxBoost;

            return new SpAccelerator(maxBoost/10, energyCast, maxBoost, links);
        }
    }


    public SpAccelerator(float add, int energyCast, float maxBoost, IItemLink[] links) {
        this.add = add;
        this.energyCast = energyCast;
        this.maxBoost = maxBoost;
        this.links = links;
    }


    public float onBoost(SpShip sp, boolean damageC, float curSpeed) {
        if(sp.takeEnergy(energyCast)) {
            if(damageC) {
                for (IItemLink link : links) {
                    ItemStack item = link.getItem();
                    int dmg = item.getItemDamage() + 1;
                    if (dmg > item.getMaxDamage()) {
                        link.setItem(ItemStack.EMPTY);
                        SpAccelerator.Builder builder = new Builder(links.length - 1);
                        for (IItemLink link0 : links) {
                            if (link0 != link) builder.feed(link0);
                        }
                    } else {
                        item.setItemDamage(dmg);
                    }
                }
            }
            if ((curSpeed += add) > maxBoost) return maxBoost;
        }
        else if ((curSpeed -= add) < 1)return 1;

        return curSpeed;
    }
}