package plus.common.spship;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.inv.IItemLink;
import plus.common.mashine.item.IItemCapacitor;
import plus.common.mashine.item.ReactComponent;
import plus.common.player.PlayerSpship;
import plus.common.spship.item.ItemCorpus;
import plus.common.spship.item.ItemModule;
import plus.common.spship.modules.*;
import plus.utls.MathUtils;

import java.util.ArrayList;


public class SpShip {
    public static final int[] SLOTS = {41,42,43,44,45,46,47,48,49,50,51};
    public final EntityPlayer owner;
    private int accTick = 0;
    public int modelId = 0;
    public int maxShield;

    //TODO Переделать расчет SPM
    //TODO асинхронность кораблей синхронно относительно мира
    public ISpGun gun = ISpGun.EMPTY;
    public SpProtector protectGen = new SpProtector(0,0,0,0, new IItemLink[0]);
    public IEnergyFolder powerSystem;
    public ITickModule[] tickModules;
    public SpAccelerator accelerator;
    public double prevSpX, prevSpY, prevSpZ;

    public SpShip(Entity owner) {
        this.owner = (EntityPlayer) owner;
    }


    public static int scanItem(ItemStack item0){
        Item item = item0.getItem();
        if(item instanceof ItemModule){
            return ((ItemModule) item).type;
        } else if (item instanceof ReactComponent) {
            return 5;
        } else if(IItemCapacitor.isCapacitor(item)){
            return 6;
        }
        return -1;
    }


    public void calcParams(){
        ArrayList<ITickModule> tickModules0 = new ArrayList<>(4);
        InventoryPlayer inv = owner.inventory;
        SPM.V raw = new SPM.V();

        SpInvProxy proxy = new SpInvProxy(inv, SLOTS.length);

        SpRegenShield.Builder regShieldBuilder = new SpRegenShield.Builder();
        SpAccelerator.Builder accBuilder = new SpAccelerator.Builder();
        SpReactor.Builder reactBuilder = new SpReactor.Builder();
        SpPowerSystem.Builder powerSysBuilder = new SpPowerSystem.Builder();
        SpProtector.Builder spProtectBuilder = new SpProtector.Builder();
        SpGun.Builder gunBuilder = new SpGun.Builder();

        for(int slot = 0;slot<proxy.size();slot++){
            ItemStack item = proxy.initItem(slot, SLOTS[slot]);
            raw.add(SPM.getSimpleSpm(item));
            switch (scanItem(item)){
                case 1: gunBuilder.feed(proxy, slot);break;
                case 2: regShieldBuilder.feed(proxy, slot);break;
                case 3: accBuilder.feed(proxy, slot);break;
                case 4: spProtectBuilder.feed(proxy, slot);break;
                case 5: reactBuilder.feed(proxy, slot);break;
                case 6: powerSysBuilder.feed(proxy, slot);break;
            }
        }

        SPM.C sum = raw.compile();

        maxShield = (int) (100 + sum.MAX_SHIELD * (1 + sum.MAX_SHIELD_PR));
        powerSystem = powerSysBuilder.complete((int) (sum.ENERGY + 100));

        if(accBuilder.canBuild()) {
            accelerator = accBuilder.complete(sum.SPEED_PR);
        } else {
            accelerator = null;
        }
        if(reactBuilder.canBuild()) {
            tickModules0.add(reactBuilder.complete(1, sum.ENERGY_GEN + 1));
        }
        if(regShieldBuilder.canBuild()) {
            tickModules0.add(regShieldBuilder.complete(sum.REGEN_SHIELD));
        }
        if(gunBuilder.canBuild()) {
            gun = gunBuilder.complete(sum.DAMAGE, sum.DAMAGE_PR, sum.CRIT_DMG, sum.CRIT_RATE);
        } else {
            gun = ISpGun.EMPTY;
        }
        protectGen = spProtectBuilder.complete();
        modelId = ItemCorpus.getColor(inv.getStackInSlot(SLOTS[10]));

        //PlayerSpship sp = (PlayerSpship) owner;

        //if(sp.getSpShield()>maxShield){
        //sp.setSpShield(maxShield);TODO проверить
        //}

        tickModules = tickModules0.toArray(new ITickModule[0]);
    }


    public void onDamage(DamageSource src, float amount){
        PlayerSpship sp = (PlayerSpship) owner;
        float shield = sp.getSpShield();
        shield = shield - protectGen.onDamage(this, src, amount);
        if(shield<0){
            ((PlayerSpship) owner).setSpMode(false);
            owner.onKillCommand();
        } else {
            sp.setSpShield(shield);
        }
    }


    public void onTick(){
        for(ITickModule module:tickModules){
            module.onTick(this);
        }
        prevSpX = owner.posX;
        prevSpY = owner.posY;
        prevSpZ = owner.posZ;
    }

    public double speedX(){
        return MathUtils.range(prevSpX, owner.posX);
    }
    public double speedY(){
        return MathUtils.range(prevSpY, owner.posY);
    }
    public double speedZ(){
        return MathUtils.range(prevSpZ, owner.posZ);
    }
    public double speedSq(){
        double num1 = speedX();
        num1 *= num1;
        double num2 = speedZ();
        num2 *= num2;
        double num3 = speedZ();
        num3 *= num3;
        return num1 + num2 + num3;
    }


    public IEnergyFolder getPowerSystem(){
        return powerSystem;
    }


    public boolean takeEnergy(int amount){
        return powerSystem.takeEnergy(amount);
    }


    public void shoot(float yaw, float pith) {
        gun.shoot(this, owner, yaw, pith);
    }


    public float accelerate(float speed){
        if(accelerator==null)return speed;
        if(++accTick==10)accTick = 0;
        return accelerator.onBoost(this, accTick==1, speed);
    }
}