package col;

import plus.fserver.utls.collections.ArraySkipList;


public class SkipList {
    public static void main(String[] args) {
        ArraySkipList<String> list = new ArraySkipList<>(7);
        for (int i=0;i<10;i++){
            list.append("TEST"+i);
        }
        list.set(5, null);
        list.set(4, null);
        int idx = list.append("RA");
        list.remove("RA");
        list.append("RA2");
        list.set(idx, "RA3");

        for (String s:list){
            System.out.println("--"+s);
        }

        System.out.println(list);

    }
}
