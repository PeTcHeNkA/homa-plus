package test1;

import plus.te4net.core.api.IRequestProcessor;
import plus.te4net.core.api.RequestSession;
import plus.te4net.core.mapping.HRouteMap;

public class test2 {


    public static void main(String[] args) {
        try {
            HRouteMap mp = new HRouteMap();
            mp.put("auth", new Ex("k1"));

            System.out.println(mp.get("/auth/check_mail?login=Te4hnoPublic@yandex.ru&crypt-key=&e2=&et=22&"));

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
        public void handle(RequestSession session) {

        }
    }
}

