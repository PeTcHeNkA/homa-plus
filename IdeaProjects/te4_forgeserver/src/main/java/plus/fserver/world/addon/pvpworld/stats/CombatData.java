package plus.fserver.world.addon.pvpworld.stats;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.Entity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import static plus.fserver.world.addon.pvpworld.stats.CombatStats.*;


public class CombatData extends Object2LongOpenHashMap<UUID> {
    private int getPos(Object k) {
        if (k == null) {
            return -1;
        } else {
            Object[] key = this.key;
            Object curr;
            int pos, mask;
            if ((curr = key[pos = HashCommon.mix(k.hashCode()) & (mask = this.mask)]) == null) {
                return -1;
            } else if (k.equals(curr)) {
                return pos;
            } else {
                while((curr = key[pos = pos + 1 & mask]) != null) {
                    if (k.equals(curr)) {
                        return pos;
                    }
                }
                return -1;
            }
        }
    }

    public int addKill(Entity entity, int n){
        return addKill(entity.getUniqueID(), n);
    }


    public int addKill(UUID uuid, int n){
        long[] value = super.value;
        int pos = getPos(uuid), cur;
        if(pos == -1){
            super.put(uuid, setKills(0L, n));
            return n;
        }
        long raw;
        value[pos] = setKills(raw = value[pos], (cur = CombatStats.getKills(raw)+n));
        return cur;
    }


    public int getKills(Entity entity){
        return getKills(entity.getUniqueID());
    }


    public int getKills(UUID uuid){
        int pos = getPos(uuid);
        if(pos == -1)return 0;
        return CombatStats.getKills(super.value[pos]);
    }


    public int addDeath(Entity entity, int n){
        return addDeath(entity.getUniqueID(), n);
    }


    public int addDeath(UUID uuid, int n){
        long[] value = super.value;
        int pos = getPos(uuid), cur;
        if(pos == -1){
            super.put(uuid, setDeaths(0L, n));
            return n;
        }
        long raw;
        value[pos] = setDeaths(raw = value[pos], (cur = CombatStats.getDeaths(raw)+n));
        return cur;
    }


    public int getDeaths(Entity entity){
        return getDeaths(entity.getUniqueID());
    }


    public int getDeaths(UUID uuid){
        int pos = getPos(uuid);
        if(pos == -1)return 0;
        return CombatStats.getDeaths(super.value[pos]);
    }


    public int addScore(Entity entity, int n){
        return addScore(entity.getUniqueID(), n);
    }


    public int addScore(UUID uuid, int n){
        long[] value = super.value;
        int pos = getPos(uuid), cur;
        if(pos == -1){
            super.put(uuid, setScore(0L, n));
            return n;
        }
        long raw;
        value[pos] = setScore(raw = value[pos], (cur = getScore(raw)+n));
        return cur;
    }


    public void setCustom(Entity entity, int custom){
        setCustom(entity.getUniqueID(), custom);
    }


    public void setCustom(UUID uuid, int custom){
        long[] value = super.value;
        int pos = getPos(uuid);
        if(pos == -1){
            super.put(uuid, CombatStats.setCustom(0L, custom));
            return;
        }
        value[pos] = CombatStats.setCustom(value[pos], custom);
    }


    public int getCustom(Entity entity){
        return getCustom(entity.getUniqueID());
    }


    public int getCustom(UUID uuid){
        int pos = getPos(uuid);
        if(pos == -1)return 0;
        return CombatStats.getCustom(super.value[pos]);
    }


    public Results dumpResults(){
        return dumpResults(null);
    }


    public Results dumpResults(Sorting sorting){
        UserInf[] infs = new UserInf[super.size()];
        int i = 0;

        ObjectIterator<Entry<UUID>> iter = super.object2LongEntrySet().fastIterator();

        while (iter.hasNext()){
            Entry<UUID> entry = iter.next();
            infs[i] = new UserInf(entry.getKey(), entry.getLongValue());
            ++i;
        }
        Results results = new Results(infs);
        if(sorting != null)return results.by(sorting);
        return results;
    }


    public static class Results {
        private final UserInf[] results;
        private Sorting sorting;

        Results(UserInf[] results) {
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
                }
            }
            return this;
        }


        public int size(){
            return results.length;
        }


        public UserInf get(int index){
            return results[index];
        }
    }


    public enum Sorting{
        SCORE, KILLS, LEAVING;
    }


    public static class UserInf{
        private final UUID uuid;
        private final long rawData;

        UserInf(UUID uuid, long rawData) {
            this.uuid = uuid;
            this.rawData = rawData;
        }

        public UUID getUUID(){
            return uuid;
        }
        public int getKills(){
            return CombatStats.getKills(rawData);
        }
        public int getDeaths(){
            return CombatStats.getDeaths(rawData);
        }
        public int getScore(){
            return CombatStats.getScore(rawData);
        }
        public int getCustom() {
            return CombatStats.getCustom(rawData);
        }
    }


    public static final Comparator<UserInf> TOP_BY_SCORE   = (o1, o2) -> Integer.compare(o1.getScore(), o2.getScore());
    public static final Comparator<UserInf> TOP_BY_KILLS   = (o1, o2) -> Integer.compare(o1.getKills(), o2.getKills());
    public static final Comparator<UserInf> TOP_BY_LEAVING = (o1, o2) -> Integer.compare(o2.getDeaths(), o1.getDeaths());




}