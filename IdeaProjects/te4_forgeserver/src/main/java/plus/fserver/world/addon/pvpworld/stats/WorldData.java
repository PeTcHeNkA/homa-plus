package plus.fserver.world.addon.pvpworld.stats;

import net.minecraft.entity.Entity;
import plus.tson.utl.Te4HashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;


public class WorldData extends Te4HashMap<UUID,PlayerCombatStats> {
    public int addKill(Entity entity, int n){
        return addKill(entity.getUniqueID(), n);
    }

    public PlayerCombatStats create(){
        return new PlayerCombatStats();
    }


    public PlayerCombatStats stats(UUID uuid){
        PlayerCombatStats stats = super.get(uuid);
        if(stats == null){
            super.fput(uuid, stats = create());
            stats.setUUID(uuid);
        }
        return stats;
    }


    public boolean contains(Entity entity){
        return super.containsKey(entity.getUniqueID());
    }


    public boolean contains(UUID uuid){
        return super.containsKey(uuid);
    }


    public int addKill(UUID uuid, int n){
        return stats(uuid).addKill(n);
    }


    public int getKills(Entity entity){
        return getKills(entity.getUniqueID());
    }


    public int getKills(UUID uuid){
        PlayerCombatStats stats = super.get(uuid);
        if(stats == null)return 0;
        return stats.getKills();
    }


    public int addDeath(Entity entity, int n){
        return addDeath(entity.getUniqueID(), n);
    }


    public int addDeath(UUID uuid, int n){
        return stats(uuid).addDeath(n);
    }


    public int getDeaths(Entity entity){
        return getDeaths(entity.getUniqueID());
    }


    public int getDeaths(UUID uuid){
        PlayerCombatStats stats = super.get(uuid);
        if(stats == null)return 0;
        return stats.getDeaths();
    }


    public int addScore(Entity entity, int n){
        return addScore(entity.getUniqueID(), n);
    }


    public int addScore(UUID uuid, int n){
        return stats(uuid).addScore(n);
    }


    public void setCustom(Entity entity, int custom){
        setCustom(entity.getUniqueID(), custom);
    }


    public void setCustom(UUID uuid, int custom){
        stats(uuid).setCustom(custom);
    }


    public int getCustom(Entity entity){
        return getCustom(entity.getUniqueID());
    }


    public int getCustom(UUID uuid){
        PlayerCombatStats stats = super.get(uuid);
        if(stats == null)return 0;
        return stats.getCustom();
    }


    public Results dumpResults(){
        return dumpResults(null);
    }


    public Results dumpResults(Sorting sorting){
        PlayerCombatStats[] infs = new PlayerCombatStats[super.size()];
        int i = 0;
        for (Entry<UUID, PlayerCombatStats> entry : super.entrySet()) {
            infs[i] = entry.getValue();
            ++i;
        }
        Results results = new Results(infs);
        if(sorting != null)return results.by(sorting);
        return results;
    }


    public static class Results {
        private final CombatStats[] results;
        private Sorting sorting;

        Results(CombatStats[] results) {
            this.results = results;
        }

        public Results by(Sorting sorting){
            if(sorting != this.sorting){
                this.sorting = sorting;
                switch (sorting){
                    case SCORE:
                        Arrays.sort(results, TOP_BY_SCORE);
                        break;
                    case KILLS:
                        Arrays.sort(results, TOP_BY_KILLS);
                        break;
                    case LEAVING:
                        Arrays.sort(results, TOP_BY_LEAVING);
                        break;
                }
            }
            return this;
        }


        public int size(){
            return results.length;
        }


        public CombatStats get(int index){
            return results[index];
        }
    }


    public enum Sorting{
        SCORE, KILLS, LEAVING;
    }


    public static final Comparator<CombatStats> TOP_BY_SCORE   = (o1, o2) -> Integer.compare(o1.getScore() , o2.getScore() );
    public static final Comparator<CombatStats> TOP_BY_KILLS   = (o1, o2) -> Integer.compare(o1.getKills() , o2.getKills() );
    public static final Comparator<CombatStats> TOP_BY_LEAVING = (o1, o2) -> Integer.compare(o2.getDeaths(), o1.getDeaths());
}