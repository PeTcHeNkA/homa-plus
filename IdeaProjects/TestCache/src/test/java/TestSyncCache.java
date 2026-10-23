import plus.fserver.utls.collections.SyncTe4Cache;

public class TestSyncCache {


    public static void main(String[] args) {

        String[] testKeys = new String[]{
                "a","b","c","d","e","f","g","h","i","j","i","j","k","l","m","n","o","p","q","r","s",
                "p","r","s","t","u","v","w","x","y","z","a1","a2","a3","a4","a5","a6","a7","a8","a9",
                "b1", "b2", "b3", "b4", "b5","1c","2c","3c","4c","5c","6c","7c","8c","9c","10c","11c","12c",
                "14c","15c","16c","s1","s2","s3","s4","s5"
        };

        SyncTe4Cache<Key, String> cache = new SyncTe4Cache();

        for (String key : testKeys) {
            cache.put(new Key(key), key, 0);
        }

        cache.put(new Key("AAAAA"), "AAAAA", 1);

        System.out.println(cache);
        cache.removeByStamp(0);

        System.out.println(cache.size());
    }


    static class Key{
        final String parent;

        Key(String parent) {
            this.parent = parent;
        }

        @Override
        public int hashCode() {
            return parent.hashCode()&15;
        }
    }
}













