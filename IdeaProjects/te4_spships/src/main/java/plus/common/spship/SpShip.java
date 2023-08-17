package plus.common.spship;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.FMLCommonHandler;
import plus.asyutl.shoulder.IBasicTask;
import plus.common.etc.ASTaskShoulder;
import plus.common.player.PlayerSpship;
import plus.common.spship.item.ItemCorpus;
import plus.common.spship.modules.ISpProtector;
import plus.common.spship.modules.ISpGun;
import plus.common.spship.modules.ITickModule;
import plus.common.spship.modules.SpGun;

import java.util.ArrayList;


public class SpShip {
    private static final int[] SLOTS = {41,42,43,44,45,46,47,48,49,50,51};
    public final EntityPlayer owner;

    public int modelId = 0;

    //public float shield;
    public int maxShield;
    public float speed;
    public float damage;
    public float critRate;
    public float critDmg;
    public float regen;

    public float energy;
    public float maxEnergy;
    public float enegryGen;

    private ISpGun gun = ISpGun.EMPTY;
    private ISpProtector protectGen = ISpProtector.EMPTY;
    private final ArrayList<ITickModule> tickModules = new ArrayList<>();


    public SpShip(Entity owner) {
        this.owner = (EntityPlayer) owner;
        calcParams(this.owner);
    }


    public void calcParams(EntityPlayer player){
        tickModules.clear();
        InventoryPlayer inv = player.inventory;
        SPM.V raw = new SPM.V();
        for(int slot:SLOTS){
            ItemStack item = inv.getStackInSlot(slot);
            raw.add(SPM.getSimpleSpm(item));
            ITickModule module = ITickModule.getOrNullTickModule(item);
            if(module != null){
                tickModules.add(module);
            }
        }
        gun = ISpGun.ofItemStack(inv.getStackInSlot(SLOTS[0]));
        protectGen = ISpProtector.ofItemStack(inv.getStackInSlot(SLOTS[5]));
        modelId = ItemCorpus.getColor(inv.getStackInSlot(SLOTS[10]));
        ((PlayerSpship)player).setSpModelId(modelId);

        SPM.C sum = raw.compile();


        maxShield = (int) (100 + sum.MAX_SHIELD * (1 + sum.MAX_SHIELD_PR));
        damage    = 1   + sum.DAMAGE     * (1 + sum.DAMAGE_PR    );

        speed     = sum.SPEED            * (1 + sum.SPEED_PR     );
        regen     = sum.REGEN_SHIELD;
        critRate  = sum.CRIT_RATE;
        critDmg   = sum.CRIT_DMG;
        maxEnergy = sum.ENERGY;
        enegryGen = sum.ENERGY_GEN;

        PlayerSpship sp = (PlayerSpship) player;

        //if(sp.getSpShield()>maxShield){
        sp.setSpShield(maxShield);
        //}

        tickModules.trimToSize();
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
        if(energy<maxEnergy){
            energy+=enegryGen;
            if(energy>maxEnergy){
                energy=maxEnergy;
            }
        }
        for(ITickModule module:tickModules){
            module.onTick(this);
        }
    }


    public boolean takeEnergy(float amount){
        if(1==1){//TODO
            return true;
        }
        if(energy>=amount){
            energy-=amount;
            return true;
        }
        return false;
    }


    private static ISpGun g2 = new SpGun(SpGun.AmmType.COMBINED,2,10,0,0);
    public void shoot(EntityLivingBase shooter, float yaw, float pith) {
        //gun.shoot(this, shooter, yaw, pith);
        g2.shoot(this, shooter, yaw, pith);
    }


    static {
        init();
    }

    private static Ticker ticker;
    public static void init() {
        MinecraftServer server = FMLCommonHandler.instance().getSidedDelegate().getServer();
        if(ticker != null){
            ticker.disable();
        }
        if(server==null){
            ticker = null;
        } else {
            ticker = new Ticker(server.getPlayerList());
            ASTaskShoulder.INST.addTask(ticker);
        }
    }
}

class Ticker implements IBasicTask{
    private final PlayerList plList;
    boolean enabled = true;

    Ticker(PlayerList plList) {
        this.plList = plList;
    }

    @Override
    public boolean needRun() {
        for(EntityPlayer player:plList.getPlayers()){
            PlayerSpship sp = (PlayerSpship) player;
            if(sp.isSpshipMode()){
                sp.getSp().onTick();
            }
        }
        return !enabled;
    }
    public void disable(){
        enabled = false;
    }

    @Override
    public void run() {}
}