import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.IRequestSession;
import plus.te4net.core.mapping.HRouteMap;

import java.util.Arrays;

public class test2 {


    public static void main(String[] args) {
        try {
            HRouteMap mp = new HRouteMap();
            mp.put("a1/a2/a3/a4", new Ex("k1"));
            mp.put("a1/a2/",new Ex("k2"));
            mp.put("a1/a2/a3",new Ex("k3"));
            mp.put("a1/a2/a4",new Ex("k3"));

            //System.out.println(mp.get("/data1/data2/data3"));

            //System.out.println(mp.get("/a1/a2/data1/data2/data3/data4"));
            System.out.println(mp.get("/a1/a2/data1/data2?k&v&d"));

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    static class Ex implements IRequestProcessor{
        final String name;
        Ex(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
        @Override
        public void handle(IRequestSession session) {

        }
    }
}

