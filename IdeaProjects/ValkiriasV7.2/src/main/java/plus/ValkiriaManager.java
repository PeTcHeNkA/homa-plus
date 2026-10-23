package plus;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import plus.util.ConfigHelper;
import plus.util.chat.ChatHelper;
import plus.item.BaseInfo;
import plus.item.BoostStats;
import plus.system.ModuleLoader;
import plus.valcirias.BaseValkiria;
import plus.valcirias.Valkiria;
import plus.valcirias.data.UserFloader;

import java.util.ArrayList;
import java.util.Random;


public class ValkiriaManager implements Listener {


    private static final Plugin plugin = Main.instance;
    private final UserFloader userFloader;

    public ValkiriaManager(UserFloader folder) {
        userFloader = folder;
    }


    private static final int[] checkArtifactSlots = new int[]{2,3,4,5,6};
    @EventHandler
    public void onCloseInv(InventoryCloseEvent e){
        Valkiria valkiria = userFloader.getValkiria(e.getPlayer().getName());
        valkiria.setState(BaseValkiria.VState.NONE);
        Player player = (Player) e.getPlayer();

        if(e.getView().getTitle().equals(ConfigHelper.valkiriaArtifacts)){
            if(valkiria.isNotEditArtifact()){
                return;
            } else {
                System.out.println(valkiria.isNotEditArtifact());
                valkiria.setNotEditArtifact(true);
            }
            double AttackBoost = 0;
            double CritBoost = 0;
            double ShieldBoost = 0;
            double CritRateBoost = 0;
            double AdditionalAttack = 0;
            double AdditionalShield = 0;
            double AdditionalEnery = 0;

            int flag = 0;
            Inventory inv = e.getInventory();
            ItemStack[] artifacts = new ItemStack[5];
            for (int selector:checkArtifactSlots){
                ItemStack item = inv.getItem(selector);
                if(item != null) {
                    artifacts[flag] = item;
                    BoostStats stats = Main.generator.getBoostStats(item);
                    AttackBoost += stats.BOOST_ATTACK;
                    CritBoost += stats.BOOST_CRIT;
                    ShieldBoost += stats.BOOST_SHIELD;
                    CritRateBoost += stats.BOOST_CRIT_RATE;
                    AdditionalAttack += stats.ADDITIONAL_ATTACK;
                    AdditionalShield += stats.ADDITIONAL_SHIELD;
                    AdditionalEnery += stats.ADDITIONAL_ENERGY;
                }
                flag+=1;
            }
            valkiria.setArtifacts(artifacts);

            valkiria.setAttackBoost(AttackBoost);
            valkiria.setCriticalPower(CritBoost);
            valkiria.setShieldBoost(ShieldBoost);
            valkiria.setCriticalRate(CritRateBoost);
            valkiria.setAdditionalAttack(AdditionalAttack);
            valkiria.setAdditionalShield(AdditionalShield);
            valkiria.setAdditionalEnergy(AdditionalEnery);

            valkiria.updateAll();
            return;
        }
        if(e.getView().getTitle().equals(ConfigHelper.valkiriaWeapons)){
            valkiria.setWeapon(e.getInventory().getItem(4));
            valkiria.updateSwordStats();
            valkiria.updateAll();
            return;
        }
        if(e.getView().getTitle().equals(ConfigHelper.valkiriaPoints)){
            valkiria.setHashPoints(null);
            return;
        }

        Inventory inv = e.getInventory();
        if(e.getView().getTitle().equals(ConfigHelper.valkitiaUpgradeArts)){
            if (e.getInventory().getItem(16) != null) {

                addOrDropItem(player, inv.getItem(16));
            } else {
                int[] checkSlots = new int[]{0, 1, 2, 3, 4, 9, 10, 11, 12, 13, 18, 19, 20, 21, 22};
                for(int slot:checkSlots){
                    ItemStack item = inv.getItem(slot);
                    if(item != null)addOrDropItem(player, item);
                }
            }
        }
    }


    public static void updateArtifactsStats(Valkiria valkiria){
        ItemStack[] artifacts = valkiria.getArtifacts();

        double AttackBoost = 0;
        double CritBoost = 0;
        double ShieldBoost = 0;
        double CritRateBoost = 0;
        double AdditionalAttack = 0;
        double AdditionalShield = 0;
        double AdditionalEnery = 0;

        int i = 0;
        for (ItemStack itemStack:artifacts){
            if(itemStack==null)continue;
            BoostStats stats = Main.generator.getBoostStats(itemStack);
            if(stats==null){
                artifacts[i] = null;
                valkiria.setArtifacts(artifacts);
                continue;
            }

            AttackBoost += stats.BOOST_ATTACK;
            CritBoost += stats.BOOST_CRIT;
            ShieldBoost += stats.BOOST_SHIELD;
            CritRateBoost += stats.BOOST_CRIT_RATE;
            AdditionalAttack += stats.ADDITIONAL_ATTACK;
            AdditionalShield += stats.ADDITIONAL_SHIELD;
            AdditionalEnery += stats.ADDITIONAL_ENERGY;
            ++i;
        }

        valkiria.setAttackBoost(AttackBoost);
        valkiria.setCriticalPower(CritBoost);
        valkiria.setShieldBoost(ShieldBoost);
        valkiria.setCriticalRate(CritRateBoost);
        valkiria.setAdditionalAttack(AdditionalAttack);
        valkiria.setAdditionalShield(AdditionalShield);
        valkiria.setAdditionalEnergy(AdditionalEnery);
    }


    @EventHandler
    public void trigger(PlayerSwapHandItemsEvent e){
        e.setCancelled(true);
        Player player = e.getPlayer();

        Valkiria v = userFloader.getValkiria(e.getPlayer());
        BaseInfo inf = Main.generator.getBaseInfo(player.getInventory().getItemInMainHand());
        if(inf != null && !v.CombatMode()) {
            if (inf.TYPE == 6) {
                ItemStack newItem = Main.generator.InitItem(e.getPlayer().getInventory().getItemInMainHand());
                int count = player.getInventory().getItemInMainHand().getAmount();

                if (count > 1) {
                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                    itemStack.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                    player.getInventory().setItemInMainHand(itemStack);

                    addOrDropItem(player, newItem);

                } else {
                    player.getInventory().setItemInMainHand(newItem);
                }
            } else if (inf.TYPE == 7) {
                if (ModuleLoader.ChestC != null) {
                    String id = Main.generator.getNITagValue(e.getPlayer().getInventory().getItemInMainHand());
                    int count = player.getInventory().getItemInMainHand().getAmount();
                    if (count > 1) {
                        ItemStack itemStack = player.getInventory().getItemInMainHand();
                        itemStack.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.getInventory().setItemInMainHand(itemStack);
                        player.openInventory(ModuleLoader.ChestC.getChest(id).getRandomChest());
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }
                }
            }
            return;
        }

        if(player.getInventory().getItem(40) != null){
            ItemStack item = player.getInventory().getItem(40);
            player.getInventory().setItem(40, null);
            addOrDropItem(player, item);
        }
        v.toggleCombatModeWithCheck();

    }


    public static void addOrDropItem(Player player, ItemStack item){
        int slot = player.getInventory().firstEmpty();
        if(slot>=0) {
            player.getInventory().setItem(slot, item);
        } else {
            player.getWorld().dropItem(player.getLocation(), item);
        }
    }

    private static final int[] checkSlots = new int[]{0, 1, 2, 3, 4, 9, 10, 11, 12, 13, 18, 19, 20, 21, 22};


    @EventHandler
    public void onClick(InventoryClickEvent e){
        Valkiria valkiria = userFloader.getValkiria(e.getWhoClicked().getName());

        if(valkiria.CombatMode()){
            valkiria.toPlayer().spigot().sendMessage(ConfigHelper.messageDisableCombat);
            return;
        }

        if(e.getClickedInventory() != null) {
            Player player = valkiria.toPlayer();
            if (e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
                if (e.getSlot() == 36) {
                    valkiria.openBackPack();
                    e.setCancelled(true);
                    return;
                }
                if (e.getSlot() == 37) {
                    valkiria.openArtifacts();
                    e.setCancelled(true);
                    return;
                }
                if (e.getSlot() == 38) {
                    valkiria.openSwordGui();
                    e.setCancelled(true);
                    return;
                }
                if (e.getSlot() == 40) {
                    if(player.getItemOnCursor().getType() != Material.AIR) {
                        e.setCancelled(true);
                        return;
                    }
                }

                if (e.getSlot()==39 ){
                    valkiria.openInfo();
                    e.setCancelled(true);
                    return;
                }
            }

            if(e.getView().getTitle().equals(ConfigHelper.valkiriaArtifacts)){

                if(e.getClick() == ClickType.SHIFT_LEFT
                        || e.getClick() == ClickType.SHIFT_RIGHT
                        || e.getClick() == ClickType.NUMBER_KEY
                        || e.getClick() == ClickType.MIDDLE
                ){
                    e.setCancelled(true);
                    return;
                }

                BaseInfo info = Main.generator.getBaseInfo(e.getCurrentItem());

                if(e.getClickedInventory().getType() == InventoryType.PLAYER){
                    if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                        if (info.TYPE != 2) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                } else {
                    if(e.getSlot()==7){
                        e.setCancelled(true);
                        return;
                    }
                    if(e.getSlot()==1){
                        e.setCancelled(true);
                        player.closeInventory();
                        return;
                    }
                    if(e.getSlot()==0) {
                        e.setCancelled(true);
                        player.closeInventory();
                        valkiria.openInfo();
                        return;
                    }
                    if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                        info = Main.generator.getBaseInfo(e.getCursor());
                        if (e.getSlot()-2 != info.CLASS) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
                valkiria.setNotEditArtifact(false);
            }

            if(e.getView().getTitle().equals(ConfigHelper.valkiriaInfo)){
                e.setCancelled(true);
                if(e.getClickedInventory().getType() != InventoryType.PLAYER) {

                    if(e.getSlot() == 13){
                        valkiria.openSwordGui();
                        return;
                    }

                    if(e.getSlot() == 14){
                        valkiria.openArtifacts();
                        return;
                    }
                    if(e.getSlot() == 15){
                        valkiria.openBackPack();
                        return;
                    }
                    if (e.getSlot()>3 && e.getSlot()<9) {
                        valkiria.openPoints();
                        return;
                    }
                }
            }

            if(e.getView().getTitle().equals(ConfigHelper.valkitiaUpgradeArts)){

                Inventory inv = e.getInventory();
                ArrayList<String> IDS = new ArrayList<>();
                double MaxPower = 0;
                double SumPower = 0;
                int CountArts = 0;
                int badslot;

                if(e.getClickedInventory().getType() == InventoryType.PLAYER && e.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY && e.getCurrentItem()!= null){
                    if(e.getInventory().firstEmpty()==16){ //???
                        e.setCancelled(true);
                        return;
                    }
                    BaseInfo info = Main.generator.getBaseInfo(e.getCurrentItem());
                    if(info==null || info.TYPE != 1 && info.TYPE != 2){
                        e.setCancelled(true);
                        return;
                    }
                    for(int slot:checkSlots){

                        ItemStack item = inv.getItem(slot);
                        if(item != null) {
                            BaseInfo info1 = Main.generator.getBaseInfo(item);
                            int power = info1.POWER;
                            if (power > MaxPower) MaxPower = power;
                            SumPower += power;
                            if (info1.TYPE == 2) {
                                IDS.add(info1.ID);
                                ++CountArts;
                            }
                        }
                    }

                    //ItemStack item = e.getCurrentItem();
                    int power = info.POWER;//ValkiriasCORE.generator.getArtifactPower(item);
                    if (power > MaxPower) MaxPower = power;
                    SumPower += power;
                    //int type = info.TYPE//ValkiriasCORE.generator.getArtifactType(item);
                    if (info.TYPE == 2) IDS.add(info.ID);

                    if(CountArts>0 && SumPower >1){
                        int FinalPower = (int) Math.round(Math.log(SumPower/MaxPower)/Math.log(2));
                        if(FinalPower==0){
                            inv.setItem(16, null);
                        } else {
                            inv.setItem(16,
                                    Main.generator.getNIItem(
                                            new BaseInfo(
                                                    IDS.get(new Random().nextInt(IDS.size())),
                                                    FinalPower +(int) MaxPower,
                                                    2,
                                                    -1
                                            ), (BoostStats) null
                                    )
                                    //ValkiriasCORE.generator.generateNonInitArtifact(
                                    //        FinalPower +(int) MaxPower, types.get(new Random().nextInt(types.size())), 5
                            );
                        }
                    } else {
                        inv.setItem(16, null);
                    }
                    UpdateInvWithCD(player);

                    return;
                }

                if(e.getClickedInventory().getType() != InventoryType.PLAYER ) {

                    if(e.getSlot()%9>4){
                        if(e.getSlot() == 16 && e.getCurrentItem() != null && (e.getCursor()==null || e.getCursor().getType()==Material.AIR)){
                            for(int slot:checkSlots){
                                inv.setItem(slot, null);
                            }
                            UpdateInvWithCD(player);
                            return;

                        } else {
                            e.setCancelled(true);
                            return;
                        }
                    } else {
                        badslot = e.getSlot();

                        if(e.getCursor()!= null && e.getCursor().getType() != Material.AIR &&
                                e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY){
                            ItemStack item = e.getCursor();
                            BaseInfo info = Main.generator.getBaseInfo(item);
                            int power = info.POWER;
                            if (power > MaxPower) MaxPower = power;
                            SumPower += power;
                            if (info.TYPE == 0) {
                                ++CountArts;
                                IDS.add(info.ID);
                            }
                        }

                        for(int slot:checkSlots){
                            if(slot != badslot){

                                ItemStack item = inv.getItem(slot);
                                if(item != null) {
                                    BaseInfo info = Main.generator.getBaseInfo(item);
                                    int power = info.POWER;
                                    if (power > MaxPower) MaxPower = power;
                                    SumPower += power;
                                    if (info.TYPE == 2) {
                                        IDS.add(info.ID);
                                        ++CountArts;
                                    }
                                }
                            }
                        }

                        if(CountArts>0 && SumPower >1){

                            int FinalPower = (int) Math.round(Math.log(SumPower/MaxPower)/Math.log(2));
                            if(FinalPower==0){
                                inv.setItem(16, null);

                            } else {
                                inv.setItem(16,
                                        Main.generator.getNIItem(
                                                new BaseInfo(
                                                        IDS.get(new Random().nextInt(IDS.size())),
                                                        FinalPower +(int) MaxPower,
                                                        2,
                                                        -1
                                                ), (BoostStats) null
                                        )
                                );
                           }
                        } else {
                            inv.setItem(16, null);
                        }
                        UpdateInvWithCD(player);
                        return;
                    }

                } else {
                    if(e.getCursor()==null || e.getCursor().getType() == Material.AIR ) {
                        BaseInfo info = Main.generator.getBaseInfo(e.getCurrentItem());
                        if (info == null || info.TYPE != 1 && info.TYPE != 2) {
                            e.setCancelled(true);
                        }
                    }
                    return;
                }

            }
            if (e.getView().getTitle().equals(ConfigHelper.valkiriaWeapons)){
                if(e.getClickedInventory().getType() == InventoryType.PLAYER) {
                    ItemStack item = e.getClickedInventory().getItem(e.getSlot());
                    if(item == null || item.getType()==Material.AIR)return;

                    BaseInfo info = Main.generator.getBaseInfo(e.getCurrentItem());
                    if(info==null){
                        e.setCancelled(true);
                        return;
                    }
                    if(info.CLASS != 0) {
                        if (info.CLASS != valkiria.getAllowedWeaponType()) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                } else {
                    e.setCancelled(true);
                    if(e.isShiftClick())return;

                    if(e.getSlot()==0){
                        valkiria.openInfo();
                        return;
                    }
                    if(e.getSlot()==1){
                        player.closeInventory();
                        return;
                    }
                    if(e.getSlot() == 4 && e.getCursor() != null && e.getCursor().getType() != Material.AIR){
                        ItemStack bufer = e.getClickedInventory().getItem(4);
                        e.getClickedInventory().setItem(4, e.getCursor());
                        e.setCursor(bufer);
                    }
                }
                return;
            }

            if(e.getView().getTitle().equals(ConfigHelper.valkiriaPoints)){
                e.setCancelled(true);
                if(e.getClickedInventory().getType() != InventoryType.PLAYER) {
                    int[] HashPoints = valkiria.getHashPoints();

                    int AvaliblePoints = valkiria.getAvailablePoints()-HashPoints[4];
                    if(AvaliblePoints>0) {
                        if (e.getSlot() == 3) {
                            int newpoints = valkiria.getAttackPoints()+HashPoints[0];
                            if(newpoints<50) {
                                HashPoints[0] += 1;
                                HashPoints[4] += 1;
                                e.getClickedInventory().getItem(3).setAmount(newpoints+1);
                            }
                        }
                        if (e.getSlot() == 4) {
                            int newpoints = valkiria.getCriticalPoints()+HashPoints[1];
                            if(newpoints<50) {
                                HashPoints[1] += 1;
                                HashPoints[4] += 1;
                                e.getClickedInventory().getItem(4).setAmount(newpoints+1);
                            }
                        }
                        if (e.getSlot() == 5) {
                            int newpoints = valkiria.getEnergyPoints()+HashPoints[2];
                            if(newpoints<50) {
                                HashPoints[2] += 1;
                                HashPoints[4] += 1;
                                e.getClickedInventory().getItem(5).setAmount(newpoints+1);
                            }
                        }
                        if(HashPoints[5]>4){
                            UpdateInvWithCD(player);
                            HashPoints[5]=0;
                        } else {
                            HashPoints[5]+=1;
                        }

                        if (e.getSlot() == 6) {
                            int newpoints = valkiria.getShieldPoints()+HashPoints[3];
                            if(newpoints<50) {
                                HashPoints[3] += 1;
                                HashPoints[4] += 1;
                                e.getClickedInventory().getItem(6).setAmount(newpoints+1);
                            }
                        }
                        e.getClickedInventory().getItem(8).setAmount(valkiria.getAvailablePoints()-HashPoints[4]);
                    }
                    if(e.getSlot()==1){
                        valkiria.setAttackPoints(valkiria.getAttackPoints()+HashPoints[0]);
                        valkiria.setCriticalPoints(valkiria.getCriticalPoints()+HashPoints[1]);
                        valkiria.setEnergyPoints(valkiria.getEnergyPoints()+HashPoints[2]);
                        valkiria.setShieldPoints(valkiria.getShieldPoints()+HashPoints[3]);
                        valkiria.setAvailablePoints(valkiria.getAvailablePoints()-HashPoints[4]);
                        player.closeInventory();
                        //valkiria.setHashPoints(null);
                        valkiria.updateAll();
                        return;
                    }
                    if(e.getSlot()==0){
                        player.closeInventory();
                        //valkiria.setHashPoints(null);
                    }
                }
            }
        }
    }

//    public static void OpenOneItem(Player player,ItemStack item){
//        Inventory inv = Bukkit.createInventory(null, InventoryType.DROPPER, ConfigHelper.ValkiriaGetArtifact);
//        inv.setItem(4, item);
//        player.openInventory(inv);
//    }

    public static void UpdateInvWithCD(Player player){
        new BukkitRunnable() {@Override public void run() {
                player.updateInventory(); }
        }.runTaskLater(plugin, 5);
    }


    public static void OpenArtifactUpgrade(Player player){
        Inventory inventory = Bukkit.createInventory(null, 27, ConfigHelper.valkitiaUpgradeArts);

        inventory.setItem(5, ConfigHelper.upgradeArtsNull);
        inventory.setItem(6, ConfigHelper.upgradeArtsNull);
        inventory.setItem(7, ConfigHelper.upgradeArtsNull);

        inventory.setItem(26, ConfigHelper.upgradeArtsBG);
        inventory.setItem(14, ConfigHelper.upgradeArtsNull);
        inventory.setItem(15, ConfigHelper.upgradeArtsNull);
        inventory.setItem(17, ConfigHelper.upgradeArtsNull);
        inventory.setItem(23, ConfigHelper.upgradeArtsNull);
        inventory.setItem(24, ConfigHelper.upgradeArtsNull);
        inventory.setItem(25, ConfigHelper.upgradeArtsNull);
        inventory.setItem(8, ConfigHelper.upgradeArtsNull);

        player.openInventory(inventory);
    }


    @EventHandler
    public void onDrag(InventoryDragEvent e){
        if(e.getView().getTitle().equals(ConfigHelper.valkitiaUpgradeArts)||
                e.getView().getTitle().equals(ConfigHelper.valkiriaArtifacts)||
                e.getView().getTitle().equals(ConfigHelper.valkiriaWeapons)||
                e.getView().getTitle().contains(ConfigHelper.craftsView)
        ){
            e.setCancelled(true);
        }
    }


    @EventHandler
    public  void onChat(AsyncPlayerChatEvent e){
        Valkiria valkiria = userFloader.getValkiria(e.getPlayer());
        if(e.getMessage().contains("$"))e.setMessage(e.getMessage().replace('$', '&'));
        if(e.getMessage().charAt(0)=='!') {
            ChatHelper.SendMessage(valkiria, ConfigHelper.Global_pefix, e.getMessage().substring(1), Bukkit.getOnlinePlayers().toArray(new Player[0]));
        } else {

            TextComponent profile = ChatHelper.getValkiriaProfile(valkiria);
            String message = e.getMessage().strip();

            for(Player target:Bukkit.getOnlinePlayers()) {
                if(e.getPlayer().getLocation().distance(target.getLocation())<100)
                    target.spigot().sendMessage(ConfigHelper.Local_pefix, profile, new TextComponent(" "+message));
            }
        }
        e.setCancelled(true);
    }


    @EventHandler
    public void onFood(PlayerItemConsumeEvent e){
        BaseInfo info = Main.generator.getBaseInfo(e.getItem());
        if(info.TYPE==4){
            if(e.getPlayer().getFoodLevel()>=20)return;
            ItemStack newItem = e.getItem();
            if(newItem.getAmount()>1){
                newItem.setAmount(newItem.getAmount()-1);
            } else {
                newItem = null;
            }
            e.getPlayer().setFoodLevel(e.getPlayer().getFoodLevel()+info.CLASS);
            e.setCancelled(true);
            e.getPlayer().getInventory().setItemInMainHand(newItem);
        } else if(info.TYPE==5){
            ItemStack newItem = e.getItem();
            if(newItem.getAmount()>1){
                newItem.setAmount(newItem.getAmount()-1);
            } else {
                newItem = null;
            }
            userFloader.getValkiria(e.getPlayer()).regenShield(info.CLASS);
            e.setCancelled(true);
            e.getPlayer().getInventory().setItemInMainHand(newItem);
        }
    }
}