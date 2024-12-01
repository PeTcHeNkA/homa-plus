package plus.fserver.world.addon.pvpworld.stats;

import net.minecraft.entity.Entity;
import plus.tson.utl.Te4HashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;


public class WorldData extends Te4HashMap<UUID, PlayerData> {
    public int addKill(Entity entity, int n){
        return addKill(entity.getUniqueID(), n);
    }

    public PlayerData create(){
        return new PlayerData();
    }


    public PlayerData stats(Entity entity){
        return stats(entity.getUniqueID());
    }


    public PlayerData stats(UUID uuid){
        PlayerData stats = super.get(uuid);
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
        PlayerData stats = super.get(uuid);
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
        PlayerData stats = super.get(uuid);
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
        PlayerData stats = super.get(uuid);
        if(stats == null)return 0;
        return stats.getCustom();
    }


    public Results dumpResults(){
        return dumpResults(null);
    }


    public Results dumpResults(Sorting sorting){
        PlayerData[] infs = new PlayerData[super.size()];
        int i = 0;
        for (Entry<UUID, PlayerData> entry : super.entrySet()) {
            infs[i] = entry.getValue();
            ++i;
        }
        Results results = new Results(infs);
        if(sorting != null)return results.by(sorting);
        return results;
    }


    public static class Results {
        private final CombatData[] results;
        private Sorting sorting;

        Results(CombatData[] results) {
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


        public CombatData get(int index){
            return results[index];
        }
    }


    public enum Sorting{
        SCORE, KILLS, LEAVING;
    }


    public static final Comparator<CombatData> TOP_BY_SCORE   = (o1, o2) -> Integer.compare(o1.getScore() , o2.getScore() );
    public static final Comparator<CombatData> TOP_BY_KILLS   = (o1, o2) -> Integer.compare(o1.getKills() , o2.getKills() );
    public static final Comparator<CombatData> TOP_BY_LEAVING = (o1, o2) -> Integer.compare(o2.getDeaths(), o1.getDeaths());
}