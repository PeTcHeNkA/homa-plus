package plus.valcirias;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import plus.ValkiriaManager;
import plus.item.BoostStats;
import plus.item.stat.StatGenerator;
import plus.quest.AbstractQuestSession;
import plus.util.Time;
import plus.valcirias.avatar.Avatar;
import plus.util.ConfigHelper;
import plus.util.NextLvlTable;
import plus.valcirias.avatar.AvatarFloader;
import plus.valcirias.data.ClassStats;
import plus.valcirias.data.StartItemKit;
import plus.Main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class BaseValkiria implements Valkiria {
    public enum VState {NONE, STATS, CRAFT, CAST_SKULL, REDIRECTING}
    protected static final Random r = new Random();

    private Inventory backPack = Bukkit.createInventory(null, 27, ConfigHelper.valkiriaBackPack);
    private final ItemStack[] HItems = new ItemStack[3];
    private final StartItemKit skills;
    protected final int weaponType;
    protected final String localClassName;
    protected final int NormalCD;
    protected final double NormalPrice;
    protected final double HeightPrice;
    protected final int HeightCD;
    protected final double startEnergyAmount;
    protected final double startValkiriaAttack;
    protected final double startCriticalRate;
    protected final double StartCriticalPower;
    protected final double startValkiriaShield;
    protected int attackPoints;
    protected int shieldPoints;
    protected int energyPoints;
    protected int criticalPoints;

    //protected String PlayerName;
    protected Player player;
    private ItemStack weapon;
    private ItemStack[] Artifacts = new ItemStack[5];
    private int[] HashPoints;
    private boolean criticalEnergy = false;
    protected boolean CombatMode = false;
    protected double attack = 0;
    private double attackBoost = 0;
    private double additionalAttack = 0;
    protected double shield = 0;
    protected double maxShield;
    private double shieldBoost = 0;
    private double additionalShield = 0;
    private double additionalEnergyAmount = 0;
    protected double criticalRate = 0;
    protected double criticalPower = 0;
    private double additionalCriticalRate = 0;
    private double AdditionalCriticalPower = 0;
    private int level = 1;
    private int experience = 0;
    private int AvailablePoints = 2;
    private double weaponAttack = 0;
    private double weaponShield = 0;
    private double weaponAttackBoost = 0;
    private double weaponShieldBoost = 0;
    private double WeaponCritBoost = 0;
    private double weaponCritRateBoost = 0;
    private double weaponAdditionalEnergy = 0;
    private boolean NotEditArtifact = true;
    private boolean Muted = false;
    private Avatar avatar = AvatarFloader.startAvatar;
    private String color = "f";
    private String lastTargetMessage = null;
    private AbstractQuestSession session;
    private String reward;
    private int countBattlePass = ClassStats.MAX_BATTLE_PASS;
    private long lastBattlePassReceiving = System.currentTimeMillis();
    protected VState state = VState.NONE;
    private boolean premium;
    private Party party;


    public BaseValkiria(int weaponType){
        this.weaponType = weaponType;
        ClassStats stats = ClassStats.getOf(ClassName());
        localClassName = stats.LOCAL_NAME;
        NormalCD = stats.NORMAL_CD;
        NormalPrice = stats.NORMAL_PRICE;
        HeightPrice = stats.HEIGHT_PRICE;
        HeightCD = stats.HEIGHT_CD;
        startEnergyAmount = stats.START_ENERGY_AMOUNT;
        startValkiriaAttack = stats.START_ATTACK;
        startCriticalRate = stats.START_CRIT_RATE;
        StartCriticalPower = stats.START_CRIT_POWER;
        startValkiriaShield = stats.START_SHIELD;
        attackPoints = stats.ATTACK_POINTS;
        shieldPoints = stats.SHIELD_POINTS;
        energyPoints = stats.ENERGY_POINTS;
        criticalPoints = stats.CRITICAL_POINTS;
        skills = stats.KIT;

        weapon = skills.WEAPON;
        maxShield = startValkiriaShield;
    }


    @Override
    public int getNormalCD() {
        return NormalCD;
    }


    @Override
    public int getHightCD(){
        return HeightCD;
    }


    @Override
    public double getShield(){
        return shield;
    }


    @Override
    public double getMaxShield(){
        return maxShield;
    }


    @Override
    public void setShield(double NewShield){
        shield = NewShield;
    }


    @Override
    public String getClassName(){
        return localClassName;
    }


    @Override
    public void updateAll(){
        maxShield = StatGenerator.MATHER_SHIED.math(
                shieldPoints, startValkiriaShield,
                shieldBoost, weaponShieldBoost,
                additionalShield, weaponShield);
        attack = StatGenerator.MATHER_ATTACK.math(
                attackPoints, startValkiriaAttack,
                additionalAttack, weaponAttack,
                attackBoost, weaponAttackBoost);
        double maxEnergyAmount = StatGenerator.MATHER_ENERGY.math(
                energyPoints, startEnergyAmount,
                additionalEnergyAmount, weaponAdditionalEnergy
        );
        criticalRate = StatGenerator.MATHER_CRIT_RATE.math(
                criticalPoints, startCriticalRate,
                additionalCriticalRate, weaponCritRateBoost
        );
        criticalPower = StatGenerator.MATHER_CRIT_POWER.math(
                criticalPoints, StartCriticalPower,
                AdditionalCriticalPower, WeaponCritBoost
        );

        if(shield > maxShield){
            shield = maxShield;
        }
        if(maxEnergyAmount>0) {
            player.setMaxHealth(maxEnergyAmount);
        } else player.setMaxHealth(0.5);
        UpdateShield();
    }


    @Override
    public boolean CombatMode(){
        return CombatMode;
    }


    @Override
    public void setCombatMode(boolean NewMode){
        CombatMode = NewMode;
    }


    @Override
    public String ClassName() {
        return this.getClass().getSimpleName();
    }


    @Override
    public ItemStack[] getArtifacts() {
        return Artifacts;
    }


    @Override
    public void setArtifacts(ItemStack[] items) {
        Artifacts = items;
    }



    public double NormalAttack(double damage){
        return damage;
    }


    @Override
    public double useNormalAttack(EntityDamageByEntityEvent e){
        float c = player.getAttackCooldown();
        return NormalAttack(c*c*(r.nextDouble()< criticalRate ? attack * criticalPower : attack));
    }


    protected void ChargeAttack(double damage, double charge){

    }


    @Override
    public void useChargeAttack(double charge){
        state = VState.CAST_SKULL;
        ChargeAttack(r.nextDouble()< criticalRate ? attack * criticalPower : attack, charge);
        state = VState.NONE;
    }


    protected void NormalSkill(double damage){

    }


    protected void HeightSkill(double damage){

    }


    @Override
    public void useNormalSkill() {
        state = VState.CAST_SKULL;
        NormalSkill(r.nextDouble()< criticalRate ? attack * criticalPower : attack);
        state = VState.NONE;
    }


    @Override
    public void useHeightSkill() {
        state = VState.CAST_SKULL;
        HeightSkill(r.nextDouble()< criticalRate ? attack * criticalPower : attack);
        state = VState.NONE;
    }


    @Override
    public String toPlayerName() {
        return player.getName();
    }


    @Override
    public void setPlayerName(String playerName){
        player = Bukkit.getPlayer(playerName);
    }


    @Override
    public void setPlayerName(Player pl) {
        player = pl;
    }


    @Override
    public double getShieldBoost() {
        return shieldBoost;
    }


    @Override
    public void setShieldBoost(double shieldBoost) {
        this.shieldBoost = shieldBoost;
    }


    @Override
    public double getAttackBoost() {
        return attackBoost;
    }


    @Override
    public void setAttackBoost(double attackBoost) {
        this.attackBoost = attackBoost;
    }


    @Override
    public int getAttackPoints() {
        return attackPoints;
    }


    @Override
    public void setAttackPoints(int attackPoints) {
        this.attackPoints = attackPoints;
    }


    @Override
    public int getShieldPoints() {
        return shieldPoints;
    }


    @Override
    public void setShieldPoints(int shieldPoints) {
        this.shieldPoints = shieldPoints;
    }


    @Override
    public double getPowerAttack(){
        return attack;
    }


    @Override
    public double getAdditionalShield() {
        return additionalShield;
    }


    @Override
    public void setAdditionalShield(double additionalShield) {
        this.additionalShield = additionalShield;
    }


    @Override
    public double getAdditionalAttack() {
        return additionalAttack;
    }


    @Override
    public void setAdditionalAttack(double additionalAttack) {
        this.additionalAttack = additionalAttack;
    }


    @Override
    public double getCriticalPower() {
        return criticalPower;
    }


    @Override
    public void setCriticalPower(double criticalPower) {
        AdditionalCriticalPower = criticalPower;
    }


    @Override
    public double getAdditionalCriticalPower(){
        return AdditionalCriticalPower;
    }


    @Override
    public double getCriticalRate() {
        return criticalRate;
    }


    @Override
    public double getAdditionalCriticalRate(){
        return additionalCriticalRate;
    }


    @Override
    public void setCriticalRate(double criticalRate) {
        additionalCriticalRate = criticalRate;
    }


    @Override
    public ItemStack getWeapon() {
        return weapon;
    }


    @Override
    public void setWeapon(ItemStack weapon) {
        this.weapon = weapon;
    }


    public StartItemKit getSKit(){
        return skills;
    }


    @Override
    public double getNormalSkillPrice() {
        return NormalPrice;
    }


    @Override
    public double getHeightSkillPrice() {
        return HeightPrice;
    }


    @Override
    public boolean isCriticalEnergy() {
        return criticalEnergy;
    }


    @Override
    public void setCriticalEnergy(boolean criticalEnergy) {
        this.criticalEnergy = criticalEnergy;
    }


    @Override
    public int getExperience() {
        return experience;
    }


    @Override
    public int getLvl() {
        return level;
    }


    @Override
    public void setExperience(int experience) {
        this.experience = experience;
        level = NextLvlTable.getLvl(experience);
    }


    @Override
    public void addExperience(int experience) {
        this.experience = this.experience +experience;
        player.sendMessage(String.format(ConfigHelper.dropExpFormat, experience));
        while(this.experience >NextLvlTable.getNextLvlExp(level)){
            level += 1;
            player.sendMessage(String.format(ConfigHelper.lvlUpFormat, level));
            AvailablePoints += 1;
        }
    }


    @Override
    public int getAvailablePoints() {
        return AvailablePoints;
    }


    @Override
    public void setAvailablePoints(int availablePoints) {
        AvailablePoints = availablePoints;
    }


    @Override
    public int getCriticalPoints() {
        return criticalPoints;
    }


    @Override
    public void setCriticalPoints(int criticalPoints) {
        this.criticalPoints = criticalPoints;
    }


    @Override
    public int getEnergyPoints() {
        return energyPoints;
    }


    @Override
    public void setEnergyPoints(int energyPoints) {
        this.energyPoints = energyPoints;
    }


    @Override
    public double getAdditionalEnergy() {
        return additionalEnergyAmount;
    }


    @Override
    public void setAdditionalEnergy(double additionalEnergy) {
        additionalEnergyAmount = additionalEnergy;
    }


    @Override
    public int[] getHashPoints() {
        return HashPoints;
    }


    @Override
    public void setHashPoints(int[] hashPoints) {
        HashPoints = hashPoints;
    }


    @Override
    public int getAllowedWeaponType(){
        return weaponType;
    }


    @Override
    public double getWeaponAttack() {
        return weaponAttack;
    }


    @Override
    public void setWeaponAttack(double weaponAttack) {
        this.weaponAttack = weaponAttack;
    }


    @Override
    public double getWeaponShield() {
        return weaponShield;
    }


    @Override
    public void setWeaponShield(double weaponShield) {
        this.weaponShield = weaponShield;
    }


    @Override
    public double getWeaponAttackBoost() {
        return weaponAttackBoost;
    }


    @Override
    public void setWeaponAttackBoost(double weaponAttackBoost) {
        this.weaponAttackBoost = weaponAttackBoost;
    }


    @Override
    public double getWeaponShieldBoost() {
        return weaponShieldBoost;
    }


    @Override
    public void setWeaponShieldBoost(double weaponShieldBoost) {
        this.weaponShieldBoost = weaponShieldBoost;
    }


    @Override
    public double getWeaponCritBoost() {
        return WeaponCritBoost;
    }


    @Override
    public void setWeaponCritBoost(double weaponCritBoost) {
        WeaponCritBoost = weaponCritBoost;
    }


    @Override
    public double getWeaponCritRateBoost() {
        return weaponCritRateBoost;
    }


    @Override
    public void setWeaponCritRateBoost(double weaponCritRateBoost) {
        this.weaponCritRateBoost = weaponCritRateBoost;
    }


    @Override
    public double getWeaponAdditionalEnergy() {
        return weaponAdditionalEnergy;
    }


    @Override
    public void setWeaponAdditionalEnergy(double weaponAdditionalEnergy) {
        this.weaponAdditionalEnergy = weaponAdditionalEnergy;
    }


    @Override
    public boolean isNotEditArtifact() {
        return NotEditArtifact;
    }


    @Override
    public void setNotEditArtifact(boolean editArtifact) {
        NotEditArtifact = editArtifact;
    }


    @Override
    public boolean isMuted() {
        return Muted;
    }


    @Override
    public void setMuted(boolean muted) {
        Muted = muted;
    }


    @Override
    public String getColor() {
        return color;
    }


    @Override
    public void setColor(String color) {
        this.color = color;
    }


    @Override
    public Avatar getAvatar() {
        return avatar;
    }


    @Override
    public void setAvatar(Avatar avatar) {
        if(avatar == null){
            this.avatar = AvatarFloader.startAvatar;
        } else {
            this.avatar = avatar;
        }
    }


    @Override
    public void sendUpdateHP() {
        player.sendExperienceChange((float) (shield / maxShield), (int) shield) ;
    }


    @Override
    public void UpdateShield(){
        new BukkitRunnable() {
            @Override
            public void run() {
                sendUpdateHP();
            }
        }.runTaskLater(Main.instance, 10);
    }


    @Override
    public Player toPlayer(){
        return this.player;
    }


    @Override
    public void openBackPack(){
        player.openInventory(backPack);
    }


    @Override
    public ItemStack[] getBackPck() {
        return backPack.getContents();
    }


    @Override
    public void setBackPack(ItemStack[] items) {
        if(items != null) {
            if(items.length>backPack.getSize()){
                ItemStack[] itemsToBackPack = Arrays.copyOfRange(items, 0, backPack.getSize());
                ItemStack[] itemsToDrop = Arrays.copyOfRange(items, backPack.getSize(), 54);
                if(player != null && player.isOnline()) {
                    for (ItemStack item : itemsToDrop) {
                        ValkiriaManager.addOrDropItem(player, item);
                    }
                } else {
                    String code = Main.floader.bd.itemCoder.codeItems(itemsToDrop);
                    if(reward != null){
                        if(reward.charAt(reward.length()-1)==';'){
                            reward = reward+code;
                        } else {
                            reward = reward+";"+code;
                        }
                    } else reward = code;
                }
                backPack.setContents(itemsToBackPack);
            } else backPack.setContents(items);
        }
    }


    @Override
    public void toggleCombatModeWithCheck(){
        if (player.getCooldown(Material.BOW) == 0) {
            toggleCombatMode();
        }
    }


    public ItemStack[] getHItems(){
        if(CombatMode)return HItems;
        else return new ItemStack[3];
    }


    @Override
    public void toggleCombatMode(){
        if(CombatMode){
            player.getInventory().setItem(0, HItems[0]);
            player.getInventory().setItem(7, HItems[1]);
            player.getInventory().setItem(8, HItems[2]);
            CombatMode = false;
        } else {
            player.getInventory().setHeldItemSlot(0);
            HItems[0] = player.getInventory().getItem(0);
            HItems[1] = player.getInventory().getItem(7);
            HItems[2] = player.getInventory().getItem(8);
            player.getInventory().setItem(0, weapon);
            player.getInventory().setItem(7, skills.NORMAL_SKILL);
            player.getInventory().setItem(8, skills.HEIGHT_SKILL);
            CombatMode = true;
        }
        player.setCooldown(Material.ENDER_PEARL, HeightCD);
        player.setCooldown(Material.ARROW, NormalCD);
        player.setCooldown(Material.BOW, 80);
    }


    @Override
    public void setHItems(ItemStack... items){
        if(items==null || items.length==0){
            HItems[0] = null;
            HItems[1] = null;
            HItems[2] = null;
            return;
        }
        HItems[0] = items[0];
        if(items.length>2) {
            HItems[1] = items[1];
            HItems[2] = items[2];
        } else  {
            if(items.length == 1){
                HItems[1] = null;
            } else {
                HItems[1] = items[1];
            }
            HItems[2] = null;
        }
    }


    @Override
    public void damage(double amount){
        shield -=amount;
        if(shield <0){
            if(CombatMode){
                toggleCombatMode();
            }
            player.setHealth(0);
            shield = maxShield;
            player.sendExperienceChange(0.0f);
            if(!premium) experience = experience-((NextLvlTable.getNextLvlExp(level) - experience)/3);
        } else {
            sendUpdateHP();
        }
    }


    @Override
    public void regenShield(double amount){
        shield += amount;
        if(shield > maxShield) shield = maxShield;
        sendUpdateHP();
    }


    @Override
    public String getLastTargetMessage() {
        return lastTargetMessage;
    }


    @Override
    public void setLastTargetMessage(String lastTargetMessage) {
        this.lastTargetMessage = lastTargetMessage;
    }


    @Override
    public void setLastTargetMessage(Player lastTargetMessage) {
        this.lastTargetMessage = lastTargetMessage.getName();
    }


    @Override
    public void onPostLoad() {
        sendUpdateHP();
        session.onPostLoad();

        if(reward!=null){
            player.spigot().sendMessage(ConfigHelper.messageHasRewards);
        }
    }


    @Override
    public void updateSwordStats(){
        BoostStats stats = Main.generator.getBoostStats(weapon);

        weaponAttackBoost =stats.BOOST_ATTACK;
        WeaponCritBoost=stats.BOOST_CRIT;
        weaponShieldBoost =stats.BOOST_SHIELD;
        weaponCritRateBoost =stats.BOOST_CRIT_RATE;
        weaponAttack =stats.ADDITIONAL_ATTACK;
        weaponShield =stats.ADDITIONAL_SHIELD;
        weaponAdditionalEnergy =stats.ADDITIONAL_ENERGY;
    }


    @Override
    public String getReward(){
        if(reward==null)return "none";
        return reward;
    }


    public void setReward(String value){
        if(value != null && !value.equals("none")) reward = value;
        else reward = null;
    }


    protected ItemStack generateIcon(){
        ItemStack Icon = new ItemStack(Material.DIAMOND);
        List<String> fullStats = new ArrayList<>();

        fullStats.add(String.format(ConfigHelper.V_LEVEL, level,getClassName()));
        fullStats.add(String.format(ConfigHelper.V_EXPERIENCE, experience,NextLvlTable.getNextLvlExp(level)));
        fullStats.add(String.format(ConfigHelper.V_SHIELD, shield, maxShield));
        fullStats.add(String.format(ConfigHelper.V_ATTACK, attack));
        fullStats.add(String.format(ConfigHelper.V_CRIT_POWER, criticalPower *100));
        fullStats.add(String.format(ConfigHelper.V_CRIT_RATE, criticalRate *100));
        fullStats.add(String.format(ConfigHelper.V_ENERGY, player.getHealth(), player.getMaxHealth()));
        fullStats.add("");

        ItemMeta IMeta = Icon.getItemMeta();
        IMeta.setLore(fullStats);
        IMeta.setCustomModelData(getAvatar().getCmd());
        IMeta.setDisplayName(ConfigHelper.statistickIco);
        Icon.setItemMeta(IMeta);
        return Icon;
    }


    @Override
    public void openInfo(){
        state = VState.STATS;
        Inventory ValkiriaPlayerInfo = Bukkit.createInventory(null, InventoryType.CHEST, ConfigHelper.valkiriaInfo);
        ItemStack Icon = generateIcon();

        ItemStack Artifacts1 = new ItemStack(Material.DIAMOND);
        List<String> fullStats = new ArrayList<>();
        fullStats.add(String.format(ConfigHelper.V_BOOST_ATTACK_PR, attackBoost *100));
        fullStats.add(String.format(ConfigHelper.V_BOOST_ATTACK, additionalAttack));
        fullStats.add(String.format(ConfigHelper.V_BOOST_CRIT_POWER,AdditionalCriticalPower*100));
        fullStats.add(String.format(ConfigHelper.V_BOOST_CRIT_RATE, additionalCriticalRate *100));
        fullStats.add(String.format(ConfigHelper.V_BOOST_ENERGY, additionalEnergyAmount));
        fullStats.add(String.format(ConfigHelper.V_BOOST_SHIELD_PR, shieldBoost *100));
        fullStats.add(String.format(ConfigHelper.V_BOOST_SHIELD, additionalShield));
        fullStats.add("");

        ItemMeta meta = Artifacts1.getItemMeta();
        if(Artifacts.length>0) {
            if (getArtifacts()[0] == null) {
                meta.setCustomModelData(ConfigHelper.artifactsButtonCmd);
            } else {
                meta.setCustomModelData(getArtifacts()[0].getItemMeta().getCustomModelData());
            }
        } else {
            meta.setCustomModelData(ConfigHelper.artifactsButtonCmd);
        }

        meta.setLore(fullStats);
        meta.setDisplayName(ConfigHelper.statistickIcoMain);
        Artifacts1.setItemMeta(meta);

        ItemStack countBattlePassIndicator = new ItemStack(Material.DIAMOND);
        meta = countBattlePassIndicator.getItemMeta();
        int count = getCountBattlePass();
        Time time = new Time(ClassStats.CD_RECOVERY_BATTLE_PASS-(System.currentTimeMillis()-lastBattlePassReceiving));
        String bpRawLore;
        if(count>=ClassStats.MAX_BATTLE_PASS) bpRawLore = ConfigHelper.battlePassMaximumFormat.
                    replace("%count%", String.valueOf(count)).
                    replace("%max%", String.valueOf(ClassStats.MAX_BATTLE_PASS));

        else bpRawLore = ConfigHelper.battlePassNormalLoreFormat.
                    replace("%count%", String.valueOf(count)).
                    replace("%max%", String.valueOf(ClassStats.MAX_BATTLE_PASS)).
                    replace("%sec%", String.valueOf(time.SEC)).
                    replace("%min%", String.valueOf(time.MIN)).
                    replace("%hor%", String.valueOf(time.HOR)).
                    replace("%day%", String.valueOf(time.DAY));

        if(count==0) {
            meta.setCustomModelData(ConfigHelper.battlePassEmptyCmd);
        } else {
            meta.setCustomModelData(ConfigHelper.battlePassCmd);
        }
        List<String> lore = new ArrayList(Arrays.asList(bpRawLore.split("\n")));
        meta.setDisplayName(lore.remove(0));
        meta.setLore(lore);
        countBattlePassIndicator.setItemMeta(meta);
        if(count>1) {
            if(count>63){
                countBattlePassIndicator.setAmount(64);
            } else countBattlePassIndicator.setAmount(count);
        }
        ValkiriaPlayerInfo.setItem(17, countBattlePassIndicator);

        ItemStack PointAttack1 = ConfigHelper.attackPoints.clone();
        PointAttack1.setAmount(attackPoints);
        ItemStack PointCritic1 = ConfigHelper.critPoints.clone();
        PointCritic1.setAmount(criticalPoints);
        ItemStack PointEnergy1 = ConfigHelper.energyPoints.clone();
        PointEnergy1.setAmount(energyPoints);
        ItemStack PointShield1 = ConfigHelper.shieldPoints.clone();
        PointShield1.setAmount(shieldPoints);
        ItemStack AvaliblePoints1 = ConfigHelper.freePoints.clone();
        AvaliblePoints1.setAmount(AvailablePoints);

        ValkiriaPlayerInfo.setItem(26, ConfigHelper.valkiriaBg);
        ValkiriaPlayerInfo.setItem(21, Icon);
        ValkiriaPlayerInfo.setItem(13, weapon);
        ValkiriaPlayerInfo.setItem(14, Artifacts1);
        ValkiriaPlayerInfo.setItem(15, ConfigHelper.folderButton);
        ValkiriaPlayerInfo.setItem(4,PointAttack1);
        ValkiriaPlayerInfo.setItem(5, PointCritic1);
        ValkiriaPlayerInfo.setItem(6, PointEnergy1);
        ValkiriaPlayerInfo.setItem(7, PointShield1);
        ValkiriaPlayerInfo.setItem(8, AvaliblePoints1);
        player.openInventory(ValkiriaPlayerInfo);
    }


    @Override
    public void openSwordGui(){
        Inventory inventory = Bukkit.createInventory(null, 9, ConfigHelper.valkiriaWeapons);
        inventory.setItem(4, weapon);
        inventory.setItem(7, skills.NORMAL_SKILL);
        inventory.setItem(8, skills.HEIGHT_SKILL);
        inventory.setItem(2, ConfigHelper.valkiriaWeapon);
        player.openInventory(inventory);
    }


    @Override
    public void openArtifacts(){
        Inventory inventory = Bukkit.createInventory(null, 9, ConfigHelper.valkiriaArtifacts);
        inventory.setItem(7, ConfigHelper.artifactsGUI);
        if(Artifacts != null) {
            int flag = 2;
            for(ItemStack item:Artifacts) {
                inventory.setItem(flag, item);
                flag+=1;
            }
        }
        player.openInventory(inventory);
    }


    @Override
    public void openPoints(){
        HashPoints = new int[]{0, 0, 0 ,0 ,0, 0};
        Inventory ValkiriaPoints = Bukkit.createInventory(null, 9, ConfigHelper.valkiriaPoints);

        ItemStack PointAttack1 = ConfigHelper.attackPoints.clone();
        PointAttack1.setAmount(attackPoints);
        ItemStack PointCritic1 = ConfigHelper.critPoints.clone();
        PointCritic1.setAmount(criticalPoints);
        ItemStack PointEnergy1 = ConfigHelper.energyPoints.clone();
        PointEnergy1.setAmount(energyPoints);
        ItemStack PointShield1 = ConfigHelper.shieldPoints.clone();
        PointShield1.setAmount(shieldPoints);
        ItemStack AvaliblePoints1 = ConfigHelper.freePoints.clone();
        AvaliblePoints1.setAmount(AvailablePoints);

        ValkiriaPoints.setItem(2, ConfigHelper.boostPoints);
        ValkiriaPoints.setItem(3, PointAttack1);
        ValkiriaPoints.setItem(4, PointCritic1);
        ValkiriaPoints.setItem(5, PointEnergy1);
        ValkiriaPoints.setItem(6, PointShield1);
        ValkiriaPoints.setItem(8, AvaliblePoints1);
        player.openInventory(ValkiriaPoints);
    }


    @Override
    public boolean isPremium() {
        return premium;
    }


    @Override
    public void setPremium(boolean newValue) {
        if(premium==newValue)return;
        premium = newValue;
        ItemStack[] items = backPack.getContents();
        if(premium){
            backPack = Bukkit.createInventory(null, 54, ConfigHelper.valkiriaBackPack);
            backPack.setContents(items);
        } else {
            backPack = Bukkit.createInventory(null, 27, ConfigHelper.valkiriaBackPack);
            if(items.length>27){
                ItemStack[] itemsToBackPack = Arrays.copyOfRange(items, 0, 27);
                ItemStack[] itemsToDrop = Arrays.copyOfRange(items, 27, 54);
                if(player != null && player.isOnline()) {
                    for (ItemStack item : itemsToDrop) {
                        ValkiriaManager.addOrDropItem(player, item);
                    }
                } else {
                    String code = Main.floader.bd.itemCoder.codeItems(itemsToDrop);
                    if(reward != null){
                        if(reward.charAt(reward.length()-1)==';'){
                            reward = reward+code;
                        } else {
                            reward = reward+";"+code;
                        }
                    } else reward = code;
                }
                backPack.setContents(itemsToBackPack);
            } else {
                backPack.setContents(items);
            }
        }
    }


    @Override
    public void setQuestSession(AbstractQuestSession session) {
        this.session = session;
    }


    @Override
    public AbstractQuestSession getQuestSession() {
        return session;
    }


    @Override
    public VState getState(){
        return state;
    }


    @Override
    public void setState(VState state){
        this.state = state;
    }


    @Override
    public void onRegen(EntityRegainHealthEvent e){
        if(state == VState.STATS){
            player.getOpenInventory().setItem(21, generateIcon());
        }
        e.setAmount(player.getMaxHealth()*0.065);
        criticalEnergy = false;
    }


    @Override
    public int getCountBattlePass() {
        if (countBattlePass >= ClassStats.MAX_BATTLE_PASS) {
            lastBattlePassReceiving = System.currentTimeMillis();
        } else {
            long nowTime = System.currentTimeMillis();
            long range = nowTime - lastBattlePassReceiving;
            if (range > ClassStats.CD_RECOVERY_BATTLE_PASS) {
                int addBattlePass = (int) range / ClassStats.CD_RECOVERY_BATTLE_PASS;
                countBattlePass = countBattlePass + addBattlePass;
                if (countBattlePass > ClassStats.MAX_BATTLE_PASS) {
                    countBattlePass = ClassStats.MAX_BATTLE_PASS;
                }
                lastBattlePassReceiving = System.currentTimeMillis();
            }
        }
        return countBattlePass;
    }


    @Override
    public void setBattlePassData(String data){
        String[] data2 = data.split(":");
        lastBattlePassReceiving = Long.parseLong(data2[0])*ClassStats.CD_PERMISSIBLE_ERROR;
        countBattlePass = Integer.parseInt(data2[1]);
    }


    @Override
    public String getBattlePassData(){
        return (lastBattlePassReceiving/ClassStats.CD_PERMISSIBLE_ERROR)+":"+countBattlePass;
    }


    @Override
    public void setCountBattlePass(int count){
        countBattlePass = count;
    }

    @Override
    public Party getParty() {
        return party;
    }

    @Override
    public void setParty(Party party) {
        this.party = party;
    }


    protected static double getClassVar(String path, double def){
        String[] name = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
        return ClassStats.getClassStatValue(name[name.length-1], path, def);
    }
    protected static int getClassVar(String path, int def){
        String[] name = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
        return ClassStats.getClassStatValue(name[name.length-1], path, def);
    }
}