package plus.valcirias;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import plus.quest.AbstractQuestSession;
import plus.quest.DungeonQuestSession;
import plus.valcirias.avatar.Avatar;
import plus.valcirias.data.StartItemKit;
import java.util.ArrayList;
import java.util.List;


public class DungeonValkiria implements Valkiria {

    private final Valkiria v;

    public DungeonValkiria(Valkiria valkiria){
        this.v = valkiria;
    }


    @Override
    public int getNormalCD() {
        return v.getNormalCD();
    }


    @Override
    public int getHightCD() {
        return v.getHightCD();
    }


    @Override
    public double getShield() {
        return v.getShield();
    }


    @Override
    public double getMaxShield() {
        return v.getMaxShield();
    }


    @Override
    public void setShield(double NewShield) {
        v.setShield(NewShield);
    }


    @Override
    public String getClassName() {
        return v.getClassName();
    }


    @Override
    public void updateAll() {
        v.updateAll();
    }


    @Override
    public boolean CombatMode() {

        return v.CombatMode();
    }


    @Override
    public void setCombatMode(boolean NewMode) {
        v.setCombatMode(NewMode);
    }


    @Override
    public String ClassName() {
        return v.ClassName();
    }


    @Override
    public ItemStack[] getArtifacts() {
        return v.getArtifacts();
    }


    @Override
    public void setArtifacts(ItemStack[] items) {
        v.setArtifacts(items);
    }


    @Override
    public double useNormalAttack(EntityDamageByEntityEvent e) {
        return v.useNormalAttack(e);
    }

    @Override
    public void useChargeAttack(double charge) {
        v.useChargeAttack(charge);
    }

    @Override
    public void useNormalSkill() {
        v.useNormalSkill();
    }

    @Override
    public void useHeightSkill() {
        v.useHeightSkill();
    }

    @Override
    public String toPlayerName() {
        return v.toPlayerName();
    }

    @Override
    public void setPlayerName(String playerName) {
        v.setPlayerName(playerName);
    }

    @Override
    public void setPlayerName(Player pl) {
        v.setPlayerName(pl);
    }


    @Override
    public double getShieldBoost() {
        return v.getShieldBoost();
    }

    @Override
    public void setShieldBoost(double shieldBoost) {
        v.setShieldBoost(shieldBoost);
    }

    @Override
    public double getAttackBoost() {
        return v.getAttackBoost();
    }

    @Override
    public void setAttackBoost(double attackBoost) {
        v.setAttackBoost(attackBoost);
    }

    @Override
    public int getAttackPoints() {
        return v.getAttackPoints();
    }

    @Override
    public void setAttackPoints(int attackPoints) {
        v.setAttackPoints(attackPoints);
    }

    @Override
    public int getShieldPoints() {
        return v.getShieldPoints();
    }

    @Override
    public void setShieldPoints(int shieldPoints) {
        v.setShieldPoints(shieldPoints);
    }

    @Override
    public double getPowerAttack() {
        return v.getPowerAttack();
    }

    @Override
    public double getAdditionalShield() {
        return v.getAdditionalShield();
    }

    @Override
    public void setAdditionalShield(double additionalShield) {
        v.setAdditionalShield(additionalShield);
    }

    @Override
    public double getAdditionalAttack() {
        return v.getAdditionalAttack();
    }

    @Override
    public void setAdditionalAttack(double additionalAttack) {
        v.setAdditionalAttack(additionalAttack);
    }

    @Override
    public double getCriticalPower() {
        return v.getCriticalPower();
    }

    @Override
    public void setCriticalPower(double criticalPower) {
        v.setCriticalPower(criticalPower);
    }

    @Override
    public double getAdditionalCriticalPower() {
        return v.getAdditionalCriticalPower();
    }

    @Override
    public double getCriticalRate() {
        return v.getCriticalRate();
    }

    @Override
    public double getAdditionalCriticalRate() {
        return v.getAdditionalCriticalRate();
    }

    @Override
    public void setCriticalRate(double criticalRate) {
        v.setCriticalRate(criticalRate);
    }

    @Override
    public ItemStack getWeapon() {
        return v.getWeapon();
    }

    @Override
    public void setWeapon(ItemStack weapons) {
        v.setWeapon(weapons);
    }

    @Override
    public void setHItems(ItemStack... items) {
        v.setHItems(items);
    }

    @Override
    public ItemStack[] getHItems() {
        return v.getHItems();
    }

    @Override
    public StartItemKit getSKit() {
        return v.getSKit();
    }

    @Override
    public double getNormalSkillPrice() {
        return v.getNormalSkillPrice();
    }

    @Override
    public double getHeightSkillPrice() {
        return v.getHeightSkillPrice();
    }

    @Override
    public boolean isCriticalEnergy() {
        return v.isCriticalEnergy();
    }

    @Override
    public void setCriticalEnergy(boolean criticalEnergy) {
        v.setCriticalEnergy(criticalEnergy);
    }

    @Override
    public int getExperience() {
        return v.getExperience();
    }

    @Override
    public int getLvl() {
        return v.getLvl();
    }

    @Override
    public void setExperience(int experience) {
        v.setExperience(experience);
    }

    @Override
    public void addExperience(int experience) {
        v.addExperience(experience);
    }

    @Override
    public int getAvailablePoints() {
        return v.getAvailablePoints();
    }

    @Override
    public void setAvailablePoints(int availablePoints) {
        v.setAvailablePoints(availablePoints);
    }

    @Override
    public int getCriticalPoints() {
        return v.getCriticalPoints();
    }

    @Override
    public void setCriticalPoints(int criticalPoints) {
        v.setCriticalPoints(criticalPoints);
    }

    @Override
    public int getEnergyPoints() {
        return v.getEnergyPoints();
    }

    @Override
    public void setEnergyPoints(int energyPoints) {
        v.setEnergyPoints(energyPoints);
    }

    @Override
    public double getAdditionalEnergy() {
        return v.getAdditionalEnergy();
    }

    @Override
    public void setAdditionalEnergy(double additionalEnergy) {
        v.setAdditionalEnergy(additionalEnergy);
    }

    @Override
    public int[] getHashPoints() {
        return v.getHashPoints();
    }

    @Override
    public void setHashPoints(int[] hashPoints) {
        v.setHashPoints(hashPoints);
    }

    @Override
    public int getAllowedWeaponType() {
        return v.getAllowedWeaponType();
    }

    @Override
    public double getWeaponAttack() {
        return v.getWeaponAttack();
    }

    @Override
    public void setWeaponAttack(double weaponAttack) {
        v.setWeaponAttack(weaponAttack);
    }

    @Override
    public double getWeaponShield() {
        return v.getWeaponShield();
    }

    @Override
    public void setWeaponShield(double weaponShield) {
        v.setWeaponShield(weaponShield);
    }


    @Override
    public double getWeaponAttackBoost() {
        return v.getWeaponAttackBoost();
    }


    @Override
    public void setWeaponAttackBoost(double weaponAttackBoost) {
        v.setWeaponAttackBoost(weaponAttackBoost);
    }


    @Override
    public double getWeaponShieldBoost() {
        return v.getWeaponShieldBoost();
    }


    @Override
    public void setWeaponShieldBoost(double weaponShieldBoost) {
        v.setWeaponShieldBoost(weaponShieldBoost);
    }


    @Override
    public double getWeaponCritBoost() {
        return v.getWeaponCritBoost();
    }


    @Override
    public void setWeaponCritBoost(double weaponCritBoost) {
        v.setWeaponCritBoost(weaponCritBoost);
    }


    @Override
    public double getWeaponCritRateBoost() {
        return v.getWeaponCritRateBoost();
    }


    @Override
    public void setWeaponCritRateBoost(double weaponCritRateBoost) {
        v.setWeaponCritRateBoost(weaponCritRateBoost);
    }


    @Override
    public double getWeaponAdditionalEnergy() {
        return v.getAdditionalEnergy();
    }


    @Override
    public void setWeaponAdditionalEnergy(double weaponAdditionalEnergy) {
        v.setWeaponAdditionalEnergy(weaponAdditionalEnergy);
    }


    @Override
    public boolean isNotEditArtifact() {
        return v.isNotEditArtifact();
    }


    @Override
    public void setNotEditArtifact(boolean editArtifact) {
        v.setNotEditArtifact(editArtifact);
    }


    @Override
    public boolean isMuted() {
        return v.isMuted();
    }


    @Override
    public void setMuted(boolean muted) {
        v.setMuted(muted);
    }


    @Override
    public String getColor() {
        return v.getColor();
    }


    @Override
    public void setColor(String color) {
        v.setColor(color);
    }


    @Override
    public Avatar getAvatar() {
        return v.getAvatar();
    }


    @Override
    public void setAvatar(Avatar avatar) {
        v.setAvatar(avatar);
    }


    @Override
    public void sendUpdateHP() {
        v.sendUpdateHP();
    }


    @Override
    public void UpdateShield() {
        v.UpdateShield();
    }


    @Override
    public Player toPlayer() {
        return v.toPlayer();
    }


    @Override
    public ItemStack[] getBackPck() {
        return v.getBackPck();
    }


    @Override
    public void setBackPack(ItemStack[] items) {
        v.setBackPack(items);
    }


    @Override
    public void toggleCombatModeWithCheck() {
        v.toggleCombatModeWithCheck();
    }


    @Override
    public void toggleCombatMode() {
        v.toggleCombatMode();
    }


    @Override
    public void damage(double amount) {
        v.damage(amount);
    }


    @Override
    public void regenShield(double amount) {
        v.regenShield(amount);
    }


    @Override
    public String getLastTargetMessage() {
        return v.getLastTargetMessage();
    }


    @Override
    public void setLastTargetMessage(String lastTargetMessage) {
        v.setLastTargetMessage(lastTargetMessage);
    }


    @Override
    public void setLastTargetMessage(Player lastTargetMessage) {
        v.setLastTargetMessage(lastTargetMessage);
    }


    @Override
    public void updateSwordStats() {
        v.updateSwordStats();
    }


    @Override
    public void onPostLoad() {
        v.onPostLoad();
    }


    @Override
    public String getReward() {
        return v.getReward();
    }

    @Override
    public void setReward(String value) {
        v.setReward(value);
    }



    @Override
    public void openBackPack() {
        //v.openBackPack();
    }
    @Override
    public void openInfo() {
        v.openInfo();
    }

    @Override
    public void openSwordGui() {}

    @Override
    public void openArtifacts() {}

    @Override
    public void openPoints() {
        v.openPoints();
    }


    @Override
    public boolean isPremium() {
        return v.isPremium();
    }


    @Override
    public void setPremium(boolean newValue) {
        v.setPremium(newValue);
    }


    @Override
    public BaseValkiria.VState getState() {
        return v.getState();
    }

    @Override
    public void setState(BaseValkiria.VState state) {
        v.setState(state);
    }


    private List<String> rawData = new ArrayList<>();
    public void setRawData(List<String> data){
        rawData = data;
    }
    public List<String> getRawData(){
        return rawData;
    }


    @Override
    public void setQuestSession(AbstractQuestSession session) {
        v.setQuestSession(session);
    }


    @Override
    public AbstractQuestSession getQuestSession() {
        return v.getQuestSession();
    }


    @Override
    public void onRegen(EntityRegainHealthEvent e){v.onRegen(e);}


    @Override
    public int getCountBattlePass() {
        return v.getCountBattlePass();
    }


    @Override
    public void setBattlePassData(String data) {
        v.setBattlePassData(data);
    }


    @Override
    public String getBattlePassData() {
        return v.getBattlePassData();
    }


    @Override
    public void setCountBattlePass(int count) {
        v.setCountBattlePass(count);
    }


    @Override
    public Party getParty() {
        return v.getParty();
    }


    @Override
    public void setParty(Party party) {
        v.setParty(party);
    }


    @Override
    public void setQuestSession(String data){
        setQuestSession(new DungeonQuestSession(data));
    }

    private double maxRegen = -1;
    private double remainsRegen = -1;
    public void setMaxRegenShield(double maxRegenShield){
        this.maxRegen = maxRegenShield;
        remainsRegen = maxRegen;
    }


    public double getMxRegenShield(){
        return maxRegen;
    }


    public double getRemainsRegen(){
        return remainsRegen;
    }


    public void setRemainsRegen(double remainsRegen){
        this.remainsRegen = remainsRegen;
    }
}