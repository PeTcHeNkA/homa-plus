package plus.ds;

import plus.util.SimpleSrcGetter;


public class Main implements SimpleSrcGetter {
    public static void main(String[] args) {
        new Main();
    }


    public Main(){
        new JDATsonBuilder("dsplugins").build();
    }
}













