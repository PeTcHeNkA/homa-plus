package plus.fserver.world.addon.pvpworld.stats;

import plus.fserver.world.addon.ScorePlayerTeamEX;


public class CombatData {
    //13 bits - kills    0 - 8191
    //13 bits - deaths   0 - 8191
    //18 bits - score    0 - 262143
    //20 bits - custom   0 - 1048575
    private long value;

    public CombatData(){}

    public CombatData(long value) {
        this.value = value;
    }


    public long getValue(){
        return value;
    }


    public void setValue(long value){
        this.value = value;
    }


    public int getKills(){
        return CombatData.getKills(value);
    }


    public int getDeaths(){
        return getDeaths(value);
    }


    public int getScore(){
        return getScore(value);
    }


    public int getCustom(){
        return getCustom(value);
    }


    public void setKills(int kills){
        value = setKills(value, kills);
    }


    public void setDeaths(int deaths){
        value = setDeaths(value, deaths);
    }


    public void setScore(int score){
        value = setScore(value, score);
    }


    public void setCustom(int custom){
        value = setCustom(value, custom);
    }


    public int addKill(int n){
        int result;
        value = setKills(value, result = getKills(value) + n);
        return result;
    }


    public int addDeath(int n){
        int result;
        value = setDeaths(value, result = getDeaths(value) + n);
        return result;
    }


    public int addScore(int n) {
        int result;
        value = setScore(value, result = getScore(value) + n);
        return result;
    }


    public static CombatData get(ScorePlayerTeamEX team){
        CombatData res = team.getCustom(CombatData.class);
        if(res == null)
            team.setCustom(CombatData.class, res = new CombatData());
        return res;
    }


    //static get math
    public static int getKills(long raw){
        return (int) (raw & 0x1fff);
    }
    public static int getDeaths(long raw){
        return (int) (raw >> 13 & 0x1fff);
    }
    public static int getScore(long raw){
        return (int) (raw >> 26 & 0x3ffff);
    }
    public static int getCustom(long raw){
        return (int) (raw >> 44);
    }


    //static set math
    public static long setKills(long raw, int kills){
        return (raw & 0x1fff0000) | (kills & 0x1fff);
    }
    public static long setDeaths(long raw, int deaths){
        return (raw & 0x1fff) | ((deaths & 0x1fff) << 13);
    }
    public static long setScore(long raw, int score){
        return (raw & 0x3ffff) | ((long) (score & 0x3ffff) << 26);
    }
    public static long setCustom(long raw, int custom){
        return (raw & 0x3ffff) | ((long) (custom) << 44);
    }
}
