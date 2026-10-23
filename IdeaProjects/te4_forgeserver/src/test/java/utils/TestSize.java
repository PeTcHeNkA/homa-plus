package utils;

import plus.tson.TsonOptimizer;
import plus.tson.TsonMap;

public class TestSize {

    public static void main(String... args){
        TsonMap map = new TsonMap("{key='data',k2='data',a={key='data',k2='data'}}");
        map = new TsonOptimizer().optimize(map);

        System.out.println(map);
    }

}