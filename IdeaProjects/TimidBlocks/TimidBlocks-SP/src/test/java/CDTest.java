import plus.timidblocks.UserSoundManager;
import plus.tson.utl.Te4HashMap;
import plus.tson.utl.Te4HashSet;

import java.util.*;

public class CDTest {
    public static void main(String[] args) {
//        UserSoundManager.CordList cordList = new UserSoundManager.CordList();
//
//        Te4HashSet<I3> te4HashSet = new Te4HashSet();
//
//        for (int x = 0; x < 16; x++) {
//            for (int z = 0; z < 16; z++) {
//                for (int y = 0; y < 255; y++) {
//
//                    te4HashSet.add(new I3(x, y, z));
//
//                    cordList.add(x, y, z);
//                }
//            }
//        }
//
//        cordList.forEach((x, y, z) -> {
//            if(!te4HashSet.remove(new I3(x, y, z))) {
//                throw  new RuntimeException("Not found "+x+" "+y+" "+z);
//            }
//            System.out.println("Found "+x+" "+y+" "+z);
//        });
//
//        if(!te4HashSet.isEmpty()) {
//            throw  new RuntimeException("Not found");
//        }
        m2();
    }


    public static void m3(){

    }


    public static void m2(){
        Te4HashMap<String, Collection<String>> wordData = new Te4HashMap<>();

        for (String keyWord : new String[]{"a", "b", "c", "d", "e", "f", "g", "h"}) {
            add(wordData, keyWord, "k1");
        }

        System.out.println(wordData.get("k1"));
    }


    private static void add(Te4HashMap<String, Collection<String>> wordData, String material, String keyWord){
        Collection<String> materials = wordData.get(keyWord);
        if(materials == null){
            materials = Collections.singleton(material);
            wordData.put(keyWord, materials);
        } else {
            if(materials.size() == 1){
                Collection<String> materialsNew = new ArrayList<>(2);
                materialsNew.addAll(materials);
                materialsNew.add(material);

                materials = materialsNew;

                wordData.put(keyWord, materials);
                return;
            }
            if(materials.size() == 4){
                Collection<String> materialsNew = new Te4HashSet<>(5);

                materialsNew.addAll(materials);
                materialsNew.add(material);

                materials = materialsNew;

                wordData.put(keyWord, materials);
                return;
            }
            materials.add(material);
        }
    }

    private static class I3{
        final int x, y, z;

        private I3(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            I3 i3 = (I3) o;
            return x == i3.x && y == i3.y && z == i3.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }
}
