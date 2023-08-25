package plus.common.spship;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import plus.common.mashine.inv.IEnergyFolder;
import plus.common.mashine.item.IItemCapacitor;
import plus.common.mashine.item.ReactComponent;
import plus.common.player.PlayerSpship;
import plus.common.spship.item.ItemCorpus;
import plus.common.spship.item.ItemModule;
import plus.common.spship.modules.*;
import java.util.ArrayList;


public class SpShip {
    public static final int[] SLOTS = {41,42,43,44,45,46,47,48,49,50,51};
    public final EntityPlayer owner;

    public int modelId = 0;

    public int maxShield;
    //public float speed;
    public float damage;
    public float critRate;
    public float critDmg;
    public float regen;

//    public float energy;
//    public float maxEnergy;
//    public float enegryGen;

    //TODO Переделать расчет SPM
    //TODO асинхронность кораблей синхронно относительно мира
    private ISpGun gun = ISpGun.EMPTY;
    private SpProtector protectGen = new SpProtector(0,0,0,0);
    private IEnergyFolder powerSystem;
    private ITickModule[] tickModules;
    public SpAccelerator accelerator;


    public SpShip(Entity owner) {
        this.owner = (EntityPlayer) owner;
        calcParams();
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


        for(int slot = 0;slot<proxy.size();slot++){
            ItemStack item = proxy.initItem(slot, SLOTS[slot]);
            raw.add(SPM.getSimpleSpm(item));
            switch (scanItem(item)){
                case 2:
                    regShieldBuilder.feed(item);break;
                case 3:
                    accBuilder.feed(proxy, slot);break;
                case 4:
                    spProtectBuilder.feed(proxy, slot);break;
                case 5:
                    reactBuilder.feed(proxy, slot);break;
                case 6:
                    powerSysBuilder.feed(proxy, slot);break;
            }
        }

        SPM.C sum = raw.compile();


        maxShield = (int) (100 + sum.MAX_SHIELD * (1 + sum.MAX_SHIELD_PR));
        damage    = 1   + sum.DAMAGE     * (1 + sum.DAMAGE_PR    );
        regen     = sum.REGEN_SHIELD;
        critRate  = sum.CRIT_RATE;
        critDmg   = sum.CRIT_DMG;


        powerSystem = powerSysBuilder.complete((int) (sum.ENERGY + 100));
        if(accBuilder.canBuild()) {
            accelerator = accBuilder.complete(sum.SPEED, sum.SPEED_PR);
        } else {
            accelerator = null;
        }
        if(reactBuilder.canBuild()) {
            tickModules0.add(reactBuilder.complete(1, sum.ENERGY_GEN + 1));
        }
        if(regShieldBuilder.canBuild()) {
            tickModules0.add(regShieldBuilder.complete(sum.REGEN_SHIELD));
        }

        gun = ISpGun.ofItemStack(inv.getStackInSlot(SLOTS[0]));
        protectGen = spProtectBuilder.complete();
        modelId = ItemCorpus.getColor(inv.getStackInSlot(SLOTS[10]));
        ((PlayerSpship)owner).setSpModelId(modelId);


        PlayerSpship sp = (PlayerSpship) owner;

        //if(sp.getSpShield()>maxShield){
        sp.setSpShield(maxShield);
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
    }


    public IEnergyFolder getPowerSystem(){
        return powerSystem;
    }


    public boolean takeEnergy(int amount){
        return powerSystem.takeEnergy(amount);
    }


    private static ISpGun g2 = new SpGun(SpGun.AmmType.COMBINED,2,10,0,20001, 1);


    public void shoot(float yaw, float pith) {
        g2.shoot(this, owner, yaw, pith);
    }

    private int accTick = 0;

    public float accelerate(float speed){
        if(accelerator==null)return speed;
        if(++accTick==10)accTick = 0;
        return accelerator.onBoost(this, accTick==1, speed);
    }


//    static {
//        init();
//    }

//    private static Ticker ticker;
//    public static void init() {
//        MinecraftServer server = FMLCommonHandler.instance().getSidedDelegate().getServer();
//        if(ticker != null){
//            ticker.disable();
//        }
//        if(server==null){
//            ticker = null;
//        } else {
//            ticker = new Ticker(server.getPlayerList());
//            ASTaskShoulder.INST.addTask(ticker);
//        }
//    }
}


//final class Ticker implements IBasicTask{
//    private final PlayerList plList;
//    boolean enabled = true;
//
//    Ticker(PlayerList plList) {
//        this.plList = plList;
//    }
//
//    @Override
//    public boolean needRun() {
//        for(EntityPlayer player:plList.getPlayers()){
//            PlayerSpship sp = (PlayerSpship) player;
//            if(sp.isSpshipMode()){
//                sp.getSp().onTick();
//            }
//        }
//        return !enabled;
//    }
//    public void disable(){
//        enabled = false;
//    }
//
//    @Override
//    public void run() {}
//}