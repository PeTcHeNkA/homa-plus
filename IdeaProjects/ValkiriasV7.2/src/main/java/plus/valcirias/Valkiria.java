package plus.valcirias;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import plus.valcirias.avatar.Avatar;
import plus.valcirias.data.StartItemKit;


public interface Valkiria extends QuestCarrier{
    int getNormalCD();

    int getHightCD();

    double getShield();

    double getMaxShield();

    void setShield(double NewShield);

    String getClassName();

    //UPDATE STATS
    void updateAll();

    boolean CombatMode();

    void setCombatMode(boolean NewMode);

    String ClassName();

    ItemStack[] getArtifacts();

    void setArtifacts(ItemStack[] items);

    //double NormalAttack(double damage);

    double useNormalAttack(EntityDamageByEntityEvent e);

    void useChargeAttack(double charge);

    void useNormalSkill();

    void useHeightSkill();

    String toPlayerName();

    void setPlayerName(String playerName);

    void setPlayerName(Player pl);

    //Auto-Generated Getter and Setter
    double getShieldBoost();

    void setShieldBoost(double shieldBoost);

    double getAttackBoost();

    void setAttackBoost(double attackBoost);

    int getAttackPoints();

    void setAttackPoints(int attackPoints);

    int getShieldPoints();

    void setShieldPoints(int shieldPoints);

    double getPowerAttack();

    double getAdditionalShield();

    void setAdditionalShield(double additionalShield);

    double getAdditionalAttack();

    void setAdditionalAttack(double additionalAttack);

    double getCriticalPower();

    void setCriticalPower(double criticalPower);

    double getAdditionalCriticalPower();

    double getCriticalRate();

    double getAdditionalCriticalRate();

    void setCriticalRate(double criticalRate);

    ItemStack getWeapon();

    void setWeapon(ItemStack weapons);


    void setHItems(ItemStack... items);
    ItemStack[] getHItems();

    StartItemKit getSKit();


    double getNormalSkillPrice();

    double getHeightSkillPrice();

    boolean isCriticalEnergy();

    void setCriticalEnergy(boolean criticalEnergy);

    int getExperience();

    int getLvl();

    void setExperience(int experience);

    void addExperience(int experience);

    int getAvailablePoints();

    void setAvailablePoints(int availablePoints);

    int getCriticalPoints();

    void setCriticalPoints(int criticalPoints);

    int getEnergyPoints();

    void setEnergyPoints(int energyPoints);

    double getAdditionalEnergy();

    void setAdditionalEnergy(double additionalEnergy);

    int[] getHashPoints();

    void setHashPoints(int[] hashPoints);

    int getAllowedWeaponType();

    double getWeaponAttack();

    void setWeaponAttack(double weaponAttack);

    double getWeaponShield();

    void setWeaponShield(double weaponShield);

    double getWeaponAttackBoost();

    void setWeaponAttackBoost(double weaponAttackBoost);

    double getWeaponShieldBoost();

    void setWeaponShieldBoost(double weaponShieldBoost);

    double getWeaponCritBoost();

    void setWeaponCritBoost(double weaponCritBoost);

    double getWeaponCritRateBoost();

    void setWeaponCritRateBoost(double weaponCritRateBoost);

    double getWeaponAdditionalEnergy();

    void setWeaponAdditionalEnergy(double weaponAdditionalEnergy);

    boolean isNotEditArtifact();

    void setNotEditArtifact(boolean editArtifact);

    boolean isMuted();

    void setMuted(boolean muted);

    String getColor();

    void setColor(String color);

    Avatar getAvatar();

    void setAvatar(Avatar avatar);

    void sendUpdateHP();

    void UpdateShield();

    Player toPlayer();

    //void setQuestSession(Quest quest);

    //void setHideQuest(Quest quest);

    //Quest getQuestSession();

    //String getQuestHash();

    //void setQuestHash(String hash);

    //void addQuestInHistory(int uid);

    //List<Boolean> getQuestHistory();

    //void setQuestHistory(List<Boolean> history);

    //boolean questComplated(int uid);

    void openBackPack();

    ItemStack[] getBackPck();

    void setBackPack(ItemStack[] items);

    void toggleCombatModeWithCheck();

    void toggleCombatMode();

    void damage(double amount);

    void regenShield(double amount);

    String getLastTargetMessage();

    void setLastTargetMessage(String lastTargetMessage);

    void setLastTargetMessage(Player lastTargetMessage);

    void updateSwordStats();

    void onPostLoad();


    String getReward();
    void setReward(String value);


    void openInfo();
    void openSwordGui();
    void openArtifacts();
    void openPoints();


    boolean isPremium();
    void setPremium(boolean newValue);

    BaseValkiria.VState getState();


    void setState(BaseValkiria.VState state);


    void onRegen(EntityRegainHealthEvent e);


    int getCountBattlePass();
    void setBattlePassData(String data);
    String getBattlePassData();
    void setCountBattlePass(int count);


    Party getParty();
    void setParty(Party party);
}