package col;

import plus.fserver.utls.collections.FlatMap;


public class TestFlat {
    public static void main(String[] args) {
        FlatMap<String> map = new FlatMap<>(64);
        int count = 0;

        for (int x=30;x<65;x++){
            for (int z=20;z<45;z++){
                map.put(x, z, ("C:"+x+":"+z).intern());
            }
        }

        for (int x=30;x<65;x++){
            for (int z=20;z<45;z++){
                map.put(x, z, ("C:"+x+":"+z).intern());
                ++count;
            }
        }

        System.out.println(count);

        for (String s:map.toArray(new String[0])){
            if(s==null)throw new RuntimeException();
        }
    }
}
