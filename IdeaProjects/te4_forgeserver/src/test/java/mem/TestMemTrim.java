package mem;

import com.google.common.collect.ImmutableMap;
import plus.fserver.utls.debug.MemTrimmer;

import java.util.HashMap;


public class TestMemTrim {
    public static void main(String[] args) {
        MemTrimmer trimmer = new MemTrimmer();


        ImmutableMap<String,String>[] maps = new ImmutableMap[10];

        HashMap<String,String> map = new HashMap<>();

        for (int i = 0; i < 10; i++){
            map.put("k"+i, "k"+i+1);
            maps[i] = ImmutableMap.copyOf(map);
        }

        //System.out.println(new ScanMem().scan(maps));

        trimmer.trim(maps);

        System.out.println(maps);

        //System.out.println(new ScanMem().scan(maps));
    }
}
